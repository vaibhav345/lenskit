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

import it.unimi.dsi.fastutil.longs.*;
import org.junit.Test;
import org.lenskit.util.collections.LongUtils;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Random;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LongSortedArraySetTest {
    @SuppressWarnings("unchecked")
    private LongSortedArraySet emptySet() {
        return new LongSortedArraySet(Collections.EMPTY_LIST);
    }

    @Test
    public void testEmptySet() {
        LongSortedArraySet set = emptySet();
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
        LongBidirectionalIterator iter = set.iterator();
        assertNotNull(iter);
        assertFalse(iter.hasNext());
        assertFalse(iter.hasPrevious());
        assertFalse(set.contains(42));
        assertFalse(set.contains(Long.valueOf(42)));
    }

    @Test
    public void testEmptySetSubset() {
        LongSortedArraySet set = emptySet();
        LongSortedSet sset = set.headSet(50);
        assertTrue(sset.isEmpty());
        assertEquals(0, sset.size());
        assertFalse(set.iterator().hasNext());
        assertFalse(set.iterator().hasPrevious());
        assertFalse(set.contains(42));
        assertFalse(set.contains(Long.valueOf(42)));
    }

    /**
     * Run a battery of tests on a standard set. Used to test a variety of
     * construction scenarios with less code duplication.
     *
     * @param set The set {2, 5, 6}.
     */
    private void testSetSimple(LongSortedSet set) {
        assertFalse(set.isEmpty());
        assertEquals(3, set.size());
        assertEquals(2, set.firstLong());
        assertEquals(6, set.lastLong());
        assertEquals(Long.valueOf(2), set.first());
        assertEquals(Long.valueOf(6), set.last());
        assertTrue(set.contains(2));
        assertTrue(set.contains(5));
        assertTrue(set.contains(6));
        assertFalse(set.contains(0));
        assertFalse(set.contains(42));
        assertFalse(set.iterator().hasPrevious());
        long[] items = LongIterators.unwrap(set.iterator());
        assertEquals(2, items[0]);
        assertEquals(5, items[1]);
        assertEquals(6, items[2]);
    }

    @Test
    public void testArrayCtor() {
        LongSortedSet set = new LongSortedArraySet(new long[]{2, 5, 6});
        testSetSimple(set);

        LongBidirectionalIterator iter = set.iterator();
        assertTrue(iter.hasNext());
        assertEquals(2, iter.nextLong());
        assertTrue(iter.hasPrevious());
        assertEquals(2, iter.previousLong());
        assertFalse(iter.hasPrevious());
    }

    @Test
    public void testArrayCtorUnsorted() {
        long[] data = {5, 2, 6};
        LongSortedSet set = new LongSortedArraySet(data);
        testSetSimple(set);
    }

    @Test
    public void testCollectionCtor() {
        long[] data = {5, 2, 6};
        LongSortedSet set = new LongSortedArraySet(new LongArrayList(data));
        testSetSimple(set);
    }

    @Test
    public void testHeadSet() {
        long[] data = {7, 5, 2, 6, 42};
        LongSortedSet set = new LongSortedArraySet(data);
        assertEquals(data.length, set.size());
        testSetSimple(set.headSet(7));
    }

    @Test
    public void testTailSet() {
        long[] data = {0, 5, 2, 6, 1};
        LongSortedSet set = new LongSortedArraySet(data);
        assertEquals(data.length, set.size());
        testSetSimple(set.tailSet(2));
    }

    @Test
    public void testSubSet() {
        long[] data = {0, 42, 5, 2, 6, 1, 7};
        LongSortedSet set = new LongSortedArraySet(data);
        assertEquals(data.length, set.size());
        testSetSimple(set.subSet(2, 7));
    }

    @Test
    public void testTailIter() {
        long[] data = {0, 42, 5, 2, 6, 1, 7};
        LongSortedSet set = new LongSortedArraySet(data);
        LongBidirectionalIterator iter = set.iterator(2);
        assertTrue(iter.hasNext());
        assertTrue(iter.hasPrevious());
        assertEquals(5, iter.nextLong());
        iter = set.iterator(2);
        assertEquals(2, iter.previousLong());
        iter = set.iterator(-5);
        assertFalse(iter.hasPrevious());
        iter = set.iterator(100);
        assertFalse(iter.hasNext());
        iter = set.iterator(3);
        assertEquals(5, iter.nextLong());
        iter = set.iterator(3);
        assertEquals(2, iter.previousLong());
    }

    @Test
    public void testRemoveDuplicates() {
        long[] data = {5, 2, 6, 2};
        LongSortedSet set = new LongSortedArraySet(data);
        testSetSimple(set);
    }

    @Test
    public void testSubsetIterator() {
        LongSortedSet set = LongUtils.packedSet(1L, 3L, 5L, 10L);
        LongSortedSet subset = set.subSet(3L, 6L);
        assertThat(subset, hasSize(2));
        assertThat(subset, contains(3L, 5L));
        LongBidirectionalIterator iter = subset.iterator();
        assertThat(iter.hasPrevious(), equalTo(false));
        try {
            iter.previousLong();
            fail("previousLong should fail");
        } catch (NoSuchElementException e) {
            /* expected */
        }
    }

    @Test
    public void testRandomSubsetEmpty() {
        LongSortedArraySet empty = LongUtils.packedSet();
        LongSortedSet sample = empty.randomSubset(new Random(), 10);
        assertThat(sample, hasSize(0));
    }

    @Test
    public void testRandomSubsetPickOnly() {
        LongSortedArraySet singleton = LongUtils.packedSet(42);
        LongSortedSet sample = singleton.randomSubset(new Random(), 1);
        assertThat(sample, contains(42L));
        sample = singleton.randomSubset(new Random(), 10);
        assertThat(sample, contains(42L));
    }

    @Test
    public void testRandomSubsetPickNone() {
        LongSortedArraySet singleton = LongUtils.packedSet(42);
        LongSortedSet sample = singleton.randomSubset(new Random(), 0);
        assertThat(sample, hasSize(0));
    }

    @Test
    public void testRandomSubsetPick() {
        LongSortedArraySet singleton = LongUtils.packedSet(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Random rng = new Random();
        for (int i = 0; i < 50; i++) {
            LongSortedSet sample = singleton.randomSubset(rng, 5);
            assertThat(sample, hasSize(5));
            assertThat(sample, everyItem(allOf(greaterThan(0L), lessThan(11L))));
        }
    }

    @Test
    public void testRandomSubsetPickExclude() {
        LongSortedArraySet singleton = LongUtils.packedSet(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Random rng = new Random();
        for (int i = 0; i < 50; i++) {
            LongSortedSet sample = singleton.randomSubset(rng, 5, LongSets.singleton(7L));
            assertThat(sample, hasSize(5));
            assertThat(sample, everyItem(allOf(greaterThan(0L), lessThan(11L))));
            assertThat(sample, not(hasItem(7L)));
        }
    }

    @Test
    public void testRandomSubsetPickAll() {
        LongSortedArraySet singleton = LongUtils.packedSet(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Random rng = new Random();
        for (int i = 0; i < 50; i++) {
            LongSortedSet sample = singleton.randomSubset(rng, 5, LongUtils.packedSet(1L, 3L, 5L, 7L, 9L));
            assertThat(sample, hasSize(5));
            assertThat(sample, containsInAnyOrder(2L, 4L, 6L, 8L, 10L));
        }
    }

    @Test
    public void testRandomSubsetPickAllMultiExclude() {
        LongSortedArraySet singleton = LongUtils.packedSet(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        for (int i = 0; i < 50; i++) {
            LongSortedSet sample = singleton.randomSubset(new Random(), 5,
                                                          LongUtils.packedSet(1L, 3L, 5L),
                                                          LongUtils.packedSet(3L, 7L, 9L));
            assertThat(sample, hasSize(5));
            assertThat(sample, containsInAnyOrder(2L, 4L, 6L, 8L, 10L));
        }
    }

    @Test
    public void testRandomSubsetPickMultiExclude() {
        LongSortedArraySet singleton = LongUtils.packedSet(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        for (int i = 0; i < 50; i++) {
            LongSortedSet sample = singleton.randomSubset(new Random(), 5,
                                                          LongUtils.packedSet(1L),
                                                          LongUtils.packedSet(3L));
            assertThat(sample, hasSize(5));
            assertThat(sample, everyItem(allOf(greaterThan(0L), lessThan(11L))));
            assertThat(sample, not(anyOf(hasItem(1L), hasItem(3L))));
        }
    }

    @Test
    public void testRandomSubsetPickLimitMultiExclude() {
        LongSortedArraySet singleton = LongUtils.packedSet(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        for (int i = 0; i < 50; i++) {
            LongSortedSet sample = singleton.randomSubset(new Random(), 4,
                                                          LongUtils.packedSet(1L, 3L, 5L),
                                                          LongUtils.packedSet(3L, 7L, 9L));
            assertThat(sample, hasSize(4));
            assertThat(sample, anyOf(containsInAnyOrder(2L, 4L, 6L, 8L),
                                     containsInAnyOrder(2L, 4L, 6L, 10L),
                                     containsInAnyOrder(2L, 4L, 8L, 10L),
                                     containsInAnyOrder(2L, 6L, 8L, 10L),
                                     containsInAnyOrder(4L, 6L, 8L, 10L)));
        }
    }
}
