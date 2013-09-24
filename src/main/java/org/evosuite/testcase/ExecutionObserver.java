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
package org.evosuite.testcase;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract base class of all execution observers
 * 
 * @author Gordon Fraser
 */
public abstract class ExecutionObserver {

	/** The test case being monitored and executed */
	protected static TestCase currentTest = null;

	/** Constant <code>WRAPPER_TYPES</code> */
	protected static final Set<Class<?>> WRAPPER_TYPES = new HashSet<Class<?>>(
	        Arrays.asList(Boolean.class, Character.class, Byte.class, Short.class,
	                      Integer.class, Long.class, Float.class, Double.class,
	                      Void.class));

	/**
	 * <p>
	 * isWrapperType
	 * </p>
	 * 
	 * @param clazz
	 *            a {@link java.lang.Class} object.
	 * @return a boolean.
	 */
	protected static boolean isWrapperType(Class<?> clazz) {
		return WRAPPER_TYPES.contains(clazz);
	}

	/**
	 * Setter method for current test case
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 */
	public static void setCurrentTest(TestCase test) {
		currentTest = test;
	}

	/**
	 * Getter method for current test case
	 * 
	 * @return a {@link org.evosuite.testcase.TestCase} object.
	 */
	public static TestCase getCurrentTest() {
		return currentTest;
	}

	/**
	 * This is called with the console output of each statement
	 * 
	 * @param position
	 *            a int.
	 * @param output
	 *            a {@link java.lang.String} object.
	 */
	public abstract void output(int position, String output);

	/**
	 * Called immediately before a statement is executed
	 * 
	 * @param statement
	 * @param scope
	 */
	public abstract void beforeStatement(StatementInterface statement, Scope scope);

	/**
	 * After execution of a statement, the result is passed to the observer
	 * 
	 * @param statement
	 *            a {@link org.evosuite.testcase.StatementInterface} object.
	 * @param scope
	 *            a {@link org.evosuite.testcase.Scope} object.
	 * @param exception
	 *            a {@link java.lang.Throwable} object.
	 */
	public abstract void afterStatement(StatementInterface statement, Scope scope,
	        Throwable exception);

	/**
	 * Need a way to clear previously produced results
	 */
	public abstract void clear();

	/**
	 * Determine the set of variables that somehow lead to this statement
	 * 
	 * @param statement
	 *            a {@link org.evosuite.testcase.StatementInterface} object.
	 * @return a {@link java.util.Set} object.
	 */
	protected Set<VariableReference> getDependentVariables(StatementInterface statement) {
		Set<VariableReference> dependencies = new HashSet<VariableReference>();
		for (VariableReference var : statement.getVariableReferences()) {
			dependencies.add(var);
			dependencies.addAll(currentTest.getDependencies(var));
		}
		return dependencies;
	}
}
