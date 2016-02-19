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
package org.evosuite.assertion;

import java.lang.reflect.Array;

import org.evosuite.Properties;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.ArrayStatement;
import org.evosuite.testcase.statements.AssignmentStatement;
import org.evosuite.testcase.statements.FunctionalMockStatement;
import org.evosuite.testcase.statements.PrimitiveStatement;

/**
 * @author Gordon Fraser
 * 
 */
public class ArrayTraceObserver extends AssertionTraceObserver<ArrayTraceEntry> {

	/** {@inheritDoc} */
	@Override
	public synchronized void afterStatement(Statement statement, Scope scope,
	        Throwable exception) {
		// By default, no assertions are created for statements that threw exceptions
		if(exception != null)
			return;
		
		// No assertions are created for mock statements
		if(statement instanceof FunctionalMockStatement)
			return;

		visitReturnValue(statement, scope);
		visitDependencies(statement, scope);
	}

	private Object[] getArray(Object val) {
		int arrlength = Array.getLength(val);
		Object[] outputArray = new Object[arrlength];
		for (int i = 0; i < arrlength; ++i) {
			outputArray[i] = Array.get(val, i);
		}
		return outputArray;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.assertion.AssertionTraceObserver#visit(org.evosuite.testcase.StatementInterface, org.evosuite.testcase.Scope, org.evosuite.testcase.VariableReference)
	 */
	/** {@inheritDoc} */
	@Override
	protected void visit(Statement statement, Scope scope, VariableReference var) {
		logger.debug("Checking array " + var);
		try {
			// Need only legal values
			if (var == null)
				return;

			// We don't need assertions on constant values
			if (statement instanceof PrimitiveStatement<?>)
				return;

			// We don't need assertions on array assignments
			if (statement instanceof AssignmentStatement)
				return;

			// We don't need assertions on array declarations
			if (statement instanceof ArrayStatement)
				return;

			Object object = var.getObject(scope);

			// We don't need to compare to null
			if (object == null)
				return;

			// We are only interested in arrays
			if (!object.getClass().isArray())
				return;

			// We are only interested in primitive arrays
			if (!object.getClass().getComponentType().isPrimitive())
				return;

			if (var.getComponentClass() == null)
				return;

			// Don't include very long arrays in assertions, as code may fail to compile
			if(Array.getLength(object) > Properties.MAX_ARRAY) 
				return;
			
			logger.debug("Observed value " + object + " for statement "
			        + statement.getCode());
			trace.addEntry(statement.getPosition(), var, new ArrayTraceEntry(var,
			        getArray(object)));

		} catch (CodeUnderTestException e) {
			logger.debug("", e);
		}
	}

	@Override
	public void testExecutionFinished(ExecutionResult r, Scope s) {
		// do nothing
	}
}
