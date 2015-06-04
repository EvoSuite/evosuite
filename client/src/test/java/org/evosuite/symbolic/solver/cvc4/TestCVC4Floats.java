package org.evosuite.symbolic.solver.cvc4;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverFloats;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestCVC4Floats {

	private static final String DEFAULT_CVC4_PATH = Properties.CVC4_PATH;

	@BeforeClass
	public static void configureCVC4Path() {
		String cvc4Path = System.getenv("cvc4_path");
		if (cvc4Path != null) {
			Properties.CVC4_PATH = cvc4Path;
		}
	}

	@AfterClass
	public static void restoreCVC4Path() {
		Properties.CVC4_PATH = DEFAULT_CVC4_PATH;
	}

	@Test
	public void testFloatEq() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.CVC4_PATH == null) {
			System.out
					.println("Warning: cvc4_path should be configured to execute this test case");
			return;
		}

		CVC4Solver solver = new CVC4Solver();
		TestSolverFloats.testEq(solver);
	}

	@Test
	public void testFloatNeq() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.CVC4_PATH == null) {
			System.out
					.println("Warning: cvc4_path should be configured to execute this test case");
			return;
		}

		CVC4Solver solver = new CVC4Solver();
		TestSolverFloats.testNeq(solver);
	}

	@Test
	public void testFloatLt() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		if (Properties.CVC4_PATH == null) {
			System.out
					.println("Warning: cvc4_path should be configured to execute this test case");
			return;
		}

		CVC4Solver solver = new CVC4Solver();
		TestSolverFloats.testLt(solver);
	}

	@Test
	public void testFloatGt() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.CVC4_PATH == null) {
			System.out
					.println("Warning: cvc4_path should be configured to execute this test case");
			return;
		}

		CVC4Solver solver = new CVC4Solver();
		TestSolverFloats.testGt(solver);
	}

	@Test
	public void testFloatLte() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		if (Properties.CVC4_PATH == null) {
			System.out
					.println("Warning: cvc4_path should be configured to execute this test case");
			return;
		}

		CVC4Solver solver = new CVC4Solver();
		TestSolverFloats.testLte(solver);
	}

	@Test
	public void testFloatGte() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.CVC4_PATH == null) {
			System.out
					.println("Warning: cvc4_path should be configured to execute this test case");
			return;
		}

		CVC4Solver solver = new CVC4Solver();
		TestSolverFloats.testGte(solver);
	}

	@Test
	public void testFloatFraction() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		if (Properties.CVC4_PATH == null) {
			System.out
					.println("Warning: cvc4_path should be configured to execute this test case");
			return;
		}

		CVC4Solver solver = new CVC4Solver();
		TestSolverFloats.testFraction(solver);
	}

	@Test
	public void testFloatAdd() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.CVC4_PATH == null) {
			System.out
					.println("Warning: cvc4_path should be configured to execute this test case");
			return;
		}

		CVC4Solver solver = new CVC4Solver();
		TestSolverFloats.testAdd(solver);
	}

	@Test
	public void testFloatSub() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.CVC4_PATH == null) {
			System.out
					.println("Warning: cvc4_path should be configured to execute this test case");
			return;
		}

		CVC4Solver solver = new CVC4Solver();
		TestSolverFloats.testSub(solver);
	}

	@Test
	public void testFloatMul() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.CVC4_PATH == null) {
			System.out
					.println("Warning: cvc4_path should be configured to execute this test case");
			return;
		}

		CVC4Solver solver = new CVC4Solver();
		TestSolverFloats.testMul(solver);
	}

	@Test
	public void testFloatDiv() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.CVC4_PATH == null) {
			System.out
					.println("Warning: cvc4_path should be configured to execute this test case");
			return;
		}

		CVC4Solver solver = new CVC4Solver();
		TestSolverFloats.testDiv(solver);
	}

	@Test
	public void testFloatMod() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.CVC4_PATH == null) {
			System.out
					.println("Warning: cvc4_path should be configured to execute this test case");
			return;
		}

		CVC4Solver solver = new CVC4Solver();
		TestSolverFloats.testMod(solver);
	}
}
