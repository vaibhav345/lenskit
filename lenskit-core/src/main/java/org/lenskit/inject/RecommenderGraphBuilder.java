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

import com.google.common.collect.*;
import org.grouplens.grapht.*;
import org.grouplens.grapht.context.ContextMatcher;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.reflect.Desires;
import org.grouplens.grapht.solver.*;
import org.lenskit.LenskitConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Build recommender graphs.  You probably don't want to use this class.
 *
 * @compat private
 * @since 2.1
 */
public class RecommenderGraphBuilder {
    private static final int RESOLVE_DEPTH_LIMIT = 100;

    private ClassLoader classLoader;
    private List<BindingFunctionBuilder> configs = Lists.newArrayList();
    private Set<Class<?>> roots = Sets.newHashSet();

    public void setClassLoader(ClassLoader loader) {
        classLoader = loader;
    }

    /**
     * Add bindings to the graph builder.  Bindings added to this builder are processed in the
     * <em>reverse</em> order as {@link DependencySolverBuilder} - add the most important bindings
     * last.
     *
     * @param bld A builder of the bindings to add.
     * @return The graph builder.
     */
    public RecommenderGraphBuilder addBindings(BindingFunctionBuilder bld) {
        configs.add(bld);
        return this;
    }

    /**
     * Add roots to the graph builder.
     * @param classes Root types for the graph.
     * @return The graph builder (for chaining).
     */
    public RecommenderGraphBuilder addRoots(Iterable<Class<?>> classes) {
        Iterables.addAll(roots, classes);
        return this;
    }

    /**
     * Add a recommender configuration.
     * @param config The configuration.
     * @return The graph builder (for chaining).
     */
    public RecommenderGraphBuilder addConfiguration(LenskitConfiguration config) {
        addBindings(config.getBindings());
        addRoots(config.getRoots());
        return this;
    }

    /**
     * Build a dependency solver from the provided bindings.
     *
     * @return The dependency solver.
     */
    public DependencySolver buildDependencySolver() {
        return buildDependencySolverImpl(SolveDirection.SOLVE);
    }

    /**
     * Build a dependency 'unsolver' from the provided bindings. The resulting solver, when rewriting
     * a graph, will replace bound targets with placeholders.
     * @return The dependency solver.
     */
    public DependencySolver buildDependencyUnsolver() {
        return buildDependencySolverImpl(SolveDirection.UNSOLVE);
    }

    public DependencySolver buildDependencySolverImpl(SolveDirection direction) {
        DependencySolverBuilder dsb = DependencySolver.newBuilder();
        for (BindingFunctionBuilder cfg: Lists.reverse(configs)) {
            dsb.addBindingFunction(direction.transform(cfg.build(BindingFunctionBuilder.RuleSet.EXPLICIT)));
            dsb.addBindingFunction(direction.transform(cfg.build(BindingFunctionBuilder.RuleSet.INTERMEDIATE_TYPES)));
            dsb.addBindingFunction(direction.transform(cfg.build(BindingFunctionBuilder.RuleSet.SUPER_TYPES)));
        }
        // default desire function cannot trigger rewrites
        dsb.addBindingFunction(DefaultDesireBindingFunction.create(classLoader), false);
        dsb.setDefaultPolicy(CachePolicy.MEMOIZE);
        dsb.setMaxDepth(RESOLVE_DEPTH_LIMIT);
        return dsb.build();
    }

    public DAGNode<Component,Dependency> buildGraph() throws ResolutionException {
        DependencySolver solver = buildDependencySolver();
        for (Class<?> root: roots) {
            solver.resolve(Desires.create(null, root, true));
        }

        return solver.getGraph();
    }

    private static enum SolveDirection {
        SOLVE {
            @Override
            public BindingFunction transform(BindingFunction bindFunction) {
                return bindFunction;
            }
        },
        UNSOLVE {
            @Override
            public BindingFunction transform(BindingFunction bindFunction) {
                if (bindFunction instanceof RuleBasedBindingFunction) {
                    RuleBasedBindingFunction rbf = (RuleBasedBindingFunction) bindFunction;
                    ListMultimap<ContextMatcher, BindRule> bindings = rbf.getRules();
                    ListMultimap<ContextMatcher, BindRule> newBindings = ArrayListMultimap.create();
                    for (Map.Entry<ContextMatcher, BindRule> entry: bindings.entries()) {
                        BindRule rule = entry.getValue();
                        BindRuleBuilder builder = rule.newCopyBuilder();
                        Class<?> type = builder.getDependencyType();
                        newBindings.put(entry.getKey(),
                                        builder.setSatisfaction(new PlaceholderSatisfaction(type))
                                               .build());
                    }
                    return new RuleBasedBindingFunction(newBindings);
                } else {
                    throw new IllegalArgumentException("cannot transform bind function " + bindFunction);
                }
            }
        };

        public abstract BindingFunction transform(BindingFunction bindFunction);
    }
}
