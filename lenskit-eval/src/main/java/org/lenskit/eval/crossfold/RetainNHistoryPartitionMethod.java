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

import static java.lang.Math.min;

/**
 * Partition the event list by retaining a fixed number of elements.
 */
public class RetainNHistoryPartitionMethod implements HistoryPartitionMethod {

    final private int count;

    /**
     * Create a count partitioner.
     *
     * @param n The number of items to put in the train partition.
     */
    public RetainNHistoryPartitionMethod(int n) {
        count = n;
    }

    @Override
    public int partition(List<? extends Rating> data) {
        return min(count, data.size());
    }

    @Override
    public String toString() {
        return String.format("retain(%d)", count);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        RetainNHistoryPartitionMethod that = (RetainNHistoryPartitionMethod) o;

        return new EqualsBuilder()
                .append(count, that.count)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(count)
                .toHashCode();
    }
}
