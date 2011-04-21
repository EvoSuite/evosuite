/**
 * 
 */
package de.unisb.cs.st.evosuite.contracts;

import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.Statement;

/**
 * An object must never equal null
 * 
 * @author Gordon Fraser
 * 
 */
public class EqualsNullContract extends Contract {

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.contracts.Contract#check(de.unisb.cs.st.evosuite.testcase.TestCase, de.unisb.cs.st.evosuite.testcase.Statement, de.unisb.cs.st.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	@Override
	public boolean check(Statement statement, Scope scope, Throwable exception) {
		for (Object object : getAllObjects(scope)) {
			if (object == null)
				continue;

			try {
				// An object always has to equal itself
				if (object.equals(null))
					return false;

			} catch (Throwable t) {
				continue;
			}
		}

		return true;
	}

	@Override
	public String toString() {
		return "Equals null check";
	}

}
