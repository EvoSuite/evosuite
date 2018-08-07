/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.symbolic.solver.z3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverStringFunctions;
import org.evosuite.symbolic.solver.cvc4.CVC4Solver;
import org.evosuite.symbolic.solver.z3.Z3Solver;
import org.junit.Test;

public class TestZ3StringFunctions extends TestZ3 {

	@Test
	public void testStringLength() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringLength(solver);
		assertNotNull(solution);
		String var0 = (String) solution.get("var0");

		assertNotNull(var0);
		assertEquals(5, var0.length());

	}

	@Test
	public void testNegativeLength() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testNegativeLength(solver);
		assertNull(solution);

	}

	@Test
	public void testStringEquals() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringEquals(solver);
		assertNotNull(solution);
		String var0 = (String) solution.get("var0");

		assertNotNull(var0);
		assertEquals("Hello World", var0);

	}

	@Test
	public void testStringAppendString() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringAppendString(solver);
		assertNotNull(solution);
	}

	@Test
	public void testStringConcat() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringConcat(solver);
		assertNotNull(solution);
	}

	@Test
	public void testStringNotEquals() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringNotEquals(solver);
		assertNotNull(solution);
		String var0 = (String) solution.get("var0");

		assertNotNull(var0);
		assertNotEquals("Hello World", var0);

	}

	@Test
	public void testStringStartsWith() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringStartsWith(solver);

		assertNotNull(solution);
		String var0 = (String) solution.get("var0");

		assertNotNull(var0);
		assertTrue(var0.startsWith("Hello"));
		assertNotEquals("Hello", var0);
		assertNotEquals("Hello".length(), var0.length());

	}

	@Test
	public void testStringStartsWithIndex() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringStartsWithIndex(solver);
		assertNotNull(solution);
		// The solution can be UNSAT since the StartsWith has no index in Z3-str
	}

	@Test
	public void testStringEndsWith() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringEndsWith(solver);
		assertNotNull(solution);
		String var0 = (String) solution.get("var0");

		assertNotNull(var0);
		assertTrue(var0.endsWith("World"));
		assertNotEquals("World", var0);

	}

	@Test
	public void testStringCharAt() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringCharAt(solver);
		assertNotNull(solution);
		String var0 = (String) solution.get("var0");

		assertNotNull(var0);
		assertTrue(var0.length() > 0);
		assertEquals('X', var0.charAt(0));

	}

	@Test
	public void testStringContains() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringContains(solver);
		assertNotNull(solution);
		String var0 = (String) solution.get("var0");

		assertNotNull(var0);
		assertTrue(!var0.equals("Hello"));
		assertTrue(var0.contains("Hello"));

	}

	@Test
	public void testStringIndexOfChar() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringIndexOfChar(solver);
		assertNotNull(solution);
	}

	@Test
	public void testStringIndexOfCharInt() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringIndexOfCharInt(solver);
		assertNotNull(solution);
	}

	@Test
	public void testStringIndexOfString() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringIndexOfString(solver);
		assertNotNull(solution);
	}

	@Test
	public void testStringIndexOfStringInt() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringIndexOfStringInt(solver);
		assertNotNull(solution);
	}

	@Test
	public void testStringTrim() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringTrim(solver);
		assertNotNull(solution);
	}

	@Test
	public void testStringLowerCase() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringLowerCase(solver);
		assertNotNull(solution);
	}

	@Test
	public void testStringUpperCase() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringUpperCase(solver);
		assertNotNull(solution);
	}

	@Test
	public void testStringLastIndexOfChar() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringLastIndexOfChar(solver);
		assertNotNull(solution);
	}

	@Test
	public void testStringLastIndexOfCharInt() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringLastIndexOfCharInt(solver);
		assertNotNull(solution);
	}

	@Test
	public void testStringLastIndexOfString() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringLastIndexOfString(solver);
		assertNotNull(solution);
	}

	@Test
	public void testStringLastIndexOfStringInt()
			throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringLastIndexOfStringInt(solver);
		assertNotNull(solution);
	}

	@Test
	public void testStringSubstring() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringSubstring(solver);
		assertNotNull(solution);
	}

	@Test
	public void testStringSubstringFromTo() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringSubstringFromTo(solver);
		assertNotNull(solution);
	}

	
	@Test
	public void testStringReplaceFirst() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringReplaceFirst(solver);
		assertNotNull(solution);
	}
	
	@Test
	public void testStringReplaceChar() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringReplaceChar(solver);
		assertNotNull(solution);
	}

	@Test
	public void testStringReplaceCharSequence()
			throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringReplaceCharSequence(solver);
		assertNotNull(solution);
	}

	@Test
	public void testStringCompareTo() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringCompareTo(solver);
		assertNotNull(solution);
	}

	@Test
	public void testStringToInteger() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testStringToInteger(solver);
		assertNotNull(solution);
	}
	
	@Test
	public void testIntegerToString() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverStringFunctions.testIntegerToString(solver);
		assertNotNull(solution);
	}
}
