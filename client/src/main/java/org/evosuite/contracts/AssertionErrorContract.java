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

import java.util.List;

import org.evosuite.Properties;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.execution.Scope;


/**
 * No method should throw an AssertionError
 *
 * Note: this case is bit tricky, because assertions are disabled by default.
 * They need to be enabled when the JVM is started
 *
 * @author Gordon Fraser
 */
public class AssertionErrorContract extends Contract {

	/* (non-Javadoc)
	 * @see org.evosuite.contracts.Contract#check(org.evosuite.testcase.Statement, org.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	/** {@inheritDoc} */
	@Override
	public ContractViolation check(Statement statement, Scope scope, Throwable exception) {
		if (!Properties.ENABLE_ASSERTS_FOR_SUT) {
			throw new IllegalArgumentException(
			        "Cannot check for assert errors if they are not enabled");
		}

		if (!isTargetStatement(statement))
			return null;

		if (exception != null) {
			// method throws no AssertionError
			if (exception instanceof AssertionError) {
				return new ContractViolation(this, statement, exception);
			}
		}
		return null;
	}

	@Override
	public void addAssertionAndComments(Statement statement,
			List<VariableReference> variables, Throwable exception) {
		statement.addComment("Assertion violation: "+exception.getMessage());
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Assertion failed";
	}

}
