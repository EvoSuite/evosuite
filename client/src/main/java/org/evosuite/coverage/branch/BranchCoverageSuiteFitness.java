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
import org.evosuite.graphs.cfg.CFGMethodAdapter;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.Statement;
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

	// branch targets
	private final int numBranches;
	public final Set<Integer> branchesId;
	public final Map<Integer, TestFitnessFunction> branchCoverageTrueMap = new LinkedHashMap<Integer, TestFitnessFunction>();
	public final Map<Integer, TestFitnessFunction> branchCoverageFalseMap = new LinkedHashMap<Integer, TestFitnessFunction>();

	// method targets
	public final int numMethods;
	private final Set<String> methods;
	private final int numBranchlessMethods;
	private final Map<String, TestFitnessFunction> branchlessMethodCoverageMap = new LinkedHashMap<String, TestFitnessFunction>();

	public final int numGoals;

	// Some stuff for debug output
	private int maxCoveredBranches = 0;
	private int maxCoveredMethods = 0;
	private double bestFitness = Double.MAX_VALUE;

	// Total coverage value, used by Regression
	public double totalCovered = 0.0;

	/**
	 * <p>
	 * Constructor for BranchCoverageSuiteFitness.
	 * </p>
	 */
	public BranchCoverageSuiteFitness() {
		this(TestGenerationContext.getInstance().getClassLoaderForSUT());
	}

	/**
	 * <p>
	 * Constructor for BranchCoverageSuiteFitness.
	 * </p>
	 */
	public BranchCoverageSuiteFitness(ClassLoader classLoader) {
		String prefix = Properties.TARGET_CLASS_PREFIX;

		if (prefix.isEmpty())
			prefix = Properties.TARGET_CLASS;

		branchesId = new LinkedHashSet<>();
		numBranches = BranchPool.getInstance(classLoader).getBranchCountForPrefix(prefix);
		numBranchlessMethods = BranchPool.getInstance(classLoader).getNumBranchlessMethodsPrefix(prefix);

		numMethods = CFGMethodAdapter.getNumMethodsPrefix(classLoader, prefix);
		methods = CFGMethodAdapter.getMethodsPrefix(classLoader, prefix);

		determineCoverageGoals();
		assert branchCoverageTrueMap.size() == branchCoverageFalseMap.size() :
			"number of true branches (" + branchCoverageTrueMap.size() +
			") is not equal to the number of false branches (" + branchCoverageFalseMap.size() + ")";
		assert branchesId.size() == branchCoverageTrueMap.size() :
			branchesId.size() + " is not equal to number of true/false branches (" + branchCoverageTrueMap.size() + ")";
		assert branchesId.size() == numBranches :
			branchesId.size() + " not equal to " + numBranches;
		assert numBranchlessMethods == branchlessMethodCoverageMap.size() :
			numBranchlessMethods + " not equal to " + branchlessMethodCoverageMap.size();

		numGoals = branchCoverageTrueMap.size() + branchCoverageFalseMap.size() + numBranchlessMethods;

		logger.info("Total branch coverage goals: " + numGoals);
		logger.info("Total branches: " + numBranches);
		logger.info("Total branchless methods: " + numBranchlessMethods);
		logger.info("Total methods: " + numMethods + ": " + methods);
	}

	/**
	 * Initialize the set of known coverage goals
	 */
	protected void determineCoverageGoals() {
		List<BranchCoverageTestFitness> goals = new BranchCoverageFactory().getCoverageGoals();
		for (BranchCoverageTestFitness goal : goals) {
			// Skip instrumented branches - we only want real branches
			if(goal.getBranch() != null) {
				if(goal.getBranch().isInstrumented()) {
					continue;
				}
			}
			if(Properties.TEST_ARCHIVE)
				Archive.getArchiveInstance().addTarget(goal);
			
			if (goal.getBranch() == null) {
				branchlessMethodCoverageMap.put(goal.getClassName() + "."
				                                        + goal.getMethod(), goal);
			} else {
				branchesId.add(goal.getBranch().getActualBranchId());
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
	private void handleConstructorExceptions(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite, List<ExecutionResult> results) {

		for (ExecutionResult result : results) {
			if (result.hasTimeout() || result.hasTestException() || result.noThrownExceptions()) {
				continue;
			}

			Integer exceptionPosition = result.getFirstPositionOfThrownException();
			// TODO: Not sure why that can happen
			if (exceptionPosition >= result.test.size()) {
				continue;
			}

			Statement statement = null;
			if (result.test.hasStatement(exceptionPosition)) {
				statement = result.test.getStatement(exceptionPosition);
			}
			if (statement instanceof ConstructorStatement) {
				ConstructorStatement c = (ConstructorStatement) statement;
				String className = c.getConstructor().getName();
				String methodName = "<init>" + Type.getConstructorDescriptor(c.getConstructor().getConstructor());
				String name = className + "." + methodName;

				TestFitnessFunction goal = branchlessMethodCoverageMap.get(name);
				if (goal == null) {
					continue;
				}

				result.test.addCoveredGoal(goal);
				branchlessMethodCoverageMap.remove(name); // branchless constructor method has been covered
				methods.remove(name);

				if (Properties.TEST_ARCHIVE) {
					Archive.getArchiveInstance().updateArchive(goal, result, 0.0);
				}
			}
		}
	}

	protected void handleBranchlessMethods(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite, ExecutionResult result) {
		for (Entry<String, Integer> entry : result.getTrace().getMethodExecutionCount().entrySet()) {

			String key = entry.getKey();
			if (key == null || !branchlessMethodCoverageMap.containsKey(key)) {
				continue;
			}

			TestFitnessFunction goal = branchlessMethodCoverageMap.get(key);
			result.test.addCoveredGoal(goal);

			branchlessMethodCoverageMap.remove(key); // branchless method has been covered
			methods.remove(key);

			if (Properties.TEST_ARCHIVE) {
				Archive.getArchiveInstance().updateArchive(goal, result, 0.0);
			}
		}
	}

	protected void handlePredicateCount(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite, ExecutionResult result, Map<Integer, Integer> predicateCount) {
		for (Entry<Integer, Integer> entry : result.getTrace().getPredicateExecutionCount().entrySet()) {

			Integer key = entry.getKey();
			if (!branchesId.contains(key)) {
				continue;
			}

			if (!predicateCount.containsKey(key)) {
				predicateCount.put(key, entry.getValue());
			} else {
				predicateCount.put(key, predicateCount.get(key) + entry.getValue());
			}
		}
	}

	protected void handleTrueDistances(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite, ExecutionResult result, Map<Integer, Double> trueDistance) {
		for (Entry<Integer, Double> entry : result.getTrace().getTrueDistances().entrySet()) {

			Integer key = entry.getKey();
			if (!this.branchCoverageTrueMap.containsKey(key)) {
				continue;
			}

			if (!trueDistance.containsKey(key)) {
				trueDistance.put(key, entry.getValue());
			} else {
				trueDistance.put(key, Math.min(trueDistance.get(key), entry.getValue()));
			}

			BranchCoverageTestFitness goal = (BranchCoverageTestFitness) this.branchCoverageTrueMap.get(key);
			assert goal != null;
			if ((Double.compare(entry.getValue(), 0.0) == 0)) {
				result.test.addCoveredGoal(goal);

				this.branchCoverageTrueMap.remove(key);
				if (!this.branchCoverageFalseMap.containsKey(key)) {
					this.branchesId.remove(key);
				}

				methods.remove(goal.getClassName() + "." + goal.getMethod());
			}

			if (Properties.TEST_ARCHIVE) {
				Archive.getArchiveInstance().updateArchive(goal, result, entry.getValue());
			}
		}

	}

	protected void handleFalseDistances(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite, ExecutionResult result, Map<Integer, Double> falseDistance) {
		for (Entry<Integer, Double> entry : result.getTrace().getFalseDistances().entrySet()) {

			Integer key = entry.getKey();
			if (!this.branchCoverageFalseMap.containsKey(key)) {
				continue;
			}

			if (!falseDistance.containsKey(key)) {
				falseDistance.put(key, entry.getValue());
			} else {
				falseDistance.put(key, Math.min(falseDistance.get(key), entry.getValue()));
			}

			BranchCoverageTestFitness goal = (BranchCoverageTestFitness) this.branchCoverageFalseMap.get(key);
			assert goal != null;
			if ((Double.compare(entry.getValue(), 0.0) == 0)) {
				result.test.addCoveredGoal(goal);

				this.branchCoverageFalseMap.remove(key);
				if (!this.branchCoverageTrueMap.containsKey(key)) {
					this.branchesId.remove(key);
				}

				methods.remove(goal.getClassName() + "." + goal.getMethod());
			}

			if (Properties.TEST_ARCHIVE) {
				Archive.getArchiveInstance().updateArchive(goal, result, entry.getValue());
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
	private boolean analyzeTraces(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite, List<ExecutionResult> results,
	        Map<Integer, Integer> predicateCount,
	        Map<Integer, Double> trueDistance, Map<Integer, Double> falseDistance) {
		boolean hasTimeoutOrTestException = false;
		for (ExecutionResult result : results) {
			if (result.hasTimeout() || result.hasTestException()) {
				hasTimeoutOrTestException = true;
				continue;
			}

			handleBranchlessMethods(suite, result);
			handlePredicateCount(suite, result, predicateCount);
			handleTrueDistances(suite, result, trueDistance);
			handleFalseDistances(suite, result, falseDistance);
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
	public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		logger.trace("Calculating branch fitness");
		double fitness = 0.0;

		List<ExecutionResult> results = runTestSuite(suite);
		Map<Integer, Double> trueDistance = new LinkedHashMap<Integer, Double>();
		Map<Integer, Double> falseDistance = new LinkedHashMap<Integer, Double>();
		Map<Integer, Integer> predicateCount = new LinkedHashMap<Integer, Integer>();

		// Collect stats in the traces 
		boolean hasTimeoutOrTestException = analyzeTraces(suite, results, predicateCount,
		                                                  trueDistance, falseDistance);
		// In case there were exceptions in a constructor
		handleConstructorExceptions(suite, results);

		// Collect branch distances of executed branches
		for (Integer key : predicateCount.keySet()) {
			// If the branch predicate was only executed once, then add 1
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

		// Ensure all methods are called
		int missingMethods = this.numMethods - this.howManyMethodsCovered();
		fitness += 1.0 * missingMethods;

		// Calculate coverage
		int coverage = 0;
		coverage += this.howManyTrueBranchesCovered();
		coverage += this.howManyFalseBranchesCovered();
		coverage += this.howManyBranchlessMethodsCovered();

		printStatusMessages(suite, coverage, numMethods - missingMethods, fitness);

		if (numGoals > 0)
			suite.setCoverage(this, (double) coverage / (double) numGoals);
		else 
            suite.setCoverage(this, 1);

		totalCovered = suite.getCoverage(this);

		suite.setNumOfCoveredGoals(this, coverage);
		suite.setNumOfNotCoveredGoals(this, numGoals - coverage);

		if (hasTimeoutOrTestException) {
			logger.info("Test suite has timed out, setting fitness to max value "
			        + (numBranches * 2 + numMethods));
			fitness = numBranches * 2 + numMethods;
			//suite.setCoverage(0.0);
		}

		updateIndividual(this, suite, fitness);

		assert (coverage <= numGoals) : "Covered " + coverage + " vs total goals "
		        + numGoals;
		assert (fitness >= 0.0);
		assert (fitness != 0.0 || coverage == numGoals) : "Fitness: " + fitness + ", "
		        + "coverage: " + coverage + "/" + numGoals;
		assert (suite.getCoverage(this) <= 1.0) && (suite.getCoverage(this) >= 0.0) : "Wrong coverage value "
		        + suite.getCoverage(this); 
		return fitness;
	}
	

	/*
	 * Max branch coverage value
	 */
	public int getMaxValue() {
		return  numBranches * 2 + numMethods;
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
			        + (numBranches * 2) + " branches and " + coveredMethods + "/"
			        + numMethods + " methods");
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.totalLengthOfTestCases());
		}
		if (coveredMethods > maxCoveredMethods) {
			logger.info("(Methods) Best individual covers " + coveredBranches + "/"
			        + (numBranches * 2) + " branches and " + coveredMethods + "/"
			        + numMethods + " methods");
			maxCoveredMethods = coveredMethods;
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.totalLengthOfTestCases());
		}
		if (fitness < bestFitness) {
			logger.info("(Fitness) Best individual covers " + coveredBranches + "/"
			        + (numBranches * 2) + " branches and " + coveredMethods + "/"
			        + numMethods + " methods");
			bestFitness = fitness;
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.totalLengthOfTestCases());
		}
	}

	private int howManyMethodsCovered() {
		return this.numMethods - this.methods.size();
		}

	private int howManyBranchlessMethodsCovered() {
		return this.numBranchlessMethods - this.branchlessMethodCoverageMap.size();
		}

	private int howManyTrueBranchesCovered() {
		return this.numBranches - this.branchCoverageTrueMap.size();
		}

	private int howManyFalseBranchesCovered() {
		return this.numBranches - this.branchCoverageFalseMap.size();
	}

}
