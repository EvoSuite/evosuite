/**
 * 
 */
package org.evosuite.assertion;

import org.evosuite.testcase.CodeUnderTestException;
import org.evosuite.testcase.Scope;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.VariableReference;

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
	protected void visit(StatementInterface statement, Scope scope, VariableReference var) {
		try {
			Object object = var.getObject(scope);
			if (object == null)
				return;

			SameTraceEntry entry = new SameTraceEntry(var);

			for (VariableReference other : scope.getElements(var.getType())) {
				Object otherObject = other.getObject(scope);
				if (other == var)
					continue;
				if (otherObject == null)
					continue;

				try {
					logger.debug("Comparison of " + var + " with " + other + " is: "
					        + object.equals(otherObject));
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
}
