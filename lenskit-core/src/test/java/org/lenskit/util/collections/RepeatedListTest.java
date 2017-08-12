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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class RepeatedListTest {

    @Test
    public void testEmpty() {
        RepeatedList<Integer> rl = new RepeatedList<>(7, 0);
        assertThat(rl.size(), equalTo(0));
        try {
            rl.get(-1);
            fail("Should throw an exception!");
        } catch(IndexOutOfBoundsException e) { /* expected */ }
        try {
            rl.get(0);
            fail("Should throw an exception!");
        } catch(IndexOutOfBoundsException e) { /* expected */ }

        try {
            rl.get(1);
            fail("Should throw an exception!");
        } catch(IndexOutOfBoundsException e) { /* expected */ }
    }

    @Test
    public void testSingle() {
        RepeatedList<Integer> rl = new RepeatedList<>(7, 1);
        assertThat(rl.size(), equalTo(1));
        assertThat(rl.get(0).intValue(), equalTo(7));
        
        try {
            rl.get(1);
            fail("Should throw an exception!");
        } catch(IndexOutOfBoundsException e) { /* expected */ }
  
    }

 }
