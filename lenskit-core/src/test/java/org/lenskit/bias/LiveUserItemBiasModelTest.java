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
package org.lenskit.bias;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommender;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.data.dao.EntityCollectionDAO;
import org.lenskit.data.entities.EntityFactory;
import org.lenskit.data.ratings.Rating;

import java.util.List;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.*;

/**
 * Created by MichaelEkstrand on 10/1/2016.
 */
public class LiveUserItemBiasModelTest {
    @Test
    public void testComputeAllMeans() {
        EntityFactory efac = new EntityFactory();
        List<Rating> ratings = Lists.newArrayList(efac.rating(100, 200, 3.0),
                                                  efac.rating(101, 200, 4.0),
                                                  efac.rating(102, 201, 2.5),
                                                  efac.rating(102, 203, 4.5),
                                                  efac.rating(101, 203, 3.5));
        LenskitConfiguration config = new LenskitConfiguration();
        config.addRoot(BiasModel.class);
        config.bind(BiasModel.class).to(LiveUserItemBiasModel.class);

        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config, EntityCollectionDAO.create(ratings));

        ratings.add(efac.rating(105, 200, 4.5));
        ratings.add(efac.rating(105, 203, 4.8));

        LenskitRecommender rec = engine.createRecommender(EntityCollectionDAO.create(ratings));
        BiasModel model = rec.get(BiasModel.class);

        assertThat(model.getIntercept(), closeTo(3.5, 1.0e-3));
        assertThat(model.getItemBias(200), closeTo(0.0, 1.0e-3));
        assertThat(model.getItemBias(201), closeTo(-1.0, 1.0e-3));
        assertThat(model.getItemBias(203), closeTo(0.5, 1.0e-3));
        assertThat(model.getUserBias(100), closeTo(-0.5, 1.0e-3));
        assertThat(model.getUserBias(101), closeTo(0, 1.0e-3));
        assertThat(model.getUserBias(102), closeTo(0.25, 1.0e-3));

        assertThat(model.getUserBias(105), closeTo(0.9, 1.0e-3));
    }
}