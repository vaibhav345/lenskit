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
package org.lenskit.util.collections;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongList;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.util.collections.Long2DoubleAccumulator;
import org.lenskit.util.collections.TopNLong2DoubleAccumulator;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TopNLong2DoubleAccumulatorTest {
    Long2DoubleAccumulator accum;

    @Before
    public void createAccumulator() {
        accum = new TopNLong2DoubleAccumulator(3);
    }

    @Test
    public void testEmpty() {
        LongList out = accum.finishList();
        assertTrue(out.isEmpty());
    }

    @Test
    public void testAccumMap() {
        accum.put(5, 4.2);
        accum.put(3, 2.9);
        accum.put(2, 9.8);
        Long2DoubleMap out = accum.finishMap();
        assertThat(out.size(), equalTo(3));
        assertThat(out, hasEntry(2L, 9.8));
        assertThat(out, hasEntry(5L, 4.2));
        assertThat(out, hasEntry(3L, 2.9));
    }

    @Test
    public void testAccumMapLimit() {
        accum.put(7, 1.0);
        accum.put(5, 4.2);
        accum.put(3, 2.9);
        accum.put(2, 9.8);
        accum.put(8, 2.1);
        Long2DoubleMap out = accum.finishMap();
        assertThat(out.size(), equalTo(3));
        assertThat(out, hasEntry(2L, 9.8));
        assertThat(out, hasEntry(5L, 4.2));
        assertThat(out, hasEntry(3L, 2.9));
    }

    @Test
    public void testAccumList() {
        accum.put(5, 4.2);
        accum.put(3, 2.9);
        accum.put(2, 9.8);
        LongList out = accum.finishList();
        assertThat(out, contains(2L, 5L, 3L));
    }
}
