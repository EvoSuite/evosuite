package org.evosuite.symbolic.solver.z3str;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverSimpleMath;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestZ3StrSimpleMath {

	private static final String DEFAULT_Z3_STR_PATH = Properties.Z3_STR_PATH;

	@BeforeClass
	public static void configureZ3StrPath() {
		String z3StrPath = System.getenv("z3_str_path");
		if (z3StrPath != null) {
			Properties.Z3_STR_PATH = z3StrPath;
		}
	}

	@AfterClass
	public static void restoreZ3StrPath() {
		Properties.Z3_STR_PATH = DEFAULT_Z3_STR_PATH;
	}

	@Test
	public void testAdd() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.Z3_STR_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3StrSolver solver = new Z3StrSolver();
		TestSolverSimpleMath.testAdd(solver);
	}

	@Test
	public void testSub() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.Z3_STR_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3StrSolver solver = new Z3StrSolver();
		TestSolverSimpleMath.testSub(solver);
	}

	@Test
	public void testMul() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.Z3_STR_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3StrSolver solver = new Z3StrSolver();
		TestSolverSimpleMath.testMul(solver);
	}

	@Test
	public void testDiv() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.Z3_STR_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3StrSolver solver = new Z3StrSolver();
		TestSolverSimpleMath.testDiv(solver);
	}

	@Test
	public void testEq() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		
		if (Properties.Z3_STR_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3StrSolver solver = new Z3StrSolver();
		TestSolverSimpleMath.testEq(solver);
	}

	@Test
	public void testNeq() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.Z3_STR_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3StrSolver solver = new Z3StrSolver();
		TestSolverSimpleMath.testNeq(solver);
	}

	@Test
	public void testLt() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.Z3_STR_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3StrSolver solver = new Z3StrSolver();
		TestSolverSimpleMath.testLt(solver);
	}

	@Test
	public void testGt() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.Z3_STR_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3StrSolver solver = new Z3StrSolver();
		TestSolverSimpleMath.testGt(solver);
	}

	@Test
	public void testLte() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.Z3_STR_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3StrSolver solver = new Z3StrSolver();
		TestSolverSimpleMath.testLte(solver);
	}

	@Test
	public void testGte() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.Z3_STR_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3StrSolver solver = new Z3StrSolver();
		TestSolverSimpleMath.testGte(solver);
	}

	@Test
	public void testMod() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.Z3_STR_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3StrSolver solver = new Z3StrSolver();
		TestSolverSimpleMath.testMod(solver);
	}

	@Test
	public void testMul2() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.Z3_STR_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3StrSolver solver = new Z3StrSolver();
		TestSolverSimpleMath.testMul2(solver);
	}

	@Test
	public void testCastRealToInt() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3StrSolver solver = new Z3StrSolver();
		TestSolverSimpleMath.testCastRealToInt(solver);
	}

	@Test
	public void testCastIntToReal() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.Z3_STR_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		Z3StrSolver solver = new Z3StrSolver();
		TestSolverSimpleMath.testCastIntToReal(solver);
	}
}
