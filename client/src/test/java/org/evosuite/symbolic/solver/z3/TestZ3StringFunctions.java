package org.evosuite.symbolic.solver.z3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverStringFunctions;
import org.evosuite.symbolic.solver.z3.Z3Solver;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestZ3StringFunctions {

	@Test
	public void testStringLength() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions
				.testStringLength(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			String var0 = (String) solution.get("var0");

			assertNotNull(var0);
			assertEquals(5, var0.length());
		}
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
		Map<String, Object> solution = TestSolverStringFunctions
				.testStringEquals(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			String var0 = (String) solution.get("var0");

			assertNotNull(var0);
			assertEquals("Hello World", var0);

		}
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
		Map<String, Object> solution = TestSolverStringFunctions
				.testStringNotEquals(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			String var0 = (String) solution.get("var0");

			assertNotNull(var0);
			assertNotEquals("Hello World", var0);
		}
	}

	@Test
	public void testStringStartsWith() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions
				.testStringStartsWith(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			String var0 = (String) solution.get("var0");

			assertNotNull(var0);
			assertTrue(var0.startsWith("Hello"));
			assertNotEquals("Hello", var0);
			assertNotEquals("Hello".length(), var0.length());
		}
	}

	@Test
	public void testStringStartsWithIndex() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions
				.testStringStartsWithIndex(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			String var0 = (String) solution.get("var0");

			assertNotNull(var0);
			assertTrue(var0.startsWith("Hello", 5));
		}
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
		Map<String, Object> solution = TestSolverStringFunctions
				.testStringCharAt(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			String var0 = (String) solution.get("var0");

			assertNotNull(var0);
			assertTrue(var0.length() > 0);
			assertEquals('X', var0.charAt(0));
		}
	}

	@Test
	public void testStringContains() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions
				.testStringContains(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			String var0 = (String) solution.get("var0");

			assertNotNull(var0);
			assertTrue(!var0.equals("Hello"));
			assertTrue(var0.contains("Hello"));
		}
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
