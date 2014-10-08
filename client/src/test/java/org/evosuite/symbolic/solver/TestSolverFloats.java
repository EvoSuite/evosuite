package org.evosuite.symbolic.solver;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.VariableReference;

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

public abstract class TestSolverFloats {

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
				.appendDoublePrimitive(Math.atan2(Math.PI,Math.PI));
		VariableReference double1 = tc.appendDoublePrimitive(Math.PI);
		VariableReference double2 = tc.appendDoublePrimitive(Math.PI);

		Method method = TestCaseAtan2.class.getMethod("test", double.class,
				double.class, double.class);
		tc.appendMethod(null, method, double0, double1, double2);
		return tc.getDefaultTestCase();
	}


	public static Map<String, Object> testSin(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseSin();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testCos(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseCos();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testTan(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseTan();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testRound(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseRound();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testAsin(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseAsin();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testAcos(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseAcos();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testAtan(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseAtan();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testAtan2(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseAtan2();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testEq(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseEq();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testFraction(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseFraction();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testGt(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseGt();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testGte(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseGte();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testLt(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseLt();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testLte(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseLte();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testNeq(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		DefaultTestCase tc = buildTestCaseNeq();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
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

	public static Map<String, Object> testLog(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseLog();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testExp(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseExp();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testSqrt(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		DefaultTestCase tc = buildTestCaseSqrt();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
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

	public static Map<String, Object> testAdd(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseFloatAdd();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testSub(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseFloatSub();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testMul(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseFloatMul();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testDiv(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseFloatDiv();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

	public static Map<String, Object> testMod(Solver solver) throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestCaseFloatMod();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);

		return solution;
	}

}
