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
package org.lenskit.knn.user;

import org.junit.Before;
import org.junit.Test;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.*;
import org.lenskit.basic.SimpleRatingPredictor;
import org.lenskit.basic.TopNItemRecommender;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.ratings.Rating;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class UserUserRecommenderBuildTest {

    private static RecommenderEngine engine;

    @SuppressWarnings("deprecation")
    @Before
    public void setup() throws RecommenderBuildException {
        List<Rating> rs = new ArrayList<>();
        rs.add(Rating.create(1, 5, 2));
        rs.add(Rating.create(1, 7, 4));
        rs.add(Rating.create(8, 4, 5));
        rs.add(Rating.create(8, 5, 4));

        StaticDataSource source = StaticDataSource.fromList(rs);
        DataAccessObject dao = source.get();

        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(DataAccessObject.class).to(dao);
        config.bind(ItemScorer.class).to(UserUserItemScorer.class);
        config.bind(NeighborFinder.class).to(LiveNeighborFinder.class);

        engine = LenskitRecommenderEngine.build(config);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testUserUserRecommenderEngineCreate() {
        try (Recommender rec = engine.createRecommender()) {

            assertThat(rec.getItemScorer(),
                       instanceOf(UserUserItemScorer.class));
            assertThat(rec.getItemRecommender(),
                       instanceOf(TopNItemRecommender.class));
            RatingPredictor pred = rec.getRatingPredictor();
            assertThat(pred, notNullValue());
            assertThat(pred, instanceOf(SimpleRatingPredictor.class));
            assertThat(((SimpleRatingPredictor) pred).getItemScorer(),
                       sameInstance(rec.getItemScorer()));
        }
    }

    @Test
    public void testSnapshot() throws RecommenderBuildException {
        List<Rating> rs = new ArrayList<>();
        rs.add(Rating.create(1, 5, 2));
        rs.add(Rating.create(1, 7, 4));
        rs.add(Rating.create(8, 4, 5));
        rs.add(Rating.create(8, 5, 4));

        StaticDataSource source = StaticDataSource.fromList(rs);
        DataAccessObject dao = source.get();

        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(ItemScorer.class).to(UserUserItemScorer.class);
        config.bind(NeighborFinder.class).to(SnapshotNeighborFinder.class);

        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config, dao);
        try (Recommender rec = engine.createRecommender(dao)) {
            assertThat(rec.getItemScorer(),
                       instanceOf(UserUserItemScorer.class));
            assertThat(rec.getItemRecommender(),
                       instanceOf(TopNItemRecommender.class));
            RatingPredictor pred = rec.getRatingPredictor();
            assertThat(pred, instanceOf(SimpleRatingPredictor.class));
            try (Recommender rec2 = engine.createRecommender(dao)) {
                assertThat(rec2.getItemScorer(), not(sameInstance(rec.getItemScorer())));
            }
        }
    }
}
