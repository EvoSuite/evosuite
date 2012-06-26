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

import java.lang.reflect.Method;

import org.evosuite.testcase.Scope;
import org.evosuite.testcase.StatementInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An object always has to equal itself
 * 
 * @author Gordon Fraser
 * 
 */
public class EqualsContract extends Contract {

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(Contract.class);

	/* (non-Javadoc)
	 * @see org.evosuite.contracts.Contract#check(org.evosuite.testcase.TestCase, org.evosuite.testcase.Statement, org.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	@Override
	public boolean check(StatementInterface statement, Scope scope, Throwable exception) {
		for (Object object : getAllObjects(scope)) {
			if (object == null)
				continue;

			// We do not want to call equals if it is the default implementation
			Class<?>[] parameters = { Object.class };
			try {
				Method equalsMethod = object.getClass().getMethod("equals", parameters);
				if (equalsMethod.getDeclaringClass().equals(Object.class))
					continue;

			} catch (SecurityException e1) {
				continue;
			} catch (NoSuchMethodException e1) {
				continue;
			}

			try {
				// An object always has to equal itself
				if (!object.equals(object))
					return false;

			} catch (NullPointerException e) {
				// No nullpointer exceptions may be thrown if the parameter was not null
				return false;
			} catch (Throwable t) {
				continue;
			}
		}

		return true;
	}

	@Override
	public String toString() {
		return "Equality check";
	}

}
