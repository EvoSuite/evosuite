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
package org.evosuite.testcase;

import java.util.HashMap;
import java.util.Map;

import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.ExecutionObserver;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;

/**
 * @author Gordon Fraser
 * 
 */
public class ConcreteValueObserver extends ExecutionObserver {

	private final Map<Integer, Object> concreteValues = new HashMap<Integer, Object>();

	public Map<Integer, Object> getConcreteValues() {
		return concreteValues;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionObserver#output(int, java.lang.String)
	 */
	@Override
	public void output(int position, String output) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionObserver#beforeStatement(org.evosuite.testcase.StatementInterface, org.evosuite.testcase.Scope)
	 */
	@Override
	public void beforeStatement(Statement statement, Scope scope) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionObserver#afterStatement(org.evosuite.testcase.StatementInterface, org.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	@Override
	public void afterStatement(Statement statement, Scope scope,
	        Throwable exception) {
		int numStatement = statement.getPosition();
		VariableReference returnValue = statement.getReturnValue();
		if (!returnValue.isPrimitive()) {
			// Only interested in primitive values
			return;
		}
		TestCase test = super.getCurrentTest();
		if (test.getStatement(returnValue.getStPosition()) instanceof PrimitiveStatement<?>) {
			// Don't need to collect primitive statement values
			return;
		}
		try {
			Object object = statement.getReturnValue().getObject(scope);
			concreteValues.put(numStatement, object);
		} catch(CodeUnderTestException e) {
			// Ignore
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionObserver#clear()
	 */
	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public void testExecutionFinished(ExecutionResult r, Scope s) {
		// do nothing
	}
}
