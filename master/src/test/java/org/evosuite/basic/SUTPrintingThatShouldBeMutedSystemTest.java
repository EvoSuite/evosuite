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
package org.evosuite.basic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.utils.LoggingUtils;
import org.junit.*;

import com.examples.with.different.packagename.InfiniteLoops;
import com.examples.with.different.packagename.PrintingThatShouldBeMuted;
import com.examples.with.different.packagename.StaticPrinting;


public class SUTPrintingThatShouldBeMutedSystemTest extends SystemTestBase {

    public static final int defaultTimeout = Properties.TIMEOUT;
    public static final boolean defaultPrintToSystem = Properties.PRINT_TO_SYSTEM;

    public static final PrintStream defaultOut = System.out;

    @Before
    public void init() {
        LoggingUtils.changeLogbackFile(LoggingUtils.getLogbackFileName());
    }

    @After
    public void resetProperties() {
        Properties.TIMEOUT = defaultTimeout;
        Properties.PRINT_TO_SYSTEM = defaultPrintToSystem;

        System.setOut(defaultOut);
        LoggingUtils.changeLogbackFile("logback.xml");
    }


    public void checkIfMuted(String targetClass, String msgSUT) {
        Properties.CLIENT_ON_THREAD = false;

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        PrintStream byteOut = new PrintStream(byteStream);
        System.setOut(byteOut);

        EvoSuite evosuite = new EvoSuite();

        Properties.TARGET_CLASS = targetClass;

        Properties.TIMEOUT = 300;

        //Properties.PRINT_TO_SYSTEM = true;

        String[] command = new String[]{
                "-generateSuite",
                "-class",
                targetClass
                , "-Dprint_to_system=true"
        };

        evosuite.parseCommandLine(command);

        String printed = byteStream.toString();
        Assert.assertTrue("PRINTED:\n" + printed, printed.contains("Starting Client"));
        Assert.assertTrue("PRINTED:\n" + printed, printed.contains(msgSUT));

        //Properties.PRINT_TO_SYSTEM = false;

        command = new String[]{
                "-generateSuite",
                "-class",
                targetClass
                , "-Dprint_to_system=false"
        };

        byteStream.reset();
        evosuite.parseCommandLine(command);

        printed = byteStream.toString();
        Assert.assertTrue("PRINTED:\n" + printed, printed.contains("Starting Client"));
        Assert.assertFalse("PRINTED:\n" + printed, printed.contains(msgSUT));
        Assert.assertFalse("PRINTED:\n" + printed, printed.contains("ERROR"));
    }


    @Test
    public void testBase() throws IOException {
        checkIfMuted(PrintingThatShouldBeMuted.class.getCanonicalName(), "Greater");
    }

    @Test
    public void testStatic() throws IOException {
        checkIfMuted(StaticPrinting.class.getCanonicalName(), "this should not be printed");
    }

    /**
     * This has quite a few side effects on other test cases
     *
     * @throws IOException
     */
    @Ignore
    @Test
    public void testInfiniteLoops() throws IOException {
        checkIfMuted(InfiniteLoops.class.getCanonicalName(), "This should not be printed");
    }
}
