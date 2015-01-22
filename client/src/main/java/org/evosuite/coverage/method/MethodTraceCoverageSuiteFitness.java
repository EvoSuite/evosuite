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
package org.evosuite.coverage.method;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

import org.evosuite.Properties;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.StatementInterface;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.evosuite.setup.TestClusterGenerator.canUse;

/**
 * Fitness function for a whole test suite for all methods.
 * Methods must be invoked directly from a test case and
 * their execution can end normally or with an exception.
 * 
 * @author Gordon Fraser, Jose Miguel Rojas
 */
public class MethodTraceCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = 4958063899628649732L;

	private final static Logger logger = LoggerFactory.getLogger(MethodTraceCoverageSuiteFitness.class);

	// Coverage targets
	public final int totalMethods;
	private final Set<String> methods;

	/**
	 * <p>
	 * Constructor for MethodTraceCoverageSuiteFitness.
	 * </p>
	 */
	public MethodTraceCoverageSuiteFitness() {
        methods = new HashSet<String>();
        determineMethods();
        totalMethods = methods.size();
        logger.info("Total methods: " + totalMethods + ": " + methods);
        determineCoverageGoals();
	}

	// Some stuff for debug output
	public int maxCoveredMethods = 0;
	public double bestFitness = Double.MAX_VALUE;

	// Each test gets a set of distinct covered goals, these are mapped by branch id
	private final Map<String, TestFitnessFunction> methodCoverageMap = new HashMap<String, TestFitnessFunction>();

    private void determineMethods() {
        String className = Properties.TARGET_CLASS;
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (clazz != null) {
            Constructor[] allConstructors = clazz.getDeclaredConstructors();
            for (Constructor c : allConstructors) {
                if (canUse(c)) {
                    String descriptor = Type.getConstructorDescriptor(c);
                    logger.info("Adding goal for constructor " + className + ".<init>" + descriptor);
                    methods.add(c.getDeclaringClass().getName() + ".<init>" + descriptor);
                }
            }
            Method[] allMethods = clazz.getDeclaredMethods();
            for (Method m : allMethods) {
                if (canUse(m)) {
                    String descriptor = Type.getMethodDescriptor(m);
                    logger.info("Adding goal for method " + className + "." + m.getName() + descriptor);
                    methods.add(m.getDeclaringClass().getName() + "." + m.getName() + descriptor);
                }
            }
        }
    }

	/**
	 * Initialize the set of known coverage goals
	 */
	private void determineCoverageGoals() {
		List<MethodTraceCoverageTestFitness> goals = new MethodTraceCoverageFactory().getCoverageGoals();
		for (MethodTraceCoverageTestFitness goal : goals) {
			methodCoverageMap.put(goal.getClassName() + "." + goal.getMethod(), goal);
		}
	}

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
				if (methodCoverageMap.containsKey(name) && !callCount.containsKey(name)) {
                    // only consider goal methods
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
	private boolean analyzeTraces(List<ExecutionResult> results,
	        Map<String, Integer> callCount) {
		boolean hasTimeoutOrTestException = false;

		for (ExecutionResult result : results) {
			if (result.hasTimeout() || result.hasTestException()) {
				hasTimeoutOrTestException = true;
			}

			for (Entry<String, Integer> entry : result.getTrace().getMethodExecutionCount().entrySet()) {
				if (methodCoverageMap.containsKey(entry.getKey())) {
					if (!callCount.containsKey(entry.getKey()))
						callCount.put(entry.getKey(), entry.getValue());
					else {
						callCount.put(entry.getKey(),
					              callCount.get(entry.getKey()) + entry.getValue());
					}
					result.test.addCoveredGoal(methodCoverageMap.get(entry.getKey()));
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

		List<ExecutionResult> results = runTestSuite(suite);
		Map<String, Integer> callCount = new HashMap<String, Integer>();

		// Collect stats in the traces 
		boolean hasTimeoutOrTestException = analyzeTraces(results, callCount);

		// In case there were exceptions in a constructor
		handleConstructorExceptions(results, callCount);

        logger.info("CallCount: " + callCount.keySet().toString() + " (length=" + callCount.keySet().size() + ")");

		// Ensure all methods are called
		int missingMethods = 0;
		for (String e : methods) {
			if (!callCount.containsKey(e)) {
                logger.info("Method not covered in trace: " + e);
				fitness += 1.0;
				missingMethods += 1;
			}
		}
        logger.debug("Fitness: " + fitness);
        logger.debug("Number of missing methods: " + missingMethods);
		printStatusMessages(suite, totalMethods - missingMethods, fitness);

		// Calculate coverage
		int coverage = callCount.keySet().size();

		if (totalMethods > 0) {
            logger.debug("Coverage: " + (double) coverage / (double) totalMethods);
            suite.setCoverage(this, (double) coverage / (double) totalMethods);
        } else {
            logger.debug("Coverage (0 methods): " + 1.0);
            suite.setCoverage(this, 1.0);
        }
		suite.setNumOfCoveredGoals(this, coverage);
		if (hasTimeoutOrTestException) {
			logger.info("Test suite has timed out, setting fitness to max value " + totalMethods);
			fitness = totalMethods;
			//suite.setCoverage(0.0);
		}

		updateIndividual(this, suite, fitness);

		assert (coverage <= totalMethods) : "Covered " + coverage + " vs total goals " + totalMethods;
		assert (fitness >= 0.0);
		assert (fitness != 0.0 || coverage == totalMethods) : "Fitness: " + fitness + ", "
		        + "coverage: " + coverage + "/" + totalMethods;
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
	private void printStatusMessages(
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
