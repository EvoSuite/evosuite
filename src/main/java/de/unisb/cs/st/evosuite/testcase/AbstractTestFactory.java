/**
 * Copyright (C) 2012 Gordon Fraser, Andrea Arcuri
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
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.testcase;

import java.io.Serializable;
import java.lang.reflect.AccessibleObject;

import de.unisb.cs.st.evosuite.ga.ConstructionFailedException;

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
	 * Replaces the call defined by statement with another call. After calling this method, statement is no longer a part of test.
	 * A new statement is created instead. 
	 * Notice that Call is an understatement. This method (at least for the DefaultTestFactory) works for Fields, Constructors and Methods.
	 * @param test
	 * @param statement
	 * @return
	 */
	public abstract boolean changeRandomCall(TestCase test, StatementInterface statement);
}
