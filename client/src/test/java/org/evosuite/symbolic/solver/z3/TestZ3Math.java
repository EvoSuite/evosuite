package org.evosuite.symbolic.solver.z3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverMath;
import org.evosuite.symbolic.solver.z3.Z3Solver;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestZ3Math {

	@BeforeClass
	public static void setUpZ3Path() {
		Properties.Z3_PATH = System.getenv("Z3_PATH");
	}

	@Test
	public void testAbs() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverMath.testAbs(solver);

		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Long var0 = (Long) solution.get("var0");

			assertTrue(Math.abs(var0.intValue()) > 0);
		}
	}

	@Test
	public void testMax() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverMath.testMax(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Long var0 = (Long) solution.get("var0");
			Long var1 = (Long) solution.get("var1");

			assertEquals(10, Math.max(var0.intValue(), var1.intValue()));
		}
	}

	@Test
	public void testMin() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverMath.testMin(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Long var0 = (Long) solution.get("var0");
			Long var1 = (Long) solution.get("var1");

			assertEquals(10, Math.min(var0.intValue(), var1.intValue()));
		}
	}
}
