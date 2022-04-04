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

import com.examples.with.different.packagename.sandbox.OpenStream;
import org.apache.commons.io.FileUtils;
import org.evosuite.Properties;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.runtime.sandbox.Sandbox;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.factories.JUnitTestCarvedChromosomeFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class JUnitAnalyzerTest {

    //we use carver to simplify the creation of test case chromosomes

    private static final Properties.Criterion[] defaultCriterion = Properties.CRITERION;
    private static final String defaultSelectedJUnit = Properties.SELECTED_JUNIT;
    private static final int defaultSeedMutations = Properties.SEED_MUTATIONS;
    private static final double defaultSeedClone = Properties.SEED_CLONE;
    private static final boolean DEFAULT_VFS = Properties.VIRTUAL_FS;
    private static final boolean DEFAULT_SANDBOX = Properties.SANDBOX;
    private static final boolean DEFAULT_ASSERTS_FOR_EVO = Properties.ENABLE_ASSERTS_FOR_EVOSUITE;
    private static final boolean DEFAULT_SCAFFOLDING = Properties.TEST_SCAFFOLDING;

    private File file = new File(OpenStream.FILE_NAME);


    @Before
    public void init() {

        ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();

        if (file.exists()) {
            file.delete();
        }
        file.deleteOnExit();
    }

    @After
    public void reset() {
        Properties.CRITERION = defaultCriterion;
        Properties.SELECTED_JUNIT = defaultSelectedJUnit;
        Properties.SEED_MUTATIONS = defaultSeedMutations;
        Properties.SEED_CLONE = defaultSeedClone;
        Properties.VIRTUAL_FS = DEFAULT_VFS;
        Properties.SANDBOX = DEFAULT_SANDBOX;
        Properties.ENABLE_ASSERTS_FOR_EVOSUITE = DEFAULT_ASSERTS_FOR_EVO;
        Properties.TEST_SCAFFOLDING = DEFAULT_SCAFFOLDING;
    }

    @Test
    public void testSandboxIssue() throws Exception {

        //First, get a TestCase from a carved JUnit

        Properties.SELECTED_JUNIT = com.examples.with.different.packagename.sandbox.OpenStreamInATryCatch_FakeTestToCarve.class.getCanonicalName();
        Properties.TARGET_CLASS = com.examples.with.different.packagename.sandbox.OpenStreamInATryCatch.class.getCanonicalName();

        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.BRANCH};
        Properties.SEED_MUTATIONS = 0;
        Properties.SEED_CLONE = 1;
        Properties.VIRTUAL_FS = false;
        Properties.SANDBOX = true;
        Properties.ENABLE_ASSERTS_FOR_EVOSUITE = true; //needed for setLoggingForJUnit
        Properties.TEST_SCAFFOLDING = false;

        //FIXME
        Sandbox.initializeSecurityManagerForSUT();

        //file should never be created
        Assert.assertFalse(file.exists());

        JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(null);
        TestChromosome carved = factory.getChromosome();

        /*
         * FIXME: issue with carver
         */
        Files.deleteIfExists(file.toPath());

        Assert.assertFalse(file.exists());

        Assert.assertNotNull(carved);

        TestCase test = carved.getTestCase();

        Assert.assertEquals("Should be: constructor, 1 variable and 1 method", 3, test.size());

        //Now that we have a test case, we check its execution after
        //recompiling it to JUnit, and see if sandbox kicks in

        List<TestCase> list = new ArrayList<>();
        list.add(test);

        Assert.assertFalse(file.exists());

        //NOTE: following order of checks reflects what is done
        // in EvoSuite after the search is finished

        System.out.println("\n COMPILATION CHECK \n");
        //first try to compile (which implies execution)
        JUnitAnalyzer.removeTestsThatDoNotCompile(list);
        Assert.assertEquals(1, list.size());
        Assert.assertFalse(file.exists());

        System.out.println("\n FIRST STABILITY CHECK \n");
        //try once
        JUnitAnalyzer.handleTestsThatAreUnstable(list);
        Assert.assertEquals(1, list.size());
        Assert.assertFalse(file.exists());

        System.out.println("\n SECOND STABILITY CHECK \n");
        //try again
        JUnitAnalyzer.handleTestsThatAreUnstable(list);
        Assert.assertEquals(1, list.size());
        Assert.assertFalse(file.exists());

        System.out.println("\n FINAL VERIFICATION \n");
        JUnitAnalyzer.verifyCompilationAndExecution(list);
        Assert.assertEquals(1, list.size());
        Assert.assertFalse(file.exists());
    }

    @Test
    public void testCreationOfTmpDir() throws IOException {

        File dir = JUnitAnalyzer.createNewTmpDir();
        Assert.assertNotNull(dir);
        Assert.assertTrue(dir.exists());

        FileUtils.deleteDirectory(dir);
        Assert.assertFalse(dir.exists());
    }


}
