/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.lenskit.eval.traintest.predict;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2DoubleFunction;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongComparators;
import org.apache.commons.lang3.StringUtils;
import org.lenskit.api.RecommenderEngine;
import org.lenskit.api.ResultMap;
import org.lenskit.eval.traintest.AlgorithmInstance;
import org.lenskit.eval.traintest.DataSet;
import org.lenskit.eval.traintest.TestUser;
import org.lenskit.eval.traintest.metrics.Discount;
import org.lenskit.eval.traintest.metrics.Discounts;
import org.lenskit.eval.traintest.metrics.MetricResult;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.math.MeanAccumulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Evaluate a recommender's predictions with normalized discounted cumulative gain.
 *
 * <p>This is a prediction evaluator that uses base-2 nDCG to evaluate recommender
 * accuracy. The items are ordered by predicted preference and the nDCG is
 * computed using the user's real rating as the gain for each item. Doing this
 * only over the queried items, rather than in the general recommend condition,
 * avoids penalizing recommenders for recommending items that would be better
 * if the user had known about them and provided ratings (e.g., for doing their
 * job).
 *
 * <p>nDCG is computed per-user and then averaged over all users.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class NDCGPredictMetric extends PredictMetric<MeanAccumulator> {
    private static final Logger logger = LoggerFactory.getLogger(NDCGPredictMetric.class);
    public static final String DEFAULT_COLUMN = "Predict.nDCG";
    private final String columnName;
    private final Discount discount;

    /**
     * Create a new log_2 nDCG metric with column name "Predict.nDCG".
     */
    public NDCGPredictMetric() {
        this(Discounts.log2(), DEFAULT_COLUMN);
    }

    /**
     * Create a new nDCG metric with column name "Predict.nDCG".
     * @param disc The discount.
     */
    public NDCGPredictMetric(Discount disc) {
        this(disc, DEFAULT_COLUMN);
    }

    /**
     * Construct a predict metric from a spec.
     * @param spec The metric spec.
     */
    @JsonCreator
    public NDCGPredictMetric(Spec spec) {
        this(spec.getParsedDiscount(),
             StringUtils.defaultString(spec.getColumnName(), DEFAULT_COLUMN));
    }

    /**
     * Create a new nDCG metric.
     * @param disc The discount.
     * @param name The column name.
     */
    public NDCGPredictMetric(Discount disc, String name) {
        super(Lists.newArrayList(name, name+".Raw"), Lists.newArrayList(name));
        columnName = name;
        discount = disc;
    }

    @Nullable
    @Override
    public MeanAccumulator createContext(AlgorithmInstance algorithm, DataSet dataSet, RecommenderEngine engine) {
        return new MeanAccumulator();
    }

    @Nonnull
    @Override
    public MetricResult getAggregateMeasurements(MeanAccumulator context) {
        logger.warn("Predict nDCG is deprecated, use nDCG in a rank context");
        return MetricResult.singleton(columnName, context.getMean());
    }

    @Nonnull
    @Override
    public MetricResult measureUser(TestUser user, ResultMap predictions, MeanAccumulator context) {
        if (predictions == null || predictions.isEmpty()) {
            return MetricResult.empty();
        }
        Long2DoubleMap ratings = user.getTestRatings();
        long[] ideal = ratings.keySet().toLongArray();
        LongArrays.quickSort(ideal, LongComparators.oppositeComparator(LongUtils.keyValueComparator(ratings)));
        long[] actual = LongUtils.asLongSet(predictions.keySet()).toLongArray();
        LongArrays.quickSort(actual, LongComparators.oppositeComparator(
                LongUtils.keyValueComparator(
                        LongUtils.asLong2DoubleFunction(predictions.scoreMap()))));
        double idealGain = computeDCG(ideal, ratings);
        double gain = computeDCG(actual, ratings);
        logger.debug("user {} has gain of {} (ideal {})", user.getUserId(), gain, idealGain);
        double score = gain / idealGain;
        synchronized (context) {
            context.add(score);
        }
        ImmutableMap.Builder<String,Double> results = ImmutableMap.builder();
        return MetricResult.fromMap(results.put(columnName, score)
                                           .put(columnName + ".Raw", gain)
                                           .build());
    }

    /**
     * Compute the DCG of a list of items with respect to a value vector.
     */
    double computeDCG(long[] items, Long2DoubleFunction values) {
        double gain = 0;
        int rank = 0;

        for (long item: items) {
            final double v = values.get(item);
            rank++;
            gain += v * discount.discount(rank);
        }

        return gain;
    }

    /**
     * Specification for configuring nDCG metrics.
     */
    @JsonIgnoreProperties("type")
    public static class Spec {
        private String name;
        private String discount;

        public String getColumnName() {
            return name;
        }

        public void setColumnName(String name) {
            this.name = name;
        }

        public String getDiscount() {
            return discount;
        }

        public void setDiscount(String discount) {
            this.discount = discount;
        }

        public Discount getParsedDiscount() {
            if (discount == null) {
                return Discounts.log2();
            } else {
                return Discounts.parse(discount);
            }
        }
    }
}
