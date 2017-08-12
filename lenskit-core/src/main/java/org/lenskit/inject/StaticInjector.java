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
package org.lenskit.inject;

import org.grouplens.grapht.*;
import org.grouplens.grapht.graph.DAGEdge;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.Desires;
import org.grouplens.grapht.reflect.QualifierMatcher;
import org.grouplens.grapht.reflect.Qualifiers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 * A Grapht injector that uses a precomputed graph.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class StaticInjector implements Injector {
    private static final Logger logger = LoggerFactory.getLogger(StaticInjector.class);

    private final LifecycleManager lifecycle;
    private RuntimeException capture;
    private boolean closed = false;
    private final NodeInstantiator instantiator;
    private DAGNode<Component, Dependency> graph;

    /**
     * Create a new static injector.
     *
     * @param g   The object graph.
     */
    public StaticInjector(DAGNode<Component,Dependency> g) {
        graph = g;
        lifecycle = new LifecycleManager();
        instantiator = NodeInstantiator.create(lifecycle);
        capture = new RuntimeException("Static injector instantiated (backtrace shows instantiation point)");
    }

    @Override
    public <T> T getInstance(Class<T> type) throws InjectionException {
        T obj = tryGetInstance(Qualifiers.matchDefault(), type);
        if (obj == null) {
            throw new ResolutionException("no resolution available for " + type);
        } else {
            return obj;
        }
    }

    public <T> T tryGetInstance(Class<? extends Annotation> qual, Class<T> type) throws InjectionException {
        return tryGetInstance(Qualifiers.match(qual), type);
    }

    public <T> T tryGetInstance(QualifierMatcher qmatch, Class<T> type) throws InjectionException {
        DAGNode<Component, Dependency> node = GraphtUtils.findSatisfyingNode(graph, qmatch, type);
        return node != null ? type.cast(instantiator.instantiate(node)) : null;
    }

    @Nullable
    public <T> T tryGetInstance(Class<T> type) throws InjectionException {
        Desire d = Desires.create(null, type, true);
        DAGEdge<Component, Dependency> e =
                graph.getOutgoingEdgeWithLabel(l -> l.hasInitialDesire(d));

        if (e != null) {
            return type.cast(instantiator.instantiate(e.getTail()));
        } else {
            DAGNode<Component, Dependency> node = GraphtUtils.findSatisfyingNode(graph, Qualifiers.matchDefault(), type);
            if (node != null) {
                return type.cast(instantiator.instantiate(node));
            } else {
                return null;
            }
        }
    }

    @Nullable
    @Override
    public <T> T tryGetInstance(Annotation qualifier, Class<T> type) throws InjectionException {
        return tryGetInstance(Qualifiers.match(qualifier), type);
    }

    @Override
    public <T> T getInstance(Annotation qualifier, Class<T> type) throws InjectionException {
        T obj = tryGetInstance(Qualifiers.match(qualifier), type);
        if (obj == null) {
            throw new ResolutionException("no resolution available for " + type + " with qualifier " + qualifier);
        } else {
            return obj;
        }
    }

    @Override
    public void close() {
        lifecycle.close();
        closed = true;
    }

    @Override
    protected void finalize() throws Throwable {
        if (!closed) {
            logger.warn("Injector " + this + " was never closed", capture);
        }
        super.finalize();
    }
}
