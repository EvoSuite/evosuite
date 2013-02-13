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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.coverage.lcsaj.LCSAJPool;
import org.evosuite.graphs.cfg.CFGMethodAdapter;
import org.evosuite.javaagent.LinePool;
import org.evosuite.rmi.ClientServices;
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
 * @author Gordon Fraser
 */
public class BranchCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = 2991632394620406243L;

	private final static Logger logger = LoggerFactory.getLogger(TestSuiteFitnessFunction.class);

	// Coverage targets
	public final int totalMethods;
	public final int totalBranches;
	public final int totalGoals;
	public final int numBranchlessMethods;
	public final Set<Integer> lines;
	private final Set<String> branchlessMethods;
	private final Set<String> methods;

	/**
	 * <p>
	 * Constructor for BranchCoverageSuiteFitness.
	 * </p>
	 */
	public BranchCoverageSuiteFitness() {

		String prefix = Properties.TARGET_CLASS_PREFIX;

		if (prefix.isEmpty()) {
			prefix = Properties.TARGET_CLASS;
			totalMethods = CFGMethodAdapter.getNumMethods();
			totalBranches = BranchPool.getBranchCounter();
			numBranchlessMethods = BranchPool.getNumBranchlessMethods();
			branchlessMethods = BranchPool.getBranchlessMethods();
			methods = CFGMethodAdapter.getMethods();

		} else {
			totalMethods = CFGMethodAdapter.getNumMethodsPrefix(prefix);
			totalBranches = BranchPool.getBranchCountForPrefix(prefix);
			numBranchlessMethods = BranchPool.getNumBranchlessMethodsPrefix(prefix);
			branchlessMethods = BranchPool.getBranchlessMethodsPrefix(prefix);
			methods = CFGMethodAdapter.getMethodsPrefix(Properties.TARGET_CLASS_PREFIX);
		}

		/* TODO: Would be nice to use a prefix here */
		lines = LinePool.getLines(Properties.TARGET_CLASS);

		totalGoals = 2 * totalBranches + numBranchlessMethods;

		logger.info("Total branch coverage goals: " + totalGoals);
		logger.info("Total branches: " + totalBranches);
		logger.info("Total branchless methods: " + numBranchlessMethods);
		logger.info("Total methods: " + totalMethods + ": " + methods);

		determineCoverageGoals();
		ClientServices.getInstance().getClientNode().trackOutputVariable("total_branchgoals", totalGoals);
	}

	// Some stuff for debug output
	public int maxCoveredBranches = 0;
	public int maxCoveredMethods = 0;
	public double bestFitness = Double.MAX_VALUE;

	// Each test gets a set of distinct covered goals, these are mapped by branch id
	private final Map<Integer, TestFitnessFunction> branchCoverageTrueMap = new HashMap<Integer, TestFitnessFunction>();
	private final Map<Integer, TestFitnessFunction> branchCoverageFalseMap = new HashMap<Integer, TestFitnessFunction>();
	private final Map<String, TestFitnessFunction> branchlessMethodCoverageMap = new HashMap<String, TestFitnessFunction>();

	/**
	 * Initialize the set of known coverage goals
	 */
	private void determineCoverageGoals() {
		List<BranchCoverageTestFitness> goals = new BranchCoverageFactory().getCoverageGoals();
		for (BranchCoverageTestFitness goal : goals) {
			if (goal.getBranch() == null) {
				branchlessMethodCoverageMap.put(goal.getClassName() + "."
				                                        + goal.getMethod(), goal);
			} else {
				if (goal.getBranchExpressionValue())
					branchCoverageTrueMap.put(goal.getBranch().getActualBranchId(), goal);
				else
					branchCoverageFalseMap.put(goal.getBranch().getActualBranchId(), goal);
			}
		}
	}

	/**
	 * If there is an exception in a superconstructor, then the corresponding 
	 * constructor might not be included in the execution trace
	 *  
	 * @param results
	 * @param callCount
	 */
	private void handleConstructorExceptions(List<ExecutionResult> results, Map<String, Integer> callCount) {
		
		for (ExecutionResult result : results) {
			if (result.hasTimeout() || result.hasTestException() || result.noThrownExceptions())
				continue;
			
			Integer exceptionPosition = result.getFirstPositionOfThrownException();
			StatementInterface statement = result.test.getStatement(exceptionPosition);
			if(statement instanceof ConstructorStatement) {
				ConstructorStatement c = (ConstructorStatement)statement;
				String className = c.getConstructor().getName();
				String methodName = "<init>"+Type.getConstructorDescriptor(c.getConstructor());
				String name = className + "." + methodName;
				if(!callCount.containsKey(name)) {
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
				if (branchlessMethodCoverageMap.containsKey(entry.getKey())) {
					result.test.addCoveredGoal(branchlessMethodCoverageMap.get(entry.getKey()));
				}

			}
			for (Entry<Integer, Integer> entry : result.getTrace().getPredicateExecutionCount().entrySet()) {
				if (!LCSAJPool.isLCSAJBranch(BranchPool.getBranch(entry.getKey()))) {
					if (!predicateCount.containsKey(entry.getKey()))
						predicateCount.put(entry.getKey(), entry.getValue());
					else {
						predicateCount.put(entry.getKey(),
						                   predicateCount.get(entry.getKey())
						                           + entry.getValue());
					}
				}
			}
			for (Entry<Integer, Double> entry : result.getTrace().getTrueDistances().entrySet()) {
				if (!LCSAJPool.isLCSAJBranch(BranchPool.getBranch(entry.getKey()))) {
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
			}
			for (Entry<Integer, Double> entry : result.getTrace().getFalseDistances().entrySet()) {
				if (!LCSAJPool.isLCSAJBranch(BranchPool.getBranch(entry.getKey()))) {
					if (!falseDistance.containsKey(entry.getKey()))
						falseDistance.put(entry.getKey(), entry.getValue());
					else {
						falseDistance.put(entry.getKey(),
						                  Math.min(falseDistance.get(entry.getKey()),
						                           entry.getValue()));
					}
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
		Set<Integer> covered_lines = new HashSet<Integer>();

		// Collect stats in the traces 
		boolean hasTimeoutOrTestException = analyzeTraces(results, predicateCount,
		                                                  callCount, trueDistance,
		                                                  falseDistance);
		
		// In case there were exceptions in a constructor
		handleConstructorExceptions(results, callCount);
		
		// Add requirement on statements
		if (Properties.BRANCH_STATEMENT) {
			for (ExecutionResult result : results) {
				for (Map<String, Map<Integer, Integer>> coverage : result.getTrace().getCoverageData().values()) {
					for (Map<Integer, Integer> coveredLines : coverage.values())
						covered_lines.addAll(coveredLines.keySet());
				}
			}
		}

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

		// Ensure all methods are called
		int missingMethods = 0;
		for (String e : methods) {
			if (!callCount.containsKey(e)) {
				fitness += 1.0;
				missingMethods += 1;
			}
		}

		// Add statement information
		if (Properties.BRANCH_STATEMENT) {
			int totalLines = lines.size();
			logger.info("Covered " + covered_lines.size() + " out of " + totalLines
			        + " lines");
			fitness += normalize(totalLines - covered_lines.size());
		}

		printStatusMessages(suite, numCoveredBranches, totalMethods - missingMethods,
		                    fitness);

		// Calculate coverage
		int coverage = numCoveredBranches;
		for (String e : branchlessMethods) {
			if (callCount.keySet().contains(e)) {
				coverage++;
			}

		}

		if (totalGoals > 0)
			suite.setCoverage((double) coverage / (double) totalGoals);

		suite.setNumOfCoveredGoals(coverage);
		

		if (hasTimeoutOrTestException) {
			logger.info("Test suite has timed out, setting fitness to max value "
			        + (totalBranches * 2 + totalMethods));
			fitness = totalBranches * 2 + totalMethods;
			//suite.setCoverage(0.0);
		}

		updateIndividual(suite, fitness);

		assert (coverage <= totalGoals) : "Covered " + coverage + " vs total goals "
		        + totalGoals;
		assert (fitness >= 0.0);
		assert (fitness != 0.0 || coverage == totalGoals) : "Fitness: " + fitness + ", "
		        + "coverage: " + coverage + "/" + totalGoals;
		assert (suite.getCoverage() <= 1.0) && (suite.getCoverage() >= 0.0) : "Wrong coverage value "
		        + suite.getCoverage();

		return fitness;
	}

	/**
	 * Some useful debug information
	 * 
	 * @param coveredBranches
	 * @param coveredMethods
	 * @param fitness
	 */
	private void printStatusMessages(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite,
	        int coveredBranches, int coveredMethods, double fitness) {
		if (coveredBranches > maxCoveredBranches) {
			maxCoveredBranches = coveredBranches;
			logger.info("(Branches) Best individual covers " + coveredBranches + "/"
			        + (totalBranches * 2) + " branches and " + coveredMethods + "/"
			        + totalMethods + " methods");
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.totalLengthOfTestCases());
		}
		if (coveredMethods > maxCoveredMethods) {
			logger.info("(Methods) Best individual covers " + coveredBranches + "/"
			        + (totalBranches * 2) + " branches and " + coveredMethods + "/"
			        + totalMethods + " methods");
			maxCoveredMethods = coveredMethods;
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.totalLengthOfTestCases());

		}
		if (fitness < bestFitness) {
			logger.info("(Fitness) Best individual covers " + coveredBranches + "/"
			        + (totalBranches * 2) + " branches and " + coveredMethods + "/"
			        + totalMethods + " methods");
			bestFitness = fitness;
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.totalLengthOfTestCases());

		}
	}

}
