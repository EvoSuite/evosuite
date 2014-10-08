package org.evosuite.symbolic.solver.z3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverBitwise;
import org.evosuite.symbolic.solver.z3.Z3Solver;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestZ3Bitwise {

	@Test
	public void testBitAnd() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverBitwise.testBitAnd(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Long var0 = (Long) solution.get("var0");
			Long var1 = (Long) solution.get("var1");

			assertEquals(var0.intValue(), (var1.intValue() & 1));
		}
	}

	@Test
	public void testBitNot() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverBitwise.testBitNot(solver);
	}

	@Test
	public void testBitOr() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverBitwise.testBitOr(solver);
	}

	@Test
	public void testBitXor() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverBitwise.testBitXor(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Long var0 = (Long) solution.get("var0");
			Long var1 = (Long) solution.get("var1");

			assertEquals(var0.intValue(), (var1.intValue() ^ 1));
		}
	}

	@Test
	public void testShiftLeft() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverBitwise.testShiftLeft(solver);
	}

	@Test
	public void testShiftRight() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverBitwise.testShiftRight(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Long var0 = (Long) solution.get("var0");
			Long var1 = (Long) solution.get("var1");

			assertEquals(var0.intValue(), var1.intValue() >> 1);
		}
	}

	@Test
	public void testShiftRightUnsigned() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverBitwise
				.testShiftRightUnsigned(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Long var0 = (Long) solution.get("var0");
			Long var1 = (Long) solution.get("var1");

			assertEquals(var0.intValue(), var1.intValue() >>> 1);
		}
	}

	@BeforeClass
	public static void setUpZ3Path() {
		Properties.Z3_PATH = System.getenv("Z3_PATH");
	}
}
