package org.evosuite.symbolic.solver;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.variable.VariableReference;

import com.examples.with.different.packagename.solver.TestCaseTokenizer;

public class TestSolverTokenizer {

	private static DefaultTestCase buildTestTokenizer()
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("Here is Ramon");
	
		Method method = TestCaseTokenizer.class.getMethod("test",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	public static Map<String, Object> testStringTokenizer(Solver solver)
			throws SecurityException, NoSuchMethodException,
			ConstraintSolverTimeoutException {
	
		DefaultTestCase tc = buildTestTokenizer();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor
				.execute(tc);
		Map<String, Object> solution = solver.solve(constraints);
		return solution;
	}

}
