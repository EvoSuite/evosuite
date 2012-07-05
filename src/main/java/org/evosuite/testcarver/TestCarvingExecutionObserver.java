package org.evosuite.testcarver;

import java.lang.reflect.Field;

import org.eclipse.jdt.core.dom.FieldAccess;
import org.evosuite.testcase.AssignmentStatement;
import org.evosuite.testcase.ExecutionObserver;
import org.evosuite.testcase.FieldReference;
import org.evosuite.testcase.FieldStatement;
import org.evosuite.testcase.MethodStatement;
import org.evosuite.testcase.Scope;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.VariableReference;
import org.evosuite.utils.LoggingUtils;
import org.objectweb.asm.Type;

import de.unisb.cs.st.testcarver.capture.FieldRegistry;

/**
 * <p>TestCarvingExecutionObserver class.</p>
 *
 * @author Gordon Fraser
 */
public final class TestCarvingExecutionObserver extends ExecutionObserver
{
	private int captureId;
	
	/**
	 * <p>Constructor for TestCarvingExecutionObserver.</p>
	 */
	public TestCarvingExecutionObserver()
	{
		// We can't know the max captureId calculated in the test carving related 
		// instrumentation. However, we know the captureId starts with Integer.MIN_VALUE.
		// For this reason, we start with Integer.MAX_VALUE and decrement the captureId
		// to avoid id collisions 
		this.captureId = Integer.MAX_VALUE;
	}


	/** {@inheritDoc} */
	@Override
	public void output(int position, String output) {
		// do nothing
	}
	
	
	/**
	 * {@inheritDoc}
	 *
	 * own comment..
	 */
	@Override
	public void statement(final StatementInterface statement, final Scope scope, final Throwable exception)
	{
		if (statement instanceof AssignmentStatement)
		{
			final AssignmentStatement assign = (AssignmentStatement) statement;
			final VariableReference   left   = assign.getReturnValue();
			
			if(left instanceof FieldReference)
			{
				final FieldReference fieldRef = (FieldReference) left;
				final Field			 field    = fieldRef.getField();
				
				FieldRegistry.notifyModification(this.captureId, 
													Type.getInternalName(field.getDeclaringClass()), 
													field.getName(), 
													Type.getDescriptor(field.getType()));
				//PUTFIELDRegistry creates PUTXXX as well as corresponding GETXXX statements
			    this.captureId-=2;
			}
		}
	}
	

	/** {@inheritDoc} */
	@Override
	public void clear() {
		// do nothing
	}

}
