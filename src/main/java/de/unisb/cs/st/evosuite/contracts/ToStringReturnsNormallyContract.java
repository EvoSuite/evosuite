/**
 * 
 */
package de.unisb.cs.st.evosuite.contracts;

import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor.TimeoutExceeded;

/**
 * @author Gordon Fraser
 * 
 */
public class ToStringReturnsNormallyContract extends Contract {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.contracts.Contract#check(de.unisb.cs.st.evosuite
	 * .testcase.TestCase, de.unisb.cs.st.evosuite.testcase.Statement,
	 * de.unisb.cs.st.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	@Override
	public boolean check(StatementInterface statement, Scope scope, Throwable exception) {
		for (Object object : getAllObjects(scope)) {
			if (object == null) {
				continue;
			}

			try {
				// toString must not throw an exception
				object.toString();

			} catch (Throwable t) {
				if (!(t instanceof TimeoutExceeded)) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public String toString() {
		return "toString returns normally check";
	}

}
