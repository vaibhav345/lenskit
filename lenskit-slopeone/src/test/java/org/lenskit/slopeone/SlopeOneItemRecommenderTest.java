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
package org.lenskit.slopeone;

import org.junit.Before;
import org.junit.Test;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommender;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.RatingPredictor;
import org.lenskit.api.Recommender;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.baseline.BaselineScorer;
import org.lenskit.baseline.ItemMeanRatingItemScorer;
import org.lenskit.baseline.UserMeanBaseline;
import org.lenskit.baseline.UserMeanItemScorer;
import org.lenskit.basic.SimpleRatingPredictor;
import org.lenskit.basic.TopNItemRecommender;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.data.ratings.Rating;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class SlopeOneItemRecommenderTest {
    private DataAccessObject dao;
    private LenskitRecommenderEngine engine;

    @SuppressWarnings("deprecation")
    @Before
    public void setup() throws RecommenderBuildException {
        List<Rating> rs = new ArrayList<>();
        rs.add(Rating.create(1, 5, 2));
        rs.add(Rating.create(1, 7, 4));
        rs.add(Rating.create(8, 4, 5));
        rs.add(Rating.create(8, 5, 4));

        StaticDataSource source = StaticDataSource.fromList(rs);
        dao = source.get();

        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(ItemScorer.class).to(SlopeOneItemScorer.class);
        config.bind(PreferenceDomain.class).to(new PreferenceDomain(1, 5));
        // factory.setComponent(UserVectorNormalizer.class, IdentityVectorNormalizer.class);
        config.bind(BaselineScorer.class, ItemScorer.class)
              .to(UserMeanItemScorer.class);
        config.bind(UserMeanBaseline.class, ItemScorer.class)
              .to(ItemMeanRatingItemScorer.class);
        engine = LenskitRecommenderEngine.build(config, dao);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testSlopeOneRecommenderEngineCreate() {
        try (Recommender rec = engine.createRecommender(dao)) {

            assertThat(rec.getItemScorer(),
                       instanceOf(SlopeOneItemScorer.class));
            RatingPredictor rp = rec.getRatingPredictor();
            assertThat(rp, notNullValue());
            assertThat(rp, instanceOf(SimpleRatingPredictor.class));
            assertThat(((SimpleRatingPredictor) rp).getItemScorer(),
                       sameInstance(rec.getItemScorer()));
            assertThat(rec.getItemRecommender(),
                       instanceOf(TopNItemRecommender.class));
        }
    }

    @Test
    public void testConfigSeparation() {
        try (LenskitRecommender rec1 = engine.createRecommender(dao);
             LenskitRecommender rec2 = engine.createRecommender(dao)) {

            assertThat(rec1.getItemScorer(),
                       not(sameInstance(rec2.getItemScorer())));
            assertThat(rec1.get(SlopeOneModel.class),
                       allOf(not(nullValue()),
                             sameInstance(rec2.get(SlopeOneModel.class))));
        }
    }
}
