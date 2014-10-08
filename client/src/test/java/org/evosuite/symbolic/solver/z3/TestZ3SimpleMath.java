package org.evosuite.symbolic.solver.z3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverSimpleMath;
import org.evosuite.symbolic.z3.Z3Solver;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestZ3SimpleMath {

	@BeforeClass
	public static void setUpZ3Path() {
		Properties.Z3_PATH = System.getenv("Z3_PATH");
	}

	@Test
	public void testAdd() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverSimpleMath.testAdd(solver);

		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Long var0 = (Long) solution.get("var0");
			Long var1 = (Long) solution.get("var1");

			assertEquals(var0.intValue(), var0.intValue() + var1.intValue());
		}

	}

	@Test
	public void testSub() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverSimpleMath.testSub(solver);
	}

	@Test
	public void testMul() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverSimpleMath.testMul(solver);
	}

	@Test
	public void testDiv() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverSimpleMath.testDiv(solver);
	}

	@Test
	public void testEq() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverSimpleMath.testEq(solver);
	}

	@Test
	public void testNeq() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverSimpleMath.testNeq(solver);
	}

	@Test
	public void testLt() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverSimpleMath.testLt(solver);
	}

	@Test
	public void testGt() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverSimpleMath.testGt(solver);
	}

	@Test
	public void testLte() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverSimpleMath.testLte(solver);
	}

	@Test
	public void testGte() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverSimpleMath.testGte(solver);
	}

	@Test
	public void testMod() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverSimpleMath.testMod(solver);
	}

	@Test
	public void testMul2() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverSimpleMath.testMul2(solver);
	}

	@Test
	public void testCastRealToInt() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverSimpleMath.testCastRealToInt(solver);
	}

	@Test
	public void testCastIntToReal() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverSimpleMath.testCastIntToReal(solver);
	}
}
