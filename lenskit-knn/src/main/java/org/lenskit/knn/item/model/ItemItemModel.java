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
package org.lenskit.knn.item.model;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.grapht.annotation.DefaultImplementation;

import javax.annotation.Nonnull;

/**
 * Item-item similarity model. It makes available the similarities
 * between items in the form of allowing queries to neighborhoods.
 * <p>
 * These similarities are post-normalization, so code using them
 * should typically use the same normalizations used by the builder
 * to make use of the similarity scores.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.10 as an interface.
 */
@DefaultImplementation(SimilarityMatrixModel.class)
public interface ItemItemModel {
    /**
     * Get the set of all items in the model.
     *
     * @return The set of item IDs for all items in the model.
     */
    LongSortedSet getItemUniverse();

    /**
     * Get the neighbors of an item scored by similarity. This is the corresponding
     * <em>row</em> of the item-item similarity matrix (see {@link org.lenskit.knn.item}).
     *
     * @param item The item to get the neighborhood for.
     * @return The row of the similarity matrix. If the item is unknown, an empty
     *         vector is returned.
     */
    @Nonnull
    Long2DoubleMap getNeighbors(long item);
}
