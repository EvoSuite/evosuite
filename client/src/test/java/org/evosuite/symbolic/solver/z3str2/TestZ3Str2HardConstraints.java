package org.evosuite.symbolic.solver.z3str2;

import static org.evosuite.symbolic.solver.TestSolver.solve;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.solver.DefaultTestCaseConcolicExecutor;
import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.junit.Test;

import com.examples.with.different.packagename.concolic.HardConstraints;

public class TestZ3Str2HardConstraints extends TestZ3Str2 {

	@Test
	public void test0() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();

		DefaultTestCase tc = buildTestCase0();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
		assertTrue(!constraints.isEmpty());
		Map<String, Object> solution = solve(solver, constraints);
		assertNotNull(solution);

		Long int0 = (Long) solution.get("var0");
		assertEquals(13075, int0.intValue());
	}
	
	@Test
	public void test1() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();

		DefaultTestCase tc = buildTestCase1();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
		assertTrue(!constraints.isEmpty());
		Map<String, Object> solution = solve(solver, constraints);
		assertNotNull(solution);

		Long int0 = (Long) solution.get("var0");
		assertEquals(25100, int0.intValue());
	}
	
	@Test
	public void test2() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();

		DefaultTestCase tc = buildTestCase2();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
		assertTrue(!constraints.isEmpty());
		Map<String, Object> solution = solve(solver, constraints);
		assertNotNull(solution);

		Double int0 = (Double) solution.get("var0");
		assertTrue(3.1416F>=int0.floatValue());
		assertTrue(3.1415F<=int0.floatValue());
	}

	private DefaultTestCase buildTestCase0() throws NoSuchMethodException, SecurityException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(13075);
		Method method = HardConstraints.class.getMethod("test0", int.class);
		tc.appendMethod(null, method, int0);
		return tc.getDefaultTestCase();
	}

	private DefaultTestCase buildTestCase1() throws NoSuchMethodException, SecurityException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(25100);
		Method method = HardConstraints.class.getMethod("test1", int.class);
		tc.appendMethod(null, method, int0);
		return tc.getDefaultTestCase();
	}

	private DefaultTestCase buildTestCase2() throws NoSuchMethodException, SecurityException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference float0 = tc.appendFloatPrimitive(3.1415998935699463f);
		Method method = HardConstraints.class.getMethod("test2", float.class);
		tc.appendMethod(null, method, float0);
		return tc.getDefaultTestCase();
	}
}
