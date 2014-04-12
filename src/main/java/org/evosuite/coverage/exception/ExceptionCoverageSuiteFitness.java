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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.testcase.CodeUnderTestException;
import org.evosuite.testcase.ConstructorStatement;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.MethodStatement;
import org.evosuite.testcase.TestCase;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = 1565793073526627496L;

	private static Logger logger = LoggerFactory.getLogger(ExceptionCoverageSuiteFitness.class);

	protected TestSuiteFitnessFunction baseFF;

	/**
	 * <p>
	 * Constructor for ExceptionCoverageSuiteFitness.
	 * </p>
	 */
	public ExceptionCoverageSuiteFitness() {
		baseFF = new BranchCoverageSuiteFitness();
	}

	/** {@inheritDoc} */
	@Override
	public double getFitness(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		logger.trace("Calculating exception fitness");

		/*
		 * We first calculate fitness based on coverage. this not only 
		 * has side-effect of changing "fitness" in individual, but also "coverage".
		 * but because "coverage" is only used for stats, no need to update it here, as
		 * anyway it d be bit difficult to define
		 */
		double coverageFitness = baseFF.getFitness(suite);

		/*
		 * for each method in the SUT, we keep track of which kind of exceptions were thrown.
		 * we distinguish between "implicit" and "explicit" 
		 */
		Map<String, Set<Class<?>>> implicitTypesOfExceptions = new HashMap<String, Set<Class<?>>>();
		Map<String, Set<Class<?>>> explicitTypesOfExceptions = new HashMap<String, Set<Class<?>>>();

		List<ExecutionResult> results = runTestSuite(suite);
		
		calculateExceptionInfo(results,implicitTypesOfExceptions,explicitTypesOfExceptions);

		int nExc = getNumExceptions(implicitTypesOfExceptions) + getNumExceptions(explicitTypesOfExceptions);

		double exceptionFitness = 1d / (1d + nExc);

		suite.setFitness(coverageFitness + exceptionFitness);
		return coverageFitness + exceptionFitness;
	}

	
	
	/**
	 * given the list of results, fill the 2 given (empty) maps with exception information
	 * 
	 * @param results
	 * @param implicitTypesOfExceptions
	 * @param explicitTypesOfExceptions
	 * @throws IllegalArgumentException
	 */
	public static void calculateExceptionInfo(List<ExecutionResult> results, 
			Map<String, Set<Class<?>>> implicitTypesOfExceptions, Map<String, Set<Class<?>>> explicitTypesOfExceptions)
		throws IllegalArgumentException{
		
		if(results==null || implicitTypesOfExceptions==null || explicitTypesOfExceptions==null ||
				!implicitTypesOfExceptions.isEmpty() || !explicitTypesOfExceptions.isEmpty()){
			throw new IllegalArgumentException();
		}
		
		Map<TestCase, Map<Integer, Boolean>> isExceptionExplicit = new HashMap<TestCase, Map<Integer, Boolean>>();

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
				
				// If the exception was thrown in the test directly, it is also not interesting
				if (t.getStackTrace().length > 0
				        && t.getStackTrace()[0].getClassName().startsWith("org.evosuite.testcase")) {
					continue;
				}
				
				// Ignore exceptions thrown in the test code itself
				if (t instanceof CodeUnderTestException){
					continue;
				}
				
				String methodName = "";
				boolean sutException = false;
				
				if (result.test.getStatement(i) instanceof MethodStatement) {
					MethodStatement ms = (MethodStatement) result.test.getStatement(i);
					Method method = ms.getMethod().getMethod();
					methodName = method.getName() + Type.getMethodDescriptor(method);
					
					if (method.getDeclaringClass().equals(Properties.getTargetClass())){
						sutException = true;
					}
					
				} else if (result.test.getStatement(i) instanceof ConstructorStatement) {
					ConstructorStatement cs = (ConstructorStatement) result.test.getStatement(i);
					Constructor<?> constructor = cs.getConstructor().getConstructor();
					methodName = "<init>" + Type.getConstructorDescriptor(constructor);
					
					if (constructor.getDeclaringClass().equals(Properties.getTargetClass())){
						sutException = true;
					}
				}

				boolean notDeclared = !result.test.getStatement(i).getDeclaredExceptions().contains(t.getClass());

				/*
				 * We only consider exceptions that were thrown directly in the SUT (not called libraries)
				 * and that are not declared in the signature of the method.
				 */

				if (notDeclared && sutException) {
					/*
					 * we need to distinguish whether it is explicit (ie "throw" in the code, eg for validating
					 * input for pre-condition) or implicit ("likely" a real fault).
					 */

					boolean isExplicit = isExceptionExplicit.get(result.test).containsKey(i)
					        && isExceptionExplicit.get(result.test).get(i);

					if (isExplicit) {
						
						if (!explicitTypesOfExceptions.containsKey(methodName)){
							explicitTypesOfExceptions.put(methodName, new HashSet<Class<?>>());
						}
						explicitTypesOfExceptions.get(methodName).add(t.getClass());
						
					} else {
						
						if (!implicitTypesOfExceptions.containsKey(methodName)){
							implicitTypesOfExceptions.put(methodName, new HashSet<Class<?>>());
						}
						implicitTypesOfExceptions.get(methodName).add(t.getClass());
						
					}
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
