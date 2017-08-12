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
package org.lenskit.baseline;

import org.grouplens.grapht.annotation.DefaultDouble;
import org.lenskit.inject.Parameter;

import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * Damping parameter for means in baseline predictors.
 *
 * <p>
 * The smoothing enabled by this parameter is based on <a
 * href="http://sifter.org/~simon/journal/20061211.html">FunkSVD</a>. The idea
 * is to bias item or user means towards the global mean based on how many items
 * or users are involved. So, if the global mean is \\(\mu\\) and smoothing \\(\gamma\\),
 * the mean of some values is \\[\frac{\gamma\mu + \sum r}{\gamma + n}\\]
 */
@Documented
@DefaultDouble(0.0)
@Parameter(Double.class)
@Qualifier
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MeanDamping {
}
