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

import com.examples.with.different.packagename.assertion.ExampleReturningEnum;
import org.evosuite.EvoSuite;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testcase.TestCase;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by gordon on 28/03/2016.
 */
public class EnumAssertionSystemTest extends SystemTestBase {

    @Test
    public void testAssertionsIncludeEnums() {

        EvoSuite evosuite = new EvoSuite();

        String targetClass = ExampleReturningEnum.class.getCanonicalName();

        String[] command = new String[]{
                "-generateSuite", "-class", targetClass, "-Dassertion_strategy=all"};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome suite = ga.getBestIndividual();
        System.out.println(suite.toString());

        Assert.assertTrue(suite.size() > 0);
        for (TestCase test : suite.getTests()) {
            boolean hasEnumAssertion = false;
            for (Assertion ass : test.getAssertions()) {
                if (ass instanceof PrimitiveAssertion) {
                    Assert.assertTrue(ass.getValue().getClass().isEnum());
                    hasEnumAssertion = true;
                }
            }
            Assert.assertTrue("Test has no enum assertions: " + test.toCode(),
                    hasEnumAssertion);
        }
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size();
        Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, suite.getCoverage(), 0.05);
    }


    @Test
    public void testAssertionsPrefersEnums() {

        EvoSuite evosuite = new EvoSuite();

        String targetClass = ExampleReturningEnum.class.getCanonicalName();

        String[] command = new String[]{
                "-generateSuite", "-class", targetClass, "-Dassertion_strategy=mutation"};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome suite = ga.getBestIndividual();
        System.out.println(suite.toString());

        Assert.assertTrue(suite.size() > 0);
        for (TestCase test : suite.getTests()) {
            boolean hasEnumAssertion = false;
            for (Assertion ass : test.getAssertions()) {
                if (ass instanceof PrimitiveAssertion) {
                    Assert.assertTrue(ass.getValue().getClass().isEnum());
                    hasEnumAssertion = true;
                }
            }
            Assert.assertTrue("Test has no enum assertions: " + test.toCode(),
                    hasEnumAssertion);
        }
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size();
        Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, suite.getCoverage(), 0.05);
    }
}
