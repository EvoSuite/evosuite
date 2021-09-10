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
package org.evosuite.localsearch;


import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.LocalSearchBudgetType;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.examples.with.different.packagename.localsearch.ArrayLocalSearchExample;
import com.examples.with.different.packagename.localsearch.DoubleLocalSearchExample;
import com.examples.with.different.packagename.localsearch.FloatLocalSearchExample;
import com.examples.with.different.packagename.localsearch.IntegerLocalSearchExample;
import com.examples.with.different.packagename.localsearch.StringLocalSearchExample;

public class LocalSearchSystemTest extends SystemTestBase {

    @Before
    public void init() {
        Properties.DSE_PROBABILITY = 0.0;
        Properties.PRIMITIVE_POOL = 0.0;
        Properties.RESET_STATIC_FIELD_GETS = true;

    }

    @Ignore // This seems to be trivial now?
    @Test
    public void testIntegerGlobalSearch() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = IntegerLocalSearchExample.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.SEARCH_BUDGET = 20000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertTrue("Did not expect optimal coverage", best.getCoverage() < 1.0);
    }

    @Test
    public void testIntegerLocalSearch() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = IntegerLocalSearchExample.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.LOCAL_SEARCH_RATE = 1;
        Properties.LOCAL_SEARCH_PROBABILITY = 1.0;
        Properties.LOCAL_SEARCH_BUDGET_TYPE = LocalSearchBudgetType.SUITES;
        Properties.LOCAL_SEARCH_BUDGET = 10;
        Properties.LOCAL_SEARCH_REFERENCES = false;
        Properties.LOCAL_SEARCH_ARRAYS = false;
        Properties.SEARCH_BUDGET = 50000;

        // Make sure that local search will have effect
        Properties.CHROMOSOME_LENGTH = 4;
        Properties.MAX_INITIAL_TESTS = 2;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }


    @Test
    public void testFloatGlobalSearch() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = FloatLocalSearchExample.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        // Properties.SEARCH_BUDGET = 20000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertTrue("Did not expect optimal coverage", best.getCoverage() < 1.0);
    }


    @Test
    public void testFloatLocalSearch() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = FloatLocalSearchExample.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.LOCAL_SEARCH_RATE = 2; // no adaptation
        Properties.LOCAL_SEARCH_BUDGET_TYPE = LocalSearchBudgetType.TESTS;
        Properties.LOCAL_SEARCH_BUDGET = 10;
        Properties.LOCAL_SEARCH_REFERENCES = false;
        Properties.LOCAL_SEARCH_ARRAYS = false;

        // Make sure that local search will have effect
        Properties.CHROMOSOME_LENGTH = 5;
        Properties.MAX_INITIAL_TESTS = 2;
        //Properties.SEARCH_BUDGET = 20000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testDoubleGlobalSearch() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = DoubleLocalSearchExample.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        //Properties.SEARCH_BUDGET = 30000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertTrue("Did not expect optimal coverage", best.getCoverage() < 1.0);
    }

    @Test
    public void testDoubleLocalSearch() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = DoubleLocalSearchExample.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.LOCAL_SEARCH_RATE = 2;
        Properties.LOCAL_SEARCH_BUDGET_TYPE = LocalSearchBudgetType.TESTS;
        Properties.LOCAL_SEARCH_REFERENCES = false;
        Properties.LOCAL_SEARCH_ARRAYS = false;
        //Properties.SEARCH_BUDGET = 30000;

        // Make sure that local search will have effect
        Properties.CHROMOSOME_LENGTH = 5;
        Properties.MAX_INITIAL_TESTS = 2;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testStringGlobalSearch() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = StringLocalSearchExample.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        //Properties.SEARCH_BUDGET = 20000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertTrue("Did not expect optimal coverage", best.getCoverage() < 1.0);
    }

    @Test
    public void testStringLocalSearch() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = StringLocalSearchExample.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.LOCAL_SEARCH_RATE = 2;
        Properties.LOCAL_SEARCH_BUDGET_TYPE = LocalSearchBudgetType.TESTS;
        Properties.LOCAL_SEARCH_REFERENCES = false;
        Properties.LOCAL_SEARCH_ARRAYS = false;
        //Properties.SEARCH_BUDGET = 30000;

        // Make sure that local search will have effect
        Properties.CHROMOSOME_LENGTH = 5;
        Properties.MAX_INITIAL_TESTS = 2;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testArrayGlobalSearch() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = ArrayLocalSearchExample.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.SEARCH_BUDGET = 50000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertTrue("Did not expect optimal coverage", best.getCoverage() < 1.0);
    }

    @Test
    public void testArrayLocalSearch() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = ArrayLocalSearchExample.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.LOCAL_SEARCH_RATE = 2;
        Properties.LOCAL_SEARCH_BUDGET_TYPE = LocalSearchBudgetType.SUITES;
        Properties.LOCAL_SEARCH_BUDGET = 1;
        Properties.LOCAL_SEARCH_REFERENCES = false;
        Properties.LOCAL_SEARCH_ARRAYS = true;
        Properties.SEARCH_BUDGET = 50000;

        // Make sure that local search will have effect
        Properties.CHROMOSOME_LENGTH = 5;
        Properties.MAX_INITIAL_TESTS = 2;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }
}
