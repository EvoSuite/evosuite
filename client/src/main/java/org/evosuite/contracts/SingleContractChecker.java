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
/**
 * 
 */
package org.evosuite.contracts;

import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.execution.ExecutionObserver;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * SingleContractChecker class.
 * </p>
 * 
 * @author fraser
 */
public class SingleContractChecker extends ExecutionObserver {

	private final Contract contract;
	
	private final static Logger logger = LoggerFactory.getLogger(SingleContractChecker.class);

	private boolean valid = true;

	/**
	 * <p>
	 * Constructor for SingleContractChecker.
	 * </p>
	 * 
	 * @param contract
	 *            a {@link org.evosuite.contracts.Contract} object.
	 */
	public SingleContractChecker(Contract contract) {
		this.contract = contract;
	}

	/**
	 * <p>
	 * isValid
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public boolean isValid() {
		return valid;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionObserver#output(int, java.lang.String)
	 */
	/** {@inheritDoc} */
	@Override
	public void output(int position, String output) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionObserver#statement(org.evosuite.testcase.StatementInterface, org.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	/** {@inheritDoc} */
	@Override
	public void afterStatement(Statement statement, Scope scope,
	        Throwable exception) {
		try {
			logger.debug("Checking contract "+contract);
			if (contract.check(statement, scope, exception) != null) {
				//FailingTestSet.addFailingTest(currentTest, contract, statement, exception);
				valid = false;
			}
		} catch (Throwable t) {
			logger.info("Caught exception during contract checking: "+t);
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionObserver#beforeStatement(org.evosuite.testcase.StatementInterface, org.evosuite.testcase.Scope)
	 */
	@Override
	public void beforeStatement(Statement statement, Scope scope) {
		// Do nothing
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionObserver#clear()
	 */
	/** {@inheritDoc} */
	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public void testExecutionFinished(ExecutionResult r, Scope s) {
		// do nothing
	}
}
