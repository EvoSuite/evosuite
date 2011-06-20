/**
 * 
 */
package de.unisb.cs.st.evosuite.contracts;

import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.st.evosuite.testcase.ConstructorStatement;
import de.unisb.cs.st.evosuite.testcase.MethodStatement;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * @author Gordon Fraser
 * 
 */
public class NullPointerExceptionContract extends Contract {

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.contracts.Contract#check(de.unisb.cs.st.evosuite.testcase.TestCase, de.unisb.cs.st.evosuite.testcase.Statement, de.unisb.cs.st.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	@Override
	public boolean check(StatementInterface statement, Scope scope, Throwable exception) {

		if (exception != null) {
			// method throws no NullPointerException if no input parameter was null
			if (exception instanceof NullPointerException) {
				List<VariableReference> parameters = new ArrayList<VariableReference>();
				if (statement instanceof MethodStatement) {
					MethodStatement ms = (MethodStatement) statement;
					parameters.addAll(ms.getVariableReferences());
				} else if (statement instanceof ConstructorStatement) {
					ConstructorStatement cs = (ConstructorStatement) statement;
					parameters.addAll(cs.getVariableReferences());
				} else {
					return true;
				}
				boolean hasNull = false;
				for (VariableReference var : parameters) {
					if (var.getObject(scope) == null) {
						hasNull = true;
						break;
					}
				}
				if (!hasNull) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public String toString() {
		return "Exceptions check";
	}

}
