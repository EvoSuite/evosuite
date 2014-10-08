package org.evosuite.symbolic.solver.z3;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverStringFunctions;
import org.evosuite.symbolic.z3.Z3Solver;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestZ3StringFunctions {

	@Test
	public void testStringLength() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringLength(solver);
	}

	@Test
	public void testNegativeLength() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testNegativeLength(solver);
	}

	@Test
	public void testStringEquals() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringEquals(solver);
	}

	@Test
	public void testStringAppendString() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringAppendString(solver);
	}

	@Test
	public void testStringConcat() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringConcat(solver);
	}

	@Test
	public void testStringNotEquals() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringNotEquals(solver);
	}

	@Test
	public void testStringStartsWith() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringStartsWith(solver);
	}

	@Test
	public void testStringStartsWithIndex() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringStartsWithIndex(solver);
	}

	@Test
	public void testStringEndsWith() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringEndsWith(solver);
	}

	@Test
	public void testStringCharAt() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringCharAt(solver);
	}

	@Test
	public void testStringContains() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringContains(solver);
	}

	@Test
	public void testStringIndexOfChar() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringIndexOfChar(solver);
	}

	@Test
	public void testStringIndexOfCharInt() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringIndexOfCharInt(solver);
	}

	@Test
	public void testStringIndexOfString() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringIndexOfString(solver);
	}

	@Test
	public void testStringIndexOfStringInt() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringIndexOfStringInt(solver);
	}

	@Test
	public void testStringTrim() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringTrim(solver);
	}

	@Test
	public void testStringLowerCase() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringLowerCase(solver);
	}

	@Test
	public void testStringUpperCase() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringUpperCase(solver);
	}

	@Test
	public void testStringLastIndexOfChar() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringLastIndexOfChar(solver);
	}

	@Test
	public void testStringLastIndexOfCharInt() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringLastIndexOfCharInt(solver);
	}

	@Test
	public void testStringLastIndexOfString() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringLastIndexOfString(solver);
	}

	@Test
	public void testStringLastIndexOfStringInt() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringLastIndexOfStringInt(solver);
	}

	@Test
	public void testStringSubstring() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringSubstring(solver);
	}

	@Test
	public void testStringSubstringFromTo() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringSubstringFromTo(solver);
	}

	@Test
	public void testStringReplaceChar() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringReplaceChar(solver);
	}

	@Test
	public void testStringReplaceCharSequence() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringReplaceCharSequence(solver);
	}

	@Test
	public void testStringCompareTo() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringCompareTo(solver);
	}

	@BeforeClass 
	public static void setUpZ3Path() {
		Properties.Z3_PATH = System.getenv("Z3_PATH");
	}
}
