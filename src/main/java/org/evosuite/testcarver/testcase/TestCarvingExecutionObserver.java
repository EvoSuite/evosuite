package org.evosuite.testcarver.testcase;

import org.evosuite.testcarver.capture.FieldRegistry;
import org.evosuite.testcase.AssignmentStatement;
import org.evosuite.testcase.ExecutionObserver;
import org.evosuite.testcase.FieldReference;
import org.evosuite.testcase.Scope;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.VariableReference;
import org.evosuite.utils.GenericField;
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
	public void afterStatement(final StatementInterface statement, final Scope scope,
	        final Throwable exception) {
		if (statement instanceof AssignmentStatement) {
			final AssignmentStatement assign = (AssignmentStatement) statement;
			final VariableReference left = assign.getReturnValue();

			if (left instanceof FieldReference) {
				final FieldReference fieldRef = (FieldReference) left;
				final GenericField field = fieldRef.getField();

				FieldRegistry.notifyModification(this.captureId,
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
	public void beforeStatement(StatementInterface statement, Scope scope) {
		// Do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		// do nothing
	}

}
