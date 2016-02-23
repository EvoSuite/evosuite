/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic.solver;

import static org.evosuite.symbolic.solver.TestSolver.solve;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.variable.VariableReference;

import com.examples.with.different.packagename.solver.TestCaseAcos;
import com.examples.with.different.packagename.solver.TestCaseAsin;
import com.examples.with.different.packagename.solver.TestCaseAtan;
import com.examples.with.different.packagename.solver.TestCaseAtan2;
import com.examples.with.different.packagename.solver.TestCaseCos;
import com.examples.with.different.packagename.solver.TestCaseExp;
import com.examples.with.different.packagename.solver.TestCaseFloatAdd;
import com.examples.with.different.packagename.solver.TestCaseFloatDiv;
import com.examples.with.different.packagename.solver.TestCaseFloatEq;
import com.examples.with.different.packagename.solver.TestCaseFloatFraction;
import com.examples.with.different.packagename.solver.TestCaseFloatGt;
import com.examples.with.different.packagename.solver.TestCaseFloatGte;
import com.examples.with.different.packagename.solver.TestCaseFloatLt;
import com.examples.with.different.packagename.solver.TestCaseFloatLte;
import com.examples.with.different.packagename.solver.TestCaseFloatMod;
import com.examples.with.different.packagename.solver.TestCaseFloatMul;
import com.examples.with.different.packagename.solver.TestCaseFloatNeq;
import com.examples.with.different.packagename.solver.TestCaseFloatSub;
import com.examples.with.different.packagename.solver.TestCaseLog;
import com.examples.with.different.packagename.solver.TestCaseRound;
import com.examples.with.different.packagename.solver.TestCaseSin;
import com.examples.with.different.packagename.solver.TestCaseSqrt;
import com.examples.with.different.packagename.solver.TestCaseTan;

public class TestSolverFloats {

	private static DefaultTestCase buildTestCaseSin() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc.appendDoublePrimitive(Math.sin(Math.PI));
		VariableReference double1 = tc.appendDoublePrimitive(Math.PI);

		Method method = TestCaseSin.class.getMethod("test", double.class,
				double.class);
		tc.appendMethod(null, method, double0, double1);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseCos() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc.appendDoublePrimitive(Math.cos(Math.PI));
		VariableReference double1 = tc.appendDoublePrimitive(Math.PI);

		Method method = TestCaseCos.class.getMethod("test", double.class,
				double.class);
		tc.appendMethod(null, method, double0, double1);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseTan() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc.appendDoublePrimitive(Math.tan(Math.PI));
		VariableReference double1 = tc.appendDoublePrimitive(Math.PI);

		Method method = TestCaseTan.class.getMethod("test", double.class,
				double.class);
		tc.appendMethod(null, method, double0, double1);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseFraction()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc.appendDoublePrimitive(Math.PI - 3);

		Method method = TestCaseFloatFraction.class.getMethod("test",
				double.class);
		tc.appendMethod(null, method, double0);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseAsin()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc
				.appendDoublePrimitive(Math.asin(Math.PI));
		VariableReference double1 = tc.appendDoublePrimitive(Math.PI);

		Method method = TestCaseAsin.class.getMethod("test", double.class,
				double.class);
		tc.appendMethod(null, method, double0, double1);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseAcos()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc
				.appendDoublePrimitive(Math.acos(Math.PI));
		VariableReference double1 = tc.appendDoublePrimitive(Math.PI);

		Method method = TestCaseAcos.class.getMethod("test", double.class,
				double.class);
		tc.appendMethod(null, method, double0, double1);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseAtan()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc
				.appendDoublePrimitive(Math.atan(Math.PI));
		VariableReference double1 = tc.appendDoublePrimitive(Math.PI);

		Method method = TestCaseAtan.class.getMethod("test", double.class,
				double.class);
		tc.appendMethod(null, method, double0, double1);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseAtan2()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc
				.appendDoublePrimitive(Math.atan(Math.PI));
		VariableReference double1 = tc.appendDoublePrimitive(Math.PI);
		VariableReference double2 = tc.appendDoublePrimitive(Math.PI);

		Method method = TestCaseAtan2.class.getMethod("test", double.class,
				double.class, double.class);
		tc.appendMethod(null, method, double0, double1, double2);
		return tc.getDefaultTestCase();
	}

	private static final double DELTA = 1e-15;

	public static void testSin(Solver solver) throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {

		DefaultTestCase tc = buildTestCaseSin();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");

		assertEquals(var0.doubleValue(), Math.sin(var1.doubleValue()), DELTA);
	}

	public static void testCos(Solver solver) throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {

		DefaultTestCase tc = buildTestCaseCos();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");

		assertEquals(var0.doubleValue(), Math.cos(var1.doubleValue()), DELTA);
	}

	public static void testTan(Solver solver) throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {

		DefaultTestCase tc = buildTestCaseTan();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");

		assertEquals(var0.doubleValue(), Math.tan(var1.doubleValue()), DELTA);
	}

	public static void testRound(Solver solver) throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {

		DefaultTestCase tc = buildTestCaseRound();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		assertNotNull(solution);
		Integer var0 = (Integer) solution.get("var0");
		Double var1 = (Double) solution.get("var1");

		assertEquals(var0.intValue(), Math.round(var1.doubleValue()));
	}

	public static void testAsin(Solver solver) throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {

		DefaultTestCase tc = buildTestCaseAsin();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");

		assertEquals(var0.doubleValue(), Math.asin(var1.doubleValue()), DELTA);
	}

	public static void testAcos(Solver solver) throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {

		DefaultTestCase tc = buildTestCaseAcos();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");

		assertEquals(var0.doubleValue(), Math.acos(var1.doubleValue()), DELTA);
	}

	public static void testAtan(Solver solver) throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {

		DefaultTestCase tc = buildTestCaseAtan();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");

		assertEquals(var0.doubleValue(), Math.atan(var1.doubleValue()), DELTA);
	}

	public static void testAtan2(Solver solver) throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {

		DefaultTestCase tc = buildTestCaseAtan2();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");
		Double var2 = (Double) solution.get("var2");

		assertEquals(var0.doubleValue(),
				Math.atan2(var1.doubleValue(), var2.doubleValue()), DELTA);
	}

	public static void testEq(Solver solver) throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {

		DefaultTestCase tc = buildTestCaseEq();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");

		assertEquals(var0.intValue(), var1.intValue());
	}

	public static void testFraction(Solver solver) throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {

		DefaultTestCase tc = buildTestCaseFraction();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");

		assertTrue(var0.doubleValue() > 0);
		assertTrue(var0.doubleValue() < 1);
	}

	public static void testGt(Solver solver) throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {

		DefaultTestCase tc = buildTestCaseGt();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");

		assertTrue(var0.doubleValue() > var1.doubleValue());
	}

	public static void testGte(Solver solver) throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {

		DefaultTestCase tc = buildTestCaseGte();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");

		assertTrue(var0.doubleValue() >= var1.doubleValue());
	}

	public static void testLt(Solver solver) throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {

		DefaultTestCase tc = buildTestCaseLt();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");

		assertTrue(var0.doubleValue() < var1.doubleValue());
	}

	public static void testLte(Solver solver) throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {

		DefaultTestCase tc = buildTestCaseLte();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");

		assertTrue(var0.doubleValue() <= var1.doubleValue());
	}

	public static void testNeq(Solver solver) throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		DefaultTestCase tc = buildTestCaseNeq();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");

		assertTrue(var0.intValue() != var1.intValue());
	}

	private static DefaultTestCase buildTestCaseEq() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc.appendDoublePrimitive(Math.PI);
		VariableReference double1 = tc.appendDoublePrimitive(Math.PI);

		Method method = TestCaseFloatEq.class.getMethod("test", double.class,
				double.class);
		tc.appendMethod(null, method, double0, double1);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseNeq() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc.appendDoublePrimitive(Math.PI);
		VariableReference double1 = tc.appendDoublePrimitive(Math.E);

		Method method = TestCaseFloatNeq.class.getMethod("test", double.class,
				double.class);
		tc.appendMethod(null, method, double0, double1);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseLt() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendDoublePrimitive(Math.PI - 1);
		VariableReference int1 = tc.appendDoublePrimitive(Math.PI);

		Method method = TestCaseFloatLt.class.getMethod("test", double.class,
				double.class);
		tc.appendMethod(null, method, int0, int1);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseLte() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendDoublePrimitive(Math.PI - 1);
		VariableReference int1 = tc.appendDoublePrimitive(Math.PI);

		Method method = TestCaseFloatLte.class.getMethod("test", double.class,
				double.class);
		tc.appendMethod(null, method, int0, int1);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseGt() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendDoublePrimitive(Math.PI);
		VariableReference int1 = tc.appendDoublePrimitive(Math.PI - 1);

		Method method = TestCaseFloatGt.class.getMethod("test", double.class,
				double.class);
		tc.appendMethod(null, method, int0, int1);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseGte() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendDoublePrimitive(Math.PI);
		VariableReference int1 = tc.appendDoublePrimitive(Math.PI - 1);

		Method method = TestCaseFloatGte.class.getMethod("test", double.class,
				double.class);
		tc.appendMethod(null, method, int0, int1);
		return tc.getDefaultTestCase();
	}

	public static void testLog(Solver solver) throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {

		DefaultTestCase tc = buildTestCaseLog();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");

		assertEquals(var0.doubleValue(), Math.tan(var1.doubleValue()), DELTA);
	}

	public static void testExp(Solver solver) throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {

		DefaultTestCase tc = buildTestCaseExp();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");

		assertEquals(var0.doubleValue(), Math.exp(var1.doubleValue()), DELTA);
	}

	public static void testSqrt(Solver solver) throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		DefaultTestCase tc = buildTestCaseSqrt();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");

		assertEquals(var0.doubleValue(), Math.sqrt(var1.doubleValue()), DELTA);
	}

	private static DefaultTestCase buildTestCaseSqrt()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc
				.appendDoublePrimitive(Math.sqrt(Math.PI));
		VariableReference double1 = tc.appendDoublePrimitive(Math.PI);

		Method method = TestCaseSqrt.class.getMethod("test", double.class,
				double.class);
		tc.appendMethod(null, method, double0, double1);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseExp() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc.appendDoublePrimitive(Math.exp(Math.PI));
		VariableReference double1 = tc.appendDoublePrimitive(Math.PI);

		Method method = TestCaseExp.class.getMethod("test", double.class,
				double.class);
		tc.appendMethod(null, method, double0, double1);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseLog() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc.appendDoublePrimitive(Math.log(Math.PI));
		VariableReference double1 = tc.appendDoublePrimitive(Math.PI);

		Method method = TestCaseLog.class.getMethod("test", double.class,
				double.class);
		tc.appendMethod(null, method, double0, double1);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseRound()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive((int) Math
				.round(Math.PI));
		VariableReference double1 = tc.appendDoublePrimitive(Math.PI);

		Method method = TestCaseRound.class.getMethod("test", int.class,
				double.class);
		tc.appendMethod(null, method, int0, double1);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseFloatAdd()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc.appendDoublePrimitive(0);
		VariableReference double1 = tc.appendDoublePrimitive(Math.PI);

		Method method = TestCaseFloatAdd.class.getMethod("test", double.class,
				double.class);
		tc.appendMethod(null, method, double0, double1);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseFloatSub()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc.appendDoublePrimitive(Math.PI);
		VariableReference double1 = tc.appendDoublePrimitive(0);

		Method method = TestCaseFloatSub.class.getMethod("test", double.class,
				double.class);
		tc.appendMethod(null, method, double0, double1);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseFloatMul()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc.appendDoublePrimitive(2.2);
		VariableReference double1 = tc.appendDoublePrimitive(1.1);

		Method method = TestCaseFloatMul.class.getMethod("test", double.class,
				double.class);
		tc.appendMethod(null, method, double0, double1);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseFloatDiv()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc.appendDoublePrimitive(1.1);
		VariableReference double1 = tc.appendDoublePrimitive(2.2);

		Method method = TestCaseFloatDiv.class.getMethod("test", double.class,
				double.class);
		tc.appendMethod(null, method, double0, double1);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestCaseFloatMod()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc.appendDoublePrimitive(2.2 % 2.0);

		Method method = TestCaseFloatMod.class.getMethod("test", double.class);
		tc.appendMethod(null, method, double0);
		return tc.getDefaultTestCase();
	}

	public static void testAdd(Solver solver) throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {

		DefaultTestCase tc = buildTestCaseFloatAdd();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");

		assertEquals(Math.PI, var0.doubleValue() + var1.doubleValue(), DELTA);
	}

	public static void testSub(Solver solver) throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {

		DefaultTestCase tc = buildTestCaseFloatSub();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");

		assertEquals(Math.PI, var0.doubleValue() - var1.doubleValue(), DELTA);
	}

	public static void testMul(Solver solver) throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {

		DefaultTestCase tc = buildTestCaseFloatMul();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");

		assertTrue(var0.doubleValue() == var1.doubleValue() * 2.0);
	}

	public static void testDiv(Solver solver) throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {

		DefaultTestCase tc = buildTestCaseFloatDiv();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");
		Double var1 = (Double) solution.get("var1");

		assertTrue(var0.doubleValue() == var1.doubleValue() / 2.0);
	}

	public static void testMod(Solver solver) throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {

		DefaultTestCase tc = buildTestCaseFloatMod();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		assertNotNull(solution);
		Double var0 = (Double) solution.get("var0");

		assertEquals(var0.doubleValue(), 2.2 % 2.0, DELTA);
	}

}
