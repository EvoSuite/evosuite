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
package org.evosuite.assertion;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.Scope;

public class PrimitiveFieldTraceObserver extends
        AssertionTraceObserver<PrimitiveFieldTraceEntry> {

	/* (non-Javadoc)
	 * @see org.evosuite.assertion.AssertionTraceObserver#visit(org.evosuite.testcase.StatementInterface, org.evosuite.testcase.Scope, org.evosuite.testcase.VariableReference)
	 */
	/** {@inheritDoc} */
	@Override
	protected void visit(Statement statement, Scope scope, VariableReference var) {
		logger.debug("Checking fields of " + var);
		try {
			if (var == null)
				return;

			if(statement.isAssignmentStatement()) {
				if(statement.getReturnValue().isArrayIndex()) {
					return;
				}
				if(statement.getReturnValue().isFieldReference()) {
					return;
				}
			}


			Object object = var.getObject(scope);
			int position = statement.getPosition();

			if (object != null && !object.getClass().isPrimitive()
			        && !object.getClass().isEnum() && !isWrapperType(object.getClass())) {

				PrimitiveFieldTraceEntry entry = new PrimitiveFieldTraceEntry(var);

				for (Field field : var.getVariableClass().getFields()) {
					// TODO Check for wrapper types
					if (Modifier.isPublic(field.getModifiers())
					        && !field.getType().equals(void.class)
					        && field.getType().isPrimitive()
					        && !Modifier.isFinal(field.getModifiers())
					        && !field.isSynthetic()) {
						try {
							logger.debug("Keeping field " + field + " with value "
							        + field.get(object));
							entry.addValue(field, field.get(object));
						} catch (IllegalArgumentException e) {
						} catch (IllegalAccessException e) {
						}
					}
				}
				trace.addEntry(position, var, entry);
			}
		} catch (CodeUnderTestException e) {
			logger.debug("", e);
			//throw new UnsupportedOperationException();
		}
	}

	@Override
	public void testExecutionFinished(ExecutionResult r, Scope s) {
		// do nothing
	}
}