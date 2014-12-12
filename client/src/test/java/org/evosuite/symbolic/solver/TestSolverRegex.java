package org.evosuite.symbolic.solver;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.VariableReference;

import com.examples.with.different.packagename.solver.TestCaseRegexMatches;

public abstract class TestSolverRegex {

	private static DefaultTestCase buildTestRegexMatches()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("aaaaaaaab");

		Method method = TestCaseRegexMatches.class.getMethod("test",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	public static Map<String, Object> testRegexMatches(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {

		DefaultTestCase tc = buildTestRegexMatches();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}

}
