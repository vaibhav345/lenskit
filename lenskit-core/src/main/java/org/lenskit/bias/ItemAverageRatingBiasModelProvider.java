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
package org.lenskit.bias;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import org.lenskit.data.ratings.RatingSummary;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Compute a bias model that returns items' average ratings.  For an item \\(i\\), the global bias \\(b\\) plus the
 * item bias \\(b_i\\) will equal the item's average rating.  User biases are all zero.
 */
public class ItemAverageRatingBiasModelProvider implements Provider<ItemBiasModel> {
    private final RatingSummary summary;
    private final double damping;

    @Inject
    public ItemAverageRatingBiasModelProvider(RatingSummary rs, @BiasDamping double damp) {
        summary = rs;
        damping = damp;
    }

    @Override
    public ItemBiasModel get() {
        Long2DoubleMap offsets;

        if (damping > 0) {
            offsets = new Long2DoubleOpenHashMap();
            LongIterator iter = summary.getItems().iterator();
            while (iter.hasNext()) {
                long item = iter.nextLong();
                double off = summary.getItemOffset(item);
                int count = summary.getItemRatingCount(item);
                offsets.put(item, count * off / (count + damping));
            }
        } else {
            offsets = summary.getItemOffets();
        }

        return new ItemBiasModel(summary.getGlobalMean(), offsets);
    }
}
