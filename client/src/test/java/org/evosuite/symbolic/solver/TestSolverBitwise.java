package org.evosuite.symbolic.solver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.variable.VariableReference;

import com.examples.with.different.packagename.solver.TestCaseBitAnd;
import com.examples.with.different.packagename.solver.TestCaseBitNot;
import com.examples.with.different.packagename.solver.TestCaseBitOr;
import com.examples.with.different.packagename.solver.TestCaseBitXor;
import com.examples.with.different.packagename.solver.TestCaseShiftLeft;
import com.examples.with.different.packagename.solver.TestCaseShiftRight;
import com.examples.with.different.packagename.solver.TestCaseShiftRightUnsigned;

public class TestSolverBitwise {

	private static DefaultTestCase buildTestCaseBitAnd()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(10 & 1);
		VariableReference int1 = tc.appendIntPrimitive(10 );

		Method method = TestCaseBitAnd.class.getMethod("test", int.class,
				int.class);
		tc.appendMethod(null, method, int0, int1);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseBitOr()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(10 | 1);
		VariableReference int1 = tc.appendIntPrimitive(10);

		Method method = TestCaseBitOr.class.getMethod("test", int.class,
				int.class);
		tc.appendMethod(null, method, int0, int1);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseBitXor()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(10 ^ 1);
		VariableReference int1 = tc.appendIntPrimitive(10);

		Method method = TestCaseBitXor.class.getMethod("test", int.class,
				int.class);
		tc.appendMethod(null, method, int0, int1);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseBitNot()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(~10);
		VariableReference int1 = tc.appendIntPrimitive(10);

		Method method = TestCaseBitNot.class.getMethod("test", int.class,
				int.class);
		tc.appendMethod(null, method, int0, int1);
		return tc.getDefaultTestCase();
	}

	public static void testBitAnd(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseBitAnd();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		assertNotNull(solution);
		Long var0 = (Long) solution.get("var0");
		Long var1 = (Long) solution.get("var1");

		assertEquals(var0.intValue(), (var1.intValue() & 1));
	}

	public static Map<String, Object>  testBitOr(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseBitOr();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}

	public static void testBitXor(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseBitXor();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		assertNotNull(solution);
		Long var0 = (Long) solution.get("var0");
		Long var1 = (Long) solution.get("var1");

		assertEquals(var0.intValue(), (var1.intValue() ^ 1));
	}

	public static Map<String, Object>  testBitNot(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseBitNot();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}

	public static Map<String, Object> testShiftLeft(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseShiftLeft();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static void testShiftRight(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseShiftRight();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		assertNotNull(solution);
		Long var0 = (Long) solution.get("var0");
		Long var1 = (Long) solution.get("var1");

		assertEquals(var0.intValue(), var1.intValue() >> 1);
	}

	public static void testShiftRightUnsigned(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseShiftRightUnsigned();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		assertNotNull(solution);
		Long var0 = (Long) solution.get("var0");
		Long var1 = (Long) solution.get("var1");

		assertEquals(var0.intValue(), var1.intValue() >>> 1);
	}

	private static DefaultTestCase buildTestCaseShiftLeft()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(10 << 1);
		VariableReference int1 = tc.appendIntPrimitive(10);

		Method method = TestCaseShiftLeft.class.getMethod("test", int.class,
				int.class);
		tc.appendMethod(null, method, int0, int1);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseShiftRight()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(10 >> 1);
		VariableReference int1 = tc.appendIntPrimitive(10);

		Method method = TestCaseShiftRight.class.getMethod("test", int.class,
				int.class);
		tc.appendMethod(null, method, int0, int1);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseShiftRightUnsigned()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(10 >>> 1);
		VariableReference int1 = tc.appendIntPrimitive(10);

		Method method = TestCaseShiftRightUnsigned.class.getMethod("test",
				int.class, int.class);
		tc.appendMethod(null, method, int0, int1);
		return tc.getDefaultTestCase();
	}
}
