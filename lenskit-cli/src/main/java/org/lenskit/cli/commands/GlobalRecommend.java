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
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.lenskit.LenskitRecommender;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.ItemBasedItemRecommender;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;
import org.lenskit.cli.Command;
import org.lenskit.cli.LenskitCommandException;
import org.lenskit.cli.util.InputData;
import org.lenskit.cli.util.RecommenderLoader;
import org.lenskit.cli.util.ScriptEnvironment;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.Entity;
import org.lenskit.util.collections.LongUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Generate Top-N non-personalized recommendations.
 *
 * @since 2.2
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@AutoService(Command.class)
public class GlobalRecommend implements Command {
    private final Logger logger = LoggerFactory.getLogger(GlobalRecommend.class);

    @Override
    public String getName() {
        return "global-recommend";
    }

    @Override
    public String getHelp() {
        return "generate non-personalized recommendations";
    }

    @Override
    public void execute(Namespace opts) throws LenskitCommandException {
        ScriptEnvironment env = new ScriptEnvironment(opts);
        InputData input = new InputData(env, opts);
        RecommenderLoader loader = new RecommenderLoader(input, env, opts);
        LenskitRecommenderEngine engine;
        try {
            engine = loader.loadEngine();
        } catch (IOException e) {
            throw new LenskitCommandException("failed to load recommender", e);
        }

        List<Long> items = opts.get("items");
        final int n = opts.getInt("num_recs");

        try (LenskitRecommender rec = engine.createRecommender(input.getDAO())) {
            ItemBasedItemRecommender irec = rec.getItemBasedItemRecommender();
            DataAccessObject dao = rec.getDataAccessObject();
            if (irec == null) {
                logger.error("recommender has no global recommender");
                throw new UnsupportedOperationException("no global recommender");
            }

            logger.info("using {} reference items: {}", items.size(), items);
            Stopwatch timer = Stopwatch.createStarted();

            ResultList recs = irec.recommendRelatedItemsWithDetails(LongUtils.packedSet(items), n, null, null);
            for (Result res : recs) {
                System.out.format("%d", res.getId());
                Entity item = dao.lookupEntity(CommonTypes.ITEM, res.getId());
                String name = item == null ? null : item.maybeGet(CommonAttributes.NAME);
                if (name != null) {
                    System.out.format(" (%s)", name);
                }
                System.out.format(": %.3f", res.getScore());
                System.out.println();
            }

            timer.stop();
            logger.info("recommended in {}", timer);
        }
    }

    public void configureArguments(ArgumentParser parser) {
        parser.description("Generates non-personalized recommendations using optional reference items.");
        InputData.configureArguments(parser);
        ScriptEnvironment.configureArguments(parser);
        parser.addArgument("-n", "--num-recs")
              .type(Integer.class)
              .setDefault(10)
              .metavar("N")
              .help("generate up to N recommendations");
        parser.addArgument("-c", "--config-file")
              .type(File.class)
              .action(Arguments.append())
              .metavar("FILE")
              .help("use configuration from FILE");
        parser.addArgument("-m", "--model-file")
              .type(File.class)
              .metavar("FILE")
              .help("load model from FILE");
        parser.addArgument("items")
              .type(Long.class)
              .nargs("*")
              .metavar("ITEM")
              .help("use ITEMS as reference for recommendation");
    }
}
