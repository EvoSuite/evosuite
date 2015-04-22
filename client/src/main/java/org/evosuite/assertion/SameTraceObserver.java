/**
 * 
 */
package org.evosuite.assertion;

import org.evosuite.testcase.statements.ArrayStatement;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.Scope;

/**
 * <p>
 * SameTraceObserver class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class SameTraceObserver extends AssertionTraceObserver<SameTraceEntry> {

	/* (non-Javadoc)
	 * @see org.evosuite.assertion.AssertionTraceObserver#visit(org.evosuite.testcase.StatementInterface, org.evosuite.testcase.Scope, org.evosuite.testcase.VariableReference)
	 */
	/** {@inheritDoc} */
	@Override
	protected void visit(Statement statement, Scope scope, VariableReference var) {
		// TODO: Only MethodStatement?
		if(statement.isAssignmentStatement())
			return;
		if(statement instanceof PrimitiveStatement<?>)
			return;
		if(statement instanceof ArrayStatement)
			return;
		if(statement instanceof ConstructorStatement)
			return;
		
		try {
			Object object = var.getObject(scope);
			if (object == null)
				return;
			if(var.isPrimitive())
				return;

			SameTraceEntry entry = new SameTraceEntry(var);

			for (VariableReference other : scope.getElements(var.getType())) {
				if (other == var)
					continue;

				Object otherObject = other.getObject(scope);
				if (otherObject == null)
					continue;
				if(otherObject.getClass() != object.getClass())
					continue;
				
				try {
					logger.debug("Comparison of " + var + " with " + other + " is: "
					        + object.equals(otherObject) +" ==> "+(object == otherObject));
					entry.addEntry(other, object == otherObject);
				} catch (Throwable t) {
					logger.debug("Exception during equals: " + t);
					// ignore?
				}
			}

			trace.addEntry(statement.getPosition(), var, entry);
		} catch (CodeUnderTestException e) {
			logger.debug("", e);
		}
	}

	@Override
	public void testExecutionFinished(ExecutionResult r) {
		// do nothing
	}
}