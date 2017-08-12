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
package org.lenskit.knn.user;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.grouplens.grapht.annotation.DefaultImplementation;

/**
 * Compute the similarity between two users.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.11
 */
@DefaultImplementation(UserVectorSimilarity.class)
public interface UserSimilarity {

    /**
     * Compute the similarity between two users.
     *
     * @param u1 The first user ID.
     * @param v1 The first user vector.
     * @param u2 The second user ID.
     * @param v2 The second user vector.
     * @return The similarity between the two users, in the range [0,1].
     */
    double similarity(long u1, Long2DoubleMap v1, long u2, Long2DoubleMap v2);

    /**
     * Query whether this similarity is sparse.
     *
     * @return {@code true} if the similarity function is sparse.
     * @see org.lenskit.similarity.VectorSimilarity#isSparse()
     */
    boolean isSparse();

    /**
     * Query whether this similarity is symmetric. <p> <b>Warning:</b> At present, asymmetric
     * similarity functions may not produce correct results. In practice, this is not a problem, as
     * most similarity functions are symmetric. Watch <a href="https://github.com/grouplens/lenskit/issues/151">issue
     * 151</a> for updates on this issue.
     *
     * @return {@code true} if the similarity function is symmetric.
     * @see org.lenskit.similarity.VectorSimilarity#isSymmetric()
     */
    boolean isSymmetric();
}
