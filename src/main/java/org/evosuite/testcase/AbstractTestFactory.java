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

import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.evosuite.ga.ConstructionFailedException;


/**
 * Abstract base class of test factory
 *
 * @author Gordon Fraser
 */
public abstract class AbstractTestFactory implements Serializable {

	private static final long serialVersionUID = -2328803453596295741L;

	/**
	 * <p>changeCall</p>
	 *
	 * @param test a {@link org.evosuite.testcase.TestCase} object.
	 * @param statement a {@link org.evosuite.testcase.StatementInterface} object.
	 * @param call a {@link java.lang.reflect.AccessibleObject} object.
	 * @throws org.evosuite.ga.ConstructionFailedException if any.
	 */
	public abstract void changeCall(TestCase test, StatementInterface statement,
	        AccessibleObject call) throws ConstructionFailedException;

	/**
	 * <p>insertRandomStatement</p>
	 *
	 * @param test a {@link org.evosuite.testcase.TestCase} object.
	 */
	public abstract void insertRandomStatement(TestCase test);

	/**
	 * <p>appendStatement</p>
	 *
	 * @param test a {@link org.evosuite.testcase.TestCase} object.
	 * @param statement a {@link org.evosuite.testcase.StatementInterface} object.
	 * @throws org.evosuite.ga.ConstructionFailedException if any.
	 */
	public abstract void appendStatement(TestCase test, StatementInterface statement)
	        throws ConstructionFailedException;

	/**
	 * <p>deleteStatement</p>
	 *
	 * @param test a {@link org.evosuite.testcase.TestCase} object.
	 * @param position a int.
	 * @throws org.evosuite.ga.ConstructionFailedException if any.
	 */
	public abstract void deleteStatement(TestCase test, int position)
	        throws ConstructionFailedException;

	/**
	 * <p>deleteStatementGracefully</p>
	 *
	 * @param test a {@link org.evosuite.testcase.TestCase} object.
	 * @param position a int.
	 * @throws org.evosuite.ga.ConstructionFailedException if any.
	 */
	public abstract void deleteStatementGracefully(TestCase test, int position)
	        throws ConstructionFailedException;

	/**
	 * Replaces the call defined by statement with another call. After calling
	 * this method, statement is no longer a part of test. A new statement is
	 * created instead. Notice that Call is an understatement. This method (at
	 * least for the DefaultTestFactory) works for Fields, Constructors and
	 * Methods.
	 *
	 * @param test a {@link org.evosuite.testcase.TestCase} object.
	 * @param statement a {@link org.evosuite.testcase.StatementInterface} object.
	 * @return a boolean.
	 */
	public abstract boolean changeRandomCall(TestCase test, StatementInterface statement);

	/**
	 * Add constructor at given position if max recursion depth has not been
	 * reached
	 *
	 * @param constructor a {@link java.lang.reflect.Constructor} object.
	 * @param position a int.
	 * @param recursion_depth a int.
	 * @throws org.evosuite.ga.ConstructionFailedException if any.
	 * @param test a {@link org.evosuite.testcase.TestCase} object.
	 * @return a {@link org.evosuite.testcase.VariableReference} object.
	 */
	public abstract VariableReference addConstructor(TestCase test,
	        Constructor<?> constructor, int position, int recursion_depth)
	        throws ConstructionFailedException;

	/**
	 * Add method at given position if max recursion depth has not been reached
	 *
	 * @param test a {@link org.evosuite.testcase.TestCase} object.
	 * @param method a {@link java.lang.reflect.Method} object.
	 * @param position a int.
	 * @param recursion_depth a int.
	 * @throws org.evosuite.ga.ConstructionFailedException if any.
	 * @return a {@link org.evosuite.testcase.VariableReference} object.
	 */
	public abstract VariableReference addMethod(TestCase test, Method method,
	        int position, int recursion_depth) throws ConstructionFailedException;

	/**
	 * <p>attemptGeneration</p>
	 *
	 * @param test a {@link org.evosuite.testcase.TestCase} object.
	 * @param type a {@link java.lang.reflect.Type} object.
	 * @param position a int.
	 * @return a {@link org.evosuite.testcase.VariableReference} object.
	 * @throws org.evosuite.ga.ConstructionFailedException if any.
	 */
	public VariableReference attemptGeneration(TestCase test, Type type, int position)
	        throws ConstructionFailedException {
		return attemptGeneration(test, type, position, 0, false);
	}

	/**
	 * <p>attemptGenerationOrNull</p>
	 *
	 * @param test a {@link org.evosuite.testcase.TestCase} object.
	 * @param type a {@link java.lang.reflect.Type} object.
	 * @param position a int.
	 * @return a {@link org.evosuite.testcase.VariableReference} object.
	 * @throws org.evosuite.ga.ConstructionFailedException if any.
	 */
	public VariableReference attemptGenerationOrNull(TestCase test, Type type,
	        int position) throws ConstructionFailedException {
		return attemptGeneration(test, type, position, 0, true);
	}

	/**
	 * Try to generate an object of a given type
	 *
	 * @param test a {@link org.evosuite.testcase.TestCase} object.
	 * @param type a {@link java.lang.reflect.Type} object.
	 * @param position a int.
	 * @param recursion_depth a int.
	 * @param allow_null a boolean.
	 * @throws org.evosuite.ga.ConstructionFailedException if any.
	 * @return a {@link org.evosuite.testcase.VariableReference} object.
	 */
	protected abstract VariableReference attemptGeneration(TestCase test, Type type,
	        int position, int recursion_depth, boolean allow_null)
	        throws ConstructionFailedException;
}
