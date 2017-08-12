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
package org.lenskit.cli;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.*;
import org.apache.commons.lang3.SystemUtils;
import org.lenskit.LenskitInfo;
import org.lenskit.cli.util.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

/**
 * Main entry point for lenskit-cli.
 *
 * @since 3.0
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        ArgumentParser parser =
                ArgumentParsers.newArgumentParser("lenskit")
                               .description("Work with LensKit recommenders and data.");
        Logging.addLoggingGroup(parser);

        Subparsers subparsers = parser.addSubparsers()
                                      .metavar("COMMAND")
                                      .title("commands");
        ServiceLoader<Command> loader = ServiceLoader.load(Command.class);
        for (Command cmd: loader) {
            Subparser cp = subparsers.addParser(cmd.getName())
                                     .help(cmd.getHelp())
                                     .setDefault("command", cmd);
            cmd.configureArguments(cp);
        }

        try {
            Namespace options = parser.parseArgs(args);
            Logging.configureLogging(options);
            Runtime rt = Runtime.getRuntime();
            logger.info("Starting LensKit {} on Java {} from {}",
                        LenskitInfo.lenskitVersion(),
                        SystemUtils.JAVA_VERSION, SystemUtils.JAVA_VENDOR);
            logger.debug("Built from Git revision {}", LenskitInfo.getHeadRevision());
            logger.debug("Using VM '{}' version {} from {}",
                         SystemUtils.JAVA_VM_NAME,
                         SystemUtils.JAVA_VM_VERSION,
                         SystemUtils.JAVA_VM_VENDOR);
            logger.info("Have {} processors and heap limit of {} MiB",
                        rt.availableProcessors(), rt.maxMemory() >> 20);
            Command cmd = options.get("command");
            cmd.execute(options);
            logger.info("If you use LensKit in published research, please see http://lenskit.org/research/");
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        } catch (LenskitCommandException e) {
            logger.error("error running command: " + e, e);
            System.exit(2);
        }
    }
}
