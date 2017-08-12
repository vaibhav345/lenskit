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
package org.lenskit.util.io;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LKFileUtilsTest {
    @Rule
    public TemporaryFolder tmpdir = new TemporaryFolder();

    @Test
    public void testUncompressedFile() throws IOException {
        File file = tmpdir.newFile("uncompressed.txt");
        Writer out = LKFileUtils.openOutput(file);
        try {
            out.write("hello\n");
        } finally {
            out.close();
        }

        Reader in = LKFileUtils.openInput(file, CompressionMode.NONE);
        try {
            char[] buffer = new char[6];
            in.read(buffer);
            assertThat(new String(buffer), equalTo("hello\n"));
        } finally {
            out.close();
        }
    }

    @Test
    public void testGzipFile() throws IOException {
        File file = tmpdir.newFile("uncompressed.txt.gz");
        Writer out = LKFileUtils.openOutput(file);
        try {
            out.write("hello\n");
        } finally {
            out.close();
        }

        Reader in = LKFileUtils.openInput(file, CompressionMode.GZIP);
        try {
            char[] buffer = new char[6];
            in.read(buffer);
            assertThat(new String(buffer), equalTo("hello\n"));
        } finally {
            out.close();
        }
    }

    @Test
    public void testXZFile() throws IOException {
        File file = tmpdir.newFile("uncompressed.txt.xz");
        Writer out = LKFileUtils.openOutput(file);
        try {
            out.write("hello\n");
        } finally {
            out.close();
        }

        Reader in = LKFileUtils.openInput(file, CompressionMode.XZ);
        try {
            char[] buffer = new char[6];
            in.read(buffer);
            assertThat(new String(buffer), equalTo("hello\n"));
        } finally {
            out.close();
        }
    }

    @Test
    public void testNoopBasename() {
        assertThat(LKFileUtils.basename("foo", true),
                   equalTo("foo"));
        assertThat(LKFileUtils.basename("foo", false),
                   equalTo("foo"));
    }

    @Test
    public void testNoPathPreservesExtension() {
        assertThat(LKFileUtils.basename("readme.txt", true),
                   equalTo("readme.txt"));
    }

    @Test
    public void testNoPathDropsExtension() {
        assertThat(LKFileUtils.basename("readme.txt", false),
                   equalTo("readme"));
    }

    @Test
    public void testPathRemoved() {
        assertThat(LKFileUtils.basename(String.format("foo%cbar.txt", File.separatorChar), true),
                   equalTo("bar.txt"));
        assertThat(LKFileUtils.basename(String.format("foo%cbar.txt", File.separatorChar), false),
                   equalTo("bar"));
    }

    @Test
    public void testSlashPathRemoved() {
        assertThat(LKFileUtils.basename("foo/bar.txt", true),
                   equalTo("bar.txt"));
        assertThat(LKFileUtils.basename("foo/bar.txt", false),
                   equalTo("bar"));
    }

    @Test
    public void testKeepDotfileName() {
        // dotfiles should not have extensions stripped, because they do have names
        assertThat(LKFileUtils.basename(".dotfile", false),
                   equalTo(".dotfile"));
    }
}
