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
package org.lenskit.eval.traintest;

import org.junit.Test;
import org.lenskit.data.dao.file.StaticDataSource;

import java.util.ArrayList;
import java.util.List;

import static net.java.quickcheck.generator.CombinedGenerators.uniqueValues;
import static net.java.quickcheck.generator.CombinedGeneratorsIterables.someNonEmptyLists;
import static net.java.quickcheck.generator.PrimitiveGenerators.nonEmptyStrings;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class DataSetBuilderTest {
    @Test
    public void testAttributeOrder() {
        for (List<String> strings: someNonEmptyLists(uniqueValues(nonEmptyStrings(), 10))) {
            DataSetBuilder bld = new DataSetBuilder(nonEmptyStrings().next());
            StaticDataSource train = new StaticDataSource("train");
            bld.setTrain(train);
            StaticDataSource test = new StaticDataSource("test");
            bld.setTest(test);
            for (String str: strings) {
                bld.setAttribute(str, nonEmptyStrings().next());
            }
            DataSet ds = bld.build();
            assertThat(ds.getAttributes().size(), equalTo(strings.size() + 1));
            List<String> stringArray = new ArrayList<>();
            stringArray.add("DataSet");
            stringArray.addAll(strings);
            String[] strs = stringArray.toArray(new String[stringArray.size()]);
            assertThat(ds.getAttributes().keySet(), contains(strs));
        }
    }
}
