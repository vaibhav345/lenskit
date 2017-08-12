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

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import net.jcip.annotations.ThreadSafe;
import org.lenskit.inject.Shareable;
import org.lenskit.util.collections.LongUtils;

import javax.inject.Inject;
import java.io.Serializable;

import static java.lang.Math.max;

/**
 * Apply significance weighting to a similarity function. The threshold
 * is configured with the {@link SigWeightThreshold} parameter.
 *
 * <p>Significance weighting decreases the similarity between two vectors when
 * the number of common entities between the two vectors is low.  For a threshold
 * \(S\) and key sets \(K_1\) and \(K_2\), the similarity is multipled by
 * \[\frac{|K_1 \cap K_2|}{\mathrm{max}(|K_1 \cap K_2|, S)}\]
 *
 * <ul>
 * <li>Herlocker, J., Konstan, J.A., and Riedl, J. <a
 * href="http://dx.doi.org/10.1023/A:1020443909834">An Empirical Analysis of
 * Design Choices in Neighborhood-Based Collaborative Filtering Algorithms</a>.
 * <i>Information Retrieval</i> Vol. 5 Issue 4 (October 2002), pp. 287-310.</li>
 * </ul>
 *
 * @see SigWeightThreshold
 */
@Shareable
@ThreadSafe
public class SignificanceWeightedVectorSimilarity implements VectorSimilarity, Serializable {

    private static final long serialVersionUID = 1L;

    private final int threshold;
    private final VectorSimilarity delegate;

    @Inject
    public SignificanceWeightedVectorSimilarity(@SigWeightThreshold int thresh,
                                                VectorSimilarity sim) {
        threshold = thresh;
        delegate = sim;
    }

    /**
     * Get the underlying similarity (for debuggin purposes).
     * @return The wrapped vector similarity.
     */
    public VectorSimilarity getDelegate() {
        return delegate;
    }

    @Override
    public double similarity(Long2DoubleMap vec1, Long2DoubleMap vec2) {
        double s = delegate.similarity(vec1, vec2);
        int n = LongUtils.intersectSize(vec1.keySet(), vec2.keySet());
        s *= n;
        return s / max(n, threshold);
    }

    @Override
    public boolean isSparse() {
        return delegate.isSparse();
    }

    @Override
    public boolean isSymmetric() {
        return delegate.isSymmetric();
    }

    @Override
    public String toString() {
        return String.format("SigWeight(%s, %d)", delegate, threshold);
    }
}
