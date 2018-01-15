/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.coverage.branch;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.ga.archive.Archive;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fitness function for a whole test suite for all branches
 * 
 * @author Gordon Fraser, Jose Miguel Rojas
 */
public class OnlyBranchCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = 2991632394620406243L;

	private final static Logger logger = LoggerFactory.getLogger(TestSuiteFitnessFunction.class);

	// Coverage targets
	private final int totalBranches;
	private final Set<Integer> branchesId;
	private final Map<Integer, TestFitnessFunction> branchCoverageTrueMap = new LinkedHashMap<Integer, TestFitnessFunction>();
	private final Map<Integer, TestFitnessFunction> branchCoverageFalseMap = new LinkedHashMap<Integer, TestFitnessFunction>();

	private final int totalGoals;

	// Some stuff for debug output
	private int maxCoveredBranches = 0;
	private double bestFitness = Double.MAX_VALUE;

	/**
	 * <p>
	 * Constructor for OnlyBranchCoverageSuiteFitness.
	 * </p>
	 */
	public OnlyBranchCoverageSuiteFitness() {

		String prefix = Properties.TARGET_CLASS_PREFIX;
		
		if (prefix.isEmpty()) {
			prefix = Properties.TARGET_CLASS;
		}
		totalBranches = BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranchCountForPrefix(prefix);
		branchesId = new LinkedHashSet<>();

		totalGoals = 2 * totalBranches;

		logger.info("Total branch coverage goals: " + totalGoals);
		logger.info("Total branches: " + totalBranches);

		determineCoverageGoals();
		assert totalGoals == this.branchCoverageTrueMap.size() + this.branchCoverageFalseMap.size();
	}

	/**
	 * Initialize the set of known coverage goals
	 */
	private void determineCoverageGoals() {
		List<OnlyBranchCoverageTestFitness> goals = new OnlyBranchCoverageFactory().getCoverageGoals();
		for (OnlyBranchCoverageTestFitness goal : goals) {
			if(Properties.TEST_ARCHIVE)
				Archive.getArchiveInstance().addTarget(goal);

			branchesId.add(goal.getBranch().getActualBranchId());
			if (goal.getBranchExpressionValue())
				branchCoverageTrueMap.put(goal.getBranch().getActualBranchId(), goal);
			else
				branchCoverageFalseMap.put(goal.getBranch().getActualBranchId(), goal);
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
	private boolean analyzeTraces( AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite, List<ExecutionResult> results,
	        Map<Integer, Integer> predicateCount, 
	        Map<Integer, Double> trueDistance, Map<Integer, Double> falseDistance) {
		
		boolean hasTimeoutOrTestException = false;
		for (ExecutionResult result : results) {
			if (result.hasTimeout() || result.hasTestException()) {
				hasTimeoutOrTestException = true;
				continue;
			}

			for (Entry<Integer, Integer> entry : result.getTrace().getPredicateExecutionCount().entrySet()) {
				if (!branchesId.contains(entry.getKey())) {
					continue;
				}

				if (!predicateCount.containsKey(entry.getKey())) {
					predicateCount.put(entry.getKey(), entry.getValue());
				} else {
					predicateCount.put(entry.getKey(),
							predicateCount.get(entry.getKey())
							+ entry.getValue());
				}
			}

			for (Entry<Integer, Double> entry : result.getTrace().getTrueDistances().entrySet()) {
				Integer key = entry.getKey();
				if (!branchCoverageTrueMap.containsKey(key)) {
					continue;
				}

				if (!trueDistance.containsKey(key)) {
					trueDistance.put(key, entry.getValue());
				} else {
					trueDistance.put(key,
							Math.min(trueDistance.get(key),
									entry.getValue()));
				}

				OnlyBranchCoverageTestFitness goal = (OnlyBranchCoverageTestFitness) branchCoverageTrueMap.get(key);
				if ((Double.compare(entry.getValue(), 0.0) == 0)) {
					result.test.addCoveredGoal(goal);
					this.branchCoverageTrueMap.remove(key);
					if (!this.branchCoverageFalseMap.containsKey(key)) {
						this.branchesId.remove(key);
					}
				}

				if (Properties.TEST_ARCHIVE) {
					Archive.getArchiveInstance().updateArchive(goal, result, entry.getValue());
				}
			}

			for (Entry<Integer, Double> entry : result.getTrace().getFalseDistances().entrySet()) {
				Integer key = entry.getKey();
				if (!branchCoverageFalseMap.containsKey(key)) {
					continue;
				}

				if (!falseDistance.containsKey(key)) {
					falseDistance.put(key, entry.getValue());
				} else {
					falseDistance.put(key,
							Math.min(falseDistance.get(key),
									entry.getValue()));
				}

				OnlyBranchCoverageTestFitness goal = (OnlyBranchCoverageTestFitness) branchCoverageFalseMap.get(key);
				if ((Double.compare(entry.getValue(), 0.0) == 0)) {
					result.test.addCoveredGoal(goal);
					this.branchCoverageFalseMap.remove(key);
					if (!this.branchCoverageTrueMap.containsKey(key)) {
						this.branchesId.remove(key);
					}
				}

				if (Properties.TEST_ARCHIVE) {
					Archive.getArchiveInstance().updateArchive(goal, result, entry.getValue());
				}
			}
		}
		return hasTimeoutOrTestException;
	}
	
	@Override
	public boolean updateCoveredGoals() {
		
		if(!Properties.TEST_ARCHIVE)
			return false;

		// TODO as soon the archive refactor is done, we can get rid of this function

		return true;
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
		Map<Integer, Double> trueDistance = new LinkedHashMap<Integer, Double>();
		Map<Integer, Double> falseDistance = new LinkedHashMap<Integer, Double>();
		Map<Integer, Integer> predicateCount = new LinkedHashMap<Integer, Integer>();

		// Collect stats in the traces 
		boolean hasTimeoutOrTestException = analyzeTraces(suite, results, predicateCount,
		                                                  trueDistance,
		                                                  falseDistance);

		// Collect branch distances of covered branches
		int numCoveredBranches = 0;

		for (Integer key : predicateCount.keySet()) {
			int numExecuted = predicateCount.get(key);
			if (!this.branchCoverageFalseMap.containsKey(key)) {
				numExecuted++;
			}
			if (!this.branchCoverageTrueMap.containsKey(key)) {
				numExecuted++;
			}

			if (numExecuted == 1) {
			// Note that a predicate must be executed at least twice, because the true
			// and false evaluations of the predicate need to be cover; if the predicate
			// were only executed once, then the search could theoretically oscillate
			// between true and false.
			fitness += 1.0;
			} else {
					double df = falseDistance.containsKey(key) ? normalize(falseDistance.get(key)) : 0.0;
					double dt = trueDistance.containsKey(key) ? normalize(trueDistance.get(key)) : 0.0;
					fitness += df + dt;
			}
		}

		// for every branch that has not been executed, we add +1 for the true evaluation and
		// +1 for the false evaluation (if each one has not been covered yet)
		for (Integer key : this.branchesId) {
				if (predicateCount.containsKey(key)) {
					// it has been executed
					continue;
				}

				if (this.branchCoverageFalseMap.containsKey(key)) {
					fitness += 1.0;
				}
				if (this.branchCoverageTrueMap.containsKey(key)) {
					fitness += 1.0;
				}
		}

		printStatusMessages(suite, numCoveredBranches, fitness);

		// Calculate coverage
		int coverage = 0;
		coverage += this.howManyTrueBranchesCovered();
		coverage += this.howManyFalseBranchesCovered();

		if (totalGoals > 0)
			suite.setCoverage(this, (double) coverage / (double) totalGoals);
        else
            suite.setCoverage(this, 1);

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
	 * @param coveredMethods
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

	private int howManyTrueBranchesCovered() {
		return this.totalBranches - this.branchCoverageTrueMap.size();
	}

	private int howManyFalseBranchesCovered() {
		return this.totalBranches - this.branchCoverageFalseMap.size();
	}

}
