package org.evosuite.symbolic.solver.cvc4;

import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverMath;
import org.junit.Test;

public class TestCVC4Math {

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
