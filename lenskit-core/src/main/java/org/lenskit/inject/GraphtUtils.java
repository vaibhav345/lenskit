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

import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import org.grouplens.grapht.CachePolicy;
import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.graph.DAGEdge;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.reflect.*;
import org.grouplens.grapht.reflect.internal.SimpleInjectionPoint;
import org.lenskit.RecommenderConfigurationException;
import org.lenskit.data.dao.DataAccessObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper utilities for Grapht integration.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.11
 */
public final class GraphtUtils {
    private static final Logger logger = LoggerFactory.getLogger(GraphtUtils.class);

    private GraphtUtils() {
    }

    /**
     * Check a graph for placeholder satisfactions.
     *
     * @param graph The graph to check.
     * @throws RecommenderConfigurationException if the graph has a placeholder satisfaction.
     */
    public static void checkForPlaceholders(DAGNode<Component, Dependency> graph, Logger logger) throws RecommenderConfigurationException {
        Set<DAGNode<Component, Dependency>> placeholders = getPlaceholderNodes(graph);
        Satisfaction sat = null;
        for (DAGNode<Component,Dependency> node: placeholders) {
            Component csat = node.getLabel();
            // special-case DAOs for non-checking
            if (DataAccessObject.class.isAssignableFrom(csat.getSatisfaction().getErasedType())) {
                logger.debug("found DAO placeholder {}");
            } else {
                // all other placeholders are bad
                if (sat == null) {
                    sat = csat.getSatisfaction();
                }
                logger.error("placeholder {} not removed", csat.getSatisfaction());
            }
        }
        if (sat != null) {
            throw new RecommenderConfigurationException("placeholder " + sat + " not removed");
        }
    }

    /**
     * Get the placeholder nodes from a graph.
     *
     * @param graph The graph.
     * @return The set of nodes that have placeholder satisfactions.
     */
    public static Set<DAGNode<Component, Dependency>> getPlaceholderNodes(DAGNode<Component,Dependency> graph) {
        return graph.getReachableNodes()
                    .stream()
                    .filter(n -> n.getLabel().getSatisfaction() instanceof PlaceholderSatisfaction)
                    .collect(Collectors.toSet());
    }

    /**
     * Determine if a node is a shareable component.
     *
     *
     * @param node The node.
     * @return {@code true} if the component is shareable.
     */
    public static boolean isShareable(DAGNode<Component, Dependency> node) {
        Component label = node.getLabel();

        if (label.getSatisfaction().hasInstance()) {
            logger.trace("node {} shareable because it has an instance", node);
            return true;
        }

        if (label.getCachePolicy() == CachePolicy.NEW_INSTANCE) {
            logger.trace("node {} not shareable because it has a new-instance cache policy", node);
            return false;
        }

        Class<?> type = label.getSatisfaction().getErasedType();
        logger.trace("node {} has satisfaction type {}", node, type);
        if (type.getAnnotation(Shareable.class) != null) {
            logger.trace("node {} shareable because it has the shareable annotation", node);
            return true;
        }

        if (type.getAnnotation(Singleton.class) != null) {
            logger.trace("node {} shareable because it has the singleton annotation", node);
            return true;
        }

        // finally examine the satisfaction in more detail
        return label.getSatisfaction().visit(new AbstractSatisfactionVisitor<Boolean>() {
            @Override
            public Boolean visitDefault() {
                logger.trace("node {} not shareable by default", node);
                return false;
            }

            @Override
            public Boolean visitProviderClass(Class<? extends Provider<?>> pclass) {
                Method m = null;
                try {
                    m = pclass.getMethod("get");
                } catch (NoSuchMethodException e) {
                /* fine, leave it null */
                }
                if (m != null && m.getAnnotation(Shareable.class) != null) {
                    logger.trace("node {} shareable because it is a provider with a shareable annotation", node);
                    return true;
                }
                logger.trace("node {} not shareable because it is an unshareable provider", node);
                return false;
            }

            @SuppressWarnings("unchecked")
            @Override
            public Boolean visitProviderInstance(Provider<?> provider) {
                // cast to raw type to work around inference issue
                return visitProviderClass((Class) provider.getClass());
            }
        });
    }

    /**
     * Determine whether a desire is transient.
     *
     * @param d The desire to test.
     * @return {@code true} if the desire is transient.
     */
    public static boolean desireIsTransient(@Nonnull Desire d) {
        InjectionPoint ip = d.getInjectionPoint();
        return ip.getAttribute(Transient.class) != null;
    }

    public static boolean edgeIsTransient(DAGEdge<?, Dependency> input) {
        Desire desire = input.getLabel().getInitialDesire();
        return desireIsTransient(desire);
    }

    private static List<String> extractOrderKey(DAGEdge<Component,Dependency> node) {
        Desire desire = node.getLabel().getInitialDesire();
        InjectionPoint ip = desire.getInjectionPoint();
        List<String> key = new ArrayList<>(4);
        Member member = ip.getMember();
        if (member instanceof Constructor) {
            key.add("0: constructor");
            key.add(Integer.toString(ip.getParameterIndex()));
        } else if (member instanceof Method) {
            key.add("1: setter");
            key.add(member.getName());
            key.add(Integer.toString(ip.getParameterIndex()));
        } else if (member instanceof Field) {
            key.add("2: field");
            key.add(member.getName());
        } else if (ip instanceof SimpleInjectionPoint) {
            key.add("5: simple");
        } else {
            key.add("9: unknown");
            key.add(ip.getClass().getName());
        }
        return key;
    }

    /**
     * An ordering over dependency edges.
     */
    public static final Ordering<DAGEdge<Component, Dependency>> DEP_EDGE_ORDER =
            Ordering.<String>natural()
                    .lexicographical()
                    .onResultOf(GraphtUtils::extractOrderKey);

    /**
     * Find the set of shareable nodes (objects that will be replaced with instance satisfactions in
     * the final graph).
     *
     * @param graph The graph to analyze.
     * @return The set of root nodes - nodes that need to be instantiated and removed. These nodes
     *         are in topologically sorted order.
     */
    public static LinkedHashSet<DAGNode<Component, Dependency>> getShareableNodes(DAGNode<Component, Dependency> graph) {
        LinkedHashSet<DAGNode<Component, Dependency>> shared = Sets.newLinkedHashSet();

        graph.getSortedNodes()
             .stream()
             .filter(GraphtUtils::isShareable)
             .forEach(node -> {
                 // see if we depend on any non-shared nodes
                 // since nodes are sorted, all shared nodes will have been seen
                 boolean isShared = true;
                 for (DAGEdge<Component,Dependency> edge: node.getOutgoingEdges()) {
                     if (!edgeIsTransient(edge)) {
                         boolean es = shared.contains(edge.getTail());
                         isShared &= es;
                         if (!es) {
                             logger.debug("node {} not shared due to non-transient dependency on {}", node, edge.getTail());
                         }
                     }
                 }
                 if (isShared) {
                     shared.add(node);
                 }
             });

        return shared;
    }

    /**
     * Find a node with a satisfaction for a specified type. Does a breadth-first
     * search to find the closest matching one.
     *
     * @param type The type to look for.
     * @return A node whose satisfaction is compatible with {@code type}.
     */
    @Nullable
    public static DAGNode<Component,Dependency> findSatisfyingNode(DAGNode<Component,Dependency> graph,
                                                                   final QualifierMatcher qmatch,
                                                                   final Class<?> type) {
        Optional<DAGEdge<Component, Dependency>> edge =
                graph.breadthFirstEdges()
                     .filter(e -> type.isAssignableFrom(e.getTail()
                                                         .getLabel()
                                                         .getSatisfaction()
                                                         .getErasedType()))
                     .filter(e -> qmatch.apply(e.getLabel()
                                                .getInitialDesire()
                                                .getInjectionPoint()
                                                .getQualifier()))
                .findFirst();

        return edge.map(DAGEdge::getTail)
                   .orElse(null);
    }
}
