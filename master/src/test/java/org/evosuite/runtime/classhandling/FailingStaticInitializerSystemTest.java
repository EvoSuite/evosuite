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
package org.evosuite.runtime.classhandling;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.staticfield.FailingStaticInitializer;

public class FailingStaticInitializerSystemTest extends SystemTestBase {


    @Test
    public void test() throws IOException {
        final String targetClass = FailingStaticInitializer.class.getCanonicalName();
        Properties.TEST_DIR = System.getProperty("user.dir");
        Properties.TARGET_CLASS = targetClass;
        Properties.CLASS_PREFIX = targetClass.substring(0,
                targetClass.lastIndexOf('.'));

        String name = Properties.TARGET_CLASS.substring(Properties.TARGET_CLASS.lastIndexOf(".") + 1)
                + Properties.JUNIT_SUFFIX;

        String junitFileName = Properties.TEST_DIR + File.separatorChar
                + Properties.CLASS_PREFIX.replace('.', File.separatorChar) + File.separatorChar + name + ".java";

        Properties.RESET_STATIC_FIELDS = true;
        Properties.JUNIT_TESTS = true;
        Properties.JUNIT_CHECK = Properties.JUnitCheckValues.TRUE;

        final Path path = Paths.get(junitFileName);
        Files.deleteIfExists(path);

        // check that the test suite does not exist
        Assert.assertFalse("Test Suite needs to be deleted first", Files.exists(path));

        EvoSuite evosuite = new EvoSuite();

        String[] command = new String[]{"-generateSuite", "-class", Properties.TARGET_CLASS};

        Object result = evosuite.parseCommandLine(command);

        // check that the test suite was created
        Assert.assertTrue("Test Suite does not exist", Files.exists(path));

        // clean-up after test execution
        Files.deleteIfExists(path);
    }

}
