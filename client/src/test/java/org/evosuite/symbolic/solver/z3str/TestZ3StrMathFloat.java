package org.evosuite.symbolic.solver.z3str;

import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverMathFloat;
import org.evosuite.symbolic.solver.z3str.Z3StrSolver;
import org.junit.Test;

public class TestZ3StrMathFloat {

	@Test
	public void testFloatAbs() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3StrSolver solver = new Z3StrSolver();
		TestSolverMathFloat.testAbs(solver);
	}

	@Test
	public void testFloatTrigonometry() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3StrSolver solver = new Z3StrSolver();
		TestSolverMathFloat.testTrigonometry(solver);
	}

	@Test
	public void testFloatMax() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3StrSolver solver = new Z3StrSolver();
		TestSolverMathFloat.testMax(solver);
	}

	@Test
	public void testFloatMin() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3StrSolver solver = new Z3StrSolver();
		TestSolverMathFloat.testMin(solver);
	}

	@Test
	public void testFloatRound() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3StrSolver solver = new Z3StrSolver();
		TestSolverMathFloat.testRound(solver);
	}

}
