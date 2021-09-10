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

import org.evosuite.runtime.Runtime;
import org.evosuite.runtime.RuntimeSettings;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Scanner;

public class CharByteReadWriteTest {

    private static final boolean VFS = RuntimeSettings.useVFS;

    @Before
    public void init() {
        RuntimeSettings.useVFS = true;
        Runtime.getInstance().resetRuntime();
    }

    @After
    public void restoreProperties() {
        RuntimeSettings.useVFS = VFS;
    }

    @Test
    public void testReadWriteByte() throws Throwable {

        String file = "FileOutputStream_file.tmp";
        String expected = "testReadWriteByte";
        byte[] data = expected.getBytes();

        MockFileOutputStream out = new MockFileOutputStream(file);
        out.write(data, 0, data.length);
        out.flush();
        out.close();

        byte[] buffer = new byte[1024];
        MockFileInputStream in = new MockFileInputStream(file);
        int read = in.read(buffer);
        in.close();
        String result = new String(buffer, 0, read);

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testReadWriteChar() throws Throwable {

        String file = "FileWriter_file.tmp";
        String expected = "testReadWriteChar";
        char[] data = expected.toCharArray();

        MockFileWriter out = new MockFileWriter(file);
        out.write(data, 0, data.length);
        out.flush();
        out.close();

        char[] buffer = new char[1024];
        MockFileReader in = new MockFileReader(file);
        int read = in.read(buffer);
        in.close();
        String result = new String(buffer, 0, read);

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testPrintWriter() throws Throwable {

        String file = "PrintWriter_file.tmp";
        String expected = "testPrintWriter";

        MockPrintWriter out = new MockPrintWriter(file);
        out.println(expected);
        out.close();

        Scanner in = new Scanner(new MockFileInputStream(file));
        String result = in.nextLine();
        in.close();

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testPrintStream() throws Throwable {

        String file = "PrintStream_file.tmp";
        String expected = "testPrintStream";

        MockPrintStream out = new MockPrintStream(file);
        out.println(expected);
        out.close();

        Scanner in = new Scanner(new MockFileInputStream(file));
        String result = in.nextLine();
        in.close();

        Assert.assertEquals(expected, result);
    }


}
