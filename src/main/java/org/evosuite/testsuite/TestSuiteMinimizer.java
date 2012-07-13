/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.testsuite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.coverage.branch.BranchCoverageFactory;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.junit.TestSuiteWriter;
import org.evosuite.testcase.AbstractTestFactory;
import org.evosuite.testcase.DefaultTestFactory;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.ExecutionTracer;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * TestSuiteMinimizer class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class TestSuiteMinimizer {

	/** Logger */
	private final static Logger logger = LoggerFactory.getLogger(TestSuiteMinimizer.class);

	private final TestFitnessFactory testFitnessFactory;

	/** Maximum number of seconds. 0 = infinite time */
	protected static int max_seconds = Properties.MINIMIZATION_TIMEOUT;

	/** Assume the search has not started until start_time != 0 */
	protected static long start_time = 0L;

	/**
	 * <p>
	 * Constructor for TestSuiteMinimizer.
	 * </p>
	 * 
	 * @param factory
	 *            a {@link org.evosuite.coverage.TestFitnessFactory} object.
	 */
	public TestSuiteMinimizer(TestFitnessFactory factory) {
		this.testFitnessFactory = factory;
	}

	/**
	 * <p>
	 * minimize
	 * </p>
	 * 
	 * @param suite
	 *            a {@link org.evosuite.testsuite.TestSuiteChromosome} object.
	 */
	public void minimize(TestSuiteChromosome suite) {
		start_time = System.currentTimeMillis();

		String strategy = Properties.SECONDARY_OBJECTIVE;
		if (strategy.contains(":"))
			strategy = strategy.substring(0, strategy.indexOf(':'));

		logger.info("Minimization Strategy: " + strategy + ", " + suite.size() + " tests");

		if (Properties.MINIMIZE_OLD)
			minimizeSuite(suite);
		else
			minimizeTests(suite);
	}

	/**
	 * Minimize test suite with respect to the isCovered Method of the goals
	 * defined by the supplied TestFitnessFactory
	 * 
	 * @param suite
	 *            a {@link org.evosuite.testsuite.TestSuiteChromosome} object.
	 */
	public void minimizeTests(TestSuiteChromosome suite) {

		logger.info("Minimizing per test");

		Properties.RECYCLE_CHROMOSOMES = false; // TODO: FIXXME!
		ExecutionTracer.enableTraceCalls();

		for (TestChromosome test : suite.getTestChromosomes()) {
			test.setChanged(true);
			test.clearCachedResults();
		}

		List<TestFitnessFunction> goals = testFitnessFactory.getCoverageGoals();
		Collections.sort(goals);
		Set<TestFitnessFunction> covered = new HashSet<TestFitnessFunction>();
		List<TestChromosome> minimizedTests = new ArrayList<TestChromosome>();
		TestSuiteWriter minimizedSuite = new TestSuiteWriter();

		for (TestFitnessFunction goal : goals) {
			for (TestChromosome test : minimizedTests) {
				if (goal.isCovered(test)) {
					logger.info("Already covered: " + goal);
					covered.add(goal);
					test.getTestCase().addCoveredGoal(goal);
					break;
				}
			}
			if (covered.contains(goal))
				continue;

			List<TestChromosome> coveredTests = new ArrayList<TestChromosome>();
			for (TestChromosome test : suite.getTestChromosomes()) {
				if (goal.isCovered(test)) {
					coveredTests.add(test);
				}
			}
			Collections.sort(coveredTests);
			if (!coveredTests.isEmpty()) {
				TestChromosome test = coveredTests.get(0);
				org.evosuite.testcase.TestCaseMinimizer minimizer = new org.evosuite.testcase.TestCaseMinimizer(
				        goal);
				TestChromosome copy = (TestChromosome) test.clone();
				minimizer.minimize(copy);

				// TODO: Need proper list of covered goals
				copy.getTestCase().clearCoveredGoals();
				copy.getTestCase().addCoveredGoal(goal);
				minimizedTests.add(copy);
				minimizedSuite.insertTest(copy.getTestCase());
				covered.add(goal);
				logger.info("After new test the suite covers " + covered.size() + "/"
				        + goals.size() + " goals");

			}
		}

		logger.info("Minimized suite covers " + covered.size() + "/" + goals.size()
		        + " goals");
		suite.tests.clear();
		for (TestCase test : minimizedSuite.getTestCases()) {
			suite.addTest(test);
		}
		for (TestFitnessFunction goal : goals) {
			if (!covered.contains(goal))
				logger.info("Failed to cover: " + goal);
		}
		// suite.tests = minimizedTests;
	}

	private boolean isTimeoutReached() {
		long current_time = System.currentTimeMillis();
		if (max_seconds != 0 && start_time != 0
		        && (current_time - start_time) / 1000 > max_seconds)
			logger.info("Timeout reached");

		return max_seconds != 0 && start_time != 0
		        && (current_time - start_time) / 1000 > max_seconds;
	}

	/**
	 * Execute a single test case
	 * 
	 * @param test
	 * @return
	 */
	@Deprecated
	private ExecutionResult runTest(TestCase test) {
		ExecutionResult result = new ExecutionResult(test, null);
		TestCaseExecutor executor = TestCaseExecutor.getInstance();
		try {
			result = executor.execute(test);
		} catch (Exception e) {
			System.out.println("TG: Exception caught: " + e);
			e.printStackTrace();
			try {
				Thread.sleep(1000);
				result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
			} catch (Exception e1) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		return result;
	}

	/**
	 * 
	 * Calculate the number of covered branches
	 * 
	 * This is just so much faster than checking individual goals, so let's keep
	 * it until we've changed to minimizeTests for real.
	 * 
	 * @param suite
	 * 
	 * @return
	 */
	@Deprecated
	private int getNumUncoveredBranches(TestSuiteChromosome suite) {
		Set<Integer> coveredTrue = new HashSet<Integer>();
		Set<Integer> coveredFalse = new HashSet<Integer>();
		Set<String> calledMethods = new HashSet<String>();
		//FIXME 
		//int total_goals = BranchCoverageSuiteFitness.total_goals;
		int total_goals = 0;
		int num = 0;
		for (TestChromosome test : suite.tests) {
			ExecutionResult result = null;
			if (test.isChanged() || test.getLastExecutionResult() == null) {
				logger.debug("Executing test " + num);
				result = runTest(test.getTestCase());
				test.setLastExecutionResult(result.clone());
				test.setChanged(false);
			} else {
				logger.debug("Skipping test " + num);
				result = test.getLastExecutionResult();
			}
			calledMethods.addAll(result.getTrace().getCoveredMethods());
			coveredTrue.addAll(result.getTrace().getCoveredTrueBranches());
			coveredFalse.addAll(result.getTrace().getCoveredFalseBranches());

			num++;
		}
		logger.debug("Called methods: " + calledMethods.size());
		return total_goals
		        - (coveredTrue.size() + coveredFalse.size() + calledMethods.size());
	}

	/**
	 * Minimize test suite with respect to the isCovered Method of the goals
	 * defined by the supplied TestFitnessFactory
	 * 
	 * @param suite
	 *            a {@link org.evosuite.testsuite.TestSuiteChromosome} object.
	 */
	public void minimizeSuite(TestSuiteChromosome suite) {

		CurrentChromosomeTracker.getInstance().modification(suite);
		Properties.RECYCLE_CHROMOSOMES = false; // TODO: FIXXME!

		// Remove previous results as they do not contain method calls
		// in the case of whole suite generation
		for (ExecutableChromosome test : suite.getTestChromosomes()) {
			test.setChanged(true);
			test.clearCachedResults();
		}

		boolean size = false;
		String strategy = Properties.SECONDARY_OBJECTIVE;
		if (strategy.contains(":"))
			strategy = strategy.substring(0, strategy.indexOf(':'));
		if (strategy.equals("size"))
			size = true;

		if (strategy.equals("size")) {
			// If we want to remove tests, start with shortest
			Collections.sort(suite.tests, new Comparator<TestChromosome>() {
				@Override
				public int compare(TestChromosome chromosome1, TestChromosome chromosome2) {
					return chromosome1.size() - chromosome2.size();
				}
			});
		} else if (strategy.equals("maxlength")) {
			// If we want to remove the longest test, start with longest
			Collections.sort(suite.tests, new Comparator<TestChromosome>() {
				@Override
				public int compare(TestChromosome chromosome1, TestChromosome chromosome2) {
					return chromosome2.size() - chromosome1.size();
				}
			});
		}

		// double fitness = fitness_function.getFitness(suite);
		// double coverage = suite.coverage;

		boolean branch = false;
		if (testFitnessFactory instanceof BranchCoverageFactory) {
			logger.info("Using old branch minimization function");
			branch = true;
		}

		double fitness = 0;

		if (branch)
			fitness = getNumUncoveredBranches(suite);
		else
			//logger.fatal("type:::: " + testFitnessFactory.getClass());
			fitness = testFitnessFactory.getFitness(suite);

		boolean changed = true;
		while (changed && !isTimeoutReached()) {
			changed = false;

			removeEmptyTestCases(suite);

			for (TestChromosome testChromosome : suite.tests) {
				if (isTimeoutReached())
					break;

				for (int i = testChromosome.size() - 1; i >= 0; i--) {
					if (isTimeoutReached())
						break;

					logger.debug("Current size: " + suite.size() + "/"
					        + suite.totalLengthOfTestCases());
					logger.debug("Deleting statement "
					        + testChromosome.getTestCase().getStatement(i).getCode()
					        + " from test");
					TestChromosome orgiginalTestChromosome = (TestChromosome) testChromosome.clone();

					try {
						AbstractTestFactory test_factory = DefaultTestFactory.getInstance();
						test_factory.deleteStatementGracefully(testChromosome.getTestCase(),
						                                       i);
						testChromosome.setChanged(true);
					} catch (ConstructionFailedException e) {
						testChromosome.setChanged(false);
						testChromosome.setTestCase(orgiginalTestChromosome.getTestCase());
						logger.debug("Deleting failed");
						continue;
					}
					// logger.debug("Trying: ");
					// logger.debug(test.test.toCode());

					double modifiedVerFitness = 0;
					if (branch)
						modifiedVerFitness = getNumUncoveredBranches(suite);
					else
						modifiedVerFitness = testFitnessFactory.getFitness(suite);

					if (Double.compare(modifiedVerFitness, fitness) <= 0) {
						fitness = modifiedVerFitness;
						changed = true;
						// 
						// 
						//
						/**
						 * This means, that we try to delete statements equally
						 * from each test case (If size is 'false'.) The hope is
						 * that the median length of the test cases is shorter,
						 * as opposed to the average length.
						 */
						if (!size)
							break;
					} else {
						// Restore previous state
						logger.debug("Can't remove statement "
						        + orgiginalTestChromosome.getTestCase().getStatement(i).getCode());
						logger.debug("Restoring fitness from " + modifiedVerFitness
						        + " to " + fitness);
						testChromosome.setTestCase(orgiginalTestChromosome.getTestCase());
						testChromosome.setLastExecutionResult(orgiginalTestChromosome.getLastExecutionResult());
						testChromosome.setChanged(false);
						// suite.setFitness(fitness); // Redo new fitness value
						// determined by fitness function
					}
				}
			}
		}
		// suite.coverage = coverage;
		removeEmptyTestCases(suite);

		//assert (checkFitness(suite) == fitness);
	}

	private void removeEmptyTestCases(TestSuiteChromosome suite) {
		Iterator<TestChromosome> it = suite.tests.iterator();
		while (it.hasNext()) {
			ExecutableChromosome test = it.next();
			if (test.size() == 0) {
				logger.debug("Removing empty test case");
				it.remove();
			}
		}
	}

}
