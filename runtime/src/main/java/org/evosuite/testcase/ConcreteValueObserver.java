/**
 * 
 */
package org.evosuite.testcase;

import java.util.HashMap;
import java.util.Map;

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
	public void beforeStatement(StatementInterface statement, Scope scope) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionObserver#afterStatement(org.evosuite.testcase.StatementInterface, org.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	@Override
	public void afterStatement(StatementInterface statement, Scope scope,
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
		Object object = scope.getObject(statement.getReturnValue());
		concreteValues.put(numStatement, object);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionObserver#clear()
	 */
	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

}
