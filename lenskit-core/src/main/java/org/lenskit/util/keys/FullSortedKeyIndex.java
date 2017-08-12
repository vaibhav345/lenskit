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
package org.lenskit.util.keys;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * Full 64-bit implementation of {@link SortedKeyIndex}.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class FullSortedKeyIndex extends SortedKeyIndex {
    private static final long serialVersionUID = 1L;
    private final long[] keys;

    public FullSortedKeyIndex(@Nonnull long[] ks, int lb, int ub) {
        super(lb, ub);
        assert ks.length >= ub;
        keys = ks;
    }

    @Override
    public int tryGetIndex(long key) {
        return Arrays.binarySearch(keys, lowerBound, upperBound, key);
    }

    @Override
    public long getKey(int idx) {
        if (idx < lowerBound || idx >= upperBound) {
            throw new IndexOutOfBoundsException("index " + idx + " is not in range [" + lowerBound + "," + upperBound + ")");
        }
        assert idx >= lowerBound && idx < upperBound;
        return keys[idx];
    }

    @Override
    public SortedKeyIndex subIndex(int lb, int ub) {
        Preconditions.checkArgument(lb >= lowerBound && lb <= upperBound, "lower bound out of range");
        Preconditions.checkArgument(lb <= ub, "range is negative");
        Preconditions.checkArgument(ub >= lowerBound && ub <= upperBound, "upper bound out of range");
        return new FullSortedKeyIndex(keys, lb, ub);
    }
}
