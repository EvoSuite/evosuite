package org.evosuite.symbolic.solver.cvc4;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverMathFloat;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestCVC4MathFloat {

	private static final String DEFAULT_CVC4_PATH = Properties.CVC4_PATH;

	@BeforeClass
	public static void configureZ3StrPath() {
		String z3StrPath = System.getenv("cvc4_path");
		if (z3StrPath != null) {
			Properties.CVC4_PATH = z3StrPath;
		}
	}

	@AfterClass
	public static void restoreZ3StrPath() {
		Properties.CVC4_PATH = DEFAULT_CVC4_PATH;
	}

	@Test
	public void testFloatAbs() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		if (Properties.CVC4_PATH == null) {
			System.out
					.println("Warning: cvc4_path should be configured to execute this test case");
			return;
		}
		
		CVC4Solver solver = new CVC4Solver();
		TestSolverMathFloat.testAbs(solver);
	}

	@Test
	public void testFloatTrigonometry() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		
		if (Properties.CVC4_PATH == null) {
			System.out
					.println("Warning: cvc4_path should be configured to execute this test case");
			return;
		}
		
		CVC4Solver solver = new CVC4Solver();
		TestSolverMathFloat.testTrigonometry(solver);
	}

	@Test
	public void testFloatMax() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		
		if (Properties.CVC4_PATH == null) {
			System.out
					.println("Warning: cvc4_path should be configured to execute this test case");
			return;
		}
		
		CVC4Solver solver = new CVC4Solver();
		TestSolverMathFloat.testMax(solver);
	}

	@Test
	public void testFloatMin() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		if (Properties.CVC4_PATH == null) {
			System.out
					.println("Warning: cvc4_path should be configured to execute this test case");
			return;
		}

		CVC4Solver solver = new CVC4Solver();
		TestSolverMathFloat.testMin(solver);
	}

	@Test
	public void testFloatRound() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		
		if (Properties.CVC4_PATH == null) {
			System.out
					.println("Warning: cvc4_path should be configured to execute this test case");
			return;
		}
		
		CVC4Solver solver = new CVC4Solver();
		TestSolverMathFloat.testRound(solver);
	}

}
