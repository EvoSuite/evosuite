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
package org.evosuite.symbolic.solver.z3str2;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverFloats;
import org.evosuite.symbolic.solver.z3str2.Z3Str2Solver;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestZ3Str2Floats {

	private static final String DEFAULT_Z3_STR_PATH = Properties.Z3_STR2_PATH;

	@BeforeClass
	public static void configureZ3StrPath() {
		String z3StrPath = System.getenv("z3_str2_path");
		if (z3StrPath != null) {
			Properties.Z3_STR2_PATH = z3StrPath;
		}
	}

	@AfterClass
	public static void restoreZ3StrPath() {
		Properties.Z3_STR2_PATH = DEFAULT_Z3_STR_PATH;
	}

	@Test
	public void testFloatEq() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str2_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverFloats.testEq(solver);
	}

	@Test
	public void testFloatNeq() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str2_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverFloats.testNeq(solver);
	}

	@Test
	public void testFloatLt() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str2_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverFloats.testLt(solver);
	}

	@Test
	public void testFloatGt() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str2_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverFloats.testGt(solver);
	}

	@Test
	public void testFloatLte() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str2_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverFloats.testLte(solver);
	}

	@Test
	public void testFloatGte() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str2_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverFloats.testGte(solver);
	}

	@Test
	public void testFloatFraction() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str2_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverFloats.testFraction(solver);
	}

	@Test
	public void testFloatAdd() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		
		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str2_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverFloats.testAdd(solver);
	}

	@Test
	public void testFloatSub() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		
		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str2_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverFloats.testSub(solver);
	}

	@Test
	public void testFloatMul() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str2_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverFloats.testMul(solver);
	}

	@Test
	public void testFloatDiv() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		
		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str2_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverFloats.testDiv(solver);
	}

	@Test
	public void testFloatMod() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str2_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverFloats.testMod(solver);
	}
}
