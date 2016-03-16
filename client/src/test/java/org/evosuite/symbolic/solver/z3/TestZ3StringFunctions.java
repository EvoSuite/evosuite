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
package org.evosuite.symbolic.solver.z3;

import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverStringFunctions;
import org.junit.Test;

public class TestZ3StringFunctions extends TestZ3{

	@Test
	public void testStringLength() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringLength(solver);
	}

	@Test
	public void testNegativeLength() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testNegativeLength(solver);
	}

	@Test
	public void testStringEquals() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringEquals(solver);
	}

	@Test
	public void testStringEqualsIgnoreCase() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringEqualsIgnoreCase(solver);
	}

	@Test
	public void testStringAppendString() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringAppendString(solver);
	}

	@Test
	public void testStringConcat() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringConcat(solver);
	}

	@Test
	public void testStringNotEquals() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringNotEquals(solver);
	}

	@Test
	public void testStringStartsWith() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringStartsWith(solver);

	}

	@Test
	public void testStringStartsWithIndex() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringStartsWithIndex(solver);

		// The solution can be UNSAT since the StartsWith has no index in Z3-str
	}

	@Test
	public void testStringEndsWith() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringEndsWith(solver);
	}

	@Test
	public void testStringCharAt() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringCharAt(solver);
	}

	@Test
	public void testStringContains() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringContains(solver);
	}

	@Test
	public void testStringIndexOfChar() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringIndexOfChar(solver);
	}

	@Test
	public void testStringIndexOfCharInt() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringIndexOfCharInt(solver);
	}

	@Test
	public void testStringIndexOfString() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringIndexOfString(solver);
	}

	@Test
	public void testStringIndexOfStringInt() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringIndexOfStringInt(solver);
	}

	@Test
	public void testStringTrim() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringTrim(solver);
	}

	@Test
	public void testStringLowerCase() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringLowerCase(solver);
	}

	@Test
	public void testStringUpperCase() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringUpperCase(solver);
	}

	@Test
	public void testStringLastIndexOfChar() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringLastIndexOfChar(solver);
	}

	@Test
	public void testStringLastIndexOfCharInt() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringLastIndexOfCharInt(solver);
	}

	@Test
	public void testStringLastIndexOfString() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringLastIndexOfString(solver);
	}

	@Test
	public void testStringLastIndexOfStringInt() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringLastIndexOfStringInt(solver);
	}

	@Test
	public void testStringSubstring() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringSubstring(solver);
	}

	@Test
	public void testStringSubstringFromTo() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringSubstringFromTo(solver);
	}

	@Test
	public void testStringReplaceChar() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringReplaceChar(solver);
	}

	@Test
	public void testStringReplaceCharSequence() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringReplaceCharSequence(solver);
	}

	@Test
	public void testStringCompareTo() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverStringFunctions.testStringCompareTo(solver);
	}
}
