/**
 * 
 */
package de.unisb.cs.st.evosuite.contracts;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.Statement;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * @author Gordon Fraser
 * 
 */
public class EqualsContract implements Contract {

	private static Logger logger = Logger.getLogger(Contract.class);

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.contracts.Contract#check(de.unisb.cs.st.evosuite.testcase.TestCase, de.unisb.cs.st.evosuite.testcase.Statement, de.unisb.cs.st.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	@Override
	public boolean check(Statement statement, Scope scope, Throwable exception) {
		VariableReference var = statement.getReturnValue();
		Object object = scope.get(var);
		if (object != null) {
			try {
				// An object always has to equal itself
				if (!object.equals(object))
					return false;

			} catch (NullPointerException e) {
				// No nullpointer exceptions may be thrown if the parameter was not null
				return false;
			} catch (Throwable t) {
				return true;
			}
		}

		return true;
	}

	@Override
	public String toString() {
		return "Equality check";
	}

}
