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
package org.evosuite.coverage.line;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.coverage.archive.TestsArchive;
import org.evosuite.instrumentation.LinePool;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnlyLineCoverageSuiteFitness extends TestSuiteFitnessFunction {

	
	private static final long serialVersionUID = -6369027784777941998L;

	private final static Logger logger = LoggerFactory.getLogger(TestSuiteFitnessFunction.class);

	// Coverage targets
	public final Set<Integer> lines = new HashSet<Integer>();

	public final Set<Integer> removedLines = new HashSet<Integer>();

	public final Set<Integer> toRemoveLines = new HashSet<Integer>();

	public OnlyLineCoverageSuiteFitness() {
		@SuppressWarnings("unused")
		String prefix = Properties.TARGET_CLASS_PREFIX;

		/* TODO: Would be nice to use a prefix here */
		for(String className : LinePool.getKnownClasses()) {		
			lines.addAll(LinePool.getLines(className));
		}
		logger.info("Total line coverage goals: " + lines);

		List<LineCoverageTestFitness> goals = new LineCoverageFactory().getCoverageGoals();
		for (LineCoverageTestFitness goal : goals) {
			linesCoverageMap.put(goal.getLine(), goal);
			if(Properties.TEST_ARCHIVE)
				TestsArchive.instance.addGoalToCover(this, goal);
		}
	}
	
	// Some stuff for debug output
	public int maxCoveredLines = 0;
	public double bestFitness = Double.MAX_VALUE;

	// Each test gets a set of distinct covered goals, these are mapped by line id
	private final Map<Integer, TestFitnessFunction> linesCoverageMap = new HashMap<Integer, TestFitnessFunction>();


	@Override
	public boolean updateCoveredGoals() {
		if(!Properties.TEST_ARCHIVE)
			return false;
		
		for (Integer line : toRemoveLines) {
			boolean removed = lines.remove(line);
			TestFitnessFunction f = linesCoverageMap.remove(line);
			if (removed && f != null) {
				removedLines.add(line);
				//removeTestCall(f.getTargetClass(), f.getTargetMethod());
			} else {
				throw new IllegalStateException("goal to remove not found");
			}
		}

		toRemoveLines.clear();
		logger.info("Current state of archive: "+TestsArchive.instance.toString());
		
		return true;
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
					if(!lines.contains(line) || removedLines.contains(line)) 
						continue;
					
					result.test.addCoveredGoal(linesCoverageMap.get(line));
					if(Properties.TEST_ARCHIVE) {
						toRemoveLines.add(line);
						TestsArchive.instance.putTest(this, linesCoverageMap.get(line), result);
					}
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
		Set<Integer> coveredLines = new HashSet<Integer>();

		// Collect stats in the traces 
		boolean hasTimeoutOrTestException = analyzeTraces(results, callCount);

		for (ExecutionResult result : results) {
			for(Integer line : result.getTrace().getCoveredLines()) {
				if(!removedLines.contains(line))
					coveredLines.add(line);
			}
		}

		int totalLines = lines.size() + removedLines.size();
		int numCoveredLines = coveredLines.size() + removedLines.size();
		
		logger.debug("Covered " + numCoveredLines + " out of " + totalLines + " lines, "+removedLines.size() +" in archive");
		fitness += normalize(totalLines - numCoveredLines);
		
		printStatusMessages(suite, numCoveredLines, fitness);

		if (totalLines > 0)
			suite.setCoverage(this, (double) numCoveredLines / (double) totalLines);
        else
            suite.setCoverage(this, 1.0);

		suite.setNumOfCoveredGoals(this, numCoveredLines);
		
		if (hasTimeoutOrTestException) {
			logger.info("Test suite has timed out, setting fitness to max value " + totalLines);
			fitness = totalLines;
			//suite.setCoverage(0.0);
		}

		updateIndividual(this, suite, fitness);

		assert (numCoveredLines <= totalLines) : "Covered " + numCoveredLines + " vs total goals " + totalLines;
		assert (fitness >= 0.0);
		assert (fitness != 0.0 || numCoveredLines == totalLines) : "Fitness: " + fitness + ", "
		        + "coverage: " + numCoveredLines + "/" + totalLines;
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
