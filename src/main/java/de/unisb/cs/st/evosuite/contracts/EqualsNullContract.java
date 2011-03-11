/**
 * 
 */
package de.unisb.cs.st.evosuite.contracts;

import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.Statement;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * @author Gordon Fraser
 * 
 */
public class EqualsNullContract implements Contract {

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.contracts.Contract#check(de.unisb.cs.st.evosuite.testcase.TestCase, de.unisb.cs.st.evosuite.testcase.Statement, de.unisb.cs.st.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	@Override
	public boolean check(Statement statement, Scope scope, Throwable exception) {
		VariableReference var = statement.getReturnValue();
		Object object = scope.get(var);
		if (object != null) {
			try {
				// An object always must not equal null
				if (object.equals(null))
					return false;

			} catch (Throwable t) {
				return true;
			}
		}

		return true;
	}

	@Override
	public String toString() {
		return "Equals null check";
	}

}
