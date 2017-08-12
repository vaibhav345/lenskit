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
/**
 * Implementation of item-item collaborative filtering.
 * <p>
 * The item-item CF implementation is built up of several pieces. The
 * {@linkplain org.lenskit.knn.item.model.ItemItemModelProvider model builder} takes the rating data
 * and several parameters and components, such as the
 * {@linkplain org.lenskit.similarity.VectorSimilarity similarity function} and {@linkplain ModelSize model size},
 * and computes the {@linkplain org.lenskit.knn.item.model.SimilarityMatrixModel similarity matrix}. The
 * {@linkplain ItemItemScorer scorer}
 * uses this model to score items.
 * <p>
 * The basic idea of item-item CF is to compute similarities between items, typically
 * based on the users that have rated them, and the recommend items similar to the items
 * that a user likes. The model is then truncated — only the {@link ModelSize} most similar
 * items are retained for each item – to save space. Neighborhoods are further truncated
 * when doing recommendation; only the {@link org.lenskit.knn.NeighborhoodSize} most similar items that
 * a user has rated are used to score any given item. {@link ModelSize} is typically
 * larger than {@link org.lenskit.knn.NeighborhoodSize} to improve the ability of the recommender to find
 * neighbors.
 * <p>
 * When the similarity function is asymmetric (\(s(i,j)=s(j,i)\) does not hold), some care
 * is needed to make sure that the function is used in the correct direction. Following
 * Deshpande and Karypis, we use the similarity function as \(s(j,i)\), where \(j\) is the
 * item the user has purchased or rated and \(i\) is the item that is going to be scored. This
 * function is then stored in row \(i\) and column \(j\) of the matrix. Rows are then truncated
 * (so we retain the {@link ModelSize} most similar items for each \(i\)); this direction differs
 * from Deshpande &amp; Karypis, as row truncation is more efficient &amp; simpler to write within
 * LensKit's item-item algorithm structure, and performs better in offline tests against the
 * MovieLens 1M data set
 * (see <a href="http://dev.grouplens.org/trac/lenskit/wiki/ItemItemTruncateDirection">writeup</a>).
 * Computation against a particular item the user has rated is done down that item's column.
 * <p>
 * The scorers and recommenders actually operate on a generic {@link org.lenskit.knn.item.model.ItemItemModel}, so the
 * item-based scoring algorithm can be used against other sources of similarity, such as
 * similarities stored in a database or text index.
 */
package org.lenskit.knn.item;

