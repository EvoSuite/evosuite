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
import org.evosuite.testcase.AbstractTestFactory;
import org.evosuite.testcase.ConstructorStatement;
import org.evosuite.testcase.DefaultTestFactory;
import org.evosuite.testcase.MethodStatement;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Gordon Fraser
 * 
 */
public class ContractViolation {

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(ContractViolation.class);

	private final Contract contract;

	private TestCase test;

	private final StatementInterface statement;

	private final Throwable exception;

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
	 * @return
	 */
	public TestCase getTestCase() {
		return test;
	}

	/**
	 * Getter for contract that was violated
	 * 
	 * @return
	 */
	public Contract getContract() {
		return contract;
	}

	/**
	 * Remove all statements that do not contribute to the contract violation
	 */
	public void minimizeTest() {
		/** Factory method that handles statement deletion */
		AbstractTestFactory testFactory = DefaultTestFactory.getInstance();

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
	 * @return
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
		}
		return false;
	}

}
