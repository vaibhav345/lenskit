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
package org.lenskit.slopeone;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemScorer;
import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.data.ratings.RatingVectorPDAO;
import org.lenskit.results.Results;
import org.lenskit.util.math.Vectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An {@link ItemScorer} that implements the Slope One algorithm.
 */
public class SlopeOneItemScorer extends AbstractItemScorer {
    protected final RatingVectorPDAO dao;
    protected SlopeOneModel model;
    protected final PreferenceDomain domain;

    @Inject
    public SlopeOneItemScorer(RatingVectorPDAO dao,
                              SlopeOneModel model,
                              @Nullable PreferenceDomain dom) {
        this.dao = dao;
        this.model = model;
        domain = dom;
    }

    @Nonnull
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        Long2DoubleMap ratings = dao.userRatingVector(user);

        List<Result> results = new ArrayList<>();
        LongIterator iter = LongIterators.asLongIterator(items.iterator());
        while (iter.hasNext()) {
            final long predicteeItem = iter.nextLong();
            if (!ratings.containsKey(predicteeItem)) {
                double total = 0;
                int nitems = 0;
                for (Long2DoubleMap.Entry e: Vectors.fastEntries(ratings)) {
                    long currentItem = e.getKey();
                    int nusers = model.getCoratings(predicteeItem, currentItem);
                    if (nusers != 0) {
                        double currentDev = model.getDeviation(predicteeItem, currentItem);
                        total += currentDev + e.getValue();
                        nitems++;
                    }
                }
                if (nitems != 0) {
                    double predValue = total / nitems;
                    if (domain != null) {
                        predValue = domain.clampValue(predValue);
                    }
                    results.add(Results.create(predicteeItem, predValue));
                }
            }
        }
        return Results.newResultMap(results);
    }

    public SlopeOneModel getModel() {
        return model;
    }
}
