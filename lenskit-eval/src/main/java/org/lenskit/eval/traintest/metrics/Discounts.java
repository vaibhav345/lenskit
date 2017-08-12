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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Definitions of different discount functions.
 */
public final class Discounts {
    private Discounts() {}

    private static final Pattern LOG_PAT = Pattern.compile("log(?:\\((\\d+)\\))?", Pattern.CASE_INSENSITIVE);
    private static final Pattern EXP_PAT = Pattern.compile("exp\\((\\d+)\\)", Pattern.CASE_INSENSITIVE);

    /**
     * Create a log-base-2 discount.  The discount function is:
     *
     * \\[\\mathrm{disc}(i) =
     * \\begin{cases}
     * 1 & i \\le 2 \\\\
     * (\\mathrm{log}_{2} i)^{-1} & \\mathrm{else}
     * \\end{cases} \\]
     *
     * @return The discount.
     */
    public static LogDiscount log2() {
        return new LogDiscount(2);
    }

    /**
     * Create a new logarithmic discount.  The discount function is:
     *
     * \\[\\mathrm{disc}(i) =
     * \\begin{cases}
     * 1 & i \\le b \\\\
     * (\\mathrm{log}_{b} i)^{-1} & \\mathrm{else}
     * \\end{cases} \\]
     *
     * @param base The log base $b$.
     * @return The discount.
     */
    public static LogDiscount log(double base) {
        return new LogDiscount(base);
    }

    /**
     * Create a new exponential (half-life) discount.  The discount function is:
     *
     * \\[\\mathrm{disc}(i) = \\left(2^{\\frac{i-1}{\\alpha-1}}\\right)^{-1}\\]
     *
     * @param hl The half-life $\\alpha$ of the decay function.
     * @return The discount.
     */
    public static ExponentialDiscount exp(double hl) {
        return new ExponentialDiscount(hl);
    }

    /**
     * Parse a discount expression from a string.
     * @param disc The discount string.
     * @return The discount.
     */
    public static Discount parse(String disc) {
        if (disc.toLowerCase().equals("log2")) {
            return log2();
        }

        Matcher m = LOG_PAT.matcher(disc);
        if (m.matches()) {
            String grp = m.group(1);
            double base = grp != null ? Double.parseDouble(grp) : 2;
            return new LogDiscount(base);
        }

        m = EXP_PAT.matcher(disc);
        if (m.matches()) {
            double hl = Double.parseDouble(m.group(1));
            return new ExponentialDiscount(hl);
        }

        throw new IllegalArgumentException("invalid discount specification " + disc);
    }
}
