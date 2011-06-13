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

import java.util.ArrayList;
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
import de.unisb.cs.st.evosuite.testcase.PrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * @author Gordon Fraser
 * 
 */
public class TestSuiteMinimizer {

	/** Logger */
	private final static Logger logger = Logger.getLogger(TestSuiteMinimizer.class);

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
			//result.trace = ExecutionTracer.getExecutionTracer().getTrace();

		} catch (Exception e) {
			System.out.println("TG: Exception caught: " + e);
			try {
				Thread.sleep(1000);
				result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
			} catch (Exception e1) {
				e.printStackTrace();
				// TODO: Do some error recovery?
				System.exit(1);
			}
		}

		return result;
	}

	private int getNumCovered(TestSuiteChromosome suite) {

		boolean calls_enabled = ExecutionTrace.trace_calls;
		if (!calls_enabled)
			ExecutionTrace.enableTraceCalls();

		int num = 0;
		for (TestFitnessFunction goal : goals) {
			for (TestChromosome test : suite.getTestChromosomes()) {
				if (goal.isCovered(test)) {
					num++;
					break;
				}
			}
		}
		if (!calls_enabled)
			ExecutionTrace.disableTraceCalls();

		return num;
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

	@SuppressWarnings("unused")
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
				logger.debug("Skipping test " + num);
				result = test.last_result;
			}
			called_methods.addAll(result.getTrace().covered_methods.keySet());
			for (Entry<String, Double> entry : result.getTrace().true_distances.entrySet()) {
				if (entry.getValue() == 0)
					covered_true.add(entry.getKey());
			}

			for (Entry<String, Double> entry : result.getTrace().false_distances.entrySet()) {
				if (entry.getValue() == 0)
					covered_false.add(entry.getKey());
			}

			num++;
		}

		logger.debug("Called methods: " + called_methods.size());
		int check = covered_true.size() + covered_false.size() + called_methods.size();
		if (check > goals.size()) {
			logger.info("Covered methods: " + called_methods.size());
			logger.info("Covered true: " + covered_true.size());
			logger.info("Covered false: " + covered_false.size());
		}
		return covered_true.size() + covered_false.size() + called_methods.size();
	}

	/**
	 * Remove all unreferenced variables
	 * 
	 * @param t
	 *            The test case
	 * @return True if something was deleted
	 */
	public boolean removeUnusedVariables(TestCase t) {
		List<Integer> to_delete = new ArrayList<Integer>();
		boolean has_deleted = false;

		int num = 0;
		for (StatementInterface s : t) {
			if (s instanceof PrimitiveStatement) {

				VariableReference var = s.getReturnValue();
				if (!t.hasReferences(var)) {
					to_delete.add(num);
					has_deleted = true;
				}
			}
			num++;
		}
		Collections.sort(to_delete, Collections.reverseOrder());
		for (Integer position : to_delete) {
			t.remove(position);
		}

		return has_deleted;
	}

	@SuppressWarnings("unused")
	private int checkFitness(TestSuiteChromosome suite) {
		for (int i = 0; i < suite.size(); i++) {
			suite.getTestChromosome(i).last_result = null;
			suite.getTestChromosome(i).setChanged(true);
		}
		return getNumCovered(suite);
	}

	/**
	 * Minimize test suite with respect to branch coverage
	 * 
	 * @param suite
	 * @param fitness_function
	 */
	public void minimize(TestSuiteChromosome suite) {

		@SuppressWarnings("unused")
		boolean branch = Properties.CRITERION == Properties.Criterion.BRANCH;
		CurrentChromosomeTracker.getInstance().modification(suite);
		Properties.RECYCLE_CHROMOSOMES = false; // TODO: FIXXME!

		for (TestCase test : suite.getTests()) {
			removeUnusedVariables(test);
		}

		// Remove previous results as they do not contain method calls
		// in the case of whole suite generation
		for (TestChromosome test : suite.getTestChromosomes()) {
			test.setChanged(true);
			test.last_result = null;
		}

		boolean size = false;
		String strategy = Properties.SECONDARY_OBJECTIVE;
		if (strategy.contains(":"))
			strategy = strategy.substring(0, strategy.indexOf(':'));
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
		int fitness = 0;

		if (branch)
			fitness = getNumCoveredBranches(suite);
		else
			fitness = getNumCovered(suite);

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
						test_factory.deleteStatementGracefully(test.test, i);
						test.setChanged(true);
					} catch (ConstructionFailedException e) {
						test.setChanged(false);
						test.test = copy.test;
						logger.debug("Deleting failed");
						continue;
					}
					// logger.debug("Trying: ");
					// logger.debug(test.test.toCode());

					int new_fitness = 0;
					if (branch)
						new_fitness = getNumCoveredBranches(suite);
					else
						new_fitness = getNumCovered(suite);

					if (new_fitness >= fitness) {
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

		//assert (checkFitness(suite) == fitness);

		logger1.setLevel(old_level1);
		logger2.setLevel(old_level2);
		logger3.setLevel(old_level3);
	}

}
