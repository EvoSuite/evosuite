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
package org.evosuite.coverage.line;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.coverage.method.MethodCoverageFactory;
import org.evosuite.instrumentation.LinePool;
import org.evosuite.testcase.ConstructorStatement;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.TestFitnessFunction;
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
public class LineCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = 7075862615475014070L;

	private final static Logger logger = LoggerFactory.getLogger(TestSuiteFitnessFunction.class);

	// Coverage targets
	public final Set<Integer> lines;

	/**
	 * <p>
	 * Constructor for LineCoverageSuiteFitness.
	 * </p>
	 */
	public LineCoverageSuiteFitness() {

		@SuppressWarnings("unused")
		String prefix = Properties.TARGET_CLASS_PREFIX;

		/* TODO: Would be nice to use a prefix here */
		lines = LinePool.getLines(Properties.TARGET_CLASS);
		
		logger.info("Total line coverage goals: " + lines);
		
		new MethodCoverageFactory().getCoverageGoals();
		List<LineCoverageTestFitness> goals = new LineCoverageFactory().getCoverageGoals();
		for (LineCoverageTestFitness goal : goals) {
			linesCoverageMap.put(goal.getLine(), goal);
		}
	}

	// Some stuff for debug output
	public int maxCoveredLines = 0;
	public double bestFitness = Double.MAX_VALUE;

	// Each test gets a set of distinct covered goals, these are mapped by line id
	private final Map<Integer, TestFitnessFunction> linesCoverageMap = new HashMap<Integer, TestFitnessFunction>();

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
			StatementInterface statement = result.test.getStatement(exceptionPosition);
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
	 * @param callCount
	 * @return
	 */
	private boolean analyzeTraces(List<ExecutionResult> results, Map<String, Integer> callCount) {
		boolean hasTimeoutOrTestException = false;

		for (ExecutionResult result : results) {
			if (result.hasTimeout() || result.hasTestException()) {
				hasTimeoutOrTestException = true;
			}

			for (Integer line : result.getTrace().getCoveredLines()) {
				if (linesCoverageMap.containsKey(line)) {
					result.test.addCoveredGoal(linesCoverageMap.get(line));
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
		Map<String, Integer> callCount = new HashMap<String, Integer>();
		Set<Integer> covered_lines = new HashSet<Integer>();

		// Collect stats in the traces 
		boolean hasTimeoutOrTestException = analyzeTraces(results, callCount);

		// In case there were exceptions in a constructor
		handleConstructorExceptions(results, callCount);

		for (ExecutionResult result : results) {
			covered_lines.addAll(result.getTrace().getCoveredLines());
		}

		int totalLines = lines.size();
		int coveredLines = covered_lines.size();
		
		logger.info("Covered " + coveredLines + " out of " + totalLines + " lines");
		fitness += normalize(totalLines - coveredLines);
		
		printStatusMessages(suite, coveredLines, fitness);

		if (totalLines > 0)
			suite.setCoverage(this, (double) coveredLines / (double) totalLines);
        else
            suite.setCoverage(this, 1.0);

		suite.setNumOfCoveredGoals(this, coveredLines);
		
		if (hasTimeoutOrTestException) {
			logger.info("Test suite has timed out, setting fitness to max value " + totalLines);
			fitness = totalLines;
			//suite.setCoverage(0.0);
		}

		updateIndividual(this, suite, fitness);

		assert (coveredLines <= totalLines) : "Covered " + coveredLines + " vs total goals " + totalLines;
		assert (fitness >= 0.0);
		assert (fitness != 0.0 || coveredLines == totalLines) : "Fitness: " + fitness + ", "
		        + "coverage: " + coveredLines + "/" + totalLines;
		assert (suite.getCoverage(this) <= 1.0) && (suite.getCoverage(this) >= 0.0) : "Wrong coverage value "
		        + suite.getCoverage(this);

		return fitness;
	}

	/**
	 * Some useful debug information
	 * 
	 * @param coveredLines
	 * @param fitness
	 */
	private void printStatusMessages(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite,
	        int coveredLines, double fitness) {
		if (coveredLines > maxCoveredLines) {
			maxCoveredLines = coveredLines;
			logger.info("(Lines) Best individual covers " + coveredLines + "/"
			        + lines + " lines");
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.totalLengthOfTestCases());
		}

		if (fitness < bestFitness) {
			logger.info("(Fitness) Best individual covers " + coveredLines + "/"
			        + lines + " lines");
			bestFitness = fitness;
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.totalLengthOfTestCases());

		}
	}

}
