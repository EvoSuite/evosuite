package org.evosuite.dse;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.numeric.BooleanPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.BytePrimitiveStatement;
import org.evosuite.testcase.statements.numeric.CharPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.DoublePrimitiveStatement;
import org.evosuite.testcase.statements.numeric.FloatPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.LongPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.ShortPrimitiveStatement;
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

	/**
	 * Creates a statement declaring a boolean variable
	 * 
	 * @param b
	 * @return
	 */
	public VariableReference appendBooleanPrimitive(boolean b) {
		BooleanPrimitiveStatement primitiveStmt = new BooleanPrimitiveStatement(tc, b);
		tc.addStatement(primitiveStmt);
		return primitiveStmt.getReturnValue();
	}

	/**
	 * Appends a float variable to a test case
	 * 
	 * @param f
	 * @return
	 */
	public VariableReference appendFloatPrimitive(float f) {
		FloatPrimitiveStatement primitiveStmt = new FloatPrimitiveStatement(tc, f);
		tc.addStatement(primitiveStmt);
		return primitiveStmt.getReturnValue();
	}

	/**
	 * Appends a double variable to a test case
	 * 
	 * @param d
	 * @return
	 */
	public VariableReference appendDoublePrimitive(double d) {
		DoublePrimitiveStatement primitiveStmt = new DoublePrimitiveStatement(tc, d);
		tc.addStatement(primitiveStmt);
		return primitiveStmt.getReturnValue();
	}

	/**
	 * Appends a char variable declaration to a test case
	 * 
	 * @param c
	 *            the char value to initialize the char variable
	 * 
	 * @return
	 */
	public VariableReference appendCharPrimitive(char c) {
		CharPrimitiveStatement primitiveStmt = new CharPrimitiveStatement(tc, c);
		tc.addStatement(primitiveStmt);
		return primitiveStmt.getReturnValue();
	}

	/**
	 * Appends a byte primitive variable declaration to the test case with a
	 * initialization value
	 * 
	 * @param b
	 *            the byte value to initialize the variable
	 * @return
	 */
	public VariableReference appendBytePrimitive(byte b) {
		BytePrimitiveStatement primitiveStmt = new BytePrimitiveStatement(tc, b);
		tc.addStatement(primitiveStmt);
		return primitiveStmt.getReturnValue();
	}

	/**
	 * Appends a long variable declaration to a test case with a long initialization
	 * value
	 * 
	 * @param l
	 *            the long value to initialize the variable
	 * @return
	 */
	public VariableReference appendLongPrimitive(long l) {
		LongPrimitiveStatement primitiveStmt = new LongPrimitiveStatement(tc, l);
		tc.addStatement(primitiveStmt);
		return primitiveStmt.getReturnValue();
	}

	/**
	 * Appends a short variable declaration to a test case
	 * 
	 * @param s
	 *            the short value to initialize the variable
	 * @return
	 */
	public VariableReference appendShortPrimitive(short s) {
		ShortPrimitiveStatement primitiveStmt = new ShortPrimitiveStatement(tc, s);
		tc.addStatement(primitiveStmt);
		return primitiveStmt.getReturnValue();
	}
}
