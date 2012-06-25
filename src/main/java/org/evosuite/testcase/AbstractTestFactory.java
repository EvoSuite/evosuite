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
 * 
 */
public abstract class AbstractTestFactory implements Serializable {

	private static final long serialVersionUID = -2328803453596295741L;

	public abstract void changeCall(TestCase test, StatementInterface statement,
	        AccessibleObject call) throws ConstructionFailedException;

	public abstract void insertRandomStatement(TestCase test);

	public abstract void appendStatement(TestCase test, StatementInterface statement)
	        throws ConstructionFailedException;

	public abstract void deleteStatement(TestCase test, int position)
	        throws ConstructionFailedException;

	public abstract void deleteStatementGracefully(TestCase test, int position)
	        throws ConstructionFailedException;

	/**
	 * Replaces the call defined by statement with another call. After calling
	 * this method, statement is no longer a part of test. A new statement is
	 * created instead. Notice that Call is an understatement. This method (at
	 * least for the DefaultTestFactory) works for Fields, Constructors and
	 * Methods.
	 * 
	 * @param test
	 * @param statement
	 * @return
	 */
	public abstract boolean changeRandomCall(TestCase test, StatementInterface statement);

	/**
	 * Add constructor at given position if max recursion depth has not been
	 * reached
	 * 
	 * @param constructor
	 * @param position
	 * @param recursion_depth
	 * @return
	 * @throws ConstructionFailedException
	 */
	public abstract VariableReference addConstructor(TestCase test,
	        Constructor<?> constructor, int position, int recursion_depth)
	        throws ConstructionFailedException;

	/**
	 * Add method at given position if max recursion depth has not been reached
	 * 
	 * @param test
	 * @param method
	 * @param position
	 * @param recursion_depth
	 * @return
	 * @throws ConstructionFailedException
	 */
	public abstract VariableReference addMethod(TestCase test, Method method,
	        int position, int recursion_depth) throws ConstructionFailedException;

	public VariableReference attemptGeneration(TestCase test, Type type, int position)
	        throws ConstructionFailedException {
		return attemptGeneration(test, type, position, 0, false);
	}

	public VariableReference attemptGenerationOrNull(TestCase test, Type type,
	        int position) throws ConstructionFailedException {
		return attemptGeneration(test, type, position, 0, true);
	}

	/**
	 * Try to generate an object of a given type
	 * 
	 * @param test
	 * @param type
	 * @param position
	 * @param recursion_depth
	 * @param constraint
	 * @param allow_null
	 * @return
	 * @throws ConstructionFailedException
	 */
	protected abstract VariableReference attemptGeneration(TestCase test, Type type,
	        int position, int recursion_depth, boolean allow_null)
	        throws ConstructionFailedException;
}
