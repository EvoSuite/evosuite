/**
 * 
 */
package de.unisb.cs.st.evosuite.contracts;

import java.lang.reflect.Method;

import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;

/**
 * o1.equals(o2) => o1.hashCode() == o2.hashCode()
 * 
 * @author Gordon Fraser
 * 
 */
public class EqualsHashcodeContract extends Contract {

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.contracts.Contract#check(de.unisb.cs.st.evosuite.testcase.Statement, de.unisb.cs.st.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	@Override
	public boolean check(StatementInterface statement, Scope scope, Throwable exception) {
		for (Pair pair : getAllObjectPairs(scope)) {
			if (pair.object1 == null || pair.object2 == null)
				continue;

			// We do not want to call hashcode if it is the default implementation
			Class<?>[] parameters = { Object.class };
			try {
				Method equalsMethod = pair.object1.getClass().getMethod("equals",
				                                                        parameters);
				Method hashCodeMethod = pair.object1.getClass().getMethod("hashCode",
				                                                          new Class<?>[0]);
				if (equalsMethod.getDeclaringClass().equals(Object.class)
				        || hashCodeMethod.getDeclaringClass().equals(Object.class))
					continue;

			} catch (SecurityException e1) {
				continue;
			} catch (NoSuchMethodException e1) {
				continue;
			}

			if (pair.object1.equals(pair.object2)) {
				if (pair.object1.hashCode() != pair.object2.hashCode())
					return false;
			} else {
				if (pair.object1.hashCode() == pair.object2.hashCode())
					return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "Equals hashcode check";
	}

}
