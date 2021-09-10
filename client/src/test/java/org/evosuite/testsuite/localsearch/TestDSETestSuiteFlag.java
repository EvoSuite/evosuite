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
package org.evosuite.testsuite.localsearch;

import com.examples.with.different.packagename.ncs.Flag;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.TestGenerationContext;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.ga.localsearch.DefaultLocalSearchObjective;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class TestDSETestSuiteFlag {

    /**
     * Creates the test case:
     *
     * <code>
     * Flag flag0 = new Flag();
     * </code>
     *
     * @return
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws ClassNotFoundException
     */
    private static DefaultTestCase buildTestCase0()
            throws NoSuchMethodException, SecurityException, ClassNotFoundException {
        TestCaseBuilder builder = new TestCaseBuilder();
        Class<?> flagClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
                .loadClass(Properties.TARGET_CLASS);

        Constructor<?> ctor = flagClass.getConstructor();
        builder.appendConstructor(ctor);

        return builder.getDefaultTestCase();
    }

    /**
     * Creates the test case:
     *
     * <code>
     * int int0 = 0;
     * int int1 = 0;
     * int int2 = 0;
     * Flag.coverMe(int0,int1,int2);
     * </code>
     *
     * @return
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws ClassNotFoundException
     */
    private static DefaultTestCase buildTestCase1()
            throws NoSuchMethodException, SecurityException, ClassNotFoundException {
        TestCaseBuilder builder = new TestCaseBuilder();
        VariableReference int0 = builder.appendIntPrimitive(0);
        VariableReference int1 = builder.appendIntPrimitive(0);
        VariableReference int2 = builder.appendIntPrimitive(0);
        Class<?> flagClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
                .loadClass(Properties.TARGET_CLASS);

        Method barMethod = flagClass.getMethod("coverMe", int.class, int.class, int.class);
        builder.appendMethod(null, barMethod, int0, int1, int2);
        return builder.getDefaultTestCase();
    }

    private static final long DEFAULT_LOCAL_SEARCH_BUDGET = Properties.LOCAL_SEARCH_BUDGET;
    private static final Properties.LocalSearchBudgetType DEFAULT_LOCAL_SEARCH_BUDGET_TYPE = Properties.LOCAL_SEARCH_BUDGET_TYPE;
    private static final Properties.SolverType DEFAULT_DSE_SOLVER = Properties.DSE_SOLVER;
    private static final Properties.DSEType DEFAULT_LOCAL_SEARCH_DSE = Properties.LOCAL_SEARCH_DSE;
    private static final double DEFAULT_DSE_PROBABILITY = Properties.DSE_PROBABILITY;

    @Before
    public void init() {
        ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();
        Properties.LOCAL_SEARCH_BUDGET = Integer.MAX_VALUE;
        Properties.LOCAL_SEARCH_BUDGET_TYPE = Properties.LocalSearchBudgetType.TESTS;
        Properties.DSE_PROBABILITY = 1.0;
        Properties.LOCAL_SEARCH_DSE = Properties.DSEType.SUITE;
    }

    @After
    public void restoreProperties() {
        Properties.LOCAL_SEARCH_BUDGET = DEFAULT_LOCAL_SEARCH_BUDGET;
        Properties.LOCAL_SEARCH_BUDGET_TYPE = DEFAULT_LOCAL_SEARCH_BUDGET_TYPE;
        Properties.DSE_SOLVER = DEFAULT_DSE_SOLVER;
        Properties.DSE_PROBABILITY = DEFAULT_DSE_PROBABILITY;
        Properties.LOCAL_SEARCH_DSE = DEFAULT_LOCAL_SEARCH_DSE;
    }

    @Test
    public void testAVMSolver() throws NoSuchMethodException, SecurityException, ClassNotFoundException {

        Properties.DSE_SOLVER = Properties.SolverType.EVOSUITE_SOLVER;
        Properties.CRITERION = new Properties.Criterion[]{Criterion.BRANCH};
        Properties.TARGET_CLASS = Flag.class.getName();

        TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);

        BranchCoverageSuiteFitness branchCoverageSuiteFitness = new BranchCoverageSuiteFitness();
        TestSuiteChromosome suite = new TestSuiteChromosome();
        suite.addFitness(branchCoverageSuiteFitness);
        branchCoverageSuiteFitness.getFitness(suite);

        // no goals covered yet
        int coveredGoals0 = suite.getNumOfCoveredGoals();
        int notCoveredGoals0 = suite.getNumOfNotCoveredGoals();
        assertEquals(0, coveredGoals0);
        assertNotEquals(0, notCoveredGoals0);

        DefaultTestCase testCase0 = buildTestCase1();
        TestChromosome testChromosome0 = new TestChromosome();
        testChromosome0.setTestCase(testCase0);
        suite.addTest(testChromosome0);

        double fitnessBeforeLocalSearch = branchCoverageSuiteFitness.getFitness(suite);
        int coveredGoalsBeforeLocalSearch = suite.getNumOfCoveredGoals();

        // some goal was covered
        assertTrue(coveredGoalsBeforeLocalSearch > 0);

        LocalSearchObjective<TestSuiteChromosome> localSearchObjective = new DefaultLocalSearchObjective<>();
        localSearchObjective.addFitnessFunction(branchCoverageSuiteFitness);

        boolean improved;
        do {
            TestSuiteLocalSearch localSearch = new TestSuiteLocalSearch();
            improved = localSearch.doSearch(suite, localSearchObjective);
        } while (improved);

        double fitnessAfterLocalSearch = branchCoverageSuiteFitness.getFitness(suite);
        int coveredGoalsAfterLocalSearch = suite.getNumOfCoveredGoals();

        assertTrue(fitnessAfterLocalSearch < fitnessBeforeLocalSearch);
        assertTrue(coveredGoalsAfterLocalSearch > coveredGoalsBeforeLocalSearch);

        int finalSuiteSize = suite.size();
        /*
         * Check at least some new goal was covered
         */
        assertTrue(coveredGoalsAfterLocalSearch > coveredGoalsBeforeLocalSearch);
        assertTrue(finalSuiteSize >= 2);
    }

    @Test
    public void testCVC4Solver() throws NoSuchMethodException, SecurityException, ClassNotFoundException {
        String cvc4_path = System.getenv("cvc4_path");
        if (cvc4_path != null) {
            Properties.CVC4_PATH = cvc4_path;
        }
        Assume.assumeTrue(Properties.CVC4_PATH != null);
        Properties.DSE_SOLVER = Properties.SolverType.CVC4_SOLVER;
        Properties.CRITERION = new Properties.Criterion[]{Criterion.BRANCH};
        Properties.TARGET_CLASS = Flag.class.getName();

        TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);

        BranchCoverageSuiteFitness branchCoverageSuiteFitness = new BranchCoverageSuiteFitness();
        TestSuiteChromosome suite = new TestSuiteChromosome();
        suite.addFitness(branchCoverageSuiteFitness);
        branchCoverageSuiteFitness.getFitness(suite);

        // no goals covered yet
        int coveredGoals0 = suite.getNumOfCoveredGoals();
        int notCoveredGoals0 = suite.getNumOfNotCoveredGoals();
        assertEquals(0, coveredGoals0);
        assertNotEquals(0, notCoveredGoals0);

        DefaultTestCase testCase0 = buildTestCase0();
        TestChromosome testChromosome0 = new TestChromosome();
        testChromosome0.setTestCase(testCase0);
        suite.addTest(testChromosome0);

        branchCoverageSuiteFitness.getFitness(suite);

        DefaultTestCase testCase1 = buildTestCase1();
        TestChromosome testChromosome1 = new TestChromosome();
        testChromosome1.setTestCase(testCase1);
        suite.addTest(testChromosome1);

        double fitnessBeforeLocalSearch = branchCoverageSuiteFitness.getFitness(suite);
        int coveredGoalsBeforeLocalSearch = suite.getNumOfCoveredGoals();

        // some goal was covered
        assertTrue(coveredGoalsBeforeLocalSearch > 0);

        LocalSearchObjective<TestSuiteChromosome> localSearchObjective = new DefaultLocalSearchObjective<>();
        localSearchObjective.addFitnessFunction(branchCoverageSuiteFitness);

        boolean improved;
        do {
            TestSuiteLocalSearch localSearch = new TestSuiteLocalSearch();
            improved = localSearch.doSearch(suite, localSearchObjective);
        } while (improved);

        double fitnessAfterLocalSearch = branchCoverageSuiteFitness.getFitness(suite);
        int coveredGoalsAfterLocalSearch = suite.getNumOfCoveredGoals();

        assertTrue(fitnessAfterLocalSearch < fitnessBeforeLocalSearch);
        assertTrue(coveredGoalsAfterLocalSearch > coveredGoalsBeforeLocalSearch);

        int finalSuiteSize = suite.size();
        /*
         * Check at least 6 goals were covered
         */
        assertTrue(coveredGoalsAfterLocalSearch >= 6);
        assertTrue(finalSuiteSize > 1);
    }

}
