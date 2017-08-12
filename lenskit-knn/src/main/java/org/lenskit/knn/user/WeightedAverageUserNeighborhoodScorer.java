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

import org.lenskit.inject.Shareable;
import org.lenskit.knn.MinNeighbors;

import javax.annotation.Nullable;
import net.jcip.annotations.Immutable;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

/**
 * Score an item using the weighted average of neighbor ratings.
 */
@Immutable
@Shareable
public class WeightedAverageUserNeighborhoodScorer implements UserNeighborhoodScorer, Serializable {
    private static final long serialVersionUID = 1L;

    private final int minimumNeighbors;

    /**
     * Construct a weighted average scorer.
     * @param min The minimum neighbors.
     */
    @Inject
    public WeightedAverageUserNeighborhoodScorer(@MinNeighbors int min) {
        minimumNeighbors = min;
    }

    @Override
    @Nullable
    public UserUserResult score(long item, List<Neighbor> neighbors) {
        if (neighbors.size() < minimumNeighbors) {
            return null;
        }
        double sum = 0;
        double weight = 0;
        for (Neighbor n: neighbors) {
            assert n.vector.containsKey(item);
            weight += Math.abs(n.similarity);
            sum += n.similarity * n.vector.get(item);
        }
        if (weight > 0) {
            return UserUserResult.newBuilder()
                                 .setItemId(item)
                                 .setScore(sum / weight)
                                 .setTotalWeight(weight)
                                 .setNeighborhoodSize(neighbors.size())
                                 .build();
        } else {
            return null;
        }
    }
}
