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
import com.google.common.base.Stopwatch;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.lenskit.cli.Command;
import org.lenskit.cli.LenskitCommandException;
import org.lenskit.cli.util.InputData;
import org.lenskit.cli.util.ScriptEnvironment;
import org.lenskit.eval.temporal.TemporalEvaluator;
import org.lenskit.eval.traintest.AlgorithmInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 * Simulates a recommender algorithm over time.
 */
@AutoService(Command.class)
public class Simulate implements Command {
    private final Logger logger = LoggerFactory.getLogger(Simulate.class);

    public void configureArguments(ArgumentParser parser) {
        parser.description("Simulates a recommender over time");
        ScriptEnvironment.configureArguments(parser);
        InputData.configureArguments(parser, true);
        parser.addArgument("-o", "--output-file")
              .type(File.class)
              .metavar("FILE")
              .setDefault("predictions.csv")
              .help("write predictions and errors to FILE");
        parser.addArgument("--extended-output")
              .type(File.class)
              .metavar("FILE")
              .setDefault("extended-output.txt")
              .help("write extended output as JSON lines in FILE");
        parser.addArgument("-n", "--list-size")
              .type(Integer.class)
              .metavar("INTEGER")
              .setDefault(10)
              .help("Length of recommendation lists");
        parser.addArgument("-r", "--rebuild-period")
              .type(Long.class)
              .setDefault(86400L)
              .metavar("SECONDS")
              .help("Rebuild Period for next build");
        parser.addArgument("config")
              .type(File.class)
              .metavar("CONFIG")
              .nargs("?")
              .help("load algorithm configuration from CONFIG");
    }

    @Override
    public String getName() {
        return "simulate";
    }

    @Override
    public String getHelp() {
        return "Simulate a recommender algorithm over time";
    }

    @Override
    public void execute(Namespace opts) throws LenskitCommandException {

        Context ctx = new Context(opts);
        ScriptEnvironment environment = new ScriptEnvironment(opts);
        InputData input = new InputData(environment, opts);

        TemporalEvaluator eval = new TemporalEvaluator();

        eval.setListSize(ctx.getListSize());
        eval.setRebuildPeriod(ctx.getRebuildPeriod());

        eval.setDataSource(input.getDAO());
        File out = ctx.getOutputFile();
        if (out != null) {
            eval.setOutputFile(out);
        }
        out = ctx.getExtendedOutputFile();
        if (out != null) {
            eval.setExtendedOutputFile(out.toPath());
        }

        List<AlgorithmInstance> algos = AlgorithmInstance.load(ctx.getConfigFile().toPath(), "algorithm",
                                                               environment.getClassLoader());
        if (algos.size() != 1) {
            logger.error("expected 1 algorithm, found {}", algos.size());
            throw new IllegalArgumentException("too many algorithms");
        } else {
            eval.setAlgorithm(algos.get(0));
        }

        Stopwatch timer = Stopwatch.createStarted();
        logger.info("beginning temporal evaluator");
        try {
            eval.execute();
        } catch (IOException e) {
            throw new LenskitCommandException(e);
        }
        timer.stop();
        logger.info("evaluator executed  in {}", timer);
    }

    private static class Context {
        private final Namespace options;

        Context(Namespace opts) {
            options = opts;
        }

        File getOutputFile() {
            return options.get("output_file");
        }

        File getExtendedOutputFile() {
            return options.get("extended_output");
        }

        File getConfigFile() {
            return options.get("config");
        }

        long getRebuildPeriod() {
            return options.get("rebuild_period");
        }

        int getListSize() {
            return options.get("list_size");
        }
    }
}
