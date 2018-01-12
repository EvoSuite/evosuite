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
package org.evosuite.coverage.method;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.evosuite.Properties;
import org.evosuite.ga.archive.Archive;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.TestChromosome;
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
 * Fitness function for a whole test suite for all methods considering only normal behaviour (no exceptions)
 *
 * @author Gordon Fraser, Jose Miguel Rojas
 */
public class MethodCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = 3359321076367091582L;

	private final static Logger logger = LoggerFactory.getLogger(TestSuiteFitnessFunction.class);

	// Each test gets a set of distinct covered goals, these are mapped by branch id
	protected final Map<String, TestFitnessFunction> methodCoverageMap = new LinkedHashMap<String, TestFitnessFunction>();

	protected final int totalMethods;

    // Some stuff for debug output
    protected int maxCoveredMethods = 0;
    protected double bestFitness = Double.MAX_VALUE;

    /**
	 * <p>
	 * Constructor for MethodCoverageSuiteFitness.
	 * </p>
	 */
	public MethodCoverageSuiteFitness() {
		determineCoverageGoals();
		totalMethods = methodCoverageMap.size();
		logger.info("Total methods: " + totalMethods);
	}

	/**
	 * Initialize the set of known coverage goals
	 */
	protected void determineCoverageGoals() {
		List<MethodCoverageTestFitness> goals = new MethodCoverageFactory().getCoverageGoals();
		for (MethodCoverageTestFitness goal : goals) {
            methodCoverageMap.put(goal.getClassName() + "." + goal.getMethod(), goal);
			if(Properties.TEST_ARCHIVE)
				Archive.getArchiveInstance().addTarget(goal);
		}
	}

	/**
	 * If there is an exception in a super-constructor, then the corresponding
	 * constructor might not be included in the execution trace
	 *
	 * @param results
	 * @param calledMethods
	 */
	protected void handleConstructorExceptions(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite,
			List<ExecutionResult> results) {

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
				if (methodCoverageMap.containsKey(name)) {
					TestFitnessFunction goal = methodCoverageMap.get(name);

					// only include methods being called
					result.test.addCoveredGoal(goal);
					this.methodCoverageMap.remove(name);

					if (Properties.TEST_ARCHIVE) {
						Archive.getArchiveInstance().updateArchive(goal, result, 0.0);
					}
				}
			}

		}
	}

	/**
	 * Iterate over all execution results and summarize statistics
	 *
	 * @param results
	 * @param calledMethods
	 * @return
	 */
	protected boolean analyzeTraces(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite,
			List<ExecutionResult> results) {
		boolean hasTimeoutOrTestException = false;

		for (ExecutionResult result : results) {
			if (result.hasTimeout() || result.hasTestException()) {
				hasTimeoutOrTestException = true;
			}

			Iterator<Entry<String, TestFitnessFunction>> it = this.methodCoverageMap.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, TestFitnessFunction> entry = it.next();
				TestFitnessFunction goal = entry.getValue();

				if (Archive.getArchiveInstance().hasSolution(goal)) {
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
		logger.trace("Calculating method fitness");
		double fitness = 0.0;

		if (this.allMethodsCovered()) {
			updateIndividual(this, suite, 0.0);
			suite.setCoverage(this, 1.0);
			suite.setNumOfCoveredGoals(this, this.totalMethods);
			return 0.0;
		}

		List<ExecutionResult> results = runTestSuite(suite);

		// Collect stats in the traces
		boolean hasTimeoutOrTestException = analyzeTraces(suite, results);

		// In case there were exceptions in a constructor
		handleConstructorExceptions(suite, results);

		int coveredMethods = this.howManyMethodsCovered();
		int missingMethods = this.totalMethods - coveredMethods;
		fitness = 1.0 * missingMethods;

        printStatusMessages(suite, totalMethods - missingMethods, fitness);

		if (totalMethods > 0)
			suite.setCoverage(this, (double) coveredMethods / (double) totalMethods);
        else
            suite.setCoverage(this, 1.0);

		suite.setNumOfCoveredGoals(this, coveredMethods);

		if (hasTimeoutOrTestException) {
			logger.info("Test suite has timed out, setting fitness to max value " + totalMethods);
			fitness = totalMethods;
			//suite.setCoverage(0.0);
		}

		updateIndividual(this, suite, fitness);

		assert (coveredMethods <= totalMethods) : "Covered " + coveredMethods + " vs total goals " + totalMethods;
		assert (fitness >= 0.0);
		assert (fitness != 0.0 || coveredMethods == totalMethods) : "Fitness: " + fitness + ", "
		        + "coverage: " + coveredMethods + "/" + totalMethods;
		assert (suite.getCoverage(this) <= 1.0) && (suite.getCoverage(this) >= 0.0) : "Wrong coverage value "
		        + suite.getCoverage(this);

		return fitness;
	}

	/**
	 * Some useful debug information
	 *
	 * @param coveredMethods
	 * @param fitness
	 */
	protected void printStatusMessages(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite,
	        int coveredMethods, double fitness) {
		if (coveredMethods > maxCoveredMethods) {
			logger.info("(Methods) Best individual covers " + coveredMethods + "/"
			        + totalMethods + " methods");
			maxCoveredMethods = coveredMethods;
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.totalLengthOfTestCases());

		}
		if (fitness < bestFitness) {
			logger.info("(Fitness) Best individual covers " + coveredMethods + "/"
			        + totalMethods + " methods");
			bestFitness = fitness;
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.totalLengthOfTestCases());

		}
	}

	@Override
	public boolean updateCoveredGoals() {

		if(!Properties.TEST_ARCHIVE)
			return false;

		// TODO as soon the archive refactor is done, we can get rid of this function

		return true;
	}

	private int howManyMethodsCovered() {
		return this.totalMethods - this.methodCoverageMap.size();
	}

	private boolean allMethodsCovered() {
		return this.methodCoverageMap.isEmpty();
	}

}
