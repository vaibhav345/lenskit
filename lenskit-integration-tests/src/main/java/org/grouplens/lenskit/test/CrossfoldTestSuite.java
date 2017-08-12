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
package org.grouplens.lenskit.test;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.lenskit.LenskitConfiguration;
import org.lenskit.eval.traintest.AlgorithmInstanceBuilder;
import org.lenskit.eval.traintest.SimpleEvaluator;
import org.lenskit.util.table.Table;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

/**
 * A test suite that does cross-validation of an algorithm.
 */
public abstract class CrossfoldTestSuite extends ML100KTestSuite {
    @Rule
    public TemporaryFolder workDir = new TemporaryFolder();

    protected abstract void configureAlgorithm(LenskitConfiguration config);

    protected abstract void checkResults(Table table);

    @Test
    public void testAlgorithmAccuracy() throws IOException {
        SimpleEvaluator evalCommand = new SimpleEvaluator();
        evalCommand.getExperiment().setThreadCount(4);
        evalCommand.setWorkDir(workDir.newFolder("data").toPath());
        AlgorithmInstanceBuilder algo = new AlgorithmInstanceBuilder();
        configureAlgorithm(algo.getConfig());
        evalCommand.addAlgorithm(algo.build());

        evalCommand.addDataSet(source, 5, 0.2);
        addExtraConfig(evalCommand);

        Table result = evalCommand.execute();
        assertThat(result, notNullValue());
        checkResults(result);
    }

    public void addExtraConfig(SimpleEvaluator eval) {
        /* do nothing */
    }
}
