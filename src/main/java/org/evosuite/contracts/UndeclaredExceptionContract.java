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

import java.util.Set;

import org.evosuite.testcase.CodeUnderTestException;
import org.evosuite.testcase.Scope;
import org.evosuite.testcase.StatementInterface;


/**
 * @author Gordon Fraser
 * 
 */
public class UndeclaredExceptionContract extends Contract {

	/* (non-Javadoc)
	 * @see org.evosuite.contracts.Contract#check(org.evosuite.testcase.StatementInterface, org.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	@Override
	public boolean check(StatementInterface statement, Scope scope, Throwable exception) {
		if (!isTargetStatement(statement))
			return true;

		if (exception != null) {
			Set<Class<?>> exceptions = statement.getDeclaredExceptions();

			if (!exceptions.contains(exception.getClass())) {
				if (exception instanceof CodeUnderTestException)
					return true;

				StackTraceElement element = exception.getStackTrace()[0];

				// If the exception was thrown in the test directly, it is also not interesting
				if (element.getClassName().startsWith("org.evosuite.testcase")) {
					return true;
				}

				/*
				 * even if possible handled by other contracts, that does not mean
				 * they check the signature. 
				 *
				// Assertion errors are checked by a different contract
				if (exception instanceof AssertionError)
					return true;

				// NullPointerExceptions are checked by a different contract
				if (exception instanceof NullPointerException) {
					return true;
				}
				*/

				return false;
			}
		}

		return true;
	}

	@Override
	public String toString() {
		return "Undeclared exception check";
	}
}
