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
package org.lenskit.util.math;

public final class Scalars {
    /**
     * The default epsilon for checking for zero.  If the Java system property {@code lenskit.zero.epsilon} is set,
     * the value of that is used; otherwise, {@link Double#MIN_NORMAL}.
     */
    public static final double DEFAULT_EPSILON;
    private static final double LOG2_ADJ = 1.0 / Math.log(2);

    private Scalars() {}

    static {
        String prop = System.getProperty("lenskit.zero.epsilon");
        if (prop == null) {
            DEFAULT_EPSILON = Double.MIN_NORMAL;
        } else {
            DEFAULT_EPSILON = Double.parseDouble(prop);
        }
    }

    /**
     * Check whether a value is zero, using a default epsilon.
     * @param val The value.
     * @return {@code true} if the value is within an epsilon of zero.
     */
    public static boolean isZero(double val) {
        return isZero(val, DEFAULT_EPSILON);
    }

    /**
     * Check whether a value is zero, using a configurable epsilon.
     * @param val The value.
     * @param epsilon The tolerance.
     * @return {@code true} if absolute value of {@code val} is less than {@code epsilon}.
     */
    public static boolean isZero(double val, double epsilon) {
        return Math.abs(val) < epsilon;
    }

    public static double log2(double x) {
        return Math.log(x) * LOG2_ADJ;
    }
}
