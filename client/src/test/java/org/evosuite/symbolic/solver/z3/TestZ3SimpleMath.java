package org.evosuite.symbolic.solver.z3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverSimpleMath;
import org.evosuite.symbolic.solver.z3.Z3Solver;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestZ3SimpleMath {

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
	public void testAdd() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.Z3_PATH == null) {
			System.out
					.println("Warning: z3_path should be configured to execute this test case");
			return;
		}

		Z3Solver solver = new Z3Solver(true);
		TestSolverSimpleMath.testAdd(solver);
	}

	@Test
	public void testSub() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.Z3_PATH == null) {
			System.out
					.println("Warning: z3_path should be configured to execute this test case");
			return;
		}

		Z3Solver solver = new Z3Solver();
		TestSolverSimpleMath.testSub(solver);
	}

	@Test
	public void testMul() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.Z3_PATH == null) {
			System.out
					.println("Warning: z3_path should be configured to execute this test case");
			return;
		}

		Z3Solver solver = new Z3Solver();
		TestSolverSimpleMath.testMul(solver);
	}

	@Test
	public void testDiv() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.Z3_PATH == null) {
			System.out
					.println("Warning: z3_path should be configured to execute this test case");
			return;
		}

		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverSimpleMath.testDiv(solver);
		
		assertNotNull(solution);
		Long var0 = (Long) solution.get("var0");
		Long var1 = (Long) solution.get("var1");

		assertEquals(var0.intValue(), var1.intValue() / 5);

	}

	@Test
	public void testEq() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		
		if (Properties.Z3_PATH == null) {
			System.out
					.println("Warning: z3_path should be configured to execute this test case");
			return;
		}

		Z3Solver solver = new Z3Solver();
		TestSolverSimpleMath.testEq(solver);
	}

	@Test
	public void testNeq() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.Z3_PATH == null) {
			System.out
					.println("Warning: z3_path should be configured to execute this test case");
			return;
		}

		Z3Solver solver = new Z3Solver();
		TestSolverSimpleMath.testNeq(solver);
	}

	@Test
	public void testLt() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.Z3_PATH == null) {
			System.out
					.println("Warning: z3_path should be configured to execute this test case");
			return;
		}

		Z3Solver solver = new Z3Solver();
		TestSolverSimpleMath.testLt(solver);
	}

	@Test
	public void testGt() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.Z3_PATH == null) {
			System.out
					.println("Warning: z3_path should be configured to execute this test case");
			return;
		}

		Z3Solver solver = new Z3Solver();
		TestSolverSimpleMath.testGt(solver);
	}

	@Test
	public void testLte() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.Z3_PATH == null) {
			System.out
					.println("Warning: z3_path should be configured to execute this test case");
			return;
		}

		Z3Solver solver = new Z3Solver();
		TestSolverSimpleMath.testLte(solver);
	}

	@Test
	public void testGte() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.Z3_PATH == null) {
			System.out
					.println("Warning: z3_path should be configured to execute this test case");
			return;
		}

		Z3Solver solver = new Z3Solver();
		TestSolverSimpleMath.testGte(solver);
	}

	@Test
	public void testMod() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.Z3_PATH == null) {
			System.out
					.println("Warning: z3_path should be configured to execute this test case");
			return;
		}

		Z3Solver solver = new Z3Solver();
		TestSolverSimpleMath.testMod(solver);
	}

	@Test
	public void testMul2() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.Z3_PATH == null) {
			System.out
					.println("Warning: z3_path should be configured to execute this test case");
			return;
		}

		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverSimpleMath.testMul2(solver);
		assertNotNull(solution);

		Long var0 = (Long) solution.get("var0");
		Long var1 = (Long) solution.get("var1");

		assertEquals(10, var0.intValue() * var1.intValue());

	}

	@Test
	public void testCastRealToInt() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_PATH == null) {
			System.out
					.println("Warning: z3_path should be configured to execute this test case");
			return;
		}

		Z3Solver solver = new Z3Solver();
		TestSolverSimpleMath.testCastRealToInt(solver);
	}

	@Test
	public void testCastIntToReal() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_PATH == null) {
			System.out
					.println("Warning: z3_path should be configured to execute this test case");
			return;
		}

		Z3Solver solver = new Z3Solver();
		TestSolverSimpleMath.testCastIntToReal(solver);
	}
}
