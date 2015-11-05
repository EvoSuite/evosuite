package org.evosuite.symbolic;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.concolic.ConcolicReflection;

public class TestConcolicReflection {

	private DefaultTestCase buildClassNewInstanceTestCase() throws NoSuchMethodException, SecurityException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(10);
		Method method = ConcolicReflection.class.getMethod("classNewInstance", int.class);
		tc.appendMethod(null, method, int0);
		return tc.getDefaultTestCase();
	}

	private DefaultTestCase buildNewInstanceNoReflectionTestCase() throws NoSuchMethodException, SecurityException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(10);
		Method method = ConcolicReflection.class.getMethod("newInstanceNoReflection", int.class);
		tc.appendMethod(null, method, int0);
		return tc.getDefaultTestCase();
	}

	private DefaultTestCase buildConstructorNewInstanceTestCase() throws NoSuchMethodException, SecurityException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(10);
		Method method = ConcolicReflection.class.getMethod("constructorNewInstance", int.class);
		tc.appendMethod(null, method, int0);
		return tc.getDefaultTestCase();
	}

	private DefaultTestCase buildNotInstrumentedConstructorNewInstanceTestCase()
			throws NoSuchMethodException, SecurityException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(10);
		Method method = ConcolicReflection.class.getMethod("objConstructorNewInstance", int.class);
		tc.appendMethod(null, method, int0);
		return tc.getDefaultTestCase();
	}

	private DefaultTestCase buildMethodInvokeTestCase() throws NoSuchMethodException, SecurityException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(10);
		Method method = ConcolicReflection.class.getMethod("methodInvoke", int.class);
		tc.appendMethod(null, method, int0);
		return tc.getDefaultTestCase();
	}

	private DefaultTestCase buildNotInstrumentedClassNewInstanceTestCase()
			throws NoSuchMethodException, SecurityException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(10);
		Method method = ConcolicReflection.class.getMethod("objClassNewInstance", int.class);
		tc.appendMethod(null, method, int0);
		return tc.getDefaultTestCase();
	}

	private static final int DEFAULT_CONCOLIC_TIMEOUT = Properties.CONCOLIC_TIMEOUT;

	@After
	public void restoreSettings() {
		Properties.CONCOLIC_TIMEOUT = DEFAULT_CONCOLIC_TIMEOUT;
	}

	@Before
	public void setMaxConcolicTime() {
		Properties.CONCOLIC_TIMEOUT = Integer.MAX_VALUE;
	}

	@Test
	public void testClassNewInstanceNoReflection() throws NoSuchMethodException, SecurityException {
		DefaultTestCase tc = buildNewInstanceNoReflectionTestCase();
		List<BranchCondition> branch_conditions = ConcolicExecution.executeConcolic(tc);
		assertTrue(!branch_conditions.isEmpty());
	}

	@Test
	public void testClassNewInstance() throws NoSuchMethodException, SecurityException {
		DefaultTestCase tc = buildClassNewInstanceTestCase();
		List<BranchCondition> branch_conditions = ConcolicExecution.executeConcolic(tc);
		assertTrue(!branch_conditions.isEmpty());
	}

	@Test
	public void testConstructorNewInstance() throws NoSuchMethodException, SecurityException {
		DefaultTestCase tc = buildConstructorNewInstanceTestCase();
		List<BranchCondition> branch_conditions = ConcolicExecution.executeConcolic(tc);
		assertTrue(!branch_conditions.isEmpty());
	}

	@Test
	public void testMethodInvoke() throws NoSuchMethodException, SecurityException {
		DefaultTestCase tc = buildMethodInvokeTestCase();
		List<BranchCondition> branch_conditions = ConcolicExecution.executeConcolic(tc);
		assertTrue(!branch_conditions.isEmpty());
	}

	@Test
	public void testNotInstrumentedConstructorNewInstance() throws NoSuchMethodException, SecurityException {
		DefaultTestCase tc = buildNotInstrumentedConstructorNewInstanceTestCase();
		List<BranchCondition> branch_conditions = ConcolicExecution.executeConcolic(tc);
		assertTrue(!branch_conditions.isEmpty());
	}

	@Test
	public void testNotInstrumentedClassNewInstance() throws NoSuchMethodException, SecurityException {
		DefaultTestCase tc = buildNotInstrumentedClassNewInstanceTestCase();
		List<BranchCondition> branch_conditions = ConcolicExecution.executeConcolic(tc);
		assertTrue(!branch_conditions.isEmpty());
	}
}
