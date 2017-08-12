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
package org.lenskit.mf.funksvd

import org.grouplens.lenskit.iterative.IterationCount
import org.grouplens.lenskit.test.ML100KTestSuite
import org.junit.Test
import org.lenskit.LenskitRecommender
import org.lenskit.LenskitRecommenderEngine
import org.lenskit.ModelDisposition
import org.lenskit.api.ItemScorer
import org.lenskit.api.RecommenderBuildException
import org.lenskit.baseline.BaselineScorer
import org.lenskit.baseline.MeanDamping
import org.lenskit.bias.BiasModel
import org.lenskit.bias.UserItemBiasModel
import org.lenskit.config.ConfigHelpers

import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.notNullValue
import static org.junit.Assert.assertThat

/**
 * Do major infrastructure tests on the FunkSVD recommender.
 */
public class FunkSVDBuildSerializeTest extends ML100KTestSuite {
    def config = ConfigHelpers.load {
        bind ItemScorer to FunkSVDItemScorer
        bind BiasModel to UserItemBiasModel
        set FeatureCount to 10
        set IterationCount to 10
        within (BaselineScorer, ItemScorer) {
            set MeanDamping to 25
        }
    }


    @Test
    void testBuildAndSerializeModel() throws RecommenderBuildException, IOException {
        LenskitRecommenderEngine engine =
            LenskitRecommenderEngine.newBuilder()
                                    .addConfiguration(config)
                                    .addConfiguration(daoConfig, ModelDisposition.EXCLUDED)
                                    .build()
        assertThat(engine, notNullValue())

        ByteArrayOutputStream out = new ByteArrayOutputStream()
        engine.write(out)
        byte[] bytes = out.toByteArray()

        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        LenskitRecommenderEngine loaded =
            LenskitRecommenderEngine.newLoader()
                                    .addConfiguration(daoConfig)
                                    .load(input)

        assertThat(loaded, notNullValue())

        LenskitRecommender rec = loaded.createRecommender()
        try {
            assertThat(rec.getItemScorer(),
                       instanceOf(FunkSVDItemScorer))
            assertThat(rec.get(FunkSVDModel),
                       notNullValue())
        } finally {
            rec.close()
        }
    }
}
