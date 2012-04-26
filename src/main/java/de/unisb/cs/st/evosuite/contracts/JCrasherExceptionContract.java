/**
 * 
 */
package de.unisb.cs.st.evosuite.contracts;

import java.util.Arrays;
import java.util.List;

import de.unisb.cs.st.evosuite.testcase.ConstructorStatement;
import de.unisb.cs.st.evosuite.testcase.MethodStatement;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;

/**
 * @author fraser
 * 
 */
public class JCrasherExceptionContract extends Contract {

	// ArrayIndexOutOfBoundsException, NegativeArraySizeException, ArrayStoreException, ClassCastException, and ArithmeticException

	private final static Class<?>[] uncheckedBugExceptions = {
	        ArrayIndexOutOfBoundsException.class, NegativeArraySizeException.class,
	        ArrayStoreException.class, ClassCastException.class,
	        ArithmeticException.class };

	private final static List<Class<?>> uncheckedExceptions = Arrays.asList(uncheckedBugExceptions);

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.contracts.Contract#check(de.unisb.cs.st.evosuite.testcase.StatementInterface, de.unisb.cs.st.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	@Override
	public boolean check(StatementInterface statement, Scope scope, Throwable exception) {
		if (!isTargetStatement(statement))
			return true;

		if (exception != null) {
			if (exception instanceof RuntimeException) {
				if (uncheckedExceptions.contains(exception.getClass()))
					return false;
				else {
					StackTraceElement element = exception.getStackTrace()[0];

					String methodName = "";
					if (statement instanceof ConstructorStatement)
						methodName = "<init>";
					else if (statement instanceof MethodStatement)
						methodName = ((MethodStatement) statement).getMethod().getName();
					else
						return true;

					// If the exception was thrown in the called method we assume it is a bug in the test, not the class		      
					if (element.getMethodName().equals(methodName)) {
						return true;
					}
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public String toString() {
		return "Undeclared exception (JCrasher style)";
	}

}
