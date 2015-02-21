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
package org.evosuite.coverage.branch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.evosuite.Properties;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fitness function for a whole test suite for all branches
 * 
 * @author Gordon Fraser, Jose Miguel Rojas
 */
public class OnlyBranchCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = 8416765652693373609L;

	private final static Logger logger = LoggerFactory.getLogger(TestSuiteFitnessFunction.class);

	// Coverage targets
	public final int totalBranches;
	public final int totalGoals;

	/**
	 * <p>
	 * Constructor for OnlyBranchCoverageSuiteFitness.
	 * </p>
	 */
	public OnlyBranchCoverageSuiteFitness() {

		String prefix = Properties.TARGET_CLASS_PREFIX;

		if (prefix.isEmpty()) {
			prefix = Properties.TARGET_CLASS;
			totalBranches = BranchPool.getBranchCounter();
		} else {
			totalBranches = BranchPool.getBranchCountForPrefix(prefix);
		}

		totalGoals = 2 * totalBranches;

		logger.info("Total branch coverage goals: " + totalGoals);
		logger.info("Total branches: " + totalBranches);

		determineCoverageGoals();
	}

	// Some stuff for debug output
	public int maxCoveredBranches = 0;
	public double bestFitness = Double.MAX_VALUE;

	// Each test gets a set of distinct covered goals, these are mapped by branch id
	private final Map<Integer, TestFitnessFunction> branchCoverageTrueMap = new HashMap<Integer, TestFitnessFunction>();
	private final Map<Integer, TestFitnessFunction> branchCoverageFalseMap = new HashMap<Integer, TestFitnessFunction>();

	/**
	 * Initialize the set of known coverage goals
	 */
	private void determineCoverageGoals() {
		List<OnlyBranchCoverageTestFitness> goals = new OnlyBranchCoverageFactory().getCoverageGoals();
		for (OnlyBranchCoverageTestFitness goal : goals) {
			if (goal.getBranchExpressionValue())
				branchCoverageTrueMap.put(goal.getBranch().getActualBranchId(), goal);
			else
				branchCoverageFalseMap.put(goal.getBranch().getActualBranchId(), goal);
		}
	}

	/**
	 * If there is an exception in a superconstructor, then the corresponding
	 * constructor might not be included in the execution trace
	 * 
	 * @param results
	 * @param callCount
	 */
	private void handleConstructorExceptions(List<ExecutionResult> results,
	        Map<String, Integer> callCount) {

		for (ExecutionResult result : results) {
			if (result.hasTimeout() || result.hasTestException()
			        || result.noThrownExceptions())
				continue;

			Integer exceptionPosition = result.getFirstPositionOfThrownException();
			Statement statement = result.test.getStatement(exceptionPosition);
			if (statement instanceof ConstructorStatement) {
				ConstructorStatement c = (ConstructorStatement) statement;
				String className = c.getConstructor().getName();
				String methodName = "<init>"
				        + Type.getConstructorDescriptor(c.getConstructor().getConstructor());
				String name = className + "." + methodName;
				if (!callCount.containsKey(name)) {
					callCount.put(name, 1);
				}
			}

		}
	}

	/**
	 * Iterate over all execution results and summarize statistics
	 * 
	 * @param results
	 * @param predicateCount
	 * @param callCount
	 * @param trueDistance
	 * @param falseDistance
	 * @return
	 */
	private boolean analyzeTraces(List<ExecutionResult> results,
	        Map<Integer, Integer> predicateCount, Map<String, Integer> callCount,
	        Map<Integer, Double> trueDistance, Map<Integer, Double> falseDistance) {
		boolean hasTimeoutOrTestException = false;

		for (ExecutionResult result : results) {
			if (result.hasTimeout() || result.hasTestException()) {
				hasTimeoutOrTestException = true;
			}

			for (Entry<String, Integer> entry : result.getTrace().getMethodExecutionCount().entrySet()) {
				if (!callCount.containsKey(entry.getKey()))
					callCount.put(entry.getKey(), entry.getValue());
				else {
					callCount.put(entry.getKey(),
					              callCount.get(entry.getKey()) + entry.getValue());
				}
			}
			for (Entry<Integer, Integer> entry : result.getTrace().getPredicateExecutionCount().entrySet()) {
				if (!predicateCount.containsKey(entry.getKey()))
					predicateCount.put(entry.getKey(), entry.getValue());
				else {
					predicateCount.put(entry.getKey(),
							predicateCount.get(entry.getKey())
							+ entry.getValue());
				}
			}
			for (Entry<Integer, Double> entry : result.getTrace().getTrueDistances().entrySet()) {
				if (!trueDistance.containsKey(entry.getKey()))
					trueDistance.put(entry.getKey(), entry.getValue());
				else {
					trueDistance.put(entry.getKey(),
							Math.min(trueDistance.get(entry.getKey()),
									entry.getValue()));
				}
				if (entry.getValue() == 0.0) {
					result.test.addCoveredGoal(branchCoverageTrueMap.get(entry.getKey()));
				}
			}
			for (Entry<Integer, Double> entry : result.getTrace().getFalseDistances().entrySet()) {
				if (!falseDistance.containsKey(entry.getKey()))
					falseDistance.put(entry.getKey(), entry.getValue());
				else {
					falseDistance.put(entry.getKey(),
							Math.min(falseDistance.get(entry.getKey()),
									entry.getValue()));
				}
				if (entry.getValue() == 0.0) {
					result.test.addCoveredGoal(branchCoverageFalseMap.get(entry.getKey()));
				}
			}
		}

		return hasTimeoutOrTestException;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Execute all tests and count covered branches
	 */
	@Override
	public double getFitness(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		logger.trace("Calculating branch fitness");
		double fitness = 0.0;

		List<ExecutionResult> results = runTestSuite(suite);
		Map<Integer, Double> trueDistance = new HashMap<Integer, Double>();
		Map<Integer, Double> falseDistance = new HashMap<Integer, Double>();
		Map<Integer, Integer> predicateCount = new HashMap<Integer, Integer>();
		Map<String, Integer> callCount = new HashMap<String, Integer>();

		// Collect stats in the traces 
		boolean hasTimeoutOrTestException = analyzeTraces(results, predicateCount,
		                                                  callCount, trueDistance,
		                                                  falseDistance);

		// In case there were exceptions in a constructor
		handleConstructorExceptions(results, callCount);

		// Collect branch distances of covered branches
		int numCoveredBranches = 0;

		for (Integer key : predicateCount.keySet()) {
			if (!trueDistance.containsKey(key) || !falseDistance.containsKey(key))
				continue;
			int numExecuted = predicateCount.get(key);
			double df = trueDistance.get(key);
			double dt = falseDistance.get(key);

			// If the branch predicate was only executed once, then add 1 
			if (numExecuted == 1) {
				fitness += 1.0;
			} else {
				fitness += normalize(df) + normalize(dt);
			}
			if (df == 0.0)
				numCoveredBranches++;

			if (dt == 0.0)
				numCoveredBranches++;
		}

		// +1 for every branch that was not executed
		fitness += 2 * (totalBranches - predicateCount.size());

		printStatusMessages(suite, numCoveredBranches, fitness);

		// Calculate coverage
		int coverage = numCoveredBranches;

		if (totalGoals > 0)
			suite.setCoverage(this, (double) coverage / (double) totalGoals);
        else
            suite.setCoverage(this, 1.0);

		suite.setNumOfCoveredGoals(this, coverage);
		suite.setNumOfNotCoveredGoals(this, totalGoals-coverage);
		if (hasTimeoutOrTestException) {
			logger.info("Test suite has timed out, setting fitness to max value "
			        + (totalBranches * 2));
			fitness = totalBranches * 2;
			//suite.setCoverage(0.0);
		}

		updateIndividual(this, suite, fitness);

		assert (coverage <= totalGoals) : "Covered " + coverage + " vs total goals "
		        + totalGoals;
		assert (fitness >= 0.0);
		assert (fitness != 0.0 || coverage == totalGoals) : "Fitness: " + fitness + ", "
		        + "coverage: " + coverage + "/" + totalGoals;
		assert (suite.getCoverage(this) <= 1.0) && (suite.getCoverage(this) >= 0.0) : "Wrong coverage value "
		        + suite.getCoverage(this);

		return fitness;
	}

	/**
	 * Some useful debug information
	 * 
	 * @param coveredBranches
	 * @param fitness
	 */
	private void printStatusMessages(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite,
	        int coveredBranches, double fitness) {
		if (coveredBranches > maxCoveredBranches) {
			maxCoveredBranches = coveredBranches;
			logger.info("(Branches) Best individual covers " + coveredBranches + "/"
			        + (totalBranches * 2) + " branches");
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.totalLengthOfTestCases());
		}

		if (fitness < bestFitness) {
			logger.info("(Fitness) Best individual covers " + coveredBranches + "/"
			        + (totalBranches * 2) + " branches");
			bestFitness = fitness;
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.totalLengthOfTestCases());

		}
	}

}
