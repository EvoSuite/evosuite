package org.evosuite.dse;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericMethod;

/**
 * Creates a test case using the
 * 
 * @author jgaleotti
 *
 */
public class TestCaseBuilder {

	/**
	 * The test case that is being built by this builder
	 */
	private final DefaultTestCase tc;

	/**
	 * Builds a new Test Case builder
	 */
	public TestCaseBuilder() {
		tc = new DefaultTestCase();
	}

	/**
	 * Appends a new integer primitive statement
	 * 
	 * @param intValue
	 *            the value to declare in the new variable
	 * 
	 * @return the VariableReference with the int value
	 */
	public VariableReference appendIntPrimitive(int intValue) {
		IntPrimitiveStatement primitiveStmt = new IntPrimitiveStatement(tc, intValue);
		tc.addStatement(primitiveStmt);
		return primitiveStmt.getReturnValue();
	}

	/**
	 * Appends a new static method call
	 * 
	 * @param staticMethod
	 *            a static method
	 * 
	 * @param parameters
	 *            the variable reference to use for the static method call
	 * @return the return value (if any)
	 */
	public VariableReference appendMethod(Method staticMethod, VariableReference... parameters) {
		if (!Modifier.isStatic(staticMethod.getModifiers())) {
			throw new IllegalArgumentException("Method should be a static method");
		}

		List<VariableReference> parameter_list = Arrays.asList(parameters);
		MethodStatement methodStmt = null;
		VariableReference callee = null;
		methodStmt = new MethodStatement(tc, new GenericMethod(staticMethod, staticMethod.getDeclaringClass()), callee,
				parameter_list);
		tc.addStatement(methodStmt);
		return methodStmt.getReturnValue();
	}

	/**
	 * Returns the Test Case being built.
	 * 
	 * @return
	 */
	public DefaultTestCase getDefaultTestCase() {
		return tc;
	}

}
