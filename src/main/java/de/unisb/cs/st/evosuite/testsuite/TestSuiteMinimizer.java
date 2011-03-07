/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.testsuite;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.coverage.TestFitnessFactory;
import de.unisb.cs.st.evosuite.ga.ConstructionFailedException;
import de.unisb.cs.st.evosuite.testcase.DefaultTestFactory;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testcase.ExecutionTracer;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;

/**
 * @author Gordon Fraser
 * 
 */
public class TestSuiteMinimizer {

	/** Logger */
	private final Logger logger = Logger.getLogger(TestSuiteMinimizer.class);

	/** Factory method that handles statement deletion */
	private final DefaultTestFactory test_factory = DefaultTestFactory.getInstance();

	/** Test execution helper */
	private final TestCaseExecutor executor = TestCaseExecutor.getInstance();

	private final List<TestFitnessFunction> goals;

	public TestSuiteMinimizer(TestFitnessFactory factory) {
		goals = factory.getCoverageGoals();
	}

	/**
	 * Execute a single test case
	 * 
	 * @param test
	 * @return
	 */
	private ExecutionResult runTest(TestCase test) {

		ExecutionResult result = new ExecutionResult(test, null);

		try {
			result = executor.execute(test);
			//result.exceptions = executor.run(test);
			executor.setLogging(true);
			//result.trace = ExecutionTracer.getExecutionTracer().getTrace();

		} catch (Exception e) {
			System.out.println("TG: Exception caught: " + e);
			try {
				Thread.sleep(1000);
				result.trace = ExecutionTracer.getExecutionTracer().getTrace();
			} catch (Exception e1) {
				e.printStackTrace();
				// TODO: Do some error recovery?
				System.exit(1);
			}
		}

		return result;
	}

	private int getNumCovered(TestSuiteChromosome suite) {
		Set<TestFitnessFunction> covered = new HashSet<TestFitnessFunction>();

		boolean calls_enabled = ExecutionTrace.trace_calls;
		if (!calls_enabled)
			ExecutionTrace.enableTraceCalls();

		int num = 0;
		for (TestChromosome test : suite.tests) {
			ExecutionResult result = null;
			if (test.isChanged() || test.last_result == null) {
				logger.debug("Executing test " + num);
				result = runTest(test.test);
				test.last_result = result;
				test.setChanged(false);
			} else {
				// logger.info("Skipping test " + num);
				result = test.last_result;
			}
			for (TestFitnessFunction goal : goals) {
				if (!covered.contains(goal) && goal.getFitness(test, result) == 0.0) { // TODO: 0.0 should not be hardcoded
					covered.add(goal);
					test.test.addCoveredGoal(goal);
				}
			}
			num++;
		}
		if (!calls_enabled)
			ExecutionTrace.disableTraceCalls();

		return covered.size();
	}

	/**
	 * 
	 * Calculate the number of covered branches
	 * 
	 * 
	 * 
	 * @param suite
	 * 
	 * @return
	 */

	private int getNumCoveredBranches(TestSuiteChromosome suite) {

		Set<String> covered_true = new HashSet<String>();
		Set<String> covered_false = new HashSet<String>();
		Set<String> called_methods = new HashSet<String>();

		int num = 0;
		for (TestChromosome test : suite.tests) {
			ExecutionResult result = null;
			if (test.isChanged() || test.last_result == null) {
				logger.debug("Executing test " + num);
				result = runTest(test.test);
				test.last_result = result.clone();
				test.setChanged(false);
			} else {
				// logger.info("Skipping test "+num);
				result = test.last_result;
			}
			called_methods.addAll(result.trace.covered_methods.keySet());
			for (Entry<String, Double> entry : result.trace.true_distances.entrySet()) {
				if (entry.getValue() == 0)
					covered_true.add(entry.getKey());
			}

			for (Entry<String, Double> entry : result.trace.false_distances.entrySet()) {
				if (entry.getValue() == 0)
					covered_false.add(entry.getKey());
			}

			num++;
		}

		logger.debug("Called methods: " + called_methods.size());
		return covered_true.size() + covered_false.size() + called_methods.size();
	}

	/**
	 * Minimize test suite with respect to branch coverage
	 * 
	 * @param suite
	 * @param fitness_function
	 */
	public void minimize(TestSuiteChromosome suite) {

		boolean branch = Properties.CRITERION.equalsIgnoreCase("branch");
		CurrentChromosomeTracker.getInstance().modification(suite);

		boolean size = false;
		String strategy = Properties.getPropertyOrDefault("secondary_objectives",
		                                                  "totallength");
		if (strategy.contains(":"))
			strategy = strategy.substring(0, strategy.indexOf(';'));
		if (strategy.equals("size"))
			size = true;

		Logger logger1 = Logger.getLogger(TestFitnessFunction.class);
		Level old_level1 = logger.getLevel();
		logger1.setLevel(Level.OFF);
		Logger logger2 = Logger.getLogger(TestSuiteFitnessFunction.class);
		Level old_level2 = logger.getLevel();
		logger2.setLevel(Level.OFF);
		Logger logger3 = Logger.getLogger(DefaultTestFactory.class);
		Level old_level3 = logger.getLevel();
		logger3.setLevel(Level.OFF);

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
		int fitness = getNumCovered(suite);
		logger.debug("Initially covered: " + fitness);

		boolean changed = true;
		while (changed) {
			changed = false;
			Iterator<TestChromosome> it = suite.tests.iterator();
			while (it.hasNext()) {
				TestChromosome test = it.next();
				if (test.size() == 0) {
					logger.debug("Removing empty test case");
					it.remove();
				}
			}

			int num = 0;
			for (TestChromosome test : suite.tests) {
				for (int i = test.size() - 1; i >= 0; i--) {
					logger.debug("Current size: " + suite.size() + "/" + suite.length());
					logger.debug("Deleting statement "
					        + test.test.getStatement(i).getCode() + " from test " + num);
					TestChromosome copy = (TestChromosome) test.clone();
					try {
						test.setChanged(true);
						test_factory.deleteStatementGracefully(test.test, i);
					} catch (ConstructionFailedException e) {
						test.setChanged(true);
						test.test = copy.test;
						logger.debug("Deleting failed");
						continue;
					}
					// logger.trace("Trying: ");
					// logger.trace(test.test.toCode());

					int new_fitness = 0;
					if (false && branch)
						new_fitness = getNumCoveredBranches(suite);
					else
						new_fitness = getNumCovered(suite);

					if (new_fitness >= fitness) {
						logger.debug("Fitness after removal: " + new_fitness + " ("
						        + fitness + ")");
						fitness = new_fitness;
						changed = true;

						// If we're optimizing the number of tests, continue
						// deleting from the same test else try to delete from
						// another test
						if (!size)
							break;
					} else {
						// Restore previous state
						logger.debug("Can't remove statement "
						        + copy.test.getStatement(i).getCode());
						logger.debug("Restoring fitness from " + new_fitness + " to "
						        + fitness);
						test.test = copy.test;
						test.last_result = copy.last_result;
						test.setChanged(false);
						// suite.setFitness(fitness); // Redo new fitness value
						// determined by fitness function
					}
				}
				num++;
			}
		}
		// suite.coverage = coverage;
		Iterator<TestChromosome> it = suite.tests.iterator();
		while (it.hasNext()) {
			TestChromosome test = it.next();
			if (test.size() == 0) {
				logger.debug("Removing empty test case");
				it.remove();
			}
		}

		logger1.setLevel(old_level1);
		logger2.setLevel(old_level2);
		logger3.setLevel(old_level3);
	}

}
