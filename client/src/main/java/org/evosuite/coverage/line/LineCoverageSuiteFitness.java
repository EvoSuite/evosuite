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
import java.util.Map.Entry;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.method.MethodCoverageFactory;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.graphs.cfg.ControlDependency;
import org.evosuite.instrumentation.LinePool;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.Statement;
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
public class LineCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = 7075862615475014070L;

	private final static Logger logger = LoggerFactory.getLogger(TestSuiteFitnessFunction.class);

	// Coverage targets
	public final Set<Integer> lines = new HashSet<Integer>();

	/**
	 * <p>
	 * Constructor for LineCoverageSuiteFitness.
	 * </p>
	 */
	public LineCoverageSuiteFitness() {

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
		}
		
		initializeControlDependencies();
	}
	
	private Set<Integer> branchesToCoverTrue  = new HashSet<Integer>();
	private Set<Integer> branchesToCoverFalse = new HashSet<Integer>();
	private Set<Integer> branchesToCoverBoth  = new HashSet<Integer>();
	
	/**
	 * Add guidance to the fitness function by including branch distances on
	 * all control dependencies
	 */
	private void initializeControlDependencies() {
		for(BytecodeInstruction bi : BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getAllInstructions()) {
			if(bi.getBasicBlock() == null) {
				// Labels get no basic block. TODO - why?
				continue;
			}
			for(ControlDependency cd : bi.getControlDependencies()) {
				if(cd.getBranchExpressionValue()) {
					branchesToCoverTrue.add(cd.getBranch().getActualBranchId());
				} else {
					branchesToCoverFalse.add(cd.getBranch().getActualBranchId());
				}
			}
		}
		branchesToCoverBoth.addAll(branchesToCoverTrue);
		branchesToCoverBoth.retainAll(branchesToCoverFalse);
		branchesToCoverTrue.removeAll(branchesToCoverBoth);
		branchesToCoverFalse.removeAll(branchesToCoverBoth);
		
		logger.info("Covering branches true: "+branchesToCoverTrue);
		logger.info("Covering branches false: "+branchesToCoverFalse);
		logger.info("Covering branches both: "+branchesToCoverBoth);
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

	private double getControlDependencyGuidance(List<ExecutionResult> results) {
		Map<Integer, Integer> predicateCount = new HashMap<Integer, Integer>();
		Map<Integer, Double> trueDistance = new HashMap<Integer, Double>();
		Map<Integer, Double> falseDistance = new HashMap<Integer, Double>();

		for (ExecutionResult result : results) {
			if (result.hasTimeout() || result.hasTestException()) {
				continue;
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
			}
			for (Entry<Integer, Double> entry : result.getTrace().getFalseDistances().entrySet()) {
				if (!falseDistance.containsKey(entry.getKey()))
					falseDistance.put(entry.getKey(), entry.getValue());
				else {
					falseDistance.put(entry.getKey(),
							Math.min(falseDistance.get(entry.getKey()),
									entry.getValue()));
				}
			}
		}
		
		double distance = 0.0;

		for(Integer branchId : branchesToCoverBoth) {
			if(!predicateCount.containsKey(branchId)) {
				distance += 2.0;
			} else if(predicateCount.get(branchId) == 1) {
				distance += 1.0;
			} else {
				distance += normalize(trueDistance.get(branchId));
				distance += normalize(falseDistance.get(branchId));
			}
		}
		
		for(Integer branchId : branchesToCoverTrue) {
			if(!trueDistance.containsKey(branchId)) {
				distance += 1;
			} else {
				distance += normalize(trueDistance.get(branchId));
			}
		}

		for(Integer branchId : branchesToCoverFalse) {
			if(!falseDistance.containsKey(branchId)) {
				distance += 1;
			} else {
				distance += normalize(falseDistance.get(branchId));
			}
		}
		
		return distance;
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
		fitness += getControlDependencyGuidance(results);
		logger.info("Branch distances: "+fitness);
		
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
