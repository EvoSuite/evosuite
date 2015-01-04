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

import java.util.ArrayList;
import java.util.List;

import org.evosuite.testcase.CodeUnderTestException;
import org.evosuite.testcase.ConstructorStatement;
import org.evosuite.testcase.MethodStatement;
import org.evosuite.testcase.Scope;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.VariableReference;

/**
 * <p>
 * NullPointerExceptionContract class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class NullPointerExceptionContract extends Contract {

	/* (non-Javadoc)
	 * @see org.evosuite.contracts.Contract#check(org.evosuite.testcase.TestCase, org.evosuite.testcase.Statement, org.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	/** {@inheritDoc} */
	@Override
	public ContractViolation check(StatementInterface statement, Scope scope, Throwable exception) {
		if (!isTargetStatement(statement))
			return null;

		try {
			if (exception != null) {
				// method throws no NullPointerException if no input parameter was null
				if (exception instanceof NullPointerException) {

					StackTraceElement element = exception.getStackTrace()[0];

					// If the exception was thrown in the test directly, it is also not interesting
					if (element.getClassName().startsWith("org.evosuite.testcase")) {
						return null;
					}

					List<VariableReference> parameters = new ArrayList<VariableReference>();
					if (statement instanceof MethodStatement) {
						MethodStatement ms = (MethodStatement) statement;
						parameters.addAll(ms.getParameterReferences());
					} else if (statement instanceof ConstructorStatement) {
						ConstructorStatement cs = (ConstructorStatement) statement;
						parameters.addAll(cs.getParameterReferences());
					} else {
						return null;
					}
					boolean hasNull = false;
					for (VariableReference var : parameters) {
						if (var.getObject(scope) == null) {
							hasNull = true;
							break;
						}
					}
					if (!hasNull) {
						return new ContractViolation(this, statement, exception);
					}
				}
			}

			return null;
		} catch (CodeUnderTestException e) {
			throw new UnsupportedOperationException();
		}
	}
	
	@Override
	public void addAssertionAndComments(StatementInterface statement,
			List<VariableReference> variables, Throwable exception) {
		statement.addComment("Throws NullPointerException: " +exception.getMessage());
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "NullPointerException";
	}

}
