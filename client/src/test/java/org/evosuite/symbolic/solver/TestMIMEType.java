package org.evosuite.symbolic.solver;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import java.util.Collection;

import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.testcase.DefaultTestCase;

import com.examples.with.different.packagename.concolic.MIMETypeTest;

public class TestMIMEType extends TestSolver {

	private static DefaultTestCase buildMIMETypeTest() throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		Method method = MIMETypeTest.class.getMethod("test");
		tc.appendMethod(null, method);
		return tc.getDefaultTestCase();
	}

	public static void testMIMEType(Solver solver)
			throws SecurityException, NoSuchMethodException, SolverTimeoutException {

		DefaultTestCase tc = buildMIMETypeTest();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
		assertNotNull(constraints);
	}
}
