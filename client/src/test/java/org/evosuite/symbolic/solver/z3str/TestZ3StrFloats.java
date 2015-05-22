package org.evosuite.symbolic.solver.z3str;

import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverFloats;
import org.junit.Test;

public class TestZ3StrFloats {

	@Test
	public void testFloatEq() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3StrSolver solver = new Z3StrSolver();
		TestSolverFloats.testEq(solver);
	}

	@Test
	public void testFloatNeq() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3StrSolver solver = new Z3StrSolver();
		TestSolverFloats.testNeq(solver);
	}

	@Test
	public void testFloatLt() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3StrSolver solver = new Z3StrSolver();
		TestSolverFloats.testLt(solver);
	}

	@Test
	public void testFloatGt() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3StrSolver solver = new Z3StrSolver();
		TestSolverFloats.testGt(solver);
	}

	@Test
	public void testFloatLte() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3StrSolver solver = new Z3StrSolver();
		TestSolverFloats.testLte(solver);
	}

	@Test
	public void testFloatGte() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3StrSolver solver = new Z3StrSolver();
		TestSolverFloats.testGte(solver);
	}

	@Test
	public void testFloatFraction() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3StrSolver solver = new Z3StrSolver();
		TestSolverFloats.testFraction(solver);
	}

	@Test
	public void testFloatAdd() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3StrSolver solver = new Z3StrSolver();
		TestSolverFloats.testAdd(solver);
	}

	@Test
	public void testFloatSub() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3StrSolver solver = new Z3StrSolver();
		TestSolverFloats.testSub(solver);
	}

	@Test
	public void testFloatMul() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3StrSolver solver = new Z3StrSolver();
		TestSolverFloats.testMul(solver);
	}

	@Test
	public void testFloatDiv() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3StrSolver solver = new Z3StrSolver();
		TestSolverFloats.testDiv(solver);
	}

	@Test
	public void testFloatMod() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3StrSolver solver = new Z3StrSolver();
		TestSolverFloats.testMod(solver);
	}
}
