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

import it.unimi.dsi.fastutil.longs.Long2DoubleSortedMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.inject.Transient;
import org.lenskit.knn.item.model.ItemItemBuildContext;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Pre-computes the deviations and number of mutual rating users for every pair
 * of items and stores the results in a {@code DeviationMatrix} and
 * {@code CoratingMatrix}. These matrices are later used by a
 * {@code SlopeOneItemScorer}.
 */
public class SlopeOneModelProvider implements Provider<SlopeOneModel> {
    private final SlopeOneModelDataAccumulator accumulator;

    private final ItemItemBuildContext buildContext;

    @Inject
    public SlopeOneModelProvider(@Transient ItemItemBuildContext context,
                                 @DeviationDamping double damping) {

        buildContext = context;
        accumulator = new SlopeOneModelDataAccumulator(damping, context.getItems());
    }

    /**
     * Constructs and returns a {@link SlopeOneModel}.
     */
    @Override
    public SlopeOneModel get() {
        LongSet items = buildContext.getItems();
        LongIterator outer = items.iterator();
        while (outer.hasNext()) {
            final long item1 = outer.nextLong();
            final Long2DoubleSortedMap vec1 = buildContext.itemVector(item1);
            LongIterator inner = items.iterator();
            while (inner.hasNext()) {
                final long item2 = inner.nextLong();
                if (item1 != item2) {
                    Long2DoubleSortedMap vec2 = buildContext.itemVector(item2);
                    accumulator.putItemPair(item1, vec1, item2, vec2);
                }
            }
        }
        return new SlopeOneModel(accumulator.buildMatrix());
    }
}
