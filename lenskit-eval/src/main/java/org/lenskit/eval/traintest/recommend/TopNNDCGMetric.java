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
package org.lenskit.eval.traintest.recommend;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import it.unimi.dsi.fastutil.longs.*;
import org.apache.commons.lang3.StringUtils;
import org.lenskit.api.Recommender;
import org.lenskit.api.RecommenderEngine;
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
import java.util.Arrays;
import java.util.Collections;

/**
 * Measure the nDCG of the top-N recommendations, using ratings as scores.
 *
 * This metric is registered with the type name `ndcg`.
 */
public class TopNNDCGMetric extends ListOnlyTopNMetric<MeanAccumulator> {
    private static final Logger logger = LoggerFactory.getLogger(TopNNDCGMetric.class);
    public static final String DEFAULT_COLUMN = "nDCG";
    private final String columnName;
    private final Discount discount;

    /**
     * Create an nDCG metric with log-2 discounting.
     */
    public TopNNDCGMetric() {
        this(Discounts.log2(), null);
    }

    /**
     * Create an nDCG metric with a default name.
     * @param disc The discount to apply.
     */
    public TopNNDCGMetric(Discount disc) {
        this(disc, null);
    }

    /**
     * Construct a top-N nDCG metric from a spec.
     * @param spec The spec.
     */
    @JsonCreator
    public TopNNDCGMetric(Spec spec) {
        this(spec.getParsedDiscount(), spec.getColumnName());
    }

    /**
     * Construct a new nDCG Top-N metric.
     * @param disc The discount to apply.
     * @param name The column name to use.
     */
    public TopNNDCGMetric(Discount disc, String name) {
        super(Collections.singletonList(StringUtils.defaultString(name, DEFAULT_COLUMN)),
              Collections.singletonList(StringUtils.defaultString(name, DEFAULT_COLUMN)));
        columnName = StringUtils.defaultString(name, DEFAULT_COLUMN);
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
        return MetricResult.singleton(columnName, context.getMean());
    }

    @Nonnull
    @Override
    public MetricResult measureUser(Recommender rec, TestUser user, int targetLength, LongList recommendations, MeanAccumulator context) {
        if (recommendations == null) {
            return MetricResult.empty();
        }

        Long2DoubleMap ratings = user.getTestRatings();
        long[] ideal = ratings.keySet().toLongArray();
        LongArrays.quickSort(ideal, LongComparators.oppositeComparator(LongUtils.keyValueComparator(ratings)));
        if (targetLength >= 0 && ideal.length > targetLength) {
            ideal = Arrays.copyOf(ideal, targetLength);
        }
        double idealGain = computeDCG(ideal, ratings);

        long[] actual = recommendations.toLongArray();
        double gain = computeDCG(actual, ratings);

        double score = gain / idealGain;

        synchronized (context) {
            context.add(score);
        }
        return MetricResult.singleton(columnName, score);
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
