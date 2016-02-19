/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.localsearch.LocalSearch;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseExpander;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.localsearch.BranchCoverageMap;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TestSuiteLocalSearch implements LocalSearch<TestSuiteChromosome> {

	protected static final Logger logger = LoggerFactory.getLogger(TestSuiteLocalSearch.class);
	
	protected void updateFitness(TestSuiteChromosome individual, LocalSearchObjective<TestSuiteChromosome> objective) {
		for(FitnessFunction<? extends Chromosome> ff : objective.getFitnessFunctions()) {
			((TestSuiteFitnessFunction)ff).getFitness(individual);
		}
	}
	
	public static TestSuiteLocalSearch getLocalSearch() {
		if(Properties.LOCAL_SEARCH_SELECTIVE)
			return new SelectiveTestSuiteLocalSearch();
		else
			return new StandardTestSuiteLocalSearch();
	}
	
	/**
	 * Before applying DSE we expand test cases, such that each primitive value
	 * is used at only exactly one position as a parameter
	 * 
	 * @param individual
	 * @return
	 */
	protected void expandTestSuite(TestSuiteChromosome individual, LocalSearchObjective<TestSuiteChromosome> objective) {
		logger.debug("Expanding tests for local search");

		TestSuiteChromosome newTestSuite = new TestSuiteChromosome();
		for (TestChromosome test : individual.getTestChromosomes()) {

			// First make sure we are up to date with the execution
			if (test.getLastExecutionResult() == null || test.isChanged()) {
				test.setLastExecutionResult(TestCaseExecutor.runTest(test
						.getTestCase()));
				test.setChanged(false);
			}

			// We skip tests that have problems
			if (test.getLastExecutionResult().hasTimeout()
					|| test.getLastExecutionResult().hasTestException()) {
				logger.info("Skipping test with timeout or exception");
				continue;
			}
			
			// If local search has already been applied on the original test
			// then we also set that flag on the expanded test
			boolean hasLocalSearchBeenApplied = test.hasLocalSearchBeenApplied();
			TestCase newTest = test.getTestCase().clone();
			TestCase expandedTest = expandTestCase(newTest);
			TestChromosome expandedTestChromosome = newTestSuite.addTest(expandedTest);
			expandedTestChromosome.setLocalSearchApplied(hasLocalSearchBeenApplied);
		}		
		List<TestChromosome> oldTests = individual.getTestChromosomes();
		oldTests.clear();
		oldTests.addAll(newTestSuite.getTestChromosomes());
		individual.setChanged(true);
		for(FitnessFunction<? extends Chromosome> ff : objective.getFitnessFunctions()) {
			((TestSuiteFitnessFunction)ff).getFitness(individual);
		}
	}
	
	private TestCase expandTestCase(TestCase test) {
		if(!Properties.LOCAL_SEARCH_EXPAND_TESTS)
			return test;
		
		TestCaseExpander expander = new TestCaseExpander();
		return expander.expandTestCase(test);
	}
	
	/**
	 * Ensure that all branches are executed twice
	 */
	protected void ensureDoubleExecution(TestSuiteChromosome individual, LocalSearchObjective<TestSuiteChromosome> objective) {
		logger.debug("Ensuring double execution");
		
		Set<TestChromosome> duplicates = new HashSet<TestChromosome>();
		TestSuiteFitnessFunction defaultFitness = (TestSuiteFitnessFunction) objective.getFitnessFunctions().get(0);
		
		Map<Integer, Integer> covered = new HashMap<Integer, Integer>();
		Map<Integer, TestChromosome> testMap = new HashMap<Integer, TestChromosome>();
		for (TestChromosome test : individual.getTestChromosomes()) {

			// Make sure we have an execution result
			if (test.getLastExecutionResult() == null || test.isChanged()) {
				ExecutionResult result = test.executeForFitnessFunction(defaultFitness);
				test.setLastExecutionResult(result); // .clone();
				test.setChanged(false);
			}

			for (Entry<Integer, Integer> entry : test.getLastExecutionResult().getTrace().getPredicateExecutionCount().entrySet()) {
				if (!covered.containsKey(entry.getKey())) {
					covered.put(entry.getKey(), 0);
				}
				covered.put(entry.getKey(),
				            covered.get(entry.getKey()) + entry.getValue());
				testMap.put(entry.getKey(), test);
			}
		}

		for(Entry<Integer, Integer> entry : covered.entrySet()) {
			int branchId = entry.getKey();
			int count = entry.getValue();
			if (count == 1) {
				TestChromosome duplicate = (TestChromosome) testMap.get(branchId).clone();
				ExecutionResult result = duplicate.executeForFitnessFunction(defaultFitness);
				duplicate.setLastExecutionResult(result); // .clone();
				duplicate.setChanged(false);
				duplicates.add(duplicate);
			}
		}

		if (!duplicates.isEmpty()) {
			logger.info("Adding " + duplicates.size()
			        + " tests to cover branches sufficiently");
			for (TestChromosome test : duplicates) {
				individual.addTest(test);
			}
			individual.setChanged(true);
			for(FitnessFunction<? extends Chromosome> ff : objective.getFitnessFunctions()) {
				((TestSuiteFitnessFunction)ff).getFitness(individual);
			}
		}
	}
	
	private Set<Integer> getCoveredTrueBranches(TestSuiteChromosome suite) {
		Set<Integer> covered = new LinkedHashSet<Integer>();
		for(TestChromosome testChromosome : suite.getTestChromosomes()) {
			ExecutionResult lastResult = testChromosome.getLastExecutionResult();
			if(lastResult != null) {
				covered.addAll(lastResult.getTrace().getCoveredTrueBranches());
			}
		}
		return covered;
	}

	private Set<Integer> getCoveredFalseBranches(TestSuiteChromosome suite) {
		Set<Integer> covered = new LinkedHashSet<Integer>();
		for(TestChromosome testChromosome : suite.getTestChromosomes()) {
			ExecutionResult lastResult = testChromosome.getLastExecutionResult();
			if(lastResult != null) {
				covered.addAll(lastResult.getTrace().getCoveredFalseBranches());
			}
		}
		return covered;
	}

	/**
	 * Ensure that all branches are executed twice
	 */
	protected void restoreBranchCoverage(TestSuiteChromosome individual, TestSuiteFitnessFunction objective) {
		logger.debug("Adding branches already covered previously");

		BranchCoverageMap branchMap = BranchCoverageMap.getInstance();

		Set<Integer> uncoveredTrueBranches  = new LinkedHashSet<Integer>(branchMap.getCoveredTrueBranches());
		Set<Integer> uncoveredFalseBranches = new LinkedHashSet<Integer>(branchMap.getCoveredFalseBranches());
		
		uncoveredTrueBranches.removeAll(getCoveredTrueBranches(individual));
		uncoveredFalseBranches.removeAll(getCoveredFalseBranches(individual));
		
		for(Integer branchId : uncoveredTrueBranches) {
			individual.addTest(branchMap.getTestCoveringTrue(branchId).clone());
		}
		for(Integer branchId : uncoveredFalseBranches) {
			individual.addTest(branchMap.getTestCoveringFalse(branchId).clone());
		}
	}
	
	/**
	 * Indicates if the fitness of the individual has improved with respected to 
	 * parameter <code>fitnessBefore</code>
	 * 
	 * @param fitnessBefore the previous fitness of the individual
	 * @param individual the individual
	 * @param objective the local search objective 
	 * @return true if fitness improved, false otherwise 
	 */
	protected boolean hasImproved(double fitnessBefore, TestSuiteChromosome individual, 
			LocalSearchObjective<TestSuiteChromosome> objective) {
		return objective.isMaximizationObjective() ? fitnessBefore < individual.getFitness()
				: fitnessBefore > individual.getFitness();
	}
}
