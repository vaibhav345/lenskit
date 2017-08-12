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
package org.lenskit.eval.crossfold;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.lenskit.data.ratings.Rating;

import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.round;

/**
 * Partition a list by fraction.
 */
public class FractionHistoryPartitionMethod implements HistoryPartitionMethod {

    private double fraction;

    /**
     * The fraction to hold out (put in the second partition).
     *
     * @param f The fraction of users to hold out.
     */
    public FractionHistoryPartitionMethod(double f) {
        fraction = f;
    }

    @Override
    public int partition(List<? extends Rating> data) {
        int n = (int) round(data.size() * fraction);
        return max(0, data.size() - n);
    }

    @Override
    public String toString() {
        return String.format("fraction(%f)", fraction);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        FractionHistoryPartitionMethod that = (FractionHistoryPartitionMethod) o;

        return new EqualsBuilder()
                .append(fraction, that.fraction)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(fraction)
                .toHashCode();
    }
}
