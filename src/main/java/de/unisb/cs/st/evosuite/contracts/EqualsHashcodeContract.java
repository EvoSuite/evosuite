/**
 * 
 */
package de.unisb.cs.st.evosuite.contracts;

import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.Statement;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * o1.equals(o2) => o1.hashCode() == o2.hashCode()
 * 
 * @author Gordon Fraser
 * 
 */
public class EqualsHashcodeContract implements Contract {

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.contracts.Contract#check(de.unisb.cs.st.evosuite.testcase.Statement, de.unisb.cs.st.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	@Override
	public boolean check(Statement statement, Scope scope, Throwable exception) {
		// TODO: Also check callee in case of non-static method calls
		VariableReference var = statement.getReturnValue();
		Object object = scope.get(var);
		if (object == null)
			return true;

		for (Object o : scope.getElements(object.getClass())) {
			if (o != null) {
				if (object.equals(o))
					return object.hashCode() == o.hashCode();
				else
					return object.hashCode() != o.hashCode();
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "Equals hashcode check";
	}

}
