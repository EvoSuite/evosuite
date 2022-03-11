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
package org.evosuite.coverage;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.ClassWithInnerClass;
import com.examples.with.different.packagename.ClassWithPrivateInnerClass;
import com.examples.with.different.packagename.ClassWithPrivateNonStaticInnerClass;

public class InnerClassGoalsSystemTest extends SystemTestBase {

    private final double oldPPool = Properties.PRIMITIVE_POOL;

    @Before
    public void resetStuff() {
        Properties.PRIMITIVE_POOL = oldPPool;
    }

    @Test
    public void testPublicStaticInnerClassWithBranch() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = ClassWithInnerClass.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.CRITERION = new Properties.Criterion[]{
                Properties.Criterion.BRANCH,
        };

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();

        System.out.println(best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(6, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testPublicStaticInnerClassWithLine() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = ClassWithInnerClass.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.CRITERION = new Properties.Criterion[]{
                Properties.Criterion.LINE,
        };

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();

        System.out.println(best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function

        // lines of 'ClassWithInnerClass': 22, 25, 26, 27, 29
        // lines of 'ClassWithInnerClass$AnInnerClass': 31, 33, 34, 36
        //
        // The number of lines actually differs dependent on the JVM platform...
        Assert.assertTrue(9 <= goals && goals <= 10);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testPrivateStaticInnerClassWithBranch() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = ClassWithPrivateInnerClass.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.CRITERION = new Properties.Criterion[]{
                Properties.Criterion.BRANCH,
        };
        // Increase chances of using seeded values to make sure the test finishes in budget
        Properties.PRIMITIVE_POOL = 1.0;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();

        System.out.println(best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        // If reflection is active, then the private constructor will be tested
        if (Properties.P_REFLECTION_ON_PRIVATE > 0.0)
            Assert.assertEquals(6, goals);
        else
            Assert.assertEquals(5, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testPrivateStaticInnerClassWithLine() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = ClassWithPrivateInnerClass.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.CRITERION = new Properties.Criterion[]{
                Properties.Criterion.LINE,
        };

        // Increase chances of using seeded values to make sure the test finishes in budget
        Properties.PRIMITIVE_POOL = 1.0;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();

        System.out.println(best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function

        // lines of 'ClassWithPrivateInnerClass': 22, 24, 25, 26, 28
        // lines of 'ClassWithPrivateInnerClass$AnInnerClass': 30, 32, 33, 35
        //
        // The number of lines actually differs dependent on the JVM platform...
        Assert.assertTrue(9 <= goals && goals <= 10);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testPrivateInnerClassWithBranch() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = ClassWithPrivateNonStaticInnerClass.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.CRITERION = new Properties.Criterion[]{
                Properties.Criterion.BRANCH,
        };

        // Increase chances of using seeded values to make sure the test finishes in budget
        Properties.PRIMITIVE_POOL = 1.0;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();

        System.out.println(best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function

        // If reflection is active, then the private constructor will be tested
        if (Properties.P_REFLECTION_ON_PRIVATE > 0.0)
            Assert.assertEquals(6, goals);
        else
            Assert.assertEquals(5, goals);

        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testPrivateInnerClassWithLine() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = ClassWithPrivateNonStaticInnerClass.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        // Increase chances of using seeded values to make sure the test finishes in budget
        Properties.PRIMITIVE_POOL = 1.0;

        Properties.CRITERION = new Properties.Criterion[]{
                Properties.Criterion.LINE,
        };

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();

        System.out.println(best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function

        // lines of 'ClassWithPrivateNonStaticInnerClass': 22, 25, 26, 27, 29
        // lines of 'ClassWithPrivateNonStaticInnerClass$AnInnerClass': 31, 33, 34, 36
        //
        // The number of lines actually differs dependent on the JVM platform...
        Assert.assertTrue(9 <= goals && goals <= 10);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }
}
