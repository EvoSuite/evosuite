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
package org.evosuite.testcase;

import com.examples.with.different.packagename.reflection.*;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.coverage.line.LineCoverageSuiteFitness;
import org.evosuite.coverage.method.MethodCoverageSuiteFitness;
import org.evosuite.coverage.method.MethodTraceCoverageSuiteFitness;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by Andrea Arcuri on 02/03/15.
 */
public class PrivateReflectionSystemTest extends SystemTestBase {

    @Test
    public void testPrivateConstructorNoMinimize() {
        Properties.MINIMIZE = false;
        testPrivateConstructor();
    }

    @Test
    public void testPrivateConstructorWithMinimize() {
        Properties.MINIMIZE = true;
        testPrivateConstructor();
    }

    @Test
    public void testPrivateConstructorWithAndWithoutMinimize() {
        Properties.MINIMIZE = true;
        testPrivateConstructor();
        Properties.MINIMIZE = false;
        TestCaseExecutor.getInstance().newObservers();
        TestSuiteChromosome best = testPrivateConstructor();
        double cov = best.getCoverageInstanceOf(LineCoverageSuiteFitness.class);

        Assert.assertEquals("Non-optimal coverage: ", 1d, cov, 0.001);
    }

    private TestSuiteChromosome testPrivateConstructor() {

        Properties.P_REFLECTION_ON_PRIVATE = 0.9;
        Properties.REFLECTION_START_PERCENT = 0.0;

        GeneticAlgorithm<?> ga = do100percentLineTestOnStandardCriteriaWithMethodTrace(PrivateConstructor.class);

        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        assertTrue(!best.getTests().isEmpty());

        double cov = best.getCoverageInstanceOf(MethodCoverageSuiteFitness.class);
        Assert.assertEquals("Non-optimal method coverage: ", 1d, cov, 0.001);

        Optional<? extends FitnessFunction<?>> ff = ga.getFitnessFunctions().stream()
                .filter(m -> m instanceof MethodCoverageSuiteFitness)
                .findAny();

        assertTrue(ff.isPresent());
        assertEquals(1, best.getNumOfCoveredGoals(ff.get()));

        cov = best.getCoverageInstanceOf(MethodTraceCoverageSuiteFitness.class);
        Assert.assertEquals("Non-optimal method trace coverage: ", 1d, cov, 0.001);
        ff = ga.getFitnessFunctions().stream()
                .filter(m -> m instanceof MethodTraceCoverageSuiteFitness)
                .findAny();


        assertTrue(ff.isPresent());
        assertEquals(1, best.getNumOfCoveredGoals(ff.get()));

        return best;
    }

    protected GeneticAlgorithm<?> do100percentLineTestOnStandardCriteriaWithMethodTrace(Class<?> target) {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = target.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        List<Properties.Criterion> criteria = new ArrayList<>(Arrays.asList(standardCriteria));
        criteria.add(Properties.Criterion.METHODTRACE);
        Properties.CRITERION = criteria.toArray(Properties.CRITERION);

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        double cov = best.getCoverageInstanceOf(LineCoverageSuiteFitness.class);

        Assert.assertEquals("Non-optimal coverage: ", 1d, cov, 0.001);

        return ga;
    }


    @Test
    public void testCoverageIssueNoPrivateAccess() {
        //Properties.P_REFLECTION_ON_PRIVATE = 0.9;
        //Properties.REFLECTION_START_PERCENT = 0.0;
        testCoverageIssue();
    }

    @Test
    public void testCoverageIssueWithPrivateAccess() {
        Properties.P_REFLECTION_ON_PRIVATE = 0.9;
        Properties.REFLECTION_START_PERCENT = 0.0;
        testCoverageIssue();
    }


    private void testCoverageIssue() {
        Properties.COVERAGE = true;
        Properties.OUTPUT_VARIABLES = "" + RuntimeVariable.LineCoverage;

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

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }


    @Test
    public void testPrivateFieldInPublicMethod() throws IOException {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = PrivateFieldInPublicMethod.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.P_REFLECTION_ON_PRIVATE = 0.9;
        Properties.REFLECTION_START_PERCENT = 0.0;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
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

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testOnlyPrivateMethods_noReflection() throws IOException {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = OnlyPrivateMethods.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.P_REFLECTION_ON_PRIVATE = 0.0;
        Properties.REFLECTION_START_PERCENT = 0.0;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Assert.assertTrue(best.getCoverage() < 1d);
    }

    @Test
    public void testOnlyPrivateMethods_noTime() throws IOException {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = OnlyPrivateMethods.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.P_REFLECTION_ON_PRIVATE = 0.9;
        Properties.REFLECTION_START_PERCENT = 1.0; //would never start

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Assert.assertTrue(best.getCoverage() < 1d);
    }

    @Test
    public void testGenericsInPrivateMethods() throws IOException {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = PrivateMethodWithGenerics.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.P_REFLECTION_ON_PRIVATE = 0.9;
        Properties.REFLECTION_START_PERCENT = 0.0;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println(best.toString());
        assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericsInPrivateMethodWithTypeVariable() throws IOException {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = PrivateMethodWithTypeVariable.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.P_REFLECTION_ON_PRIVATE = 0.9;
        Properties.REFLECTION_START_PERCENT = 0.0;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println(best.toString());
        assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }
}
