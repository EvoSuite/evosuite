package org.evosuite.symbolic.solver.z3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverSimpleMath;
import org.evosuite.symbolic.solver.z3.Z3Solver;
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
		Map<String, Object> solution = TestSolverSimpleMath.testSub(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Long var0 = (Long) solution.get("var0");
			Long var1 = (Long) solution.get("var1");

			assertEquals(var0.intValue(), var1.intValue() - 10);
		}
	}

	@Test
	public void testMul() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverSimpleMath.testMul(solver);

		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Long var0 = (Long) solution.get("var0");
			Long var1 = (Long) solution.get("var1");

			assertTrue(var0.intValue() != 0);
			assertEquals(var1.intValue(), var0.intValue() * 2);
		}
	}

	@Test
	public void testDiv() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverSimpleMath.testDiv(solver);

		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Long var0 = (Long) solution.get("var0");
			Long var1 = (Long) solution.get("var1");

			assertEquals(var0.intValue(), var1.intValue() / 5);
		}
	}

	@Test
	public void testEq() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverSimpleMath.testEq(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Long var0 = (Long) solution.get("var0");
			Long var1 = (Long) solution.get("var1");

			assertEquals(var0.intValue(), var1.intValue());
		}
	}

	@Test
	public void testNeq() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverSimpleMath.testNeq(solver);

		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Long var0 = (Long) solution.get("var0");
			Long var1 = (Long) solution.get("var1");

			assertTrue(var0.intValue() != var1.intValue());
		}
	}

	@Test
	public void testLt() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverSimpleMath.testLt(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Long var0 = (Long) solution.get("var0");
			Long var1 = (Long) solution.get("var1");

			assertTrue(var0.intValue() < var1.intValue());
		}
	}

	@Test
	public void testGt() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverSimpleMath.testGt(solver);

		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Long var0 = (Long) solution.get("var0");
			Long var1 = (Long) solution.get("var1");

			assertTrue(var0.intValue() > var1.intValue());
		}
	}

	@Test
	public void testLte() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverSimpleMath.testLte(solver);

		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Long var0 = (Long) solution.get("var0");
			Long var1 = (Long) solution.get("var1");

			assertTrue(var0.intValue() <= var1.intValue());
		}
	}

	@Test
	public void testGte() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverSimpleMath.testGte(solver);

		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Long var0 = (Long) solution.get("var0");
			Long var1 = (Long) solution.get("var1");

			assertTrue(var0.intValue() >= var1.intValue());
		}

	}

	@Test
	public void testMod() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverSimpleMath.testMod(solver);

		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Long var0 = (Long) solution.get("var0");
			Long var1 = (Long) solution.get("var1");

			assertEquals(var0.intValue(), var1.intValue() % 2);
		}
	}

	@Test
	public void testMul2() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverSimpleMath.testMul2(solver);

		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);

			Long var0 = (Long) solution.get("var0");
			Long var1 = (Long) solution.get("var1");

			assertEquals(10, var0.intValue() * var1.intValue());
		}
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
		Map<String, Object> solution = TestSolverSimpleMath
				.testCastIntToReal(solver);

		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Long var0 = (Long) solution.get("var0");

			assertTrue(var0.doubleValue() != 0);
		}
	}
}
