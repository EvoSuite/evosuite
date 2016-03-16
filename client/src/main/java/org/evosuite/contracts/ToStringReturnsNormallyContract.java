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
import java.util.Arrays;
import java.util.List;

import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.execution.TestCaseExecutor.TimeoutExceeded;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.utils.generic.GenericMethod;


/**
 * <p>ToStringReturnsNormallyContract class.</p>
 *
 * @author Gordon Fraser
 */
public class ToStringReturnsNormallyContract extends Contract {

	/* (non-Javadoc)
	 * @see org.evosuite.contracts.Contract#check(org.evosuite.testcase.TestCase, org.evosuite.testcase.Statement, org.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	/** {@inheritDoc} */
	@Override
	public ContractViolation check(Statement statement, Scope scope, Throwable exception) {
		for(VariableReference var : getAllVariables(scope)) {
			logger.debug("Current variable: "+var);
			Object object = scope.getObject(var);

			if (object == null) {
				logger.debug("Current object is null");
				continue;
			}

			// We do not want to call toString if it is the default implementation
			Class<?>[] parameters = {};
			try {
				Method equalsMethod = object.getClass().getMethod("toString", parameters);
				if (equalsMethod.getDeclaringClass().equals(Object.class))
					continue;

			} catch (SecurityException e1) {
				continue;
			} catch (NoSuchMethodException e1) {
				continue;
			}

			try {
				// toString must not throw an exception
				object.toString();

			} catch (Throwable t) {
				if (!(t instanceof TimeoutExceeded)) {
					logger.debug("Violation found");
					return new ContractViolation(this, statement, t, var);					
				} else {
					logger.debug("Timeout");
				}
			}
		}

		return null;
	}

	@Override
	public void addAssertionAndComments(Statement statement,
			List<VariableReference> variables, Throwable exception) {
		TestCase test = statement.getTestCase();
		int position = statement.getPosition();
		VariableReference a = variables.get(0);

		try {
			Method hashCodeMethod = a.getGenericClass().getRawClass().getMethod("toString", new Class<?>[] {});

			GenericMethod method = new GenericMethod(hashCodeMethod, a.getGenericClass());

			Statement st1 = new MethodStatement(test, method, a, Arrays.asList(new VariableReference[] {}));
			test.addStatement(st1, position + 1);
			st1.addComment("Throws exception: "+exception.getMessage());
			
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "toString returns normally check";
	}

}
