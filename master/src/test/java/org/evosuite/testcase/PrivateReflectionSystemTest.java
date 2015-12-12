/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.testcase;

import com.examples.with.different.packagename.reflection.CoverageIssue;
import com.examples.with.different.packagename.reflection.OnlyPrivateMethods;
import com.examples.with.different.packagename.reflection.PrivateFieldInPrivateMethod;
import com.examples.with.different.packagename.reflection.PrivateFieldInPublicMethod;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by Andrea Arcuri on 02/03/15.
 */
public class PrivateReflectionSystemTest extends SystemTest {


    @Test
    public void testCoverageIssueNoPrivateAccess(){
        //Properties.P_REFLECTION_ON_PRIVATE = 0.9;
        //Properties.REFLECTION_START_PERCENT = 0.0;
        testCoverageIssue();
    }

    @Test
    public void testCoverageIssueWithPrivateAccess(){
        Properties.P_REFLECTION_ON_PRIVATE = 0.9;
        Properties.REFLECTION_START_PERCENT = 0.0;
        testCoverageIssue();
    }


    private void testCoverageIssue(){
        Properties.COVERAGE = true;
        Properties.OUTPUT_VARIABLES = ""+RuntimeVariable.LineCoverage;

        do100percentLineTestOnStandardCriteria(CoverageIssue.class);

        OutputVariable out = getOutputVariable(RuntimeVariable.LineCoverage);
        double lineCov = (Double) out.getValue();
        assertEquals(1d, lineCov, 0.01);
    }


    @Test
    public void testPrivateFieldInPrivateMethod() throws IOException {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = PrivateFieldInPrivateMethod.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.P_REFLECTION_ON_PRIVATE = 0.9;
        Properties.REFLECTION_START_PERCENT = 0.0;

        String[] command = new String[] { "-generateSuite", "-class", targetClass };
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }


    @Test
    public void testPrivateFieldInPublicMethod() throws IOException {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = PrivateFieldInPublicMethod.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.P_REFLECTION_ON_PRIVATE = 0.9;
        Properties.REFLECTION_START_PERCENT = 0.0;

        String[] command = new String[] { "-generateSuite", "-class", targetClass };
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println(best.toString());
        assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }


    @Test
    public void testOnlyPrivateMethods() throws IOException {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = OnlyPrivateMethods.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.P_REFLECTION_ON_PRIVATE = 0.9;
        Properties.REFLECTION_START_PERCENT = 0.0;

        String[] command = new String[] { "-generateSuite", "-class", targetClass };
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testOnlyPrivateMethods_noReflection() throws IOException {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = OnlyPrivateMethods.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.P_REFLECTION_ON_PRIVATE = 0.0;
        Properties.REFLECTION_START_PERCENT = 0.0;

        String[] command = new String[] { "-generateSuite", "-class", targetClass };
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        Assert.assertTrue(best.getCoverage() < 1d);
    }

    @Test
    public void testOnlyPrivateMethods_noTime() throws IOException {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = OnlyPrivateMethods.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.P_REFLECTION_ON_PRIVATE = 0.9;
        Properties.REFLECTION_START_PERCENT = 1.0; //would never start

        String[] command = new String[] { "-generateSuite", "-class", targetClass };
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        Assert.assertTrue( best.getCoverage() < 1d);
    }

}
