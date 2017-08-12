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

import com.google.common.collect.ImmutableSet;
import org.grouplens.grapht.BindingFunctionBuilder;
import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.ResolutionException;
import org.grouplens.grapht.context.ContextPattern;
import org.grouplens.grapht.graph.DAGNode;
import org.lenskit.api.*;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.inject.AbstractConfigContext;
import org.lenskit.inject.RecommenderGraphBuilder;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A LensKit algorithm configuration.  Once you have a configuration, you can pass it to
 * {@link LenskitRecommenderEngine#build(LenskitConfiguration)}
 * to build a recommender engine, or {@link LenskitRecommender#build(LenskitConfiguration)}
 * to skip the engine and just build a recommender.
 *
 * @since 1.2
 * @compat Public
 */
public class LenskitConfiguration extends AbstractConfigContext {
    private static final Class<?>[] INITIAL_ROOTS = {
            RatingPredictor.class,
            ItemScorer.class,
            ItemRecommender.class,
            ItemBasedItemScorer.class,
            ItemBasedItemRecommender.class,
            DataAccessObject.class
    };

    private final BindingFunctionBuilder bindings;
    private final Set<Class<?>> roots;

    /**
     * Create a new LensKit configuration.
     */
    public LenskitConfiguration() {
        bindings = new BindingFunctionBuilder(true);
        roots = new HashSet<>();
        Collections.addAll(roots, INITIAL_ROOTS);
    }

    /**
     * Create a new copy of a LensKit configuration.
     * @param other The configuration to copy.
     */
    public LenskitConfiguration(LenskitConfiguration other) {
        bindings = other.bindings.clone();
        roots = new HashSet<>(other.roots);
    }

    /**
     * Convenience method to copy a LensKit configuration.
     * @return An independent copy of this configuration.
     */
    public LenskitConfiguration copy() {
        return new LenskitConfiguration(this);
    }

    /**
     * Add the specified component type as a root component. This forces it (and its
     * dependencies) to be resolved, and makes it available from the resulting
     * recommenders.
     *
     * @param componentType The type of component to add as a root (typically an interface).
     * @see LenskitRecommender#get(Class)
     */
    public void addRoot(Class<?> componentType) {
        roots.add(componentType);
    }

    /**
     * Clear the set of roots, removing all configured <em>and default</em> roots.  This is almost
     * never desired in production, but is useful for testing.
     */
    public void clearRoots() {
        roots.clear();
    }

    @Override
    public <T> LenskitBinding<T> bind(Class<T> type) {
        return wrapContext(bindings.getRootContext()).bind(type);
    }

    @Override
    public LenskitConfigContext within(Class<?> type) {
        return wrapContext(bindings.getRootContext().within(type));
    }

    @Override
    public LenskitConfigContext within(Class<? extends Annotation> qualifier, Class<?> type) {
        return wrapContext(bindings.getRootContext().within(qualifier, type));
    }

    @Override
    public LenskitConfigContext within(Annotation qualifier, Class<?> type) {
        return wrapContext(bindings.getRootContext().within(qualifier, type));
    }

    @Override
    public LenskitConfigContext matching(ContextPattern pattern) {
        return wrapContext(bindings.getRootContext().matching(pattern));
    }

    @Override
    public LenskitConfigContext at(Class<?> type) {
        return wrapContext(bindings.getRootContext().at(type));
    }

    @Override
    public LenskitConfigContext at(Class<? extends Annotation> qualifier, Class<?> type) {
        return wrapContext(bindings.getRootContext().at(qualifier, type));
    }

    @Override
    public LenskitConfigContext at(Annotation qualifier, Class<?> type) {
        return wrapContext(bindings.getRootContext().at(qualifier, type));
    }

    public BindingFunctionBuilder getBindings() {
        return bindings;
    }

    public Set<Class<?>> getRoots() {
        return ImmutableSet.copyOf(roots);
    }

    /**
     * Get a mockup of the full recommender graph. This fully resolves the graph so that
     * it can be analyzed, but does not create any objects.
     *
     * @return The full graph.
     * @deprecated This shouldn't be used anymore.
     */
    @Deprecated
    public DAGNode<Component,Dependency> buildGraph() throws RecommenderConfigurationException {
        RecommenderGraphBuilder rgb = new RecommenderGraphBuilder();
        rgb.addBindings(bindings);
        rgb.addRoots(roots);
        try {
            return rgb.buildGraph();
        } catch (ResolutionException e) {
            throw new RecommenderConfigurationException("Cannot resolve configuration graph", e);
        }
    }
}
