/**
 * 
 */
package de.unisb.cs.st.evosuite.contracts;

import java.lang.reflect.Method;

import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;

/**
 * @author Gordon Fraser
 * 
 */
public class EqualsSymmetricContract extends Contract {

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.contracts.Contract#check(de.unisb.cs.st.evosuite.testcase.Statement, de.unisb.cs.st.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	@Override
	public boolean check(StatementInterface statement, Scope scope, Throwable exception) {
		for (Pair pair : getAllObjectPairs(scope)) {
			if (pair.object1 == null || pair.object2 == null)
				continue;

			// We do not want to call equals if it is the default implementation
			Class<?>[] parameters = { Object.class };
			try {
				Method equalsMethod = pair.object1.getClass().getMethod("equals",
				                                                        parameters);
				if (equalsMethod.getDeclaringClass().equals(Object.class))
					continue;

			} catch (SecurityException e1) {
				continue;
			} catch (NoSuchMethodException e1) {
				continue;
			}

			if (pair.object1.equals(pair.object2)) {
				if (!pair.object2.equals(pair.object1))
					return false;
			} else {
				if (pair.object2.equals(pair.object1))
					return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "Equals symmetric check";
	}
}
