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
package org.evosuite.coverage.mutation;

import java.util.Arrays;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.Properties.Criterion;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.mutation.MutationPropagation;
import com.examples.with.different.packagename.mutation.SimpleMutationExample1;
import com.examples.with.different.packagename.mutation.SimpleMutationExample2;

public class MutationSystemTest extends SystemTestBase {

    private Properties.Criterion[] oldCriteria = Arrays.copyOf(Properties.CRITERION, Properties.CRITERION.length);
    private Properties.StoppingCondition oldStoppingCondition = Properties.STOPPING_CONDITION;
    private double oldPrimitivePool = Properties.PRIMITIVE_POOL;

    @Before
    public void beforeTest() {
        oldCriteria = Arrays.copyOf(Properties.CRITERION, Properties.CRITERION.length);
        oldStoppingCondition = Properties.STOPPING_CONDITION;
        oldPrimitivePool = Properties.PRIMITIVE_POOL;
        //Properties.MINIMIZE = false;
    }

    @After
    public void restoreProperties() {
        Properties.CRITERION = oldCriteria;
        Properties.STOPPING_CONDITION = oldStoppingCondition;
        Properties.PRIMITIVE_POOL = oldPrimitivePool;
    }

    @Test
    public void testWeakMutationSimpleExampleWithArchive() {
        EvoSuite evosuite = new EvoSuite();
        boolean archive = Properties.TEST_ARCHIVE;
        Properties.TEST_ARCHIVE = true;
        Properties.CRITERION = new Properties.Criterion[]{Criterion.WEAKMUTATION};

        String targetClass = SimpleMutationExample1.class.getCanonicalName();

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Properties.TEST_ARCHIVE = archive;

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(12, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testWeakMutationSimpleExampleWithoutArchive() {
        EvoSuite evosuite = new EvoSuite();
        boolean archive = Properties.TEST_ARCHIVE;
        Properties.TEST_ARCHIVE = false;
        Properties.CRITERION = new Properties.Criterion[]{Criterion.WEAKMUTATION};

        String targetClass = SimpleMutationExample1.class.getCanonicalName();

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Properties.TEST_ARCHIVE = archive;

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(12, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testStrongMutationSimpleExampleWithArchive() {
        EvoSuite evosuite = new EvoSuite();
        boolean archive = Properties.TEST_ARCHIVE;
        Properties.TEST_ARCHIVE = true;
        Properties.CRITERION = new Properties.Criterion[]{Criterion.STRONGMUTATION};

        String targetClass = SimpleMutationExample1.class.getCanonicalName();

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Properties.TEST_ARCHIVE = archive;

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(12, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testStrongMutationSimpleExampleWithoutArchive() {
        EvoSuite evosuite = new EvoSuite();
        boolean archive = Properties.TEST_ARCHIVE;
        Properties.TEST_ARCHIVE = false;
        Properties.CRITERION = new Properties.Criterion[]{Criterion.STRONGMUTATION};

        String targetClass = SimpleMutationExample1.class.getCanonicalName();

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Properties.TEST_ARCHIVE = archive;

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(12, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testWeakMutationSimpleExampleWithArchive2() {
        EvoSuite evosuite = new EvoSuite();
        boolean archive = Properties.TEST_ARCHIVE;
        Properties.TEST_ARCHIVE = true;
        Properties.CRITERION = new Properties.Criterion[]{Criterion.WEAKMUTATION};

        String targetClass = SimpleMutationExample2.class.getCanonicalName();

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Properties.TEST_ARCHIVE = archive;

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(22, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testWeakMutationSimpleExampleWithoutArchive2() {
        EvoSuite evosuite = new EvoSuite();
        boolean archive = Properties.TEST_ARCHIVE;
        Properties.TEST_ARCHIVE = false;
        Properties.CRITERION = new Properties.Criterion[]{Criterion.WEAKMUTATION};

        String targetClass = SimpleMutationExample2.class.getCanonicalName();

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Properties.TEST_ARCHIVE = archive;

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(22, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testStrongMutationSimpleExampleWithArchive2() {
        EvoSuite evosuite = new EvoSuite();
        boolean archive = Properties.TEST_ARCHIVE;
        Properties.TEST_ARCHIVE = true;
        Properties.CRITERION = new Properties.Criterion[]{Criterion.STRONGMUTATION};
        Properties.SEARCH_BUDGET = 50000;

        String targetClass = SimpleMutationExample2.class.getCanonicalName();

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Properties.TEST_ARCHIVE = archive;

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(22, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testStrongMutationSimpleExampleWithoutArchive2() {
        EvoSuite evosuite = new EvoSuite();
        boolean archive = Properties.TEST_ARCHIVE;
        Properties.TEST_ARCHIVE = false;
        Properties.CRITERION = new Properties.Criterion[]{Criterion.STRONGMUTATION};

        String targetClass = SimpleMutationExample2.class.getCanonicalName();

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Properties.TEST_ARCHIVE = archive;

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(22, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testWeakMutationPropagationExampleWithArchive() {
        EvoSuite evosuite = new EvoSuite();
        boolean archive = Properties.TEST_ARCHIVE;
        Properties.TEST_ARCHIVE = true;
        Properties.CRITERION = new Properties.Criterion[]{Criterion.WEAKMUTATION};

        String targetClass = MutationPropagation.class.getCanonicalName();

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Properties.TEST_ARCHIVE = archive;

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(24, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testWeakMutationPropagationExampleWithoutArchive() {
        EvoSuite evosuite = new EvoSuite();
        boolean archive = Properties.TEST_ARCHIVE;
        Properties.TEST_ARCHIVE = false;
        Properties.CRITERION = new Properties.Criterion[]{Criterion.WEAKMUTATION};

        String targetClass = MutationPropagation.class.getCanonicalName();

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Properties.TEST_ARCHIVE = archive;

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(24, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testStrongMutationPropagationExampleWithArchive() {
        EvoSuite evosuite = new EvoSuite();
        boolean archive = Properties.TEST_ARCHIVE;
        Properties.TEST_ARCHIVE = true;
        Properties.CRITERION = new Properties.Criterion[]{Criterion.STRONGMUTATION};

        String targetClass = MutationPropagation.class.getCanonicalName();

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Properties.TEST_ARCHIVE = archive;

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(24, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testStrongMutationPropagationExampleWithoutArchive() {
        EvoSuite evosuite = new EvoSuite();
        boolean archive = Properties.TEST_ARCHIVE;
        Properties.TEST_ARCHIVE = false;
        Properties.CRITERION = new Properties.Criterion[]{Criterion.STRONGMUTATION};

        String targetClass = MutationPropagation.class.getCanonicalName();

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Properties.TEST_ARCHIVE = archive;

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(24, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }
}
