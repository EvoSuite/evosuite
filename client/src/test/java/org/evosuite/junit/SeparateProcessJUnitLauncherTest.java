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
package org.evosuite.junit;

import com.examples.with.different.packagename.junit.PassingFooTest;
import org.evosuite.Properties;
import org.evosuite.runtime.util.JavaExecCmdUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SeparateProcessJUnitLauncherTest {

    private final Properties.OutputFormat defaultTestFormat = Properties.TEST_FORMAT;

    @After
    public void restoreProperties() {
        Properties.TEST_FORMAT = defaultTestFormat;
    }

    @Test
    public void launchesJUnitInSeparateJvmAndReturnsSerializedResult() throws Exception {
        Properties.TEST_FORMAT = Properties.OutputFormat.JUNIT4;

        File resultFile = File.createTempFile("evosuite-junit-result", ".ser");
        resultFile.deleteOnExit();

        String classPath = System.getProperty("java.class.path");
        List<String> command = Arrays.asList(
                JavaExecCmdUtil.getJavaBinExecutablePath(true),
                "-cp",
                classPath,
                SeparateProcessJUnitLauncher.class.getName(),
                resultFile.getAbsolutePath(),
                PassingFooTest.class.getName()
        );

        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        try (InputStream in = process.getInputStream()) {
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            String output = new String(in.readAllBytes());
            Assert.assertTrue("Timed out while waiting for child JVM", finished);
            Assert.assertEquals(output, 0, process.exitValue());
        }
        Assert.assertTrue(resultFile.isFile());

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(resultFile))) {
            Object result = in.readObject();
            Assert.assertTrue(result instanceof JUnitResult);
            JUnitResult junitResult = (JUnitResult) result;
            Assert.assertTrue(junitResult.wasSuccessful());
            Assert.assertEquals(0, junitResult.getFailureCount());
            Assert.assertEquals(3, junitResult.getRunCount());
        }
    }

}
