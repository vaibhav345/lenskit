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
package org.lenskit.cli.util;

import com.google.common.base.Stopwatch;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.lenskit.util.io.CompressionMode;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.LenskitRecommenderEngineBuilder;
import org.lenskit.LenskitRecommenderEngineLoader;
import org.lenskit.api.RecommenderBuildException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Load recommenders from models or configurations.
 */
public class RecommenderLoader {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final InputData input;
    private final ScriptEnvironment environment;
    private final Namespace options;

    public RecommenderLoader(InputData in, ScriptEnvironment env, Namespace opts) {
        input = in;
        environment = env;
        options = opts;
    }

    public static void configureArguments(ArgumentParser parser) {
        parser.addArgument("-c", "--config-file")
              .type(File.class)
              .action(Arguments.append())
              .metavar("FILE")
              .help("use configuration from FILE");
        parser.addArgument("-m", "--model-file")
              .type(File.class)
              .metavar("FILE")
              .help("load model from FILE");
    }

    public List<File> getConfigFiles() {
        return options.getList("config_file");
    }

    public LenskitRecommenderEngine loadEngine() throws RecommenderBuildException, IOException {
        File modelFile = options.get("model_file");
        if (modelFile == null) {
            logger.info("creating fresh recommender");
            LenskitRecommenderEngineBuilder builder = LenskitRecommenderEngine.newBuilder();
            for (LenskitConfiguration config: environment.loadConfigurations(getConfigFiles())) {
                builder.addConfiguration(config);
            }
            Stopwatch timer = Stopwatch.createStarted();
            LenskitRecommenderEngine engine = builder.build(input.getDAO());
            timer.stop();
            logger.info("built recommender in {}", timer);
            return engine;
        } else {
            logger.info("loading recommender from {}", modelFile);
            LenskitRecommenderEngineLoader loader = LenskitRecommenderEngine.newLoader();
            loader.setClassLoader(environment.getClassLoader());
            for (LenskitConfiguration config: environment.loadConfigurations(getConfigFiles())) {
                loader.addConfiguration(config);
            }
            Stopwatch timer = Stopwatch.createStarted();
            LenskitRecommenderEngine engine;
            InputStream input = new FileInputStream(modelFile);
            try {
                input = CompressionMode.autodetect(modelFile).wrapInput(input);
                engine = loader.load(input);
            } finally {
                input.close();
            }
            timer.stop();
            logger.info("loaded recommender in {}", timer);
            return engine;
        }
    }
}
