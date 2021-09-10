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
package org.evosuite.coverage.line;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.StoppingCondition;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.ClassWithAnonymousClass;
import com.examples.with.different.packagename.instrumentation.testability.FlagExample3;
import com.examples.with.different.packagename.IntExample;
import com.examples.with.different.packagename.SingleMethod;
import com.examples.with.different.packagename.coverage.IntExampleWithNoElse;
import com.examples.with.different.packagename.staticfield.StaticFoo;

/**
 * @author Jose Miguel Rojas
 */
public class LineCoverageFitnessFunctionSystemTest extends SystemTestBase {

    private Properties.Criterion[] oldCriteria = Arrays.copyOf(Properties.CRITERION, Properties.CRITERION.length);
    private Properties.StoppingCondition oldStoppingCondition = Properties.STOPPING_CONDITION;
    private double oldPrimitivePool = Properties.PRIMITIVE_POOL;
    private boolean oldResetStaticFields = Properties.RESET_STATIC_FIELDS;
    private final int oldChromosomeLength = Properties.CHROMOSOME_LENGTH;

    @Before
    public void beforeTest() {
        oldCriteria = Arrays.copyOf(Properties.CRITERION, Properties.CRITERION.length);
        oldStoppingCondition = Properties.STOPPING_CONDITION;
        oldPrimitivePool = Properties.PRIMITIVE_POOL;
        Properties.CRITERION = new Properties.Criterion[]{Criterion.LINE};
        oldResetStaticFields = Properties.RESET_STATIC_FIELDS;
        //Properties.MINIMIZE = false;
    }

    @After
    public void restoreProperties() {
        Properties.CRITERION = oldCriteria;
        Properties.STOPPING_CONDITION = oldStoppingCondition;
        Properties.PRIMITIVE_POOL = oldPrimitivePool;
        Properties.RESET_STATIC_FIELDS = oldResetStaticFields;
        Properties.CHROMOSOME_LENGTH = oldChromosomeLength;
    }

    @Test
    public void testOnlyLineCoverageFitnessSimpleExampleWithArchive() {
        EvoSuite evosuite = new EvoSuite();
        boolean archive = Properties.TEST_ARCHIVE;
        Properties.TEST_ARCHIVE = false;

        String targetClass = SingleMethod.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.CRITERION = new Properties.Criterion[]{Criterion.ONLYLINE};

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Properties.TEST_ARCHIVE = archive;

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(2, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testLineCoverageFitnessSimpleExampleWithArchive() {
        EvoSuite evosuite = new EvoSuite();
        boolean archive = Properties.TEST_ARCHIVE;
        Properties.TEST_ARCHIVE = false;

        String targetClass = SingleMethod.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Properties.TEST_ARCHIVE = archive;

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(2, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testLineCoverageFitnessSimpleExampleWithoutArchive() {
        EvoSuite evosuite = new EvoSuite();
        boolean archive = Properties.TEST_ARCHIVE;
        Properties.TEST_ARCHIVE = true;

        String targetClass = SingleMethod.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Properties.TEST_ARCHIVE = archive;

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(2, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testLineCoverageFitnessFlagExample3WithoutArchive() {
        EvoSuite evosuite = new EvoSuite();
        boolean archive = Properties.TEST_ARCHIVE;
        Properties.TEST_ARCHIVE = false;

        String targetClass = FlagExample3.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.STOPPING_CONDITION = StoppingCondition.MAXTIME;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Properties.TEST_ARCHIVE = archive;

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(5, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testLineCoverageFitnessFlagExample3WithArchive() {
        EvoSuite evosuite = new EvoSuite();
        boolean archive = Properties.TEST_ARCHIVE;
        Properties.TEST_ARCHIVE = true;

        String targetClass = FlagExample3.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.STOPPING_CONDITION = StoppingCondition.MAXTIME;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Properties.TEST_ARCHIVE = archive;

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(5, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testLineCoverageFitnessBranchGuidanceWithoutArchive() {
        EvoSuite evosuite = new EvoSuite();
        boolean archive = Properties.TEST_ARCHIVE;
        Properties.TEST_ARCHIVE = false;

        String targetClass = IntExample.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        // To see whether there is any guidance we turn off
        // seeding, but need to increase the budget
        Properties.PRIMITIVE_POOL = 0.0;
        Properties.SEARCH_BUDGET = 500_000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Properties.TEST_ARCHIVE = archive;

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(6, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testLineCoverageFitnessBranchGuidanceWithArchive() {
        EvoSuite evosuite = new EvoSuite();
        boolean archive = Properties.TEST_ARCHIVE;
        Properties.TEST_ARCHIVE = true;

        String targetClass = IntExample.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        // To see whether there is any guidance we turn off
        // seeding, but need to increase the budget
        Properties.PRIMITIVE_POOL = 0.0;
        Properties.SEARCH_BUDGET = 150_000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Properties.TEST_ARCHIVE = archive;

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(6, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testOnlyLineCoverageFitnessBranchGuidanceWithArchive() {
        EvoSuite evosuite = new EvoSuite();
        boolean archive = Properties.TEST_ARCHIVE;
        Properties.TEST_ARCHIVE = true;
        Properties.CRITERION = new Properties.Criterion[]{Criterion.ONLYLINE};

        String targetClass = IntExample.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        // To see whether there is any guidance we turn off
        // seeding, but need to increase the budget
        Properties.PRIMITIVE_POOL = 0.0;
        Properties.SEARCH_BUDGET = 150000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Properties.TEST_ARCHIVE = archive;

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(6, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
        // Assert.assertTrue("Did not expect optimal coverage: ", best.getCoverage() < 1);
    }

    @Test
    public void testLineCoverageFitnessBranchGuidance2WithoutArchive() {
        EvoSuite evosuite = new EvoSuite();
        boolean archive = Properties.TEST_ARCHIVE;
        Properties.TEST_ARCHIVE = false;

        String targetClass = IntExampleWithNoElse.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        // To see whether there is any guidance we turn off
        // seeding, but need to increase the budget
        Properties.PRIMITIVE_POOL = 0.0;
        Properties.SEARCH_BUDGET = 150000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Properties.TEST_ARCHIVE = archive;

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(6, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testLineCoverageFitnessBranchGuidance2WithArchive() {
        EvoSuite evosuite = new EvoSuite();
        boolean archive = Properties.TEST_ARCHIVE;
        Properties.TEST_ARCHIVE = true;

        String targetClass = IntExampleWithNoElse.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        // To see whether there is any guidance we turn off
        // seeding, but need to increase the budget
        Properties.PRIMITIVE_POOL = 0.0;
        Properties.SEARCH_BUDGET = 150000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Properties.TEST_ARCHIVE = archive;

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(6, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testListOfGoalsWith_RESET_STATIC_FIELDS_enable() {
        String targetClass = StaticFoo.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.RESET_STATIC_FIELDS = true;

        EvoSuite evosuite = new EvoSuite();

        String[] command = new String[]{
                "-printStats",
                "-class", targetClass
        };

        evosuite.parseCommandLine(command);

        LineCoverageFactory rc = new LineCoverageFactory();

        List<LineCoverageTestFitness> goals = rc.getCoverageGoals();
        for (LineCoverageTestFitness goal : goals)
            System.out.println(goal);

        assertEquals(8, goals.size());
    }

    @Test
    public void testListOfGoalsWith_RESET_STATIC_FIELDS_disable() {
        String targetClass = StaticFoo.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.RESET_STATIC_FIELDS = false;

        EvoSuite evosuite = new EvoSuite();

        String[] command = new String[]{
                "-printStats",
                "-class", targetClass
        };

        evosuite.parseCommandLine(command);

        LineCoverageFactory rc = new LineCoverageFactory();

        List<LineCoverageTestFitness> goals = rc.getCoverageGoals();
        for (LineCoverageTestFitness goal : goals)
            System.out.println(goal);

        assertEquals(8, goals.size());
    }

    @Test
    public void testListOfGoals_AnonymousClass() {
        String targetClass = ClassWithAnonymousClass.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.CRITERION = new Properties.Criterion[]{Criterion.ONLYLINE};

        String[] command = new String[]{
                "-class", targetClass,
                "-printStats"
        };

        EvoSuite evosuite = new EvoSuite();
        evosuite.parseCommandLine(command);

        LineCoverageFactory line_factory = new LineCoverageFactory();
        List<LineCoverageTestFitness> lines = line_factory.getCoverageGoals();
        for (LineCoverageTestFitness line : lines) {
            System.out.println(line);
        }

        // lines: 22, 24, 27, 30, 31, 32, 33, 35, 38 
        Assert.assertEquals(9, lines.size());
    }

    @Test
    public void testCoveredGoals_AnonymousClass() {
        String targetClass = ClassWithAnonymousClass.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.CRITERION = new Properties.Criterion[]{Criterion.ONLYLINE};

        String[] command = new String[]{
                "-class", targetClass,
                "-generateSuite"
        };

        EvoSuite evosuite = new EvoSuite();

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();

        // lines: 22, 24, 27, 30, 31, 32, 33, 35, 38
        Assert.assertEquals(9, TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size());
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }
}
