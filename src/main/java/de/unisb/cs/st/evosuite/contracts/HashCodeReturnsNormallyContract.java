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
/**
 * 
 */
package de.unisb.cs.st.evosuite.contracts;

import java.lang.reflect.Method;

import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor.TimeoutExceeded;

/**
 * @author Gordon Fraser
 * 
 */
public class HashCodeReturnsNormallyContract extends Contract {

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.contracts.Contract#check(de.unisb.cs.st.evosuite.testcase.Statement, de.unisb.cs.st.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	@Override
	public boolean check(StatementInterface statement, Scope scope, Throwable exception) {
		for (Object object : getAllObjects(scope)) {
			if (object == null)
				continue;

			// We do not want to call hashCode if it is the default implementation
			Class<?>[] parameters = {};
			try {
				Method equalsMethod = object.getClass().getMethod("hashCode", parameters);
				if (equalsMethod.getDeclaringClass().equals(Object.class))
					continue;

			} catch (SecurityException e1) {
				continue;
			} catch (NoSuchMethodException e1) {
				continue;
			}

			try {
				// hashCode must not throw an exception
				object.hashCode();

			} catch (Throwable t) {
				if (!(t instanceof TimeoutExceeded))
					return false;
			}
		}

		return true;
	}

	@Override
	public String toString() {
		return "hashCode returns normally check";
	}
}
