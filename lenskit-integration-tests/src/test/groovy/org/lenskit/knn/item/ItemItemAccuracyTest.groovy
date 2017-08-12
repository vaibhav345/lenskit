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
package org.lenskit.knn.item

import org.grouplens.lenskit.test.CrossfoldTestSuite
import org.lenskit.transform.normalize.BiasUserVectorNormalizer
import org.lenskit.transform.normalize.UserVectorNormalizer
import org.lenskit.similarity.SimilarityDamping
import org.lenskit.LenskitConfiguration
import org.lenskit.api.ItemScorer
import org.lenskit.baseline.BaselineScorer
import org.lenskit.bias.BiasItemScorer
import org.lenskit.bias.BiasModel
import org.lenskit.bias.UserItemBiasModel
import org.lenskit.config.ConfigHelpers
import org.lenskit.eval.traintest.SimpleEvaluator
import org.lenskit.eval.traintest.recommend.RecommendEvalTask
import org.lenskit.eval.traintest.recommend.TopNMAPMetric
import org.lenskit.knn.NeighborhoodSize
import org.lenskit.util.table.Table

import static org.hamcrest.Matchers.closeTo
import static org.junit.Assert.assertThat

/**
 * Do major tests on the item-item recommender.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ItemItemAccuracyTest extends CrossfoldTestSuite {
    @Override
    void addExtraConfig(SimpleEvaluator eval) {
        def rec = new RecommendEvalTask()
        rec.listSize = 50
        rec.addMetric(new TopNMAPMetric())
        eval.experiment.addTask(rec)
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void configureAlgorithm(LenskitConfiguration config) {
        ConfigHelpers.configure(config) {
            bind ItemScorer to ItemItemScorer
            bind (BaselineScorer, ItemScorer) to BiasItemScorer
            bind BiasModel to UserItemBiasModel
            bind UserVectorNormalizer to BiasUserVectorNormalizer
            within (ItemSimilarity) {
                set SimilarityDamping to 100.0
            }
            set NeighborhoodSize to 30
        }
    }

    @Override
    protected void checkResults(Table table) {
        assertThat(table.column("MAE.ByRating").average(),
                   closeTo(0.70d, 0.025d))
        assertThat(table.column("RMSE.ByUser").average(),
                   closeTo(0.90d, 0.05d))
    }
}
