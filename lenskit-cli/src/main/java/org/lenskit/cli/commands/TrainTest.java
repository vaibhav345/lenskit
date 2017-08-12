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
package org.lenskit.cli.commands;

import com.google.auto.service.AutoService;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.grouplens.grapht.util.ClassLoaderContext;
import org.grouplens.grapht.util.ClassLoaders;
import org.lenskit.cli.Command;
import org.lenskit.cli.LenskitCommandException;
import org.lenskit.cli.util.ScriptEnvironment;
import org.lenskit.eval.traintest.EvaluationException;
import org.lenskit.eval.traintest.TrainTestExperiment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

@AutoService(Command.class)
public class TrainTest implements Command {
    private final Logger logger = LoggerFactory.getLogger(TrainTest.class);

    @Override
    public String getName() {
        return "train-test";
    }

    @Override
    public String getHelp() {
        return "Run a train-test evaluation of algorithms over data sets.";
    }

    @Override
    public void configureArguments(ArgumentParser parser) {
        ScriptEnvironment.configureArguments(parser);
        parser.addArgument("config_file")
              .metavar("CONFIG")
              .help("Load train-test configuration from CONFIG")
              .type(File.class)
              .required(true);
    }

    private File getSpecFile(Namespace options) {
        return options.get("config_file");
    }

    @Override
    public void execute(Namespace options) throws LenskitCommandException {
        ScriptEnvironment env = new ScriptEnvironment(options);
        File specFile = getSpecFile(options);
        logger.info("loading train-test configuration from {}", specFile);

        ClassLoader cl = env.getClassLoader();
        ClassLoaderContext ctx = ClassLoaders.pushContext(cl);

        try {
            TrainTestExperiment experiment = TrainTestExperiment.load(specFile.toPath());
            experiment.execute();
        } catch (IOException e) {
            throw new LenskitCommandException("could not load spec file", e);
        } catch (EvaluationException e) {
            throw new LenskitCommandException("error running LensKit experiment", e);
        } finally {
            ctx.pop();
        }

    }
}
