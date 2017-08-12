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
 * Normalize a user's vector. This vector is typically a rating or purchase vector.
 * <p>
 * This interface is essentially a user-aware version of {@link VectorNormalizer}. The
 * default implementation, {@link DefaultUserVectorNormalizer}, delegates to a
 * {@link VectorNormalizer}. Implement this interface directly to create a normalizer
 * that is aware of the fact that it is normalizing a user and e.g. uses user properties
 * outside the vector to aid in the normalization. Otherwise, use a context-sensitive
 * binding of {@link VectorNormalizer} to configure the user vector normalizer:
 * </p>
 *
 * {@code
 * factory.in(UserVectorNormalizer.class)
 * .bind(VectorNormalizer.class)
 * .to(MeanVarianceNormalizer.class);
 * }
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @see VectorNormalizer
 * @since 0.11
 */
@DefaultImplementation(DefaultUserVectorNormalizer.class)
public interface UserVectorNormalizer {
    /**
     * Make a vector transformation for a user. The resulting transformation will be applied
     * to user vectors to normalize and denormalize them.
     *
     * @param user   The user ID to normalize for.
     * @param vector The user's vector to use as the reference vector.
     * @return The vector transformation normalizing for this user.
     */
    InvertibleFunction<Long2DoubleMap,Long2DoubleMap> makeTransformation(long user, Long2DoubleMap vector);
}
