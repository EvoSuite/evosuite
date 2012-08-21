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
/**
 * 
 */
package org.evosuite.contracts;

import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.testcase.AssignmentStatement;
import org.evosuite.testcase.ConstructorStatement;
import org.evosuite.testcase.FieldReference;
import org.evosuite.testcase.MethodStatement;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.VariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * ContractViolation class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class ContractViolation {

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(ContractViolation.class);

	private final Contract contract;

	private TestCase test;

	private final StatementInterface statement;

	private final Throwable exception;

	/**
	 * <p>
	 * Constructor for ContractViolation.
	 * </p>
	 * 
	 * @param contract
	 *            a {@link org.evosuite.contracts.Contract} object.
	 * @param test
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param statement
	 *            a {@link org.evosuite.testcase.StatementInterface} object.
	 * @param exception
	 *            a {@link java.lang.Throwable} object.
	 */
	public ContractViolation(Contract contract, TestCase test,
	        StatementInterface statement, Throwable exception) {
		this.contract = contract;
		this.test = test.clone();
		this.statement = statement.clone(this.test);
		this.exception = exception;
	}

	/**
	 * Getter for test case
	 * 
	 * @return a {@link org.evosuite.testcase.TestCase} object.
	 */
	public TestCase getTestCase() {
		return test;
	}

	/**
	 * Getter for contract that was violated
	 * 
	 * @return a {@link org.evosuite.contracts.Contract} object.
	 */
	public Contract getContract() {
		return contract;
	}

	/**
	 * Remove all statements that do not contribute to the contract violation
	 */
	public void minimizeTest() {
		/** Factory method that handles statement deletion */
		TestFactory testFactory = TestFactory.getInstance();

		TestCase origTest = test.clone();

		boolean changed = true;
		while (changed) {
			changed = false;

			for (int i = test.size() - 1; i >= 0; i--) {
				try {
					testFactory.deleteStatementGracefully(test, i);
					if (!contract.fails(test)) {
						test = origTest.clone();
					} else {
						origTest = test.clone();
					}
				} catch (ConstructionFailedException e) {
					test = origTest.clone();
				}
			}
		}
	}

	/**
	 * Determine if we have already seen an instance of this violation
	 * 
	 * @param other
	 *            a {@link org.evosuite.contracts.ContractViolation} object.
	 * @return a boolean.
	 */
	public boolean same(ContractViolation other) {

		// Same contract?
		if (!contract.getClass().equals(other.contract.getClass()))
			return false;

		// Same type of statement?
		if (!statement.getClass().equals(other.statement.getClass()))
			return false;

		// Same exception type?
		if (exception != null && other.exception != null) {
			if (!exception.getClass().equals(other.exception.getClass()))
				return false;
		}

		// Same method call / constructor?
		if (statement instanceof MethodStatement) {
			MethodStatement ms1 = (MethodStatement) statement;
			MethodStatement ms2 = (MethodStatement) other.statement;
			if (ms1.getMethod().equals(ms2.getMethod())) {
				return true;
			}
		} else if (statement instanceof ConstructorStatement) {
			ConstructorStatement ms1 = (ConstructorStatement) statement;
			ConstructorStatement ms2 = (ConstructorStatement) other.statement;
			if (ms1.getConstructor().equals(ms2.getConstructor())) {
				return true;
			}
		} else if (statement instanceof AssignmentStatement) {
			VariableReference var1 = statement.getReturnValue();
			VariableReference var2 = other.statement.getReturnValue();
			if (var1 instanceof FieldReference && var2 instanceof FieldReference) {
				if (((FieldReference) var1).getField().equals(((FieldReference) var2).getField()))
					return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Violated contract: " + contract + " in statement " + statement
		        + " with exception " + exception;
	}

}
