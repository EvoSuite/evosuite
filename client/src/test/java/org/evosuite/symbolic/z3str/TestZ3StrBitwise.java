package org.evosuite.symbolic.z3str;

import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverBitwise;
import org.junit.Test;

public class TestZ3StrBitwise {

	@Test
	public void testBitAnd() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3StrSolver solver = new Z3StrSolver();
		TestSolverBitwise.testBitAnd(solver);
	}

	@Test
	public void testBitNot() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3StrSolver solver = new Z3StrSolver();
		TestSolverBitwise.testBitNot(solver);
	}

	@Test
	public void testBitOr() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3StrSolver solver = new Z3StrSolver();
		TestSolverBitwise.testBitOr(solver);
	}

	@Test
	public void testBitXor() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3StrSolver solver = new Z3StrSolver();
		TestSolverBitwise.testBitXor(solver);
	}

	@Test
	public void testShiftLeft() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3StrSolver solver = new Z3StrSolver();
		TestSolverBitwise.testShiftLeft(solver);
	}

	@Test
	public void testShiftRight() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3StrSolver solver = new Z3StrSolver();
		TestSolverBitwise.testShiftRight(solver);
	}

	@Test
	public void testShiftRightUnsigned() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3StrSolver solver = new Z3StrSolver();
		TestSolverBitwise.testShiftRightUnsigned(solver);
	}
}
