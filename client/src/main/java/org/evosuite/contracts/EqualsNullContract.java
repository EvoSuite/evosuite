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

import java.lang.reflect.Method;
import java.util.List;

import org.evosuite.assertion.EqualsAssertion;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.execution.Scope;


/**
 * An object must never equal null
 *
 * @author Gordon Fraser
 */
public class EqualsNullContract extends Contract {

	/* (non-Javadoc)
	 * @see org.evosuite.contracts.Contract#check(org.evosuite.testcase.TestCase, org.evosuite.testcase.Statement, org.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	/** {@inheritDoc} */
	@Override
	public ContractViolation check(Statement statement, Scope scope, Throwable exception) {
		for(VariableReference var : getAllVariables(scope)) {
			logger.debug("Current variable: "+var);
			Object object = scope.getObject(var);
			logger.debug("Current object: "+object);
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
			logger.debug("Defines equals");

			try {
				// An object may not equal to null
				if (object.equals(null)) {
					logger.debug("Violation found");
					return new ContractViolation(this, statement, exception, var);
				} else {
					logger.debug("No violation found");
				}

			} catch (Throwable t) {
				logger.debug("Exception: "+t);
				continue;
			}
		}

		return null;
	}

	@Override
	public void addAssertionAndComments(Statement statement,
			List<VariableReference> variables, Throwable exception) {
		EqualsAssertion assertion = new EqualsAssertion();
		assertion.setStatement(statement);
		VariableReference var = variables.get(0);
		assertion.setSource(var);
		assertion.setDest(new ConstantValue(statement.getTestCase(), Object.class));
		//assertion.setDest(new NullReference(statement.getTestCase(), var.getType()));
		assertion.setValue(false);
		statement.addAssertion(assertion);
		statement.addComment("Violates contract equals(null)");
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Equals null check";
	}

}
