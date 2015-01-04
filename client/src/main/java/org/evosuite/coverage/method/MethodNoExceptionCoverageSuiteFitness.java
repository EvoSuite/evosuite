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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.graphs.cfg.CFGMethodAdapter;
import org.evosuite.testcase.*;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.evosuite.setup.TestClusterGenerator.canUse;


/**
 * Fitness function for a whole test suite for all methods considering only normal behaviour (no exceptions)
 * 
 * @author Gordon Fraser, Jose Miguel Rojas
 */
public class MethodNoExceptionCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = -704561530935529634L;

	private final static Logger logger = LoggerFactory.getLogger(TestSuiteFitnessFunction.class);

	// Coverage targets
	public final int totalMethods;
	public final Set<String> methods;

    // Some stuff for debug output
    public int maxCoveredMethods = 0;
    public double bestFitness = Double.MAX_VALUE;

    // Each test gets a set of distinct covered goals, these are mapped by branch id
    private final Map<String, TestFitnessFunction> methodNoExceptionCoverageMap = new HashMap<String, TestFitnessFunction>();

    /**
	 * <p>
	 * Constructor for MethodNoExceptionCoverageSuiteFitness.
	 * </p>
	 */
	public MethodNoExceptionCoverageSuiteFitness() {
        methods = new HashSet<String>();
        determineMethods();
        totalMethods = methods.size();
        logger.info("Total methods: " + totalMethods + ": " + methods);
        determineCoverageGoals();
	}

    private void determineMethods() {
        String className = Properties.TARGET_CLASS;
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
        	logger.warn("Class could not be loaded: " + className);
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
		List<MethodNoExceptionCoverageTestFitness> goals = new MethodNoExceptionCoverageFactory().getCoverageGoals();
		for (MethodNoExceptionCoverageTestFitness goal : goals)
            methodNoExceptionCoverageMap.put(goal.getClassName() + "." + goal.getMethod(), goal);
	}

	/**
	 * If there is an exception in a super-constructor, then the corresponding
	 * constructor might not be included in the execution trace
	 * 
	 * @param results
	 * @param calledMethods
	 */
	private void handleConstructorExceptions(List<ExecutionResult> results,
	        Set<String> calledMethods) {

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
				if (methodNoExceptionCoverageMap.containsKey(name) && !calledMethods.contains(name)) {
                    calledMethods.add(name);
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
	private boolean analyzeTraces(List<ExecutionResult> results,
	        Set<String> calledMethods) {
		boolean hasTimeoutOrTestException = false;

		for (ExecutionResult result : results) {
            if (result.hasTimeout() || result.hasTestException()) {
                hasTimeoutOrTestException = true;
            }


            List<Integer> exceptionPositions = asSortedList(result.getPositionsWhereExceptionsWereThrown());
            for (StatementInterface stmt : result.test) {
                if (! isValidPosition(exceptionPositions, stmt.getPosition()))
                    break;
                if ((stmt instanceof MethodStatement || stmt instanceof ConstructorStatement)
                        && (! exceptionPositions.contains(stmt.getPosition()))) {
                    String className;
                    String methodName;
                    if (stmt instanceof MethodStatement) {
                        MethodStatement m = (MethodStatement) stmt;
                        className = m.getMethod().getMethod().getDeclaringClass().getName();
                        methodName = m.toString();
                    } else { //stmt instanceof ConstructorStatement
                        ConstructorStatement c = (ConstructorStatement)stmt;
                        className = c.getConstructor().getDeclaringClass().getName();
                        methodName = "<init>" + Type.getConstructorDescriptor(c.getConstructor().getConstructor());
                    }
                    String fullName = className + "." + methodName;
                    if (methodNoExceptionCoverageMap.containsKey(fullName)) {
                        calledMethods.add(fullName);
                        result.test.addCoveredGoal(methodNoExceptionCoverageMap.get(fullName));
                    }
                }
            }
        }
		return hasTimeoutOrTestException;
	}

    private boolean isValidPosition(List<Integer> exceptionPositions, Integer position) {
        if (Properties.BREAK_ON_EXCEPTION) {
            return exceptionPositions.isEmpty() ? true : position > exceptionPositions.get(0);
        } else
            return true;


    }

    private static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
        List<T> list = new ArrayList<T>(c);
        java.util.Collections.sort(list);
        return list;
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
		Set<String> calledMethods = new HashSet<String>();

		// Collect stats in the traces 
		boolean hasTimeoutOrTestException = analyzeTraces(results, calledMethods);

		// In case there were exceptions in a constructor
		handleConstructorExceptions(results, calledMethods);

		// Ensure all methods are called
		int missingMethods = 0;
		for (String e : methods) {
			if (!calledMethods.contains(e)) {
				fitness += 1.0;
				missingMethods += 1;
			}
		}

        // Calculate coverage
        int coveredMethods = calledMethods.size();
        assert (totalMethods == coveredMethods + missingMethods);

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
	private void printStatusMessages(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite,
	        int coveredMethods, double fitness) {
		if (coveredMethods > maxCoveredMethods) {
			logger.info("(Methods No-Exc) Best individual covers " + coveredMethods + "/"
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
