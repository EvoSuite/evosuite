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
package org.evosuite.coverage.line;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.ga.archive.Archive;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.graphs.cfg.ControlDependency;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.TestChromosome;
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
public class LineCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = -6369027784777941998L;

	private final static Logger logger = LoggerFactory.getLogger(TestSuiteFitnessFunction.class);

	private final Map<Integer, TestFitnessFunction> lineGoals = new LinkedHashMap<Integer, TestFitnessFunction>();

	private final int numLines;

	// Some stuff for debug output
    private int maxCoveredLines = 0;
    private double bestFitness = Double.MAX_VALUE;

    private Set<Integer> branchesToCoverTrue  = new LinkedHashSet<Integer>();
    private Set<Integer> branchesToCoverFalse = new LinkedHashSet<Integer>();
    private Set<Integer> branchesToCoverBoth  = new LinkedHashSet<Integer>();

	public LineCoverageSuiteFitness() {
		@SuppressWarnings("unused")
		String prefix = Properties.TARGET_CLASS_PREFIX;

		/* TODO: Would be nice to use a prefix here */
//		for(String className : LinePool.getKnownClasses()) {
//			lines.addAll(LinePool.getLines(className));
//		}

		List<LineCoverageTestFitness> goals = new LineCoverageFactory().getCoverageGoals();
		for (LineCoverageTestFitness goal : goals) {
			lineGoals.put(goal.getLine(), goal);
			if(Properties.TEST_ARCHIVE)
				Archive.getArchiveInstance().addTarget(goal);
		}
		this.numLines = lineGoals.size();
		logger.info("Total line coverage goals: " + this.numLines);

		initializeControlDependencies();
	}

	@Override
	public boolean updateCoveredGoals() {
		if(!Properties.TEST_ARCHIVE)
			return false;

		// TODO as soon the archive refactor is done, we can get rid of this function

		return true;
	}
	
	/**
	 * Iterate over all execution results and summarize statistics
	 * 
	 * @param results
	 * @param callCount
	 * @return
	 */
	private boolean analyzeTraces(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite, List<ExecutionResult> results) {
		boolean hasTimeoutOrTestException = false;

		for (ExecutionResult result : results) {
			if (result.hasTimeout() || result.hasTestException()) {
				hasTimeoutOrTestException = true;
				continue;
			}

			Iterator<Entry<Integer, TestFitnessFunction>> it = this.lineGoals.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Integer, TestFitnessFunction> entry = it.next();
				TestFitnessFunction goal = entry.getValue();

				if (Archive.getArchiveInstance().hasSolution(goal)) {
					// Is it worth continue looking for other results (i.e., tests) that cover this already covered
					// goal? Maybe. However, as checking whether a test covers a specific goal and updating the
					// archive could be very time consuming tasks, in here we just skip the search for yet another
					// test case. The main objective (i.e., having a test case for this goal) has been completed
					// anyway.
					it.remove();
					continue;
				}

				TestChromosome tc = new TestChromosome();
				tc.setTestCase(result.test);
				double fit = goal.getFitness(tc, result);

				if (fit == 0.0) {
					it.remove();
					result.test.addCoveredGoal(goal);
				}

				if (Properties.TEST_ARCHIVE) {
					Archive.getArchiveInstance().updateArchive(goal, result, fit);
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

		if (this.allLinesCovered()) {
			updateIndividual(this, suite, 0.0);
			suite.setCoverage(this, 1.0);
			suite.setNumOfCoveredGoals(this, this.numLines);
			return 0.0;
		}

		List<ExecutionResult> results = runTestSuite(suite);
		fitness += getControlDependencyGuidance(results);
		logger.info("Branch distances: "+fitness);

		boolean hasTimeoutOrTestException = analyzeTraces(suite, results);

		int totalLines = this.numLines;
		int numCoveredLines = this.howManyLinesCovered();
		
		logger.debug("Covered " + numCoveredLines + " out of " + totalLines + " lines");
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
			        + this.numLines + " lines");
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.totalLengthOfTestCases());
		}

		if (fitness < bestFitness) {
			logger.info("(Fitness) Best individual covers " + coveredLines + "/"
			        + this.numLines + " lines");
			bestFitness = fitness;
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.totalLengthOfTestCases());

		}
	}
	
	/**
	 * Add guidance to the fitness function by including branch distances on
	 * all control dependencies
	 */
	private void initializeControlDependencies() {
		// In case we target more than one class (context, or inner classes) 
		Set<String> targetClasses = new LinkedHashSet<String>();
		for(TestFitnessFunction ff : lineGoals.values()) {
			targetClasses.add(ff.getTargetClass());
		}
		for(String className : targetClasses) {
			List<BytecodeInstruction> instructions = BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getInstructionsIn(className);
			if(instructions == null) {
				logger.info("No instructions known for class {} (is it an enum?)", className);
				continue;
			}
			for(BytecodeInstruction bi : instructions) {
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
		}
		branchesToCoverBoth.addAll(branchesToCoverTrue);
		branchesToCoverBoth.retainAll(branchesToCoverFalse);
		branchesToCoverTrue.removeAll(branchesToCoverBoth);
		branchesToCoverFalse.removeAll(branchesToCoverBoth);
		
		logger.info("Covering branches true: "+branchesToCoverTrue);
		logger.info("Covering branches false: "+branchesToCoverFalse);
		logger.info("Covering branches both: "+branchesToCoverBoth);
	}

	private double getControlDependencyGuidance(List<ExecutionResult> results) {
		Map<Integer, Integer> predicateCount = new LinkedHashMap<Integer, Integer>();
		Map<Integer, Double> trueDistance = new LinkedHashMap<Integer, Double>();
		Map<Integer, Double> falseDistance = new LinkedHashMap<Integer, Double>();

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

	private int howManyLinesCovered() {
	  return this.numLines - this.lineGoals.size();
	}

	private boolean allLinesCovered() {
	  return this.lineGoals.isEmpty();
	}

}
