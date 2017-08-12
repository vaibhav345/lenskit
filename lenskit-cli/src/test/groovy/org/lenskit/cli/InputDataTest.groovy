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
package org.lenskit.cli

import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.inf.ArgumentParserException
import org.lenskit.data.dao.file.StaticDataSource
import org.lenskit.data.ratings.PreferenceDomain

import org.junit.Test
import org.lenskit.cli.util.InputData

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertThat

class InputDataTest {
    static InputData parse(String... args) {
        def parser = ArgumentParsers.newArgumentParser("lenskit-input");
        InputData.configureArguments(parser, true)
        def options = parser.parseArgs(args)
        return new InputData(null, options)
    }

    @Test
    public void testNoFiles() {
        shouldFail(ArgumentParserException) {
            parse()
        }
    }

    @Test
    public void testCSVFile() {
        def data = parse('--csv-file', 'foo.csv')
        def input = data.source as StaticDataSource
        assertThat(input.sources[0].file.fileName.toString(),
                   equalTo('foo.csv'))
        assertThat(input.sources[0].format.delimiter, equalTo(','))
    }

    @Test
    public void testTSVFile() {
        def data = parse('--tsv-file', 'foo.tsv')
        def input = data.source as StaticDataSource
        assertThat(input.sources[0].file.fileName.toString(), equalTo('foo.tsv'))
        assertThat(input.sources[0].format.delimiter, equalTo('\t'))
    }

    @Test
    public void testRatingFile() {
        def data = parse('--ratings-file', 'foo.tsv', '-d', '\t')
        def input = data.source as StaticDataSource
        assertThat(input.sources[0].file.fileName.toString(), equalTo('foo.tsv'))
        assertThat(input.sources[0].format.delimiter, equalTo('\t'))
    }

    @Test
    public void testRatingFileOddDelim() {
        def data = parse('--ratings-file', 'ratings.dat', '-d', '::')
        def input = data.source as StaticDataSource
        assertThat(input.sources[0].file.fileName.toString(), equalTo('ratings.dat'))
        assertThat(input.sources[0].format.delimiter, equalTo('::'))
    }

    @Test
    public void testDataSource() {
        def file = File.createTempFile("input", ".json")
        file.text = """{
  "file": "foo.tsv",
  "delimiter": "\\t",
  "metadata": {
  "domain": {
    "minimum": 0.5,
    "maximum": 5.0,
    "precision": 0.5
  }
  }
}"""
        def data = parse('--data-source', file.absolutePath)
        assertThat(data.source, instanceOf(StaticDataSource))
        def input = data.source as StaticDataSource
        assertThat(input.sources[0].file.fileName.toString(), equalTo('foo.tsv'))
        assertThat(input.sources[0].format.delimiter, equalTo('\t'))
        assertThat(input.preferenceDomain, equalTo(PreferenceDomain.fromString("[0.5,5.0]/0.5")))
    }
}
