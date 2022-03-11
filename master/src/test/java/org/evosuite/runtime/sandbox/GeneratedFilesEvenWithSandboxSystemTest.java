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
package org.evosuite.runtime.sandbox;

import java.io.File;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.sandbox.OpenStream;
import com.examples.with.different.packagename.sandbox.OpenStreamInATryCatch;
import com.examples.with.different.packagename.sandbox.OpenStreamInSpecificTryCatch;

public class GeneratedFilesEvenWithSandboxSystemTest extends SystemTestBase {

    public static final boolean DEFAULT_VFS = Properties.VIRTUAL_FS;
    public static final boolean DEFAULT_SANDBOX = Properties.SANDBOX;


    private final File file = new File(OpenStream.FILE_NAME);

    @Before
    public void init() {
        if (file.exists()) {
            file.delete();
        }
        file.deleteOnExit();
    }

    @After
    public void tearDown() {
        Properties.VIRTUAL_FS = DEFAULT_VFS;
        Properties.SANDBOX = DEFAULT_SANDBOX;
    }

    @Test
    public void testCreateWithNoCatch() {

        Assert.assertFalse(file.exists());

        EvoSuite evosuite = new EvoSuite();

        String targetClass = OpenStream.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.SANDBOX = true;
        Properties.JUNIT_TESTS = true;
        Properties.VIRTUAL_FS = false;
        Properties.JUNIT_CHECK_ON_SEPARATE_PROCESS = false;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();

        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertTrue("Should not achieve optimal coverage ", best.getCoverage() < 1);

        //SUT should not generate the file
        Assert.assertFalse(file.exists());
    }

    @Test
    public void testCreateInATryCatch() {

        Assert.assertFalse(file.exists());

        EvoSuite evosuite = new EvoSuite();

        String targetClass = OpenStreamInATryCatch.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.SANDBOX = true;
        Properties.JUNIT_TESTS = true;
        Properties.VIRTUAL_FS = false;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();

        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals("Wrong number of goals: ", 5, goals);
        Assert.assertEquals("", 0.8d, best.getCoverage(), 0.001); //one branch is infeasible

        System.out.println(best);

        //SUT should not generate the file, even if full coverage and SecurityException is catch in the SUT
        Assert.assertFalse(file.exists());
    }

    @Test
    public void testCreateInATryCatchThatDoesNotCatchSecurityException() {

        Assert.assertFalse(file.exists());

        EvoSuite evosuite = new EvoSuite();

        String targetClass = OpenStreamInSpecificTryCatch.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.SANDBOX = true;
        Properties.JUNIT_TESTS = true;
        Properties.VIRTUAL_FS = false;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();

        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertTrue("Should not achive optimala coverage ", best.getCoverage() < 1);

        System.out.println(best);

        //SUT should not generate the file, even if full coverage and SecurityException is catch in the SUT
        Assert.assertFalse(file.exists());
    }
}
