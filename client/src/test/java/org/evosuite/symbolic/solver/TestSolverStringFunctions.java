package org.evosuite.symbolic.solver;

import static org.junit.Assert.assertNull;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.StringUnaryToIntegerExpression;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.VariableReference;

import com.examples.with.different.packagename.solver.TestCaseCastIntToString;
import com.examples.with.different.packagename.solver.TestCaseCastStringToInt;
import com.examples.with.different.packagename.solver.TestCaseStringAppendInteger;
import com.examples.with.different.packagename.solver.TestCaseStringAppendString;
import com.examples.with.different.packagename.solver.TestCaseStringCharAt;
import com.examples.with.different.packagename.solver.TestCaseStringCompareTo;
import com.examples.with.different.packagename.solver.TestCaseStringConcat;
import com.examples.with.different.packagename.solver.TestCaseStringConcatBoolean;
import com.examples.with.different.packagename.solver.TestCaseStringConcatChar;
import com.examples.with.different.packagename.solver.TestCaseStringContains;
import com.examples.with.different.packagename.solver.TestCaseStringEndsWith;
import com.examples.with.different.packagename.solver.TestCaseStringEquals;
import com.examples.with.different.packagename.solver.TestCaseStringIndexOfChar;
import com.examples.with.different.packagename.solver.TestCaseStringIndexOfCharInt;
import com.examples.with.different.packagename.solver.TestCaseStringIndexOfString;
import com.examples.with.different.packagename.solver.TestCaseStringIndexOfStringInt;
import com.examples.with.different.packagename.solver.TestCaseStringLastIndexOfChar;
import com.examples.with.different.packagename.solver.TestCaseStringLastIndexOfCharInt;
import com.examples.with.different.packagename.solver.TestCaseStringLastIndexOfString;
import com.examples.with.different.packagename.solver.TestCaseStringLastIndexOfStringInt;
import com.examples.with.different.packagename.solver.TestCaseStringLength;
import com.examples.with.different.packagename.solver.TestCaseStringLowerCase;
import com.examples.with.different.packagename.solver.TestCaseStringNotEquals;
import com.examples.with.different.packagename.solver.TestCaseStringReplaceChar;
import com.examples.with.different.packagename.solver.TestCaseStringReplaceCharSequence;
import com.examples.with.different.packagename.solver.TestCaseStringStartsWith;
import com.examples.with.different.packagename.solver.TestCaseStringStartsWithIndex;
import com.examples.with.different.packagename.solver.TestCaseStringSubstring;
import com.examples.with.different.packagename.solver.TestCaseStringSubstringFromTo;
import com.examples.with.different.packagename.solver.TestCaseStringTrim;
import com.examples.with.different.packagename.solver.TestCaseStringUpperCase;

public abstract class TestSolverStringFunctions {

	private static DefaultTestCase buildTestLength() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("01234");

		Method method = TestCaseStringLength.class.getMethod("test",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	public static Map<String, Object> testStringLength(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestLength();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testNegativeLength(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		IntegerConstraint newIntegerConstraint = new IntegerConstraint(
				new StringUnaryToIntegerExpression(new StringVariable("var0",
						"01234"), Operator.LENGTH, (long) 5), Comparator.LT,
				new IntegerConstant(0));

		Collection<Constraint<?>> constraints = Collections
				.<Constraint<?>> singleton(newIntegerConstraint);

		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}

	private static DefaultTestCase buildTestEquals() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("Hello World");

		Method method = TestCaseStringEquals.class.getMethod("test",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestNotEquals()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc
				.appendStringPrimitive("Not equal to Hello World");

		Method method = TestCaseStringNotEquals.class.getMethod("test",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestStartsWith()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("Hello World");

		Method method = TestCaseStringStartsWith.class.getMethod("test",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestEndsWith()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("Hello World");

		Method method = TestCaseStringEndsWith.class.getMethod("test",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestConcat() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("ha");

		Method method = TestCaseStringConcat.class.getMethod("test",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestConcatChar()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference char0 = tc.appendCharPrimitive('X');

		Method method = TestCaseStringConcatChar.class.getMethod("test",
				char.class);
		tc.appendMethod(null, method, char0);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestConcatBoolean()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference boolean0 = tc.appendBooleanPrimitive(true);

		Method method = TestCaseStringConcatBoolean.class.getMethod("test",
				boolean.class);
		tc.appendMethod(null, method, boolean0);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestAppendString()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("ha");

		Method method = TestCaseStringAppendString.class.getMethod("test",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestAppendInteger()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("ha");

		Method method = TestCaseStringAppendInteger.class.getMethod("test",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestStartsWithIndex()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive(".....Hello");

		Method method = TestCaseStringStartsWithIndex.class.getMethod("test",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestIndexOfChar()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive(".....Xello");

		Method method = TestCaseStringIndexOfChar.class.getMethod("test",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestIndexOfCharInt()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("H....Xello");

		Method method = TestCaseStringIndexOfCharInt.class.getMethod("test",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestIndexOfStringInt()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("H....Xello");

		Method method = TestCaseStringIndexOfStringInt.class.getMethod("test",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestIndexOfString()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive(".....Xello");

		Method method = TestCaseStringIndexOfString.class.getMethod("test",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	public static Map<String, Object> testStringEquals(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestEquals();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testStringNotEquals(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestNotEquals();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testStringStartsWith(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestStartsWith();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testStringStartsWithIndex(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestStartsWithIndex();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testStringEndsWith(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		DefaultTestCase tc = buildTestEndsWith();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	private static DefaultTestCase buildTestCharAt2() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("AHello");
		VariableReference int0 = tc.appendIntPrimitive(65);

		Method method = TestCaseStringCharAt.class.getMethod("test",
				String.class, int.class);
		tc.appendMethod(null, method, string0, int0);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCharAt3() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("5Hello");

		Method method = TestCaseStringCharAt.class.getMethod("test3",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCharAt() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("XHello");

		Method method = TestCaseStringCharAt.class.getMethod("test",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	public static Map<String, Object> testStringCharAt3(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		DefaultTestCase tc = buildTestCharAt3();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testStringCharAt(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		DefaultTestCase tc = buildTestCharAt();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testStringCharAt2(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		DefaultTestCase tc = buildTestCharAt2();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	private static DefaultTestCase buildTestContains()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("xxxHelloyyyyy");

		Method method = TestCaseStringContains.class.getMethod("test",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	public static Map<String, Object> testStringContains(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestContains();

		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testStringIndexOfChar(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestIndexOfChar();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}

	public static Map<String, Object> testStringIndexOfCharInt(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestIndexOfCharInt();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}

	public static Map<String, Object> testStringIndexOfString(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestIndexOfString();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}

	public static Map<String, Object> testStringIndexOfStringInt(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestIndexOfStringInt();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}

	private static DefaultTestCase buildTestTrim() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc
				.appendStringPrimitive("   Hello World   ");

		Method method = TestCaseStringTrim.class
				.getMethod("test", String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestLowerCase()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("Hello World");

		Method method = TestCaseStringLowerCase.class.getMethod("test",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestUpperCase()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("Hello worlD");

		Method method = TestCaseStringUpperCase.class.getMethod("test",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	public static Map<String, Object> testStringTrim(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestTrim();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}

	public static Map<String, Object> testCastStringToInt(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCastStringToInt();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}

	public static Map<String, Object> testCastIntToString(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCastIntToString();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}
	
	public static Map<String, Object> testCastLongToString(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCastLongToString();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}

	public static Map<String, Object> testCastCharToString(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCastCharToString();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}

	public static Map<String, Object> testCastBooleanToString(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCastBooleanToString();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}

	public static Map<String, Object> testStringAppendString(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestAppendString();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}

	public static Map<String, Object> testStringAppendInteger(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestAppendInteger();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}

	public static Map<String, Object> testStringSubstringFromTo(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestSubstringFromTo();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}

	public static Map<String, Object> testStringUpperCase(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestUpperCase();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testStringLowerCase(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestLowerCase();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	private static DefaultTestCase buildTestLastIndexOfChar()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive(".....Xello");

		Method method = TestCaseStringLastIndexOfChar.class.getMethod("test",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestLastIndexOfCharInt()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("H....Xello");

		Method method = TestCaseStringLastIndexOfCharInt.class.getMethod(
				"test", String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestLastIndexOfStringInt()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("H....Xello");

		Method method = TestCaseStringLastIndexOfStringInt.class.getMethod(
				"test", String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestLastIndexOfString()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive(".....Xello");

		Method method = TestCaseStringLastIndexOfString.class.getMethod("test",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	public static Map<String, Object> testStringLastIndexOfChar(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestLastIndexOfChar();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}

	public static Map<String, Object> testStringLastIndexOfCharInt(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestLastIndexOfCharInt();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}

	public static Map<String, Object> testStringLastIndexOfString(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestLastIndexOfString();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}

	public static Map<String, Object> testStringLastIndexOfStringInt(
			Solver solver) throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestLastIndexOfStringInt();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}

	private static DefaultTestCase buildTestSubstring()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("unhappy");

		Method method = TestCaseStringSubstring.class.getMethod("test",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCastStringToInt()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("12345");

		Method method = TestCaseCastStringToInt.class.getMethod("test",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCastIntToString()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(0);

		Method method = TestCaseCastIntToString.class.getMethod("test",
				int.class);
		tc.appendMethod(null, method, int0);
		return tc.getDefaultTestCase();
	}
	
	private static DefaultTestCase buildTestCastLongToString()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference long0 = tc.appendLongPrimitive(0);

		Method method = TestCaseCastIntToString.class.getMethod("test",
				long.class);
		tc.appendMethod(null, method, long0);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCastCharToString()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference char0 = tc.appendCharPrimitive('0');

		Method method = TestCaseCastIntToString.class.getMethod("test",
				char.class);
		tc.appendMethod(null, method, char0);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCastBooleanToString()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference boolean0 = tc.appendBooleanPrimitive(false);

		Method method = TestCaseCastIntToString.class.getMethod("test",
				boolean.class);
		tc.appendMethod(null, method, boolean0);
		return tc.getDefaultTestCase();
	}

	
	
	private static DefaultTestCase buildTestSubstringFromTo()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("hamburger");

		Method method = TestCaseStringSubstringFromTo.class.getMethod("test",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	public static Map<String, Object> testStringSubstring(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestSubstring();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}

	public static Map<String, Object> testStringConcat(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestConcat();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}

	public static Map<String, Object> testStringConcatChar(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestConcatChar();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}

	public static Map<String, Object> testStringConcatBoolean(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestConcatBoolean();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}

	private static DefaultTestCase buildTestReplaceChar()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("happx");

		Method method = TestCaseStringReplaceChar.class.getMethod("test",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestReplaceCharSequence()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("happx");

		Method method = TestCaseStringReplaceCharSequence.class.getMethod(
				"test", String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	public static Map<String, Object> testStringReplaceChar(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestReplaceChar();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}

	public static Map<String, Object> testStringReplaceCharSequence(
			Solver solver) throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestReplaceCharSequence();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}

	private static DefaultTestCase buildTestCompareTo()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("Hello");

		Method method = TestCaseStringCompareTo.class.getMethod("test",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	public static Map<String, Object> testStringCompareTo(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCompareTo();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}
}
