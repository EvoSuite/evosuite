/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.runtime.mock.java.io;

import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.vfs.VirtualFileSystem;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class MockRandomAccessFileTest {

    @Test
    public void testNoWritePermission() {

        MockFramework.enable();
        VirtualFileSystem.getInstance().resetSingleton();
        VirtualFileSystem.getInstance().init();

        String fileName = "foo_random_access.txt";

        RandomAccessFile ra = null;
        try {
            ra = new MockRandomAccessFile(fileName, "r");
            Assert.fail();
        } catch (FileNotFoundException e1) {
            //expected as file does not exist
        }

        File file = new MockFile(fileName);
        try {
            file.createNewFile();
        } catch (IOException e1) {
            Assert.fail(); //we should be able to create it
        }

        try {
            ra = new MockRandomAccessFile(fileName, "r");
        } catch (FileNotFoundException e1) {
            Assert.fail(); //we should be able to open the stream
        }

        final int LENGTH = 10;

        try {
            ra.setLength(LENGTH);
            Assert.fail();
        } catch (IOException e) {
            //expected, as we do now have write permissions;
        }

        long size = -1;

        try {
            ra.close();
            ra = new MockRandomAccessFile(fileName, "rw");
            ra.setLength(LENGTH);
            size = ra.length();
            ra.close();
        } catch (IOException e) {
            Assert.fail();
        }

        Assert.assertEquals(LENGTH, size);
    }
}
