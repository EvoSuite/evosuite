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
package org.evosuite.testcarver.testcase;

import org.evosuite.testcarver.capture.FieldRegistry;
import org.evosuite.testcase.variable.FieldReference;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.execution.ExecutionObserver;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.AssignmentStatement;
import org.evosuite.utils.generic.GenericField;
import org.objectweb.asm.Type;

public final class TestCarvingExecutionObserver extends ExecutionObserver {
	private int captureId;

	public TestCarvingExecutionObserver() {
		// We can't know the max captureId calculated in the test carving related 
		// instrumentation. However, we know the captureId starts with Integer.MIN_VALUE.
		// For this reason, we start with Integer.MAX_VALUE and decrement the captureId
		// to avoid id collisions 
		this.captureId = Integer.MAX_VALUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void output(int position, String output) {
		// do nothing
	}

	/**
	 * own comment..
	 */
	@Override
	public void afterStatement(final Statement statement, final Scope scope,
	        final Throwable exception) {
		if (statement instanceof AssignmentStatement) {
			final AssignmentStatement assign = (AssignmentStatement) statement;
			final VariableReference left = assign.getReturnValue();

			if (left instanceof FieldReference) {
				final FieldReference fieldRef = (FieldReference) left;
				final GenericField field = fieldRef.getField();

				FieldRegistry.notifyModification(field.isStatic() ? null : scope.getObject(fieldRef.getSource()), this.captureId,
				                                 Type.getInternalName(field.getDeclaringClass()),
				                                 field.getName(),
				                                 Type.getDescriptor(field.getField().getType()));
				//PUTFIELDRegistry creates PUTXXX as well as corresponding GETXXX statements
				this.captureId -= 2;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionObserver#beforeStatement(org.evosuite.testcase.StatementInterface, org.evosuite.testcase.Scope)
	 */
	@Override
	public void beforeStatement(Statement statement, Scope scope) {
		// Do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		// do nothing
	}

	@Override
	public void testExecutionFinished(ExecutionResult r, Scope s) {
		// do nothing
	}

}
