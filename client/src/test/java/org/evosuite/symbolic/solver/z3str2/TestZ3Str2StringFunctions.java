package org.evosuite.symbolic.solver.z3str2;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverStringFunctions;
import org.evosuite.symbolic.solver.z3str2.Z3Str2Solver;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestZ3Str2StringFunctions {

	private static final String DEFAULT_Z3_STR_PATH = Properties.Z3_STR2_PATH;

	@BeforeClass
	public static void configureZ3StrPath() {
		String z3StrPath = System.getenv("z3_str_path");
		if (z3StrPath != null) {
			Properties.Z3_STR2_PATH = z3StrPath;
		}
	}

	@AfterClass
	public static void restoreZ3StrPath() {
		Properties.Z3_STR2_PATH = DEFAULT_Z3_STR_PATH;
	}

	@Test
	public void testStringLength() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringLength(solver);
	}

	@Test
	public void testNegativeLength() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testNegativeLength(solver);
	}

	@Test
	public void testStringEquals() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringEquals(solver);
	}

	@Test
	public void testStringAppendString() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringAppendString(solver);
	}

	@Test
	public void testStringConcat() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringConcat(solver);
	}

	@Test
	public void testStringNotEquals() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringNotEquals(solver);
	}

	@Test
	public void testStringStartsWith() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringStartsWith(solver);

	}

	@Test
	public void testStringStartsWithIndex() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringStartsWithIndex(solver);

		// The solution can be UNSAT since the StartsWith has no index in Z3-str
	}

	@Test
	public void testStringEndsWith() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringEndsWith(solver);
	}

	@Test
	public void testStringCharAt() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringCharAt(solver);
	}

	@Test
	public void testStringContains() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringContains(solver);
	}

	@Test
	public void testStringIndexOfChar() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringIndexOfChar(solver);
	}

	@Test
	public void testStringIndexOfCharInt() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringIndexOfCharInt(solver);
	}

	@Test
	public void testStringIndexOfString() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringIndexOfString(solver);
	}

	@Test
	public void testStringIndexOfStringInt() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringIndexOfStringInt(solver);
	}

	@Test
	public void testStringTrim() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringTrim(solver);
	}

	@Test
	public void testStringLowerCase() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringLowerCase(solver);
	}

	@Test
	public void testStringUpperCase() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringUpperCase(solver);
	}

	@Test
	public void testStringLastIndexOfChar() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringLastIndexOfChar(solver);
	}

	@Test
	public void testStringLastIndexOfCharInt() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringLastIndexOfCharInt(solver);
	}

	@Test
	public void testStringLastIndexOfString() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringLastIndexOfString(solver);
	}

	@Test
	public void testStringLastIndexOfStringInt() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringLastIndexOfStringInt(solver);
	}

	@Test
	public void testStringSubstring() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringSubstring(solver);
	}

	@Test
	public void testStringSubstringFromTo() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringSubstringFromTo(solver);
	}

	@Test
	public void testStringReplaceChar() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringReplaceChar(solver);
	}

	@Test
	public void testStringReplaceCharSequence() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringReplaceCharSequence(solver);
	}

	@Test
	public void testStringCompareTo() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringCompareTo(solver);
	}
}
