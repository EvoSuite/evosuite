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
package org.evosuite.coverage.method;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

import org.evosuite.Properties;
import org.evosuite.coverage.archive.TestsArchive;
import org.evosuite.setup.TestUsageChecker;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fitness function for a whole test suite for all methods.
 * Methods must be invoked directly from a test case and
 * their execution can end normally or with an exception.
 * 
 * @author Gordon Fraser, Jose Miguel Rojas
 */
public class MethodTraceCoverageSuiteFitness extends MethodCoverageSuiteFitness {

	private static final long serialVersionUID = 4958063899628649732L;

	private final static Logger logger = LoggerFactory.getLogger(MethodTraceCoverageSuiteFitness.class);

	/**
	 * Initialize the set of known coverage goals
	 */
	@Override
	protected void determineCoverageGoals() {
		List<MethodTraceCoverageTestFitness> goals = new MethodTraceCoverageFactory().getCoverageGoals();
		for (MethodTraceCoverageTestFitness goal : goals) {
			methodCoverageMap.put(goal.getClassName() + "." + goal.getMethod(), goal);
			if(Properties.TEST_ARCHIVE)
				TestsArchive.instance.addGoalToCover(this, goal);
		}
	}

	/**
	 * If there is an exception in a superconstructor, then the corresponding
	 * constructor might not be included in the execution trace
	 * 
	 * @param results
	 * @param calledMethods
	 */
	@Override
	protected void handleConstructorExceptions(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite,
			List<ExecutionResult> results,
			Set<String> calledMethods) {

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
				if (methodCoverageMap.containsKey(name) && calledMethods.contains(name) && !removedMethods.contains(name)) {
                    // only consider goal methods
					calledMethods.add(name);
                    result.test.addCoveredGoal(methodCoverageMap.get(name));
					if(Properties.TEST_ARCHIVE) {
						TestsArchive.instance.putTest(this, methodCoverageMap.get(name), result);
						toRemoveMethods.add(name);
						suite.isToBeUpdated(true);
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
	@Override
	protected boolean analyzeTraces(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite, 
			List<ExecutionResult> results,
	        Set<String> calledMethods) {
		boolean hasTimeoutOrTestException = false;

		for (ExecutionResult result : results) {
			if (result.hasTimeout() || result.hasTestException()) {
				hasTimeoutOrTestException = true;
			}

			for (Entry<String, Integer> entry : result.getTrace().getMethodExecutionCount().entrySet()) {
				String canonicalName = entry.getKey().replace('$','.'); // Goals contain canonical method names
				if(!methods.contains(canonicalName)||removedMethods.contains(canonicalName)) continue;
				if (methodCoverageMap.containsKey(canonicalName)) {
					calledMethods.add(canonicalName);
					result.test.addCoveredGoal(methodCoverageMap.get(canonicalName));
					if(Properties.TEST_ARCHIVE) {
						TestsArchive.instance.putTest(this, methodCoverageMap.get(canonicalName), result);
						toRemoveMethods.add(canonicalName);
						suite.isToBeUpdated(true);
					}
                }
			}
		}
		return hasTimeoutOrTestException;
	}

	/**
	 * Some useful debug information
	 *
	 * @param coveredMethods
	 * @param fitness
	 */
	@Override
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

}
