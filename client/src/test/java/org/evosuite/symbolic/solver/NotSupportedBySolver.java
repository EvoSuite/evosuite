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
import org.junit.Test;

import com.examples.with.different.packagename.solver.TestCaseAcos;
import com.examples.with.different.packagename.solver.TestCaseAsin;
import com.examples.with.different.packagename.solver.TestCaseAtan;
import com.examples.with.different.packagename.solver.TestCaseAtan2;
import com.examples.with.different.packagename.solver.TestCaseCos;
import com.examples.with.different.packagename.solver.TestCaseExp;
import com.examples.with.different.packagename.solver.TestCaseLog;
import com.examples.with.different.packagename.solver.TestCaseRound;
import com.examples.with.different.packagename.solver.TestCaseSin;
import com.examples.with.different.packagename.solver.TestCaseSqrt;
import com.examples.with.different.packagename.solver.TestCaseTan;
import com.examples.with.different.packagename.solver.TestCaseTokenizer;

public abstract class NotSupportedBySolver extends TestSolver {

	private static DefaultTestCase buildTestTokenizer() throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("Here is Ramon");

		Method method = TestCaseTokenizer.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testStringTokenizer() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

		Solver solver = getSolver();
		DefaultTestCase tc = buildTestTokenizer();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
		Map<String, Object> solution = solve(solver, constraints);
		assertNotNull(solution);
	}

	private static final double DELTA = 1e-15;

	private static DefaultTestCase buildTestCaseCos() throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc.appendDoublePrimitive(Math.cos(Math.PI));
		VariableReference double1 = tc.appendDoublePrimitive(Math.PI);

		Method method = TestCaseCos.class.getMethod("test", double.class, double.class);
		tc.appendMethod(null, method, double0, double1);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCos() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

		DefaultTestCase tc = buildTestCaseCos();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
		Map<String, Object> solution = solve(getSolver(), constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");

		assertEquals(var0.doubleValue(), Math.cos(var1.doubleValue()), DELTA);
	}

	private static DefaultTestCase buildTestCaseExp() throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc.appendDoublePrimitive(Math.exp(Math.PI));
		VariableReference double1 = tc.appendDoublePrimitive(Math.PI);

		Method method = TestCaseExp.class.getMethod("test", double.class, double.class);
		tc.appendMethod(null, method, double0, double1);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testExp() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

		DefaultTestCase tc = buildTestCaseExp();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
		Map<String, Object> solution = solve(getSolver(), constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");

		assertEquals(var0.doubleValue(), Math.exp(var1.doubleValue()), DELTA);
	}

	private static DefaultTestCase buildTestCaseLog() throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc.appendDoublePrimitive(Math.log(Math.PI));
		VariableReference double1 = tc.appendDoublePrimitive(Math.PI);
	
		Method method = TestCaseLog.class.getMethod("test", double.class, double.class);
		tc.appendMethod(null, method, double0, double1);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testLog() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
	
		DefaultTestCase tc = buildTestCaseLog();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
		Map<String, Object> solution = solve(getSolver(), constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");
	
		assertEquals(var0.doubleValue(), Math.tan(var1.doubleValue()), DELTA);
	}

	private static DefaultTestCase buildTestCaseSin() throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc.appendDoublePrimitive(Math.sin(Math.PI));
		VariableReference double1 = tc.appendDoublePrimitive(Math.PI);
	
		Method method = TestCaseSin.class.getMethod("test", double.class, double.class);
		tc.appendMethod(null, method, double0, double1);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testSin() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
	
		DefaultTestCase tc = buildTestCaseSin();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
		Map<String, Object> solution = solve(getSolver(), constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");
	
		assertEquals(var0.doubleValue(), Math.sin(var1.doubleValue()), DELTA);
	}

	private static DefaultTestCase buildTestCaseTan() throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc.appendDoublePrimitive(Math.tan(Math.PI));
		VariableReference double1 = tc.appendDoublePrimitive(Math.PI);
	
		Method method = TestCaseTan.class.getMethod("test", double.class, double.class);
		tc.appendMethod(null, method, double0, double1);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testTan() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
	
		DefaultTestCase tc = buildTestCaseTan();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
		Map<String, Object> solution = solve(getSolver(), constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");
	
		assertEquals(var0.doubleValue(), Math.tan(var1.doubleValue()), DELTA);
	}

	private static DefaultTestCase buildTestCaseAcos() throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc.appendDoublePrimitive(Math.acos(Math.PI));
		VariableReference double1 = tc.appendDoublePrimitive(Math.PI);
	
		Method method = TestCaseAcos.class.getMethod("test", double.class, double.class);
		tc.appendMethod(null, method, double0, double1);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseAsin() throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc.appendDoublePrimitive(Math.asin(Math.PI));
		VariableReference double1 = tc.appendDoublePrimitive(Math.PI);
	
		Method method = TestCaseAsin.class.getMethod("test", double.class, double.class);
		tc.appendMethod(null, method, double0, double1);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseAtan() throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc.appendDoublePrimitive(Math.atan(Math.PI));
		VariableReference double1 = tc.appendDoublePrimitive(Math.PI);
	
		Method method = TestCaseAtan.class.getMethod("test", double.class, double.class);
		tc.appendMethod(null, method, double0, double1);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseAtan2() throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc.appendDoublePrimitive(Math.atan(Math.PI));
		VariableReference double1 = tc.appendDoublePrimitive(Math.PI);
		VariableReference double2 = tc.appendDoublePrimitive(Math.PI);
	
		Method method = TestCaseAtan2.class.getMethod("test", double.class, double.class, double.class);
		tc.appendMethod(null, method, double0, double1, double2);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testAcos() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
	
		DefaultTestCase tc = buildTestCaseAcos();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
		Map<String, Object> solution = solve(getSolver(), constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");
	
		assertEquals(var0.doubleValue(), Math.acos(var1.doubleValue()), DELTA);
	}

	@Test
	public void testAsin() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
	
		DefaultTestCase tc = buildTestCaseAsin();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
		Map<String, Object> solution = solve(getSolver(), constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");
	
		assertEquals(var0.doubleValue(), Math.asin(var1.doubleValue()), DELTA);
	}

	@Test
	public void testAtan() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
	
		DefaultTestCase tc = buildTestCaseAtan();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
		Map<String, Object> solution = solve(getSolver(), constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");
	
		assertEquals(var0.doubleValue(), Math.atan(var1.doubleValue()), DELTA);
	}

	@Test
	public void testAtan2() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
	
		DefaultTestCase tc = buildTestCaseAtan2();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
		Map<String, Object> solution = solve(getSolver(), constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");
		Double var2 = (Double) solution.get("var2");
	
		assertEquals(var0.doubleValue(), Math.atan2(var1.doubleValue(), var2.doubleValue()), DELTA);
	}

	private static DefaultTestCase buildTestCaseSqrt() throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc.appendDoublePrimitive(Math.sqrt(Math.PI));
		VariableReference double1 = tc.appendDoublePrimitive(Math.PI);
	
		Method method = TestCaseSqrt.class.getMethod("test", double.class, double.class);
		tc.appendMethod(null, method, double0, double1);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testSqrt() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
		DefaultTestCase tc = buildTestCaseSqrt();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
		Map<String, Object> solution = solve(getSolver(), constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");
	
		assertEquals(var0.doubleValue(), Math.sqrt(var1.doubleValue()), DELTA);
	}

	private static DefaultTestCase buildTestCaseRound() throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive((int) Math.round(Math.PI));
		VariableReference double1 = tc.appendDoublePrimitive(Math.PI);
	
		Method method = TestCaseRound.class.getMethod("test", int.class, double.class);
		tc.appendMethod(null, method, int0, double1);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testRound() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
	
		DefaultTestCase tc = buildTestCaseRound();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
		Map<String, Object> solution = solve(getSolver(), constraints);
		assertNotNull(solution);
		Integer var0 = (Integer) solution.get("var0");
		Double var1 = (Double) solution.get("var1");
	
		assertEquals(var0.intValue(), Math.round(var1.doubleValue()));
	}

}
