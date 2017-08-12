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
package org.lenskit.similarity;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import org.grouplens.grapht.annotation.DefaultInteger;
import org.lenskit.inject.Parameter;

/**
 * The weight threshold for significance weighting.  If the vectors in question
 * have fewer than <i>n</i> keys in common, then the similarity is decreased.
 *
 * @see org.lenskit.similarity.SignificanceWeightedVectorSimilarity
 * @see SignificanceWeightedVectorSimilarity
 */
@Documented
@DefaultInteger(0)
@Parameter(Integer.class)
@Qualifier
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SigWeightThreshold {
}
