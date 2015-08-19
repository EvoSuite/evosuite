/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic.solver.z3;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverBitwise;
import org.evosuite.symbolic.solver.z3.Z3Solver;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestZ3Bitwise {

	private static final String DEFAULT_Z3_PATH = Properties.Z3_PATH;

	@BeforeClass
	public static void configureZ3Path() {
		String z3StrPath = System.getenv("z3_path");
		if (z3StrPath != null) {
			Properties.Z3_PATH = z3StrPath;
		}
	}

	@AfterClass
	public static void restoreZ3Path() {
		Properties.Z3_PATH = DEFAULT_Z3_PATH;
	}

	@Test
	public void testBitAnd() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		if (Properties.Z3_PATH == null) {
			System.out
					.println("Warning: z3_path should be configured to execute this test case");
			return;
		}

		Z3Solver solver = new Z3Solver();
		TestSolverBitwise.testBitAnd(solver);
	}

	@Test
	public void testBitNot() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		if (Properties.Z3_PATH == null) {
			System.out
					.println("Warning: z3_path should be configured to execute this test case");
			return;
		}

		Z3Solver solver = new Z3Solver();
		TestSolverBitwise.testBitNot(solver);
	}

	@Test
	public void testBitOr() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		if (Properties.Z3_PATH == null) {
			System.out
					.println("Warning: z3_path should be configured to execute this test case");
			return;
		}

		Z3Solver solver = new Z3Solver();
		TestSolverBitwise.testBitOr(solver);
	}

	@Test
	public void testBitXor() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		if (Properties.Z3_PATH == null) {
			System.out
					.println("Warning: z3_path should be configured to execute this test case");
			return;
		}

		Z3Solver solver = new Z3Solver();
		TestSolverBitwise.testBitXor(solver);
	}

	@Test
	public void testShiftLeft() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		if (Properties.Z3_PATH == null) {
			System.out
					.println("Warning: z3_path should be configured to execute this test case");
			return;
		}

		Z3Solver solver = new Z3Solver();
		TestSolverBitwise.testShiftLeft(solver);
	}

	@Test
	public void testShiftRight() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		if (Properties.Z3_PATH == null) {
			System.out
					.println("Warning: z3_path should be configured to execute this test case");
			return;
		}

		Z3Solver solver = new Z3Solver();
		TestSolverBitwise.testShiftRight(solver);
	}

	@Test
	public void testShiftRightUnsigned() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		if (Properties.Z3_PATH == null) {
			System.out
					.println("Warning: z3_path should be configured to execute this test case");
			return;
		}

		Z3Solver solver = new Z3Solver();
		TestSolverBitwise.testShiftRightUnsigned(solver);
	}
}
