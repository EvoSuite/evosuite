/**
 * 
 */
package de.unisb.cs.st.evosuite.contracts;

import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.Statement;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor.TimeoutExceeded;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * @author Gordon Fraser
 * 
 */
public class ToStringReturnsNormally implements Contract {

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.contracts.Contract#check(de.unisb.cs.st.evosuite.testcase.TestCase, de.unisb.cs.st.evosuite.testcase.Statement, de.unisb.cs.st.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	@Override
	public boolean check(Statement statement, Scope scope, Throwable exception) {
		VariableReference var = statement.getReturnValue();
		Object object = scope.get(var);
		if (object != null) {
			try {
				// toString must not throw an exception
				object.toString();

			} catch (Throwable t) {
				if (!(t instanceof TimeoutExceeded))
					return false;
			}
		}

		return true;
	}

	@Override
	public String toString() {
		return "toString returns normally check";
	}

}
