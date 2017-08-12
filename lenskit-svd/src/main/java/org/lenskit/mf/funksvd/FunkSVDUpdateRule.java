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
package org.lenskit.mf.funksvd;

import org.grouplens.lenskit.iterative.LearningRate;
import org.grouplens.lenskit.iterative.RegularizationTerm;
import org.grouplens.lenskit.iterative.StoppingCondition;
import org.grouplens.lenskit.iterative.TrainingLoopController;
import org.lenskit.bias.BiasModel;
import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.data.ratings.RatingMatrix;
import org.lenskit.inject.Shareable;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * Configuration for computing FunkSVD updates.
 *
 * @since 1.0
 */
@Shareable
public final class FunkSVDUpdateRule implements Serializable {
    private static final long serialVersionUID = 2L;

    private final double learningRate;
    private final double trainingRegularization;
    private final BiasModel biasModel;
    private final StoppingCondition stoppingCondition;
    @Nullable
    private final PreferenceDomain domain;

    /**
     * Construct a new FunkSVD configuration.
     *
     * @param lrate The learning rate.
     * @param reg   The regularization term.
     * @param stop  The stopping condition
     */
    @Inject
    public FunkSVDUpdateRule(@LearningRate double lrate,
                             @RegularizationTerm double reg,
                             BiasModel bias,
                             @Nullable PreferenceDomain dom,
                             StoppingCondition stop) {
        learningRate = lrate;
        trainingRegularization = reg;
        biasModel = bias;
        domain = dom;
        stoppingCondition = stop;
    }

    /**
     * Create an estimator to use while training the recommender.
     *
     * @return The estimator to use.
     */
    public TrainingEstimator makeEstimator(RatingMatrix snapshot) {
        return new TrainingEstimator(snapshot, biasModel, domain);
    }

    public double getLearningRate() {
        return learningRate;
    }

    public double getTrainingRegularization() {
        return trainingRegularization;
    }

    public StoppingCondition getStoppingCondition() {
        return stoppingCondition;
    }

    @Nullable
    public PreferenceDomain getDomain() {
        return domain;
    }

    public TrainingLoopController getTrainingLoopController() {
        return stoppingCondition.newLoop();
    }

    public FunkSVDUpdater createUpdater() {
        return new FunkSVDUpdater(this);
    }
}
