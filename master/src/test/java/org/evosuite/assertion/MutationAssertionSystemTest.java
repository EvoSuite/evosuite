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

package org.evosuite.assertion;

import com.examples.with.different.packagename.*;
import org.evosuite.EvoSuite;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testcase.TestCase;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import org.junit.Ignore;

/**
 * @author fraser
 */
public class MutationAssertionSystemTest extends SystemTestBase {
    private TestSuiteChromosome generateSuite(Class<?> clazz) {
        EvoSuite evosuite = new EvoSuite();
        int generations = 1;

        String targetClass = clazz.getCanonicalName();

        String[] command = new String[]{
                //EvoSuite.JAVA_CMD,
                "-generateTests", "-class", targetClass, "-Dplot=false",
                "-Djunit_tests=false", "-Dshow_progress=false",
                "-Dgenerations=" + generations, "-assertions",
                "-Dassertion_strategy=mutation", "-Dserialize_result=true"};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        return ga.getBestIndividual();
    }

    @Ignore
    @Test
    public void test1() {
        TestSuiteChromosome suite = generateSuite(ExampleObserverClass.class);

        Assert.assertTrue(suite.size() > 0);
        for (TestCase test : suite.getTests()) {
            Assert.assertTrue("Test has no assertions: " + test.toCode(),
                    test.hasAssertions());
        }
    }

    @Ignore
    @Test
    public void test2() {
        TestSuiteChromosome suite = generateSuite(ExampleFieldClass.class);

        Assert.assertTrue(suite.size() > 0);
        for (TestCase test : suite.getTests()) {
            Assert.assertTrue("Test has no assertions: " + test.toCode(),
                    test.hasAssertions());
        }
    }

    @Ignore
    @Test
    public void test3() {
        TestSuiteChromosome suite = generateSuite(ExampleInheritedClass.class);

        Assert.assertTrue(suite.size() > 0);
        for (TestCase test : suite.getTests()) {
            Assert.assertTrue("Test has no assertions: " + test.toCode(),
                    test.hasAssertions());
        }
    }

    @Ignore
    @Test
    public void test4() {
        TestSuiteChromosome suite = generateSuite(ExampleStaticVoidSetterClass.class);

        Assert.assertTrue(suite.size() > 0);
        for (TestCase test : suite.getTests()) {
            if (test.size() > 1)
                Assert.assertTrue("Test has no assertions: " + test.toCode(),
                        test.hasAssertions());
        }
    }

    @Test
    public void testsAssertionsAreGeneratedForWrapperTypes() {

        EvoSuite evosuite = new EvoSuite();

        String targetClass = ExampleNullAssertion.class.getCanonicalName();

        String[] command = new String[]{
                "-generateSuite", "-class", targetClass,
                "-criterion=INPUT",
                "-Djunit_tests=false", "-Dshow_progress=false",
                "-Dassertions=true", "-Dassertion_strategy=mutation"};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome suite = ga.getBestIndividual();

        Assert.assertTrue(suite.size() > 0);
        for (TestCase test : suite.getTests()) {
            if (test.size() > 1)
                Assert.assertTrue("Test has no assertions: " + test.toCode(),
                        test.hasAssertions());
        }
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size();
        Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, suite.getCoverage(), 0.05);
    }
}
