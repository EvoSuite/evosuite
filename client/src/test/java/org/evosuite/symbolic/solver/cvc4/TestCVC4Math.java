package org.evosuite.symbolic.solver.cvc4;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverMath;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestCVC4Math {

	@BeforeClass 
	public static void setUpCVC4Path() {
		Properties.CVC4_PATH = System.getenv("CVC4_PATH");
	}
	
	@Test
	public void testAbs() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		CVC4Solver solver = new CVC4Solver();
		TestSolverMath.testAbs(solver);
	}

	@Test
	public void testMax() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		CVC4Solver solver = new CVC4Solver();
		TestSolverMath.testMax(solver);
	}

	@Test
	public void testMin() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		CVC4Solver solver = new CVC4Solver();
		TestSolverMath.testMin(solver);
	}
}
