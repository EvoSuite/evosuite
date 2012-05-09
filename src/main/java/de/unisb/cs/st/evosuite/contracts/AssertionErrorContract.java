/**
 * 
 */
package de.unisb.cs.st.evosuite.contracts;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;

/**
 * No method should throw an AssertionError
 * 
 * Note: this case is bit tricky, because assertions are disabled by default.
 * They need to be enabled when the JVM is started
 * 
 * @author Gordon Fraser
 * 
 */
public class AssertionErrorContract extends Contract {

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.contracts.Contract#check(de.unisb.cs.st.evosuite.testcase.Statement, de.unisb.cs.st.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	@Override
	public boolean check(StatementInterface statement, Scope scope, Throwable exception) {
		if (!Properties.ENABLE_ASSERTS_FOR_SUT) {
			throw new IllegalArgumentException(
			        "Cannot check for assert errors if they are not enabled");
		}

		if (!isTargetStatement(statement))
			return true;

		if (exception != null) {
			// method throws no AssertionError
			if (exception instanceof AssertionError) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "Assertion failed";
	}

}
