/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic.solver.z3str2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverStringFunctions;
import org.junit.Test;

public class TestZ3Str2StringFunctions extends TestZ3Str2 {

	@Test
	public void testStringLength() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		Map<String, Object> solution = TestSolverStringFunctions
				.testStringLength(solver);
		assertNotNull(solution);
		String var0 = (String) solution.get("var0");

		assertNotNull(var0);
		assertEquals(5, var0.length());

	}

	@Test
	public void testNegativeLength() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		Map<String, Object> solution = TestSolverStringFunctions
				.testNegativeLength(solver);
		assertNull(solution);

	}

	@Test
	public void testStringEquals() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		Map<String, Object> solution = TestSolverStringFunctions
				.testStringEquals(solver);
		assertNotNull(solution);
		String var0 = (String) solution.get("var0");

		assertNotNull(var0);
		assertEquals("Hello World", var0);

	}

	@Test
	public void testStringAppendString() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringAppendString(solver);
	}

	@Test
	public void testStringConcat() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringConcat(solver);
	}

	@Test
	public void testStringNotEquals() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		Map<String, Object> solution = TestSolverStringFunctions
				.testStringNotEquals(solver);
		assertNotNull(solution);
		String var0 = (String) solution.get("var0");

		assertNotNull(var0);
		assertNotEquals("Hello World", var0);

	}

	@Test
	public void testStringStartsWith() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		Map<String, Object> solution = TestSolverStringFunctions
				.testStringStartsWith(solver);

		assertNotNull(solution);
		String var0 = (String) solution.get("var0");

		assertNotNull(var0);
		assertTrue(var0.startsWith("Hello"));
		assertNotEquals("Hello", var0);
		assertNotEquals("Hello".length(), var0.length());

	}

	@Test
	public void testStringStartsWithIndex() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringStartsWithIndex(solver);

		// The solution can be UNSAT since the StartsWith has no index in Z3-str
	}

	@Test
	public void testStringEndsWith() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		Map<String, Object> solution = TestSolverStringFunctions
				.testStringEndsWith(solver);
		assertNotNull(solution);
		String var0 = (String) solution.get("var0");

		assertNotNull(var0);
		assertTrue(var0.endsWith("World"));
		assertNotEquals("World", var0);

	}

	@Test
	public void testStringCharAt() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringCharAt(solver);
		assertNotNull(solution);
		String var0 = (String) solution.get("var0");

		assertNotNull(var0);
		assertTrue(var0.length() > 0);
		assertEquals('X', var0.charAt(0));

	}

	@Test
	public void testStringContains() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringContains(solver);
		assertNotNull(solution);
		String var0 = (String) solution.get("var0");

		assertNotNull(var0);
		assertTrue(!var0.equals("Hello"));
		assertTrue(var0.contains("Hello"));

	}

	@Test
	public void testStringIndexOfChar() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringIndexOfChar(solver);
	}

	@Test
	public void testStringIndexOfCharInt() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringIndexOfCharInt(solver);
	}

	@Test
	public void testStringIndexOfString() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringIndexOfString(solver);
	}

	@Test
	public void testStringIndexOfStringInt() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringIndexOfStringInt(solver);
	}

	@Test
	public void testStringTrim() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringTrim(solver);
	}

	@Test
	public void testStringLowerCase() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringLowerCase(solver);
	}

	@Test
	public void testStringUpperCase() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringUpperCase(solver);
	}

	@Test
	public void testStringLastIndexOfChar() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringLastIndexOfChar(solver);
	}

	@Test
	public void testStringLastIndexOfCharInt() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringLastIndexOfCharInt(solver);
	}

	@Test
	public void testStringLastIndexOfString() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringLastIndexOfString(solver);
	}

	@Test
	public void testStringLastIndexOfStringInt() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringLastIndexOfStringInt(solver);
	}

	@Test
	public void testStringSubstring() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringSubstring(solver);
	}

	@Test
	public void testStringSubstringFromTo() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringSubstringFromTo(solver);
	}

	@Test
	public void testStringReplaceChar() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringReplaceChar(solver);
	}

	@Test
	public void testStringReplaceCharSequence() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringReplaceCharSequence(solver);
	}

	@Test
	public void testStringCompareTo() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverStringFunctions.testStringCompareTo(solver);
	}
}
