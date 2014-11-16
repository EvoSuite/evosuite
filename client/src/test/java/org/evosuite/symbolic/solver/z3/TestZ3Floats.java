package org.evosuite.symbolic.solver.z3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverFloats;
import org.evosuite.symbolic.solver.z3.Z3Solver;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestZ3Floats {

	private static final double DELTA = 1e-15;

	@Test
	public void testFloatEq() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverFloats.testEq(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Double var0 = (Double) solution.get("var0");
			Double var1 = (Double) solution.get("var1");

			assertEquals(var0.intValue(), var1.intValue());
		}
	}

	@Test
	public void testFloatAcos() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverFloats.testAcos(solver);

		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Double var0 = (Double) solution.get("var0");
			Double var1 = (Double) solution.get("var1");

			assertEquals(var0.doubleValue(), Math.acos(var1.doubleValue()),
					DELTA);
		}
	}

	@Test
	public void testFloatAsin() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverFloats.testAsin(solver);

		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Double var0 = (Double) solution.get("var0");
			Double var1 = (Double) solution.get("var1");

			assertEquals(var0.doubleValue(), Math.asin(var1.doubleValue()),
					DELTA);
		}
	}

	@Test
	public void testFloatAtan() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverFloats.testAtan(solver);

		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Double var0 = (Double) solution.get("var0");
			Double var1 = (Double) solution.get("var1");

			assertEquals(var0.doubleValue(), Math.atan(var1.doubleValue()),
					DELTA);
		}
	}

	@Test
	public void testFloatAtan2() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverFloats.testAtan2(solver);

		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Double var0 = (Double) solution.get("var0");
			Double var1 = (Double) solution.get("var1");
			Double var2 = (Double) solution.get("var2");

			assertEquals(var0.doubleValue(),
					Math.atan2(var1.doubleValue(), var2.doubleValue()), DELTA);
		}
	}

	@Test
	public void testFloatCos() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverFloats.testCos(solver);

		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Double var0 = (Double) solution.get("var0");
			Double var1 = (Double) solution.get("var1");

			assertEquals(var0.doubleValue(), Math.cos(var1.doubleValue()),
					DELTA);
		}
	}

	@Test
	public void testFloatExp() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverFloats.testExp(solver);

		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Double var0 = (Double) solution.get("var0");
			Double var1 = (Double) solution.get("var1");

			assertEquals(var0.doubleValue(), Math.exp(var1.doubleValue()),
					DELTA);

		}
	}

	@Test
	public void testFloatLog() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverFloats.testLog(solver);

		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Double var0 = (Double) solution.get("var0");
			Double var1 = (Double) solution.get("var1");

			assertEquals(var0.doubleValue(), Math.log(var1.doubleValue()),
					DELTA);

		}
	}

	@Test
	public void testFloatRound() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverFloats.testRound(solver);

		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Long var0 = (Long) solution.get("var0");
			Double var1 = (Double) solution.get("var1");

			assertEquals(var0.intValue(), Math.round(var1.doubleValue()));

		}
	}

	@Test
	public void testFloatSin() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverFloats.testSin(solver);

		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Double var0 = (Double) solution.get("var0");
			Double var1 = (Double) solution.get("var1");

			assertEquals(var0.doubleValue(), Math.sin(var1.doubleValue()),
					DELTA);

		}
	}

	@Test
	public void testFloatSqrt() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverFloats.testSqrt(solver);

		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Double var0 = (Double) solution.get("var0");
			Double var1 = (Double) solution.get("var1");

			assertEquals(var0.doubleValue(), Math.sqrt(var1.doubleValue()),
					DELTA);
		}
	}

	@Test
	public void testFloatNeq() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverFloats.testNeq(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Double var0 = (Double) solution.get("var0");
			Double var1 = (Double) solution.get("var1");

			assertTrue(var0.intValue() != var1.intValue());
		}
	}

	@Test
	public void testFloatLt() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverFloats.testLt(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Double var0 = (Double) solution.get("var0");
			Double var1 = (Double) solution.get("var1");

			assertTrue(var0.doubleValue() < var1.doubleValue());
		}
	}

	@Test
	public void testFloatGt() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverFloats.testGt(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Double var0 = (Double) solution.get("var0");
			Double var1 = (Double) solution.get("var1");

			assertTrue(var0.doubleValue() > var1.doubleValue());
		}
	}

	@Test
	public void testFloatTan() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverFloats.testTan(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Double var0 = (Double) solution.get("var0");
			Double var1 = (Double) solution.get("var1");

			assertEquals(var0.doubleValue(), Math.tan(var1.doubleValue()),
					DELTA);
		}
	}

	@Test
	public void testFloatLte() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverFloats.testLte(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Double var0 = (Double) solution.get("var0");
			Double var1 = (Double) solution.get("var1");

			assertTrue(var0.doubleValue() <= var1.doubleValue());
		}
	}

	@Test
	public void testFloatGte() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverFloats.testGte(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Double var0 = (Double) solution.get("var0");
			Double var1 = (Double) solution.get("var1");

			assertTrue(var0.doubleValue() >= var1.doubleValue());
		}
	}

	@Test
	public void testFloatFraction() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverFloats.testFraction(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Double var0 = (Double) solution.get("var0");

			assertTrue(var0.doubleValue() > 0);
			assertTrue(var0.doubleValue() < 1);
		}
	}

	@Test
	public void testFloatAdd() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverFloats.testAdd(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Double var0 = (Double) solution.get("var0");
			Double var1 = (Double) solution.get("var1");

			assertEquals(Math.PI, var0.doubleValue() + var1.doubleValue(),
					DELTA);
		}
	}

	@Test
	public void testFloatSub() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverFloats.testSub(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Double var0 = (Double) solution.get("var0");
			Double var1 = (Double) solution.get("var1");

			assertEquals(Math.PI, var0.doubleValue() - var1.doubleValue(),
					DELTA);
		}
	}

	@Test
	public void testFloatMul() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverFloats.testMul(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Double var0 = (Double) solution.get("var0");
			Double var1 = (Double) solution.get("var1");

			assertTrue(var0.doubleValue() == var1.doubleValue() * 2.0);
		}
	}

	@Test
	public void testFloatDiv() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverFloats.testDiv(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Double var0 = (Double) solution.get("var0");
			Double var1 = (Double) solution.get("var1");

			assertTrue(var0.doubleValue() == var1.doubleValue() / 2.0);
		}
	}

	@Test
	public void testFloatMod() throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		Map<String, Object> solution = TestSolverFloats.testMod(solver);
		if (Properties.Z3_PATH != null) {
			assertNotNull(solution);
			Double var0 = (Double) solution.get("var0");

			assertEquals(var0.doubleValue(), 2.2 % 2.0, DELTA);
		}
	}

	@BeforeClass
	public static void setUpZ3Path() {
		Properties.Z3_PATH = System.getenv("Z3_PATH");
	}
}
