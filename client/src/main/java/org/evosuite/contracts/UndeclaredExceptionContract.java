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
import java.util.Set;

import org.evosuite.PackageInfo;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.MethodStatement;


/**
 * <p>UndeclaredExceptionContract class.</p>
 *
 * @author Gordon Fraser
 */
public class UndeclaredExceptionContract extends Contract {

	/* (non-Javadoc)
	 * @see org.evosuite.contracts.Contract#check(org.evosuite.testcase.StatementInterface, org.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	/** {@inheritDoc} */
	@Override
	public ContractViolation check(Statement statement, Scope scope, Throwable exception) {
		if (!isTargetStatement(statement))
			return null;

		if (exception != null) {
			Set<Class<?>> exceptions = statement.getDeclaredExceptions();

			if (!exceptions.contains(exception.getClass())) {
				if (exception instanceof CodeUnderTestException)
					return null;

				StackTraceElement element = exception.getStackTrace()[0];

				// If the exception was thrown in the test directly, it is also not interesting
				if (element.getClassName().startsWith(PackageInfo.getEvoSuitePackage()+".testcase")) {
					return null;
				}

				/*
				 * even if possible handled by other contracts, that does not mean
				 * they check the signature. 
				 * TODO: Not sure I can follow, what does that have to do with the signature
				 */
				// Assertion errors are checked by a different contract
				if (exception instanceof AssertionError)
					return null;
				/*
				// NullPointerExceptions are checked by a different contract
				if (exception instanceof NullPointerException) {
					return true;
				}
				*/
				if(statement instanceof MethodStatement) {
					// hashCode and toString are covered already
					String methodName = ((MethodStatement)statement).getMethod().getName();
					if(methodName.equals("toString") || methodName.equals("hashCode")) {
						return null;
					}
					
				}
				

				return new ContractViolation(this, statement, exception);
			}
		}

		return null;
	}
	
	@Override
	public void addAssertionAndComments(Statement statement,
			List<VariableReference> variables, Throwable exception) {
		statement.addComment("Throws undeclared exception: " +exception.getMessage());
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Undeclared exception check";
	}
}
