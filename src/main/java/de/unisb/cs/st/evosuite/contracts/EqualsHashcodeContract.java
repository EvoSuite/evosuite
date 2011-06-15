/**
 * 
 */
package de.unisb.cs.st.evosuite.contracts;

import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;

/**
 * o1.equals(o2) => o1.hashCode() == o2.hashCode()
 * 
 * @author Gordon Fraser
 * 
 */
public class EqualsHashcodeContract extends Contract {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.contracts.Contract#check(de.unisb.cs.st.evosuite
	 * .testcase.Statement, de.unisb.cs.st.evosuite.testcase.Scope,
	 * java.lang.Throwable)
	 */
	@Override
	public boolean check(StatementInterface statement, Scope scope, Throwable exception) {
		for (Pair pair : getAllObjectPairs(scope)) {
			if ((pair.object1 == null) || (pair.object2 == null)) {
				continue;
			}
			if (pair.object1.equals(pair.object2)) {
				if (pair.object1.hashCode() != pair.object2.hashCode()) {
					return false;
				}
			} else {
				if (pair.object1.hashCode() == pair.object2.hashCode()) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "Equals hashcode check";
	}

}
