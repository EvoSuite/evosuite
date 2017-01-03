package org.evosuite.testcase.localsearch;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.Collections;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.localsearch.TestSuiteLocalSearchObjective;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.symbolic.Foo;

public class TestDSETestCaseLocalSearch {

	/**
	 * Creates the test case:
	 * 
	 * <code>
	 * int int0 = 10;
	 * int int1 = 10;
	 * int int2 = 10;
	 * Foo.bar(int0,int1,int2);
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
		VariableReference int0 = builder.appendIntPrimitive(10);
		VariableReference int1 = builder.appendIntPrimitive(10);
		VariableReference int2 = builder.appendIntPrimitive(10);
		Class<?> fooClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
				.loadClass(Properties.TARGET_CLASS);
		Method barMethod = fooClass.getMethod("bar", int.class, int.class, int.class);
		builder.appendMethod(null, barMethod, int0, int1, int2);
		return builder.getDefaultTestCase();
	}

	private static final long DEFAULT_LOCAL_SEARCH_BUDGET = Properties.LOCAL_SEARCH_BUDGET;
	private static final Properties.LocalSearchBudgetType DEFAULT_LOCAL_SEARCH_BUDGET_TYPE = Properties.LOCAL_SEARCH_BUDGET_TYPE;

	private static final Properties.SolverType DEFAULT_DSE_SOLVER = Properties.DSE_SOLVER;

	@Before
	public void init() {
		ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();
		Properties.LOCAL_SEARCH_BUDGET = Integer.MAX_VALUE;
		Properties.LOCAL_SEARCH_BUDGET_TYPE = Properties.LocalSearchBudgetType.TESTS;
	}

	@After
	public void restoreProperties() {
		Properties.LOCAL_SEARCH_BUDGET = DEFAULT_LOCAL_SEARCH_BUDGET;
		Properties.LOCAL_SEARCH_BUDGET_TYPE = DEFAULT_LOCAL_SEARCH_BUDGET_TYPE;
		Properties.DSE_SOLVER = DEFAULT_DSE_SOLVER;
	}

	@Test
	public void testAVMSolver() throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		Properties.DSE_SOLVER = Properties.SolverType.EVOSUITE_SOLVER;
		Properties.CRITERION = new Properties.Criterion[] { Criterion.BRANCH };
		Properties.TARGET_CLASS = Foo.class.getName();

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

		double fitnessBeforeLocalSearch = branchCoverageSuiteFitness.getFitness(suite);
		int coveredGoalsBeforeLocalSearch = suite.getNumOfCoveredGoals();

		// some goal was covered
		assertTrue(coveredGoalsBeforeLocalSearch > 0);

		DefaultTestCase duplicatedTestCase0 = buildTestCase0();
		TestChromosome duplicatedTestChromosome0 = new TestChromosome();
		duplicatedTestChromosome0.setTestCase(duplicatedTestCase0);
		suite.addTest(duplicatedTestChromosome0);

		TestSuiteLocalSearchObjective localSearchObjective = TestSuiteLocalSearchObjective
				.buildNewTestSuiteLocalSearchObjective(Collections.singletonList(branchCoverageSuiteFitness), suite, 1);

		DSETestCaseLocalSearch localSearch = new DSETestCaseLocalSearch();
		boolean improved = localSearch.doSearch(duplicatedTestChromosome0, localSearchObjective);
		assertTrue(improved);

		double fitnessAfterLocalSearch = branchCoverageSuiteFitness.getFitness(suite);
		int coveredGoalsAfterLocalSearch = suite.getNumOfCoveredGoals();

		assertTrue(fitnessAfterLocalSearch < fitnessBeforeLocalSearch);
		assertTrue(coveredGoalsAfterLocalSearch > coveredGoalsBeforeLocalSearch);

	}

	@Test
	public void testCVC4Solver() throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		String cvc4_path = System.getenv("cvc4_path");
		if (cvc4_path != null) {
			Properties.CVC4_PATH = cvc4_path;
		}
		Assume.assumeTrue(Properties.CVC4_PATH!=null);
		Properties.DSE_SOLVER = Properties.SolverType.CVC4_SOLVER;
		Properties.CRITERION = new Properties.Criterion[] { Criterion.BRANCH };
		Properties.TARGET_CLASS = Foo.class.getName();
	
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
	
		double fitnessBeforeLocalSearch = branchCoverageSuiteFitness.getFitness(suite);
		int coveredGoalsBeforeLocalSearch = suite.getNumOfCoveredGoals();
	
		// some goal was covered
		assertTrue(coveredGoalsBeforeLocalSearch > 0);
	
		DefaultTestCase duplicatedTestCase0 = buildTestCase0();
		TestChromosome duplicatedTestChromosome0 = new TestChromosome();
		duplicatedTestChromosome0.setTestCase(duplicatedTestCase0);
		suite.addTest(duplicatedTestChromosome0);
	
		TestSuiteLocalSearchObjective localSearchObjective = TestSuiteLocalSearchObjective
				.buildNewTestSuiteLocalSearchObjective(Collections.singletonList(branchCoverageSuiteFitness), suite, 1);
	
		DSETestCaseLocalSearch localSearch = new DSETestCaseLocalSearch();
		boolean improved = localSearch.doSearch(duplicatedTestChromosome0, localSearchObjective);
		assertTrue(improved);
	
		double fitnessAfterLocalSearch = branchCoverageSuiteFitness.getFitness(suite);
		int coveredGoalsAfterLocalSearch = suite.getNumOfCoveredGoals();
	
		assertTrue(fitnessAfterLocalSearch < fitnessBeforeLocalSearch);
		assertTrue(coveredGoalsAfterLocalSearch > coveredGoalsBeforeLocalSearch);
	
	}

}
