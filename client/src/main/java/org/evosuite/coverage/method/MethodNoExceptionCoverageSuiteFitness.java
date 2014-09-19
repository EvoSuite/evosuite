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
	private final Set<String> methods;

	/**
	 * <p>
	 * Constructor for MethodNoExceptionCoverageSuiteFitness.
	 * </p>
	 */
	public MethodNoExceptionCoverageSuiteFitness() {

		String prefix = Properties.TARGET_CLASS_PREFIX;

		if (prefix.isEmpty()) {
			prefix = Properties.TARGET_CLASS;
			totalMethods = CFGMethodAdapter.getNumMethods();
			methods = CFGMethodAdapter.getMethods();

		} else {
			totalMethods = CFGMethodAdapter.getNumMethodsPrefix(prefix);
			methods = CFGMethodAdapter.getMethodsPrefix(Properties.TARGET_CLASS_PREFIX);
		}

		logger.info("Total methods: " + totalMethods + ": " + methods);

		determineCoverageGoals();
	}

	// Some stuff for debug output
	public int maxCoveredMethods = 0;
	public double bestFitness = Double.MAX_VALUE;

	// Each test gets a set of distinct covered goals, these are mapped by branch id
	private final Map<String, TestFitnessFunction> methodNoExceptionCoverageMap = new HashMap<String, TestFitnessFunction>();

	/**
	 * Initialize the set of known coverage goals
	 */
	private void determineCoverageGoals() {
		List<MethodNoExceptionCoverageTestFitness> goals = new MethodNoExceptionCoverageFactory().getCoverageGoals();
		for (MethodNoExceptionCoverageTestFitness goal : goals)
            methodNoExceptionCoverageMap.put(goal.getClassName() + "." + goal.getMethod(), goal);
	}

	/**
	 * If there is an exception in a superconstructor, then the corresponding
	 * constructor might not be included in the execution trace
	 * 
	 * @param results
	 * @param calledMethodsNoExc
	 */
	private void handleConstructorExceptions(List<ExecutionResult> results,
	        Set<String> calledMethodsNoExc) {

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
				if (!calledMethodsNoExc.contains(name)) {
                    calledMethodsNoExc.add(name);
				}
			}

		}
	}

	/**
	 * Iterate over all execution results and summarize statistics
	 * 
	 * @param results
	 * @param calledMethodsNoExc
	 * @return
	 */
	private boolean analyzeTraces(List<ExecutionResult> results,
	        Set<String> calledMethodsNoExc) {
		boolean hasTimeoutOrTestException = false;

		for (ExecutionResult result : results) {
            if (result.hasTimeout() || result.hasTestException()) {
                hasTimeoutOrTestException = true;
            }


            List<Integer> exceptionPositions = asSortedList(result.getPositionsWhereExceptionsWereThrown());
            for (StatementInterface stmt : result.test) {
                if (! isValidPosition(exceptionPositions, stmt.getPosition()))
                    break;
                if ( (stmt instanceof MethodStatement || stmt instanceof ConstructorStatement)
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
                        calledMethodsNoExc.add(fullName);
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
		Set<String> calledMethodsNoExc = new HashSet<String>();

		// Collect stats in the traces 
		boolean hasTimeoutOrTestException = analyzeTraces(results, calledMethodsNoExc);

		// In case there were exceptions in a constructor
		handleConstructorExceptions(results, calledMethodsNoExc);

		// Ensure all methods are called
		int missingMethods = 0;
		for (String e : methods) {
			if (!calledMethodsNoExc.contains(e)) {
				fitness += 1.0;
				missingMethods += 1;
			}
		}

        // Calculate coverage
        int coveredMethodsNoExc = calledMethodsNoExc.size();
        assert (totalMethods == coveredMethodsNoExc + missingMethods);

        printStatusMessages(suite, totalMethods - missingMethods, fitness);


		if (totalMethods > 0)
			suite.setCoverage((double) coveredMethodsNoExc / (double) totalMethods);

		suite.setNumOfCoveredGoals(coveredMethodsNoExc);

		if (hasTimeoutOrTestException) {
			logger.info("Test suite has timed out, setting fitness to max value " + totalMethods);
			fitness = totalMethods;
			//suite.setCoverage(0.0);
		}

		updateIndividual(this, suite, fitness);

		assert (coveredMethodsNoExc <= totalMethods) : "Covered " + coveredMethodsNoExc + " vs total goals " + totalMethods;
		assert (fitness >= 0.0);
		assert (fitness != 0.0 || coveredMethodsNoExc == totalMethods) : "Fitness: " + fitness + ", "
		        + "coverage: " + coveredMethodsNoExc + "/" + totalMethods;
		assert (suite.getCoverage() <= 1.0) && (suite.getCoverage() >= 0.0) : "Wrong coverage value "
		        + suite.getCoverage();

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
