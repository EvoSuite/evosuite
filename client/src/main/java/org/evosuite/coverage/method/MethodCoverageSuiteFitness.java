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


import org.evosuite.Properties;
import org.evosuite.coverage.archive.TestsArchive;
import org.evosuite.setup.TestUsageChecker;
import org.evosuite.testcase.*;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.EntityWithParametersStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;




/**
 * Fitness function for a whole test suite for all methods considering only normal behaviour (no exceptions)
 *
 * @author Gordon Fraser, Jose Miguel Rojas
 */
public class MethodCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = 3359321076367091582L;

	private final static Logger logger = LoggerFactory.getLogger(TestSuiteFitnessFunction.class);

    // Coverage targets
	protected final int totalMethods;
	protected final Set<String> methods;

    // Some stuff for debug output
    protected int maxCoveredMethods = 0;
    protected double bestFitness = Double.MAX_VALUE;

    protected Set<String> toRemoveMethods = new LinkedHashSet<>();
    protected Set<String> removedMethods  = new LinkedHashSet<>();

    // Each test gets a set of distinct covered goals, these are mapped by branch id
    protected final Map<String, TestFitnessFunction> methodCoverageMap = new HashMap<String, TestFitnessFunction>();

    /**
	 * <p>
	 * Constructor for MethodCoverageSuiteFitness.
	 * </p>
	 */
	public MethodCoverageSuiteFitness() {
        methods = new HashSet<String>();
        determineMethods();
		totalMethods = methods.size();
		logger.info("Total methods: " + totalMethods + ": " + methods);
		determineCoverageGoals();
	}

    protected void determineMethods() {
        String className = Properties.TARGET_CLASS;
		Class<?> clazz = Properties.getTargetClassAndDontInitialise();
        if (clazz != null) {
			determineMethods(clazz, className);
	        Class<?>[] innerClasses = clazz.getDeclaredClasses();
	        for (Class<?> innerClass : innerClasses) {
		        String innerClassName = innerClass.getCanonicalName();
		        determineMethods(innerClass, innerClassName);
	        }
        }
    }

    protected void determineMethods(Class<?> clazz, String className) {
	    Constructor<?>[] allConstructors = clazz.getDeclaredConstructors();
	    for (Constructor<?> c : allConstructors) {
		    if (TestUsageChecker.canUse(c)) {
			    String descriptor = Type.getConstructorDescriptor(c);
			    logger.info("Adding goal for constructor " + className + ".<init>" + descriptor);
			    methods.add(c.getDeclaringClass().getCanonicalName() + ".<init>" + descriptor);
		    }
	    }
	    Method[] allMethods = clazz.getDeclaredMethods();
	    for (Method m : allMethods) {
		    if (TestUsageChecker.canUse(m)) {
			    String descriptor = Type.getMethodDescriptor(m);
			    logger.info("Adding goal for method " + className + "." + m.getName() + descriptor);
			    methods.add(m.getDeclaringClass().getCanonicalName() + "." + m.getName() + descriptor);
		    }
	    }
    }

	/**
	 * Initialize the set of known coverage goals
	 */
	protected void determineCoverageGoals() {
		List<MethodCoverageTestFitness> goals = new MethodCoverageFactory().getCoverageGoals();
		for (MethodCoverageTestFitness goal : goals) {
            methodCoverageMap.put(goal.getClassName() + "." + goal.getMethod(), goal);
			if(Properties.TEST_ARCHIVE)
				TestsArchive.instance.addGoalToCover(this, goal);
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
				if (methodCoverageMap.containsKey(name) && !calledMethods.contains(name) && !removedMethods.contains(name)) {
                    // only include methods being called
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
	protected boolean analyzeTraces(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite,
			List<ExecutionResult> results,
	        Set<String> calledMethods) {
		boolean hasTimeoutOrTestException = false;

		for (ExecutionResult result : results) {
            if (result.hasTimeout() || result.hasTestException()) {
                hasTimeoutOrTestException = true;
            }

            for (Statement stmt : result.test) {
                if (! isValidPosition(result, stmt.getPosition()))
                    break;
                if ((stmt instanceof MethodStatement || stmt instanceof ConstructorStatement)) {
					EntityWithParametersStatement ps = (EntityWithParametersStatement)stmt;
					String className  = ps.getDeclaringClassName();
					String methodDesc = ps.getDescriptor();
					String methodName = ps.getMethodName() + methodDesc;
                    String fullName = className + "." + methodName;
    				if(!methods.contains(fullName)||removedMethods.contains(fullName)) continue;
                    if (methodCoverageMap.containsKey(fullName)) {
                        calledMethods.add(fullName);
                        result.test.addCoveredGoal(methodCoverageMap.get(fullName));
    					if(Properties.TEST_ARCHIVE) {
    						TestsArchive.instance.putTest(this, methodCoverageMap.get(fullName), result);
    						toRemoveMethods.add(fullName);
    						suite.isToBeUpdated(true);
    					}
                    }
                }
            }
        }
		return hasTimeoutOrTestException;
	}

    protected boolean isValidPosition(ExecutionResult result, Integer position) {
        List<Integer> exceptionPositions = asSortedList(result.getPositionsWhereExceptionsWereThrown());
        if (Properties.BREAK_ON_EXCEPTION) {
            return exceptionPositions.isEmpty() ? true : position > exceptionPositions.get(0);
        } else
            return true;


    }

    protected static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
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
		boolean hasTimeoutOrTestException = analyzeTraces(suite, results, calledMethods);

		// In case there were exceptions in a constructor
		handleConstructorExceptions(suite, results, calledMethods);

		// Ensure all methods are called
		int missingMethods = 0;
		for (String e : methods) {
			if (!calledMethods.contains(e)) {
				fitness += 1.0;
				missingMethods += 1;
			}
		}

        // Calculate coverage
        int coveredMethods = calledMethods.size() + removedMethods.size();
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

		for (String method : toRemoveMethods) {
			boolean removed = methods.remove(method);
			TestFitnessFunction f = methodCoverageMap.remove(method);
			if (removed && f != null) {
				// totalMethods--;
				methods.remove(method);
				removedMethods.add(method);
				//removeTestCall(f.getTargetClass(), f.getTargetMethod());
			} else {
				throw new IllegalStateException("Goal to remove not found: "+method+", candidates: "+methodCoverageMap.keySet());
			}
		}
		toRemoveMethods.clear();

		return true;
	}
}
