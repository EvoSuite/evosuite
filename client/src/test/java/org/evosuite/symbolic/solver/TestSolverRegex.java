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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.solver.cvc4.CVC4Solver;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.variable.VariableReference;

import com.examples.with.different.packagename.solver.TestCaseRegex;

public abstract class TestSolverRegex {

	private static DefaultTestCase buildTestConcat() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("aaaaaaaab");

		Method method = TestCaseRegex.class.getMethod("testConcat",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	public static Map<String, Object> testConcat(Solver solver)
			throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {

		DefaultTestCase tc = buildTestConcat();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		return solution;
	}

	private static DefaultTestCase buildTestUnion() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("a");

		Method method = TestCaseRegex.class
				.getMethod("testUnion", String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	private static DefaultTestCase buildTestOptional()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("a");

		Method method = TestCaseRegex.class.getMethod("testOptional",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	public static Map<String, Object> testUnion(Solver solver)
			throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {

		DefaultTestCase tc = buildTestUnion();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		return solution;
	}

	public static Map<String, Object> testOptional(CVC4Solver solver)
			throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		DefaultTestCase tc = buildTestOptional();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		return solution;
	}

	public static Map<String, Object> testString(CVC4Solver solver)
			throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		DefaultTestCase tc = buildTestString();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		return solution;
	}

	private static DefaultTestCase buildTestString() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("hello");

		Method method = TestCaseRegex.class.getMethod("testString",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	public static Map<String, Object> testAnyChar(CVC4Solver solver)
			throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		DefaultTestCase tc = buildTestAnyChar();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		return solution;
	}

	private static DefaultTestCase buildTestAnyChar() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("X");

		Method method = TestCaseRegex.class.getMethod("testAnyChar",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	public static Map<String, Object> testEmpty(CVC4Solver solver)
			throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		DefaultTestCase tc = buildTestEmpty();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		return solution;
	}

	private static DefaultTestCase buildTestEmpty() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("");

		Method method = TestCaseRegex.class
				.getMethod("testEmpty", String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	public static Map<String, Object> testCross(CVC4Solver solver)
			throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		DefaultTestCase tc = buildTestCross();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		return solution;
	}

	private static DefaultTestCase buildTestCross() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("a");

		Method method = TestCaseRegex.class
				.getMethod("testCross", String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	public static Map<String, Object> testRepeatMin(CVC4Solver solver)
			throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		DefaultTestCase tc = buildRepeatMin();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		return solution;
	}

	private static DefaultTestCase buildRepeatMin() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("aaa");

		Method method = TestCaseRegex.class.getMethod("testRepeatMin",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	public static Map<String, Object> testRepeatMinMax(CVC4Solver solver)
			throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		DefaultTestCase tc = buildRepeatMinMax();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		return solution;
	}

	private static DefaultTestCase buildRepeatMinMax()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("aaaa");

		Method method = TestCaseRegex.class.getMethod("testRepeatMinMax",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	public static Map<String, Object> testRepeatN(CVC4Solver solver)
			throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		DefaultTestCase tc = buildRepeatN();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		return solution;
	}

	private static DefaultTestCase buildRepeatN() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("aaaaa");

		Method method = TestCaseRegex.class.getMethod("testRepeatN",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	public static Map<String, Object> testIntersection(CVC4Solver solver)
			throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		DefaultTestCase tc = buildIntersection();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		return solution;
	}

	private static DefaultTestCase buildIntersection()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("4");

		Method method = TestCaseRegex.class.getMethod("testIntersection",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	public static Map<String, Object> testChoice(CVC4Solver solver)
			throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		DefaultTestCase tc = buildChoice();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		return solution;
	}

	private static DefaultTestCase buildChoice() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("b");

		Method method = TestCaseRegex.class.getMethod("testChoice",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}
	
	public static Map<String, Object> testRange(CVC4Solver solver)
			throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		DefaultTestCase tc = buildRange();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solve(solver,constraints);
		return solution;
	}

	private static DefaultTestCase buildRange() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("a");

		Method method = TestCaseRegex.class.getMethod("testRange",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}
}
