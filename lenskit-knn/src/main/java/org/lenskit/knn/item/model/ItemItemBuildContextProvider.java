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

import it.unimi.dsi.fastutil.longs.*;
import org.lenskit.data.ratings.RatingVectorPDAO;
import org.lenskit.inject.Transient;
import org.lenskit.knn.item.MinCommonUsers;
import org.lenskit.transform.normalize.UserVectorNormalizer;
import org.lenskit.util.IdBox;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.keys.SortedKeyIndex;
import org.lenskit.util.math.Vectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Iterator;

/**
 * Provider that sets up an {@link ItemItemBuildContext}.
 * 
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ItemItemBuildContextProvider implements Provider<ItemItemBuildContext> {

    private static final Logger logger = LoggerFactory.getLogger(ItemItemBuildContextProvider.class);

    private final RatingVectorPDAO rvDAO;
    private final UserVectorNormalizer normalizer;
    private final int minCommonUsers;

    /**
     * Construct an item-item build context provider.
     *
     * @param rvd The rating vector DAO.
     * @param normalizer The user vector normalizer.
     */
    @Inject
    public ItemItemBuildContextProvider(@Transient RatingVectorPDAO rvd,
                                        @Transient UserVectorNormalizer normalizer,
                                        @MinCommonUsers int minCU) {
        rvDAO = rvd;
        this.normalizer = normalizer;
        minCommonUsers = minCU;
    }

    public ItemItemBuildContextProvider(@Transient RatingVectorPDAO rvd,
                                        @Transient UserVectorNormalizer normalizer) {
        this(rvd, normalizer, 1);
    }

    /**
     * Constructs and returns a new ItemItemBuildContext.
     *
     * @return a new ItemItemBuildContext.
     */
    @Override
    public ItemItemBuildContext get() {
        logger.info("constructing build context");
        logger.debug("using normalizer {}", normalizer);

        logger.debug("Building item data");
        Long2ObjectMap<Long2DoubleMap> itemRatingData = new Long2ObjectOpenHashMap<>(1000);
        Long2ObjectMap<LongSortedSet> userItems = new Long2ObjectOpenHashMap<>(1000);
        buildItemRatings(itemRatingData, userItems);
        int oldN = itemRatingData.size();
        pruneItems(itemRatingData, userItems);
        logger.info("retaining data for {} of {} items", itemRatingData.size(), oldN);

        SortedKeyIndex items = SortedKeyIndex.fromCollection(itemRatingData.keySet());
        final int n = items.size();
        assert n == itemRatingData.size();
        // finalize the item data into vectors
        Long2DoubleSortedMap[] itemRatings = new Long2DoubleSortedMap[n];

        for (int i = 0; i < n; i++) {
            final long item = items.getKey(i);
            Long2DoubleMap ratings = itemRatingData.get(item);
            itemRatings[i] = LongUtils.frozenMap(ratings);
            // release some memory
            ratings.clear();
        }

        logger.debug("item data completed");
        return new ItemItemBuildContext(items, itemRatings, userItems);
    }

    /**
     * Transpose the user matrix so we have a matrix of item ids to ratings. Accumulate user item vectors into
     * the candidate sets for each item
     *  @param itemRatings    mapping from item ids to (userId: rating) maps (to be filled)
     * @param userItems mapping of user IDs to rated item sets to be filled.
     */
    private void buildItemRatings(Long2ObjectMap<Long2DoubleMap> itemRatings,
                                  Long2ObjectMap<LongSortedSet> userItems) {
        // initialize the transposed array to collect item vector data
        try (ObjectStream<IdBox<Long2DoubleMap>> stream = rvDAO.streamUsers()) {
            for (IdBox<Long2DoubleMap> user : stream) {
                long uid = user.getId();
                Long2DoubleMap ratings = user.getValue();
                Long2DoubleMap normed = normalizer.makeTransformation(uid, ratings).apply(ratings);
                assert normed != null;

                Iterator<Long2DoubleMap.Entry> iter = Vectors.fastEntryIterator(normed);
                while (iter.hasNext()) {
                    Long2DoubleMap.Entry rating = iter.next();
                    final long item = rating.getLongKey();
                    // get the item's rating accumulator
                    Long2DoubleMap ivect = itemRatings.get(item);
                    if (ivect == null) {
                        ivect = new Long2DoubleOpenHashMap();
                        itemRatings.put(item, ivect);
                    }
                    ivect.put(uid, rating.getDoubleValue());
                }

                // store the user's item set
                // if the user only has 1 rating, they will never be for a neighborhood
                if (normed.size() > 1) {
                    userItems.put(uid, LongUtils.packedSet(normed.keySet()));
                }
            }
        }
    }

    private void pruneItems(Long2ObjectMap<Long2DoubleMap> itemRatingData, Long2ObjectMap<LongSortedSet> userItems) {
        if (minCommonUsers <= 0) {
            return;
        }

        // copy items to array to all
        long[] items = itemRatingData.keySet().toLongArray();
        for (long item: items) {
            Long2DoubleMap iv = itemRatingData.get(item);
            if (iv.size() < minCommonUsers) {
                itemRatingData.remove(item);
            }
        }

        for (Long2ObjectMap.Entry<LongSortedSet> e: userItems.long2ObjectEntrySet()) {
            e.setValue(LongUtils.setIntersect(e.getValue(), itemRatingData.keySet()));
        }
    }
}
