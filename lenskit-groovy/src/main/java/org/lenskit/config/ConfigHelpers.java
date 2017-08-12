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
package org.lenskit.config;

import com.google.common.base.Preconditions;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.lang.GroovyRuntimeException;
import org.lenskit.LenskitConfiguration;
import org.lenskit.RecommenderConfigurationException;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * LensKit configuration helper utilities.
 *
 * @since 1.2
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ConfigHelpers {
    private ConfigHelpers() {
    }

    /**
     * Load a LensKit configuration from a Groovy closure.  This is useful for using the Groovy
     * DSL in unit tests.
     *
     * @param block The block to evaluate.  This block will be evaluated with a delegate providing
     *              the LensKit DSL and the {@link groovy.lang.Closure#DELEGATE_FIRST} resolution strategy.
     * @return The LensKit configuration.
     * @see ConfigurationLoader#load(groovy.lang.Closure)
     */
    public static LenskitConfiguration load(@DelegatesTo(LenskitConfigDSL.class) Closure<?> block) throws RecommenderConfigurationException {
        Preconditions.checkNotNull(block, "Configuration block");
        LenskitConfiguration config = new LenskitConfiguration();
        configure(config, block);
        return config;
    }

    /**
     * Load a LensKit configuration from a script (as a string).
     *
     * @param script The script source text to evaluate.
     * @return The LensKit configuration.
     * @deprecated Loading from Groovy source strings is confusing.
     */
    @Deprecated
    public static LenskitConfiguration load(String script) throws RecommenderConfigurationException {
        return new ConfigurationLoader().load(script);
    }

    /**
     * Load a LensKit configuration from a script file.
     *
     * @param script The script source file to evaluate.
     * @return The LensKit configuration.
     */
    public static LenskitConfiguration load(File script) throws IOException, RecommenderConfigurationException {
        return new ConfigurationLoader().load(script);
    }

    /**
     * Load a LensKit configuration from a script URL.
     *
     * @param script The script source URL to evaluate.
     * @return The LensKit configuration.
     */
    public static LenskitConfiguration load(URL script) throws IOException, RecommenderConfigurationException {
        return new ConfigurationLoader().load(script);
    }

    /**
     * Modify a configuration from a closure. The class loader is not really consulted in this case.
     * @param block The block to evaluate. This block will be evaluated with a delegate providing
     *              the LensKit DSL and the {@link Closure#DELEGATE_FIRST} resolution strategy.
     */
    public static void configure(LenskitConfiguration config,
                                 @Nonnull @DelegatesTo(LenskitConfigDSL.class) Closure<?> block) throws RecommenderConfigurationException {
        Preconditions.checkNotNull(block, "Configuration block");
        BindingDSL delegate = LenskitConfigDSL.forConfig(config);
        try {
            GroovyUtils.callWithDelegate(block, delegate);
        } catch (GroovyRuntimeException e) {
            // this quite possibly wraps an exception we want to throw
            if (e.getClass().equals(GroovyRuntimeException.class) && e.getCause() != null) {
                throw new RecommenderConfigurationException("Error evaluating Groovy block",
                                                            e.getCause());
            } else {
                throw new RecommenderConfigurationException("Error evaluating Groovy block", e);
            }
        } catch (RuntimeException e) {
            throw new RecommenderConfigurationException("Error evaluating Groovy block", e);
        }
    }
}
