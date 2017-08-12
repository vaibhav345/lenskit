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
package org.grouplens.lenskit.util.test;

import com.google.common.base.Equivalence;
import org.hamcrest.Matcher;

import java.io.File;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.equalTo;

/**
 * Entry point for extra matchers used by LensKit tests.
 */
public final class ExtraMatchers {
    private ExtraMatchers() {}

    /**
     * Match {@link Double#NaN}.
     * @return A matcher that accepts {@link Double#NaN}.
     */
    public static Matcher<Double> notANumber() {
        return new NotANumberMatcher();
    }

    public static Matcher<File> existingFile() {
        return new FileExistsMatcher();
    }

    public static Matcher<File> lineCount(int n) {
        return hasLineCount(equalTo(n));
    }

    public static Matcher<File> hasLineCount(Matcher<? extends Integer> m) {
        return new LineCountMatcher(m);
    }

    /**
     * Match a string against a regular expression.
     * @param pattern The regular expression to match.
     * @return A matcher that tests strings against the specified regular expression.
     */
    public static Matcher<CharSequence> matchesPattern(String pattern) {
        return new RegexMatcher(Pattern.compile(pattern));
    }

    /**
     * Test if the object is equivalent to object object.
     * @param obj The expected object.
     * @param equiv An equivalence relation.
     * @param <T> The type of object to compare.
     * @return A matcher that accepts objects equivalent to `obj`.
     */
    public static <T> Matcher<T> equivalentTo(T obj, Equivalence<T> equiv) {
        return new EquivalenceMatcher<>(obj, equiv);
    }
}
