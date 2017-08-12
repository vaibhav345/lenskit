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
package org.lenskit.transform.normalize;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.grouplens.grapht.annotation.DefaultImplementation;
import org.lenskit.util.InvertibleFunction;

/**
 * Normalizes an item's vector.
 */
@DefaultImplementation(DefaultItemVectorNormalizer.class)
public interface ItemVectorNormalizer {

    /**
     * Make a vector transformation for a item. The resulting transformation will be applied
     * to item vectors to normalize and denormalize them.
     *
     * @param itemId The item ID to normalize for.
     * @param vector The item's vector to use as the reference vector.
     * @return The vector transformation normalizing for this item.
     */
    InvertibleFunction<Long2DoubleMap,Long2DoubleMap> makeTransformation(long itemId, Long2DoubleMap vector);
}
