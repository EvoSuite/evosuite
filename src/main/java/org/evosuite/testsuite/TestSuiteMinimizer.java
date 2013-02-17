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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.coverage.branch.BranchCoverageFactory;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.junit.TestSuiteWriter;
import org.evosuite.rmi.ClientServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.rmi.service.ClientStateInformation;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.ExecutionTracer;
import org.evosuite.testcase.StructuredTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFactory;
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

	private final TestFitnessFactory<?> testFitnessFactory;

	/** Assume the search has not started until startTime != 0 */
	protected static long startTime = 0L;

	/**
	 * <p>
	 * Constructor for TestSuiteMinimizer.
	 * </p>
	 * 
	 * @param factory
	 *            a {@link org.evosuite.coverage.TestFitnessFactory} object.
	 */
	public TestSuiteMinimizer(TestFitnessFactory<?> factory) {
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
		startTime = System.currentTimeMillis();

		String strategy = Properties.SECONDARY_OBJECTIVE;
		if (strategy.contains(":"))
			strategy = strategy.substring(0, strategy.indexOf(':'));

		ClientServices.getInstance().getClientNode().trackOutputVariable("full_size", suite.size());
		ClientServices.getInstance().getClientNode().trackOutputVariable("full_length", suite.totalLengthOfTestCases());

		logger.info("Minimization Strategy: " + strategy + ", " + suite.size() + " tests");
		suite.clearMutationHistory();
		
		if (Properties.MINIMIZE_OLD)
			minimizeSuite(suite);
		else
			minimizeTests(suite);

		ClientServices.getInstance().getClientNode().trackOutputVariable("minimized_size", suite.size());
		ClientServices.getInstance().getClientNode().trackOutputVariable("minimized_length", suite.totalLengthOfTestCases());
	}

	private void updateClientStatus(int progress) {
		ClientState state = ClientState.MINIMIZATION;
		ClientStateInformation information = new ClientStateInformation(state);
		information.setProgress(progress);
		ClientServices.getInstance().getClientNode().changeState(state, information);
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

		List<TestFitnessFunction> goals = new ArrayList<TestFitnessFunction>(testFitnessFactory.getCoverageGoals());
		List<TestFitnessFunction> branchGoals = new ArrayList<TestFitnessFunction>();
		int numCovered = 0;
		int currentGoal = 0;

		if (Properties.CRITERION != Properties.Criterion.BRANCH) {
			BranchCoverageFactory branchFactory = new BranchCoverageFactory();
			branchGoals.addAll(branchFactory.getCoverageGoals());
			goals.addAll(branchGoals);
		}

		int numGoals = goals.size();

		Collections.sort(goals);
		Set<TestFitnessFunction> covered = new LinkedHashSet<TestFitnessFunction>();
		List<TestChromosome> minimizedTests = new ArrayList<TestChromosome>();
		TestSuiteWriter minimizedSuite = new TestSuiteWriter();
				
		for (TestFitnessFunction goal : goals) {
			updateClientStatus(100 * currentGoal / numGoals);
			currentGoal++;
			if (isTimeoutReached()){
				/*
				 * FIXME: if timeout, this algorithm should be changed in a way that the modifications
				 * done so far are not lost
				 */
				logger.warn("Minimization timeout. Roll back to original test suite");
				return;
			}
			logger.info("Considering goal: " + goal);
			for (TestChromosome test : minimizedTests) {	
				if (isTimeoutReached()){
					logger.warn("Minimization timeout. Roll back to original test suite");
					return;
				}
				if (goal.isCovered(test)) {
					if (Properties.STRUCTURED_TESTS) {
						StructuredTestCase structuredTest = (StructuredTestCase) test.getTestCase();
						if (structuredTest.getTargetMethods().contains(goal.getTargetMethod())) {
							logger.info("Covered by minimized test targeting "
							        + structuredTest.getTargetMethods() + ": " + goal
							        + " ");
							covered.add(goal);
							if (!branchGoals.contains(goal))
								numCovered++;
							structuredTest.addPrimaryGoal(goal);
							break;
						}

					} else {
						logger.info("Covered by minimized test: " + goal);
						covered.add(goal);
						if (!branchGoals.contains(goal))
							numCovered++;
						test.getTestCase().addCoveredGoal(goal);
						break;
					}
				}
			}
			if (covered.contains(goal)) {
				logger.info("Already covered: " + goal);
				continue;
			}

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
				if (Properties.STRUCTURED_TESTS) {
					copy.setTestCase(new StructuredTestCase(test.getTestCase()));
				}
				minimizer.minimize(copy);
				if (Properties.STRUCTURED_TESTS) {
					// TODO: Find proper way to determine statements
					((StructuredTestCase) copy.getTestCase()).setExerciseStatement(copy.size() - 1);
				}

				// TODO: Need proper list of covered goals
				copy.getTestCase().clearCoveredGoals();
				if (Properties.STRUCTURED_TESTS) {
					((StructuredTestCase) copy.getTestCase()).addPrimaryGoal(goal);
				} else {
					copy.getTestCase().addCoveredGoal(goal);
				}
				minimizedTests.add(copy);
				minimizedSuite.insertTest(copy.getTestCase());
				covered.add(goal);
				if (!branchGoals.contains(goal))
					numCovered++;

				logger.info("After new test the suite covers " + covered.size() + "/"
				        + goals.size() + " goals");

			} else {
				logger.info("Goal is not covered: " + goal);
			}
		}

		logger.info("Minimized suite covers " + covered.size() + "/" + goals.size()
		        + " goals");
		suite.tests.clear();
		for (TestCase test : minimizedSuite.getTestCases()) {
			suite.addTest(test);
		}
		if (numGoals == 0)
			suite.setCoverage(1.0);
		else
			suite.setCoverage((double) numCovered / (double) numGoals);

		SearchStatistics.getInstance().setCoveredGoals(numCovered);

		for (TestFitnessFunction goal : goals) {
			if (!covered.contains(goal))
				logger.info("Failed to cover: " + goal);
		}
		// suite.tests = minimizedTests;
	}

	private boolean isTimeoutReached() {
		long currentTime = System.currentTimeMillis();
		int maxSeconds = Properties.MINIMIZATION_TIMEOUT;
		if (maxSeconds != 0 && startTime != 0
		        && (currentTime - startTime) / 1000 > maxSeconds)
			logger.info("Timeout reached");

		return maxSeconds != 0 && startTime != 0
		        && (currentTime - startTime) / 1000 > maxSeconds;
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
			logger.warn("TG: Exception caught: " + e.getMessage(), e);
			try {
				Thread.sleep(1000);
				result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
			} catch (Exception e1) {
				throw new Error(e1);
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
						TestFactory testFactory = TestFactory.getInstance();
						testFactory.deleteStatementGracefully(testChromosome.getTestCase(),
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
