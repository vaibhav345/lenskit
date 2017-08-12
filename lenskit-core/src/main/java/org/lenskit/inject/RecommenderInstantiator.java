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

import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.InjectionException;
import org.grouplens.grapht.LifecycleManager;
import org.grouplens.grapht.graph.DAGNode;
import org.lenskit.LenskitConfiguration;
import org.lenskit.RecommenderConfigurationException;
import org.lenskit.api.RecommenderBuildException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Process a recommender graph to deal with its shareable nodes.
 *
 * @since 1.2
 * @compat Experimental
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class RecommenderInstantiator {
    private static final Logger logger = LoggerFactory.getLogger(RecommenderInstantiator.class);
    private final DAGNode<Component, Dependency> graph;

    public static RecommenderInstantiator create(DAGNode<Component,Dependency> g) {
        return new RecommenderInstantiator(g);
    }

    @Deprecated
    public static RecommenderInstantiator forConfig(LenskitConfiguration config) throws RecommenderConfigurationException {
        return create(config.buildGraph());
    }

    private RecommenderInstantiator(DAGNode<Component, Dependency> g) {
        graph = g;
    }

    /**
     * Get the graph from this instantiator.
     *
     * @return The graph.  This method returns a defensive copy, so modifying it will not modify the
     *         graph underlying this instantiator.
     */
    public DAGNode<Component, Dependency> getGraph() {
        return graph;
    }

    /**
     * Instantiate the recommender graph.  This requires the graph to have been resolved with a real
     * DAO instance, not just a class, if anything references the DAO.  Use {@link
     * LenskitConfiguration#buildGraph()} to get such a graph.  The result of instantiating a graph
     * is that all shareable nodes will be instantiated.
     *
     * @return A new recommender graph with all shareable nodes pre-instantiated.
     * @throws RecommenderBuildException If there is an error instantiating the graph.
     */
    public DAGNode<Component,Dependency> instantiate() throws RecommenderBuildException {
        try (LifecycleManager lm = new LifecycleManager()) {
            NodeInstantiator instantiator = NodeInstantiator.create(lm);
            // TODO Verify that no sharable components are lifecycle-managed
            return replaceShareableNodes(NodeProcessors.instantiate(instantiator));
        } catch (InjectionException e) {
            throw new RecommenderBuildException("Recommender instantiation failed", e);
        }
    }

    /**
     * Simulate instantiating a graph.
     * @return The simulated graph.
     */
    public DAGNode<Component,Dependency> simulate() throws RecommenderBuildException {
        try {
            return replaceShareableNodes(NodeProcessors.simulateInstantiation());
        } catch (InjectionException e) {
            throw new RecommenderBuildException("Simulated instantiation failed", e);
        }
    }

    /**
     * Replace shareable nodes in a graph.
     *
     * @param replace   A replacement function. For each shareable node, this function will
     *                  be called; if it returns a node different from its input node, the
     *                  new node will be used as a replacement for the old.
     */
    private DAGNode<Component,Dependency> replaceShareableNodes(NodeProcessor replace) throws InjectionException {
        logger.debug("replacing nodes in graph with {} nodes", graph.getReachableNodes().size());
        Set<DAGNode<Component,Dependency>> toReplace = GraphtUtils.getShareableNodes(graph);
        logger.debug("found {} shared nodes", toReplace.size());

        DAGNode<Component, Dependency> result =
                NodeProcessors.processNodes(graph, toReplace, replace);
        logger.debug("final graph has {} nodes", result.getReachableNodes().size());
        return result;
    }
}
