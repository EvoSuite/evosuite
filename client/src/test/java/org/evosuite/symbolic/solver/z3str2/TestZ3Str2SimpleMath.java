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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverSimpleMath;
import org.evosuite.symbolic.solver.z3str2.Z3Str2Solver;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestZ3Str2SimpleMath {

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
	public void testAdd() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str2_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverSimpleMath.testAdd(solver);
	}

	@Test
	public void testSub() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str2_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverSimpleMath.testSub(solver);
	}

	@Test
	public void testMul() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str2_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverSimpleMath.testMul(solver);
	}

	@Test
	public void testDiv() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str2_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		Map<String, Object> solution = TestSolverSimpleMath.testDiv(solver);
		
		assertNotNull(solution);
		Long var0 = (Long) solution.get("var0");
		Long var1 = (Long) solution.get("var1");

		assertEquals(var0.intValue(), var1.intValue() / 5);

	}

	@Test
	public void testEq() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		
		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str2_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverSimpleMath.testEq(solver);
	}

	@Test
	public void testNeq() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str2_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverSimpleMath.testNeq(solver);
	}

	@Test
	public void testLt() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str2_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverSimpleMath.testLt(solver);
	}

	@Test
	public void testGt() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str2_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverSimpleMath.testGt(solver);
	}

	@Test
	public void testLte() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str2_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverSimpleMath.testLte(solver);
	}

	@Test
	public void testGte() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str2_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverSimpleMath.testGte(solver);
	}

	@Test
	public void testMod() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str2_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverSimpleMath.testMod(solver);
	}

	@Test
	public void testMul2() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str2_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		Map<String, Object> solution = TestSolverSimpleMath.testMul2(solver);
		assertNotNull(solution);

		Long var0 = (Long) solution.get("var0");
		Long var1 = (Long) solution.get("var1");

		assertEquals(10, var0.intValue() * var1.intValue());

	}

	@Test
	public void testCastRealToInt() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str2_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverSimpleMath.testCastRealToInt(solver);
	}

	@Test
	public void testCastIntToReal() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {

		if (Properties.Z3_STR2_PATH == null) {
			System.out
					.println("Warning: z3_str2_path should be configured to execute this test case");
			return;
		}

		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverSimpleMath.testCastIntToReal(solver);
	}
}
