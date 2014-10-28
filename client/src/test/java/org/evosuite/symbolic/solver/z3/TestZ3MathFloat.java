package org.evosuite.symbolic.solver.z3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverMathFloat;
import org.evosuite.symbolic.solver.z3.Z3Solver;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestZ3MathFloat {

	private static final double DELTA = 1e-15;

	@Test
	public void testFloatAbs() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverMathFloat.testAbs(solver);

		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Double var0 = (Double) solution.get("var0");

			assertTrue(Math.abs(var0.doubleValue()) > 0);
		}
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
		Map<String, Object> solution = TestSolverMathFloat.testMax(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Double var0 = (Double) solution.get("var0");
			Double var1 = (Double) solution.get("var1");

			assertEquals(10, Math.max(var0.doubleValue(), var1.doubleValue()),
					DELTA);
		}
	}

	@Test
	public void testFloatMin() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverMathFloat.testMin(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Double var0 = (Double) solution.get("var0");
			Double var1 = (Double) solution.get("var1");

			assertEquals(10, Math.min(var0.doubleValue(), var1.doubleValue()),
					DELTA);
		}
	}

	@Test
	public void testFloatRound() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverMathFloat.testRound(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Double var0 = (Double) solution.get("var0");
			Long var1 = (Long) solution.get("var1");

			assertEquals(Math.round(var0.doubleValue()), var1.intValue());
		}
	}

}
