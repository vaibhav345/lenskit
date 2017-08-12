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
import org.lenskit.inject.Shareable;
import org.lenskit.util.InvertibleFunction;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * Default user vector normalizer that delegates to a generic {@link VectorNormalizer}.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.11
 */
@Shareable
public class DefaultUserVectorNormalizer implements UserVectorNormalizer, Serializable {
    private static final long serialVersionUID = 1L;
    protected final VectorNormalizer delegate;

    /**
     * Construct a new user vector normalizer that uses the identity normalization.
     */
    public DefaultUserVectorNormalizer() {
        this(new IdentityVectorNormalizer());
    }

    /**
     * Construct a new user vector normalizer wrapping a generic vector normalizer.
     *
     * @param norm The generic normalizer to use.
     */
    @Inject
    public DefaultUserVectorNormalizer(VectorNormalizer norm) {
        delegate = norm;
    }

    /**
     * Get the delegate vector normalizer.
     * @return The vector normalizer used by this UVN.
     */
    public VectorNormalizer getVectorNormalizer() {
        return delegate;
    }

    @Override
    public InvertibleFunction<Long2DoubleMap, Long2DoubleMap> makeTransformation(long user, Long2DoubleMap vector) {
        return delegate.makeTransformation(vector);
    }
}
