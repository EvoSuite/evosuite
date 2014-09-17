/**
 * 
 */
package org.evosuite.assertion;

import org.evosuite.Properties;
import org.evosuite.testcase.CodeUnderTestException;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.Scope;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.VariableReference;
import org.objectweb.asm.Type;

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
		if(statement.isAssignmentStatement())
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
				if(other.isPrimitive())
					continue;
				Object otherObject = other.getObject(scope);
				if (otherObject == null)
					continue;

				if (Properties.PURE_EQUALS) {
					String className = object.getClass().getCanonicalName();
					String methodName = "equals";
					String descriptor = Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(Object.class));
					CheapPurityAnalyzer cheapPurityAnalyzer = CheapPurityAnalyzer.getInstance();
					if (!cheapPurityAnalyzer.isPure(className, methodName, descriptor))
						continue; //Don't compare using impure equals(Object) methods		
				}
				
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