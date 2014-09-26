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
package org.evosuite.coverage.output;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * @author Jose Miguel Rojas
 *
 */
public class OutputCoverageSuiteFitness  extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = -8345906214972153096L;

	public final int totalGoals;
	
	// Some stuff for debug output
	public int maxCoveredGoals = 0;
	public double bestFitness = Double.MAX_VALUE;

	// Each test gets a set of distinct covered goals, these are mapped by branch id
	private final Map<String, TestFitnessFunction> outputCoverageMap = new HashMap<String, TestFitnessFunction>();

	
	/**
	 * <p>
	 * Constructor for OutputCoverageSuiteFitness.
	 * </p>
	 */
	public OutputCoverageSuiteFitness() {

		// Add observer
		TestCaseExecutor executor = TestCaseExecutor.getInstance();
		OutputObserver observer = new OutputObserver();
		executor.addObserver(observer);
		//TODO: where to remove observer?: executor.removeObserver(observer);
		
		determineCoverageGoals();
		
		totalGoals = outputCoverageMap.size();
	}

	/**
	 * Initialize the set of known coverage goals
	 */
	private void determineCoverageGoals() {
		List<OutputCoverageTestFitness> goals = new OutputCoverageFactory().getCoverageGoals();
		for (OutputCoverageTestFitness goal : goals) {
			outputCoverageMap.put(goal.toString(), goal);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Execute all tests and count covered branches
	 */
	@Override
	public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		logger.trace("Calculating method fitness");
		double fitness = 0.0;

		List<ExecutionResult> results = runTestSuite(suite);

		HashSet<String> setCoveredGoals = new HashSet<String>();
		
		boolean hasTimeoutOrTestException = false;
		for (ExecutionResult result : results) {
			if (result.hasTimeout() || result.hasTestException()) {
				// timed out or has exception
				hasTimeoutOrTestException = true;
			} else {
				updateCoveredGoals(result, setCoveredGoals);
			}
		}
		int coveredGoals = setCoveredGoals.size();
		// calculate fitness
		fitness += normalize(totalGoals - coveredGoals);

		printStatusMessages(suite, coveredGoals, fitness);

		if (totalGoals > 0)
			suite.setCoverage(this, (double) coveredGoals / (double) totalGoals);
        else
            suite.setCoverage(this, 1.0);

		suite.setNumOfCoveredGoals(this, coveredGoals);

		if (hasTimeoutOrTestException) {
			logger.info("Test suite has timed out, setting fitness to max value " + totalGoals);
			fitness = totalGoals;
		}

		updateIndividual(this, suite, fitness);

		assert (coveredGoals <= totalGoals) : "Covered " + coveredGoals + " vs total goals " + totalGoals;
		assert (fitness >= 0.0);
		assert (fitness != 0.0 || coveredGoals == totalGoals) : "Fitness: " + fitness + ", "
		        + "coverage: " + coveredGoals + "/" + totalGoals;
		assert (suite.getCoverage(this) <= 1.0) && (suite.getCoverage(this) >= 0.0) : "Wrong coverage value "
		        + suite.getCoverage(this);

		return fitness;
	}

	private void updateCoveredGoals(ExecutionResult result, HashSet<String> setCoveredGoals) {
		HashSet<String> strGoals = OutputCoverageTestFitness.listCoveredGoals(result.getReturnValues());
		for (String strGoal : strGoals) {
			if (outputCoverageMap.containsKey(strGoal)) {
				setCoveredGoals.add(strGoal);
				result.test.addCoveredGoal(outputCoverageMap.get(strGoal));
			}
		}
	}

	/**
	 * Some useful debug information
	 * 
	 * @param coveredGoals
	 * @param fitness
	 */
	private void printStatusMessages(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite,
	        int coveredGoals, double fitness) {
		if (coveredGoals > maxCoveredGoals) {
			logger.info("(Output Goals) Best individual covers " + coveredGoals + "/"
			        + totalGoals + " output goals");
			maxCoveredGoals = coveredGoals;
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.totalLengthOfTestCases());

		}
		if (fitness < bestFitness) {
			logger.info("(Fitness) Best individual covers " + coveredGoals + "/"
			        + totalGoals + " output goals");
			bestFitness = fitness;
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.totalLengthOfTestCases());

		}
	}

}
