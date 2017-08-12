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
package org.lenskit;

import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.InjectionException;
import org.grouplens.grapht.graph.DAGNode;
import org.lenskit.api.*;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.inject.StaticInjector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 * Recommender implementation built on LensKit containers.  Recommenders built
 * with {@link LenskitRecommenderEngine} will produce this type of
 * recommender.
 *
 * The {@link Recommender} interface will meet most needs, so most users can
 * ignore this class.  However, if you need to inspect internal components of a
 * recommender (e.g. extract the item-item similarity matrix), this class and its
 * {@link #get(Class)} method can be useful.
 *
 * @compat Public
 */
public class LenskitRecommender implements Recommender {
    private final StaticInjector injector;

    /**
     * Create a new LensKit recommender.  Most code does not need to call this constructor, but
     * rather use {@link #build(LenskitConfiguration)} or a {@link LenskitRecommenderEngine}.
     *
     * @param graph This recommender's configuration graph.
     */
    public LenskitRecommender(DAGNode<Component, Dependency> graph) {
        injector = new StaticInjector(graph);
    }

    /**
     * Get a particular component from the recommender session. Generally you
     * want to use one of the type-specific getters; this method only exists for
     * specialized applications which need deep access to the recommender
     * components.
     *
     * @param <T> The type of component to get.
     * @param cls The component class to get.
     * @return The instance of the specified component.
     */
    @Nullable
    public <T> T get(Class<T> cls) {
        try {
            return injector.tryGetInstance(cls);
        } catch (InjectionException e) {
            throw new RecommenderBuildException("error instantiating component", e);
        }
    }

    /**
     * Get a particular qualified component from the recommender session.  Generally you
     * want to use one of the type-specific getters; this method only exists for
     * specialized applications which need deep access to the recommender
     * components.
     *
     * @param <T> The type of component to get.
     * @param qual The qualifying annotation of the component class.
     * @param cls The component class to get.
     * @return The instance of the specified component.
     */
    @Nullable
    public <T> T get(Class<? extends Annotation> qual, Class<T> cls) {
        try {
            return injector.tryGetInstance(qual, cls);
        } catch (InjectionException e) {
            throw new RecommenderBuildException("error instantiating component", e);
        }
    }

    /**
     * Get a particular qualified component from the recommender session.  Generally you
     * want to use one of the type-specific getters; this method only exists for
     * specialized applications which need deep access to the recommender
     * components.
     *
     * @param <T> The type of component to get.
     * @param qual The qualifying annotation of the component class.
     * @param cls The component class to get.
     * @return The instance of the specified component.
     */
    @Nullable
    public <T> T get(Annotation qual, Class<T> cls) {
        try {
            return injector.getInstance(qual, cls);
        } catch (InjectionException e) {
            throw new RecommenderBuildException("error instantiating component", e);
        }
    }

    @Override
    public ItemScorer getItemScorer() {
        return get(ItemScorer.class);
    }

    @Override
    public RatingPredictor getRatingPredictor() {
        return get(RatingPredictor.class);
    }

    @Override
    public ItemRecommender getItemRecommender() {
        return get(ItemRecommender.class);
    }

    @Nullable
    @Override
    public ItemBasedItemScorer getItemBasedItemScorer() {
        return get(ItemBasedItemScorer.class);
    }

    @Nullable
    @Override
    public ItemBasedItemRecommender getItemBasedItemRecommender() {
        return get(ItemBasedItemRecommender.class);
    }

    /**
     * Get the data access object from this recommender.
     * @return The data access object.
     */
    @Nonnull
    public DataAccessObject getDataAccessObject() {
        DataAccessObject dao =  get(DataAccessObject.class);
        if (dao == null) {
            throw new IllegalStateException("recommender has no DAO");
        }
        return dao;
    }

    @Override
    public void close() {
        injector.close();
    }

    /**
     * Build a recommender from a configuration.  The recommender is immediately usable.  This is
     * mostly useful for evaluations and test programs; more sophisticated applications that need
     * to build multiple recommenders from the same model should use a {@linkplain LenskitRecommenderEngine
     * recommender engine}.
     *
     * @param config The configuration.
     * @return The recommender.
     * @throws RecommenderBuildException If there is an error building the recommender.
     * @since 2.0
     * @deprecated Use {@link #build(LenskitConfiguration, DataAccessObject)}
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public static LenskitRecommender build(LenskitConfiguration config) throws RecommenderBuildException {
        return LenskitRecommenderEngine.build(config).createRecommender();
    }

    /**
     * Build a recommender from a configuration.  The recommender is immediately usable.  This is
     * mostly useful for evaluations and test programs; more sophisticated applications that need
     * to build multiple recommenders from the same model should use a {@linkplain LenskitRecommenderEngine
     * recommender engine}.
     *
     * @param config The configuration.
     * @param dao The data access object.
     * @return The recommender.
     * @throws RecommenderBuildException If there is an error building the recommender.
     * @since 2.0
     */
    public static LenskitRecommender build(LenskitConfiguration config, DataAccessObject dao) throws RecommenderBuildException {
        return LenskitRecommenderEngine.build(config, dao).createRecommender(dao);
    }
}
