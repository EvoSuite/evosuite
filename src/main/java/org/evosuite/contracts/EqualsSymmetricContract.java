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


/**
 * @author Gordon Fraser
 * 
 */
public class EqualsSymmetricContract extends Contract {

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.contracts.Contract#check(de.unisb.cs.st.evosuite.testcase.Statement, de.unisb.cs.st.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	@Override
	public boolean check(StatementInterface statement, Scope scope, Throwable exception) {
		for (Pair pair : getAllObjectPairs(scope)) {
			if (pair.object1 == null || pair.object2 == null)
				continue;

			// We do not want to call equals if it is the default implementation
			Class<?>[] parameters = { Object.class };
			try {
				Method equalsMethod = pair.object1.getClass().getMethod("equals",
				                                                        parameters);
				if (equalsMethod.getDeclaringClass().equals(Object.class))
					continue;

			} catch (SecurityException e1) {
				continue;
			} catch (NoSuchMethodException e1) {
				continue;
			}

			if (pair.object1.equals(pair.object2)) {
				if (!pair.object2.equals(pair.object1))
					return false;
			} else {
				if (pair.object2.equals(pair.object1))
					return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "Equals symmetric check";
	}
}
