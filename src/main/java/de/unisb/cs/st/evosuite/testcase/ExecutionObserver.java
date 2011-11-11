/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.testcase;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract base class of all execution observers
 * 
 * @author Gordon Fraser
 * 
 */
public abstract class ExecutionObserver {

	/** The test case being monitored and executed */
	protected static TestCase currentTest = null;

	@SuppressWarnings("unchecked")
	protected static final Set<Class<?>> WRAPPER_TYPES = new HashSet<Class<?>>(
	        Arrays.asList(Boolean.class, Character.class, Byte.class, Short.class,
	                      Integer.class, Long.class, Float.class, Double.class,
	                      Void.class));

	protected static boolean isWrapperType(Class<?> clazz) {
		return WRAPPER_TYPES.contains(clazz);
	}

	/**
	 * Setter method for current test case
	 * 
	 * @param test
	 */
	public static void setCurrentTest(TestCase test) {
		currentTest = test;
	}

	/**
	 * Getter method for current test case
	 * 
	 * @return
	 */
	public static TestCase getCurrentTest() {
		return currentTest;
	}

	/**
	 * This is called with the console output of each statement
	 * 
	 * @param position
	 * @param output
	 */
	public abstract void output(int position, String output);

	/**
	 * After execution of a statement, the result is passed to the observer
	 * 
	 * @param statement
	 * @param scope
	 * @param exception
	 */
	public abstract void statement(StatementInterface statement, Scope scope,
	        Throwable exception);

	/**
	 * Need a way to clear previously produced results
	 */
	public abstract void clear();

	/**
	 * Determine the set of variables that somehow lead to this statement
	 * 
	 * @param statement
	 * @return
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
