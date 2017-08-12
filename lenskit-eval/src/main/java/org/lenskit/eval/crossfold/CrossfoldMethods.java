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

import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.CommonTypes;

public final class CrossfoldMethods {
    private CrossfoldMethods() {}

    /**
     * Create a crossfold method that splits users into disjoint partitions.
     * @param order The ordering for user rating partitions.
     * @param part the partition algorithm for user ratings.
     * @return The crossfold method.
     */
    public static CrossfoldMethod partitionUsers(SortOrder order, HistoryPartitionMethod part) {
        return new GroupedCrossfoldMethod(CommonTypes.USER, CommonAttributes.USER_ID,
                                          GroupEntitySplitter.partition(),
                                          order, part);
    }

    /**
     * Create a crossfold method that splits users into disjoint samples.
     * @param order The ordering for user rating partitions.
     * @param part the partition algorithm for user ratings.
     * @param size The number of users per sample.
     * @return The crossfold method.
     */
    public static CrossfoldMethod sampleUsers(SortOrder order, HistoryPartitionMethod part, int size) {
        return new GroupedCrossfoldMethod(CommonTypes.USER, CommonAttributes.USER_ID,
                                          GroupEntitySplitter.disjointSample(size),
                                          order, part);
    }

    /**
     * Create a crossfold method that partitions ratings into disjoint partitions.
     * @return The crossfold method.
     */
    public static CrossfoldMethod partitionEntities() {
        return new EntityPartitionCrossfoldMethod();
    }

    /**
     * Create a crossfold method that splits items into disjoint partitions.
     * @param part the partition algorithm for item ratings.
     * @return The crossfold method.
     */
    public static CrossfoldMethod partitionItems(HistoryPartitionMethod part) {
        return new GroupedCrossfoldMethod(CommonTypes.ITEM, CommonAttributes.ITEM_ID,
                                          GroupEntitySplitter.partition(),
                                          SortOrder.RANDOM, part);
    }

    /**
     * Create a crossfold method that splits items into disjoint samples.
     * @param part the partition algorithm for item ratings.
     * @param size The number of items per sample.
     * @return The crossfold method.
     */
    public static CrossfoldMethod sampleItems(HistoryPartitionMethod part, int size) {
        return new GroupedCrossfoldMethod(CommonTypes.ITEM, CommonAttributes.ITEM_ID,
                                          GroupEntitySplitter.disjointSample(size),
                                          SortOrder.RANDOM, part);
    }
}
