package org.evosuite.ga.localsearch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.evosuite.Properties;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestCaseExpander;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TestSuiteLocalSearch implements LocalSearch<TestSuiteChromosome> {

	protected static final Logger logger = LoggerFactory.getLogger(TestSuiteLocalSearch.class);
	
	
	public static TestSuiteLocalSearch getLocalSearch() {
		if(Properties.LOCAL_SEARCH_SELECTIVE)
			return new AdaptiveTestSuiteLocalSearch();
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
	protected TestSuiteChromosome expandTestSuite(TestSuiteChromosome individual) {
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

			TestCase newTest = test.getTestCase().clone();
			// TODO: We could cut away the call that leads to an exception?
			/*
			 * if (!test.getLastExecutionResult().noThrownExceptions()) { while
			 * (newTest.size() - 1 >=
			 * test.getLastExecutionResult().getFirstPositionOfThrownException
			 * ()) { newTest.remove(newTest.size() - 1); } }
			 */
			TestCase expandedTest = expandTestCase(newTest);
			newTestSuite.addTest(expandedTest);
		}		
		List<TestChromosome> oldTests = individual.getTestChromosomes();
		oldTests.clear();
		oldTests.addAll(newTestSuite.getTestChromosomes());
		return newTestSuite;
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
	protected void ensureDoubleExecution(TestSuiteChromosome individual, TestSuiteFitnessFunction objective) {
		logger.debug("Ensuring double execution");
		
		Set<TestChromosome> duplicates = new HashSet<TestChromosome>();

		Map<Integer, Integer> covered = new HashMap<Integer, Integer>();
		Map<Integer, TestChromosome> testMap = new HashMap<Integer, TestChromosome>();
		for (TestChromosome test : individual.getTestChromosomes()) {

			// Make sure we have an execution result
			if (test.getLastExecutionResult() == null || test.isChanged()) {
				ExecutionResult result = test.executeForFitnessFunction(objective);
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
				ExecutionResult result = duplicate.executeForFitnessFunction(objective);
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
			objective.getFitness(individual);
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
	
}
