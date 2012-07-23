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

import org.evosuite.testcase.ExecutionObserver;
import org.evosuite.testcase.Scope;
import org.evosuite.testcase.StatementInterface;

/**
 * <p>SingleContractChecker class.</p>
 *
 * @author fraser
 */
public class SingleContractChecker extends ExecutionObserver {

	private final Contract contract;

	private boolean valid = true;

	/**
	 * <p>Constructor for SingleContractChecker.</p>
	 *
	 * @param contract a {@link org.evosuite.contracts.Contract} object.
	 */
	public SingleContractChecker(Contract contract) {
		this.contract = contract;
	}

	/**
	 * <p>isValid</p>
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
	public void statement(StatementInterface statement, Scope scope, Throwable exception) {
		try {
			if (!contract.check(statement, scope, exception)) {
				//FailingTestSet.addFailingTest(currentTest, contract, statement, exception);
				valid = false;
			}
		} catch (Throwable t) {
			//logger.info("Caught exception during contract checking");
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionObserver#clear()
	 */
	/** {@inheritDoc} */
	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

}
