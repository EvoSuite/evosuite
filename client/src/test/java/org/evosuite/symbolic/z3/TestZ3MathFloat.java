package org.evosuite.symbolic.z3;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverMathFloat;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestZ3MathFloat {

	@Test
	public void testFloatAbs() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverMathFloat.testAbs(solver);
	}

	@Test
	public void testFloatTrigonometry() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverMathFloat.testTrigonometry(solver);
	}

	@BeforeClass 
	public static void setUpZ3Path() {
		Properties.Z3_PATH = System.getenv("Z3_PATH");
	}
	
	@Test
	public void testFloatMax() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverMathFloat.testMax(solver);
	}

	@Test
	public void testFloatMin() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverMathFloat.testMin(solver);
	}

	@Test
	public void testFloatRound() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverMathFloat.testRound(solver);
	}

}
