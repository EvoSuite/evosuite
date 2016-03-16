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

import java.util.ArrayList;
import java.util.List;

import org.evosuite.PackageInfo;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;

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
	public ContractViolation check(Statement statement, Scope scope, Throwable exception) {
		if (!isTargetStatement(statement))
			return null;

		try {
			if (exception != null) {
				// method throws no NullPointerException if no input parameter was null
				if (exception instanceof NullPointerException) {

					StackTraceElement element = exception.getStackTrace()[0];

					// If the exception was thrown in the test directly, it is also not interesting
					if (element.getClassName().startsWith(PackageInfo.getEvoSuitePackage()+".testcase")) {
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
	public void addAssertionAndComments(Statement statement,
			List<VariableReference> variables, Throwable exception) {
		statement.addComment("Throws NullPointerException: " +exception.getMessage());
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "NullPointerException";
	}

}
