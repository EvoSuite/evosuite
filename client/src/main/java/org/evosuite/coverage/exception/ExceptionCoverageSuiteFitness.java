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
 * 
 * @author Gordon Fraser
 */
package org.evosuite.coverage.exception;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import org.evosuite.Properties;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception fitness is different from the others, as we do not know a priori how
 * many exceptions could de thrown in the SUT. In other words, we cannot really
 * speak about coverage percentage here
 */
public class ExceptionCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = 1565793073526627496L;

	private static Logger logger = LoggerFactory.getLogger(ExceptionCoverageSuiteFitness.class);

    private static int maxExceptionsCovered = 0;
    

	public ExceptionCoverageSuiteFitness() {
	}
	
    public static int getMaxExceptionsCovered() {
        return maxExceptionsCovered;
    }

	@Override
	public double getFitness(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		logger.trace("Calculating exception fitness");


		/*
		 * for each method in the SUT, we keep track of which kind of exceptions were thrown.
		 * we distinguish between "implicit", "explicit" and "declared"
		 */
		Map<String, Set<Class<?>>> implicitTypesOfExceptions = new HashMap<>();
        Map<String, Set<Class<?>>> explicitTypesOfExceptions = new HashMap<>();
        Map<String, Set<Class<?>>> declaredTypesOfExceptions = new HashMap<>();

		List<ExecutionResult> results = runTestSuite(suite);

		calculateExceptionInfo(results,implicitTypesOfExceptions,explicitTypesOfExceptions,declaredTypesOfExceptions);

		int nExc = getNumExceptions(implicitTypesOfExceptions) + getNumExceptions(explicitTypesOfExceptions) +
                getNumExceptions(declaredTypesOfExceptions);

        if (nExc > maxExceptionsCovered) {
            logger.info("(Exceptions) Best individual covers " + nExc + " exceptions");
            maxExceptionsCovered = nExc;
        }

        // We cannot set a coverage here, as it does not make any sense
       	// suite.setCoverage(this, 1.0);

		double exceptionFitness = 1d / (1d + nExc);

        suite.setFitness(this, exceptionFitness);

        return exceptionFitness;
	}

	
	
	/**
	 * Given the list of results, fill the 3 given (empty) maps with exception information.
	 * Also, add exception coverage goals to mapping in {@link ExceptionCoverageFactory}
	 * 
	 * @param results
	 * @param implicitTypesOfExceptions
	 * @param explicitTypesOfExceptions
     * @param declaredTypesOfExceptions
	 * @throws IllegalArgumentException
	 */
	public static void calculateExceptionInfo(List<ExecutionResult> results, 
			Map<String, Set<Class<?>>> implicitTypesOfExceptions, Map<String, Set<Class<?>>> explicitTypesOfExceptions,
            Map<String, Set<Class<?>>> declaredTypesOfExceptions)
		throws IllegalArgumentException{
		
		if(results==null || implicitTypesOfExceptions==null || explicitTypesOfExceptions==null ||
				!implicitTypesOfExceptions.isEmpty() || !explicitTypesOfExceptions.isEmpty() ||
                declaredTypesOfExceptions==null || !declaredTypesOfExceptions.isEmpty()){
			throw new IllegalArgumentException();
		}

		//keep track of what is directly thrown in SUT with "throw new ..."
		Map<TestCase, Map<Integer, Boolean>> isExceptionExplicit = new HashMap<>();

		// for each test case
		for (ExecutionResult result : results) {
			isExceptionExplicit.put(result.test, result.explicitExceptions);

			//iterate on the indexes of the statements that resulted in an exception
			for (Integer i : result.getPositionsWhereExceptionsWereThrown()) {
				if (i >= result.test.size()) {
					// Timeouts are put after the last statement if the process was forcefully killed
					continue;
				}

				//not interested in security exceptions when Sandbox is active
				Throwable t = result.getExceptionThrownAtPosition(i);
				if (t instanceof SecurityException && Properties.SANDBOX){
					continue;
				}

				/*
				 	Ignore exceptions thrown in the test code itself. Eg, due to mutation we
				 	can end up with tests like:

				 	Foo foo = null:
					foo.bar();
				  */
				if (t instanceof CodeUnderTestException){
					continue;
				}

				// This is to cover cases not handled by CodeUnderTestException, or if bug in EvoSuite itself
				if (t.getStackTrace().length > 0
						&& t.getStackTrace()[0].getClassName().startsWith("org.evosuite.testcase")) {
					continue;
				}


				String methodIdentifier = ""; //eg name+descriptor
				boolean sutException = false; // was the exception originated by a direct call on the SUT?

				if (result.test.getStatement(i) instanceof MethodStatement) {
					MethodStatement ms = (MethodStatement) result.test.getStatement(i);
					Method method = ms.getMethod().getMethod();
					methodIdentifier = method.getName() + Type.getMethodDescriptor(method);

                    if (method.getDeclaringClass().equals(Properties.getTargetClass())){
						sutException = true;
					}
					
				} else if (result.test.getStatement(i) instanceof ConstructorStatement) {
					ConstructorStatement cs = (ConstructorStatement) result.test.getStatement(i);
					Constructor<?> constructor = cs.getConstructor().getConstructor();
					methodIdentifier = "<init>" + Type.getConstructorDescriptor(constructor);
					
					if (constructor.getDeclaringClass().equals(Properties.getTargetClass())){
						sutException = true;
					}
				}
				
				boolean notDeclared = true;
				// Check if thrown exception is declared, or subclass of a declared exception 
				for(Class<?> declaredExceptionClass : result.test.getStatement(i).getDeclaredExceptions()) {
					if(declaredExceptionClass.isAssignableFrom(t.getClass())) {
						notDeclared = false;
						break;
					}
				}

				/*
				 * We only consider exceptions that were thrown by calling directly the SUT (not the other
				 * used libraries). However, this would ignore cases in which the SUT is indirectly tested
				 * through another class
				 */

				if (sutException) {

                    if(notDeclared) {
                        /*
					     * we need to distinguish whether it is explicit (ie "throw" in the code, eg for validating
					     * input for pre-condition) or implicit ("likely" a real fault).
					     */

                        boolean isExplicit = isExceptionExplicit.get(result.test).containsKey(i)
                                && isExceptionExplicit.get(result.test).get(i);

                        if (isExplicit) {

                            if (!explicitTypesOfExceptions.containsKey(methodIdentifier)) {
                                explicitTypesOfExceptions.put(methodIdentifier, new HashSet<Class<?>>());
                            }
                            explicitTypesOfExceptions.get(methodIdentifier).add(t.getClass());
                        } else {

                            if (!implicitTypesOfExceptions.containsKey(methodIdentifier)) {
                                implicitTypesOfExceptions.put(methodIdentifier, new HashSet<Class<?>>());
                            }
                            implicitTypesOfExceptions.get(methodIdentifier).add(t.getClass());
                        }
                    } else {
                        if (!declaredTypesOfExceptions.containsKey(methodIdentifier)) {
                            declaredTypesOfExceptions.put(methodIdentifier, new HashSet<Class<?>>());
                        }
                        declaredTypesOfExceptions.get(methodIdentifier).add(t.getClass());
                    }

                    /*
                     * Add goal to ExceptionCoverageFactory
                     * FIXME: need refactoring and define type, ie explicit, implicit, declared
                     */
                    ExceptionCoverageTestFitness goal = new ExceptionCoverageTestFitness(methodIdentifier, t.getClass());
                    ExceptionCoverageFactory.getGoals().put(methodIdentifier + t.getClass().getName(), goal);
				}

			}
		}
	}
	
	public static int getNumExceptions(Map<String, Set<Class<?>>> exceptions) {
		int total = 0;
		for (Set<Class<?>> exceptionSet : exceptions.values()) {
			total += exceptionSet.size();
		}
		return total;
	}

	public static int getNumClassExceptions(Map<String, Set<Class<?>>> exceptions) {
		Set<Class<?>> classExceptions = new HashSet<Class<?>>();
		for (Set<Class<?>> exceptionSet : exceptions.values()) {
			classExceptions.addAll(exceptionSet);
		}
		return classExceptions.size();
	}
}
