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
package org.lenskit.eval.traintest.metrics;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

public class DiscountsTest {
    @Test
    public void testParseLog() {
        Discount discount = Discounts.parse("log");
        assertThat(discount, instanceOf(LogDiscount.class));
        assertThat(((LogDiscount) discount).getLogBase(),
                   equalTo(2.0));
    }

    @Test
    public void testParseLog2() {
        Discount discount = Discounts.parse("log2");
        assertThat(discount, instanceOf(LogDiscount.class));
        assertThat(((LogDiscount) discount).getLogBase(),
                   equalTo(2.0));
    }

    @Test
    public void testParseLogBase() {
        Discount discount = Discounts.parse("log(10)");
        assertThat(discount, instanceOf(LogDiscount.class));
        assertThat(((LogDiscount) discount).getLogBase(),
                   equalTo(10.0));
    }

    @Test
    public void testParseExp() {
        Discount discount = Discounts.parse("exp(5)");
        assertThat(discount, instanceOf(ExponentialDiscount.class));
        assertThat(((ExponentialDiscount) discount).getHalfLife(),
                   equalTo(5.0));
    }
}