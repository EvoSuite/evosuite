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

import org.evosuite.Properties;
import org.evosuite.testcase.Scope;
import org.evosuite.testcase.StatementInterface;


/**
 * No method should throw an AssertionError
 * 
 * Note: this case is bit tricky, because assertions are disabled by default.
 * They need to be enabled when the JVM is started
 * 
 * @author Gordon Fraser
 * 
 */
public class AssertionErrorContract extends Contract {

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.contracts.Contract#check(de.unisb.cs.st.evosuite.testcase.Statement, de.unisb.cs.st.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	@Override
	public boolean check(StatementInterface statement, Scope scope, Throwable exception) {
		if (!Properties.ENABLE_ASSERTS_FOR_SUT) {
			throw new IllegalArgumentException(
			        "Cannot check for assert errors if they are not enabled");
		}

		if (!isTargetStatement(statement))
			return true;

		if (exception != null) {
			// method throws no AssertionError
			if (exception instanceof AssertionError) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "Assertion failed";
	}

}
