/**
 * 
 */
package de.unisb.cs.st.evosuite.contracts;

import java.util.Set;

import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;

/**
 * @author Gordon Fraser
 * 
 */
public class UndeclaredExceptionContract extends Contract {

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.contracts.Contract#check(de.unisb.cs.st.evosuite.testcase.StatementInterface, de.unisb.cs.st.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	@Override
	public boolean check(StatementInterface statement, Scope scope, Throwable exception) {
		if (!isTargetStatement(statement))
			return true;

		if (exception != null) {
			Set<Class<?>> exceptions = statement.getDeclaredExceptions();

			if (!exceptions.contains(exception.getClass())) {
				
				/*
				 * even if possible handled by other contracts, that does not mean
				 * they check the signature. 
				 *
				// Assertion errors are checked by a different contract
				if (exception instanceof AssertionError)
					return true;

				// NullPointerExceptions are checked by a different contract
				if (exception instanceof NullPointerException) {
					return true;
				}
				*/
				
				return false;
			}
		}

		return true;
	}

	@Override
	public String toString() {
		return "Undeclared exception check";
	}
}
