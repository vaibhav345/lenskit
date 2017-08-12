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

import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.java.quickcheck.Generator;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.java.quickcheck.generator.CombinedGenerators.sortedLists;
import static net.java.quickcheck.generator.CombinedGenerators.uniqueValues;
import static net.java.quickcheck.generator.PrimitiveGenerators.longs;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeThat;

/**
 * Dynamic tests for long key sets.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@RunWith(Theories.class)
public class DynamicSortedKeyIndexTest {
    private static class KeyData {
        List<Long> allKeys;

        public KeyData(List<Long> keys) {
            allKeys = keys;
        }

        public long getKey() {
            return getKey(0);
        }

        public long getKey(int k) {
            return allKeys.get(1 + k*2);
        }

        public long[] getKeys(int n) {
            long[] keys = new long[n];
            for (int i = 0; i < n; i++) {
                keys[i] = getKey(i);
            }
            return keys;
        }

        public long getLow() {
            return allKeys.get(0);
        }
        public long getAfter(int k) {
            return allKeys.get(2 + k*2);
        }
    }

    @DataPoints
    public static KeyData[] DATA_POINTS;

    /**
     * Method to compute the data points. Split out so that errors it throws get reported.
     */
    @BeforeClass
    public static void makeDataPoints() {
        KeyData[] data = new KeyData[10];
        Generator<Long> intGen = longs(Integer.MIN_VALUE, Integer.MAX_VALUE);
        Generator<Long> longGen = longs(Integer.MAX_VALUE + 1L, Long.MAX_VALUE);
        for (int i = 0; i < 10; i++) {
            Generator<List<Long>> listGen;
            if (i % 2 == 0) {
                // generate ints
                listGen = sortedLists(uniqueValues(intGen), 25, 25);
            } else {
                // generate longs
                listGen = sortedLists(uniqueValues(longGen), 25, 25);
            }
            List<Long> nums = listGen.next();
            data[i] = new KeyData(nums);
        }
        DATA_POINTS = data;
    }

    @Theory
    public void testSingleton(KeyData data) {
        assumeThat(data, notNullValue());
        long key = data.getKey();       // key to use
        long low = data.getLow();       // unused low key
        long high = data.getAfter(1);   // unused high key
        long[] rawKeys = {key};
        SortedKeyIndex keys = SortedKeyIndex.wrap(rawKeys, 1);
        assertThat(keys.size(), equalTo(1));
        assertThat(keys.size(), equalTo(1));
        assertThat(keys.keySet(), hasSize(1));

        assertThat(keys.tryGetIndex(key), equalTo(0));
        assertThat(keys.tryGetIndex(low), lessThan(0));
        assertThat(keys.tryGetIndex(high), lessThan(0));
        assertThat(keys.getKeyList(), contains(key));
    }

    @Theory
    public void testMultiple(KeyData data) {
        long[] rawKeys = data.getKeys(3);
        long k1 = rawKeys[0];
        long k2 = rawKeys[1];
        long k3 = rawKeys[2];
        SortedKeyIndex keys = SortedKeyIndex.wrap(rawKeys, 3);
        assertThat(keys.size(), equalTo(3));
        assertThat(keys.size(), equalTo(3));
        assertThat(keys.getKeyList(), contains(k1, k2, k3));
        assertThat(keys.keySet(), contains(k1, k2, k3));

        assertThat(keys.tryGetIndex(k1), equalTo(0));
        assertThat(keys.tryGetIndex(k2), equalTo(1));
        assertThat(keys.tryGetIndex(k3), equalTo(2));
        assertThat(keys.tryGetIndex(data.getLow()), lessThan(0));
        assertThat(keys.tryGetIndex(data.getAfter(0)), lessThan(0));
        assertThat(keys.tryGetIndex(data.getAfter(1)), lessThan(0));
        assertThat(keys.tryGetIndex(data.getAfter(2)), lessThan(0));
    }

    @Theory
    public void testABunch(KeyData data) {
        long[] rawKeys = data.getKeys(10);
        List<Long> keyList = new LongArrayList(rawKeys);
        SortedKeyIndex keys = SortedKeyIndex.wrap(rawKeys, rawKeys.length);
        assertThat(keys.size(), equalTo(10));
        assertThat(keys.size(), equalTo(10));

        assertThat(keys.getKeyList(), contains(keyList.toArray()));
        assertThat(keys.keySet(), contains(keyList.toArray()));

        assertThat(keys.tryGetIndex(data.getLow()), lessThan(0));
        for (int i = 0; i < 10; i++) {
            assumeThat(keys.tryGetIndex(rawKeys[i]), equalTo(i));
            assertThat(keys.tryGetIndex(data.getAfter(i)), lessThan(0));
        }
    }
}
