package org.evosuite.symbolic;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.VariableReference;
import org.junit.Test;

import static org.evosuite.symbolic.SymbolicObserverTest.printConstraints;

public class ConcolicExecutionTest {

	@Test
	public void testCase0() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase0();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	@Test
	public void testCase1() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase1();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	private List<BranchCondition> executeTest(DefaultTestCase tc) {
		Properties.CLIENT_ON_THREAD = true;
		Properties.PRINT_TO_SYSTEM = true;
		Properties.TIMEOUT = 5000000;

		System.out.println("TestCase=");
		System.out.println(tc.toCode());

		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic(tc);

		printConstraints(branch_conditions);
		return branch_conditions;
	}

	private DefaultTestCase buildTestCase0() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		Method method = TestCase0.class.getMethod("test");
		tc.appendMethod(null, method);
		return tc.getDefaultTestCase();
	}

	/*
	 * int int0 = ConcolicMarker.mark(179,"int0");
	 * 
	 * int int1 = ConcolicMarker.mark(-374,"int1");
	 */
	private DefaultTestCase buildTestCase1() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(179);
		VariableReference int1 = tc.appendIntPrimitive(179);
		Method method = TestCase1.class.getMethod("test", int.class, int.class);
		tc.appendMethod(null, method, int0, int1);
		return tc.getDefaultTestCase();
	}

	//
	// double double0 = ConcolicMarker.mark(-2020.5367255717083,"var1");
	// double double1 = ConcolicMarker.mark(698.931685369782,"var2");
	// double double3 = ConcolicMarker.mark(1.8078644807328579,"var3");
	// double double4 = ConcolicMarker.mark(1756.567093813958,"var4");
	//
	private DefaultTestCase buildTestCase10() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc
				.appendDoublePrimitive(-2020.5367255717083);
		VariableReference double1 = tc.appendDoublePrimitive(698.931685369782);
		VariableReference double3 = tc
				.appendDoublePrimitive(1.8078644807328579);
		VariableReference double4 = tc.appendDoublePrimitive(1756.567093813958);
		Method method = TestCase10.class.getMethod("test", double.class,
				double.class, double.class, double.class);
		tc.appendMethod(null, method, double0, double1, double3, double4);
		return tc.getDefaultTestCase();
	}

	// int int0 = ConcolicMarker.mark(1, "var1");
	// int int1 = ConcolicMarker.mark(4, "var2");
	// int int3 = ConcolicMarker.mark(16, "var3");
	// int int5 = ConcolicMarker.mark(0, "var4");
	private DefaultTestCase buildTestCase11() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(1);
		VariableReference int1 = tc.appendIntPrimitive(4);
		VariableReference int3 = tc.appendIntPrimitive(16);
		VariableReference int5 = tc.appendIntPrimitive(0);
		Method method = TestCase11.class.getMethod("test", int.class,
				int.class, int.class, int.class);
		tc.appendMethod(null, method, int0, int1, int3, int5);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase10() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase10();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	@Test
	public void testCase11() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase11();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(3, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase12() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference long0 = tc.appendLongPrimitive(1);
		VariableReference long1 = tc.appendLongPrimitive(4);
		VariableReference long3 = tc.appendLongPrimitive(16);
		VariableReference long5 = tc.appendLongPrimitive(0);
		Method method = TestCase12.class.getMethod("test", long.class,
				long.class, long.class, long.class);
		tc.appendMethod(null, method, long0, long1, long3, long5);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase12() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase12();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(3, branch_conditions.size());
	}

	// double double0 = ConcolicMarker.mark(DOUBLE_VALUE, "double0");
	private DefaultTestCase buildTestCase13() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc
				.appendDoublePrimitive(TestCase13.DOUBLE_VALUE);
		Method method = TestCase13.class.getMethod("test", double.class);
		tc.appendMethod(null, method, double0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase13() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase13();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	// double double1 = ConcolicMarker.mark(DOUBLE_VALUE, "double1");
	private DefaultTestCase buildTestCase14() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double1 = tc
				.appendDoublePrimitive(TestCase14.DOUBLE_VALUE);
		Method method = TestCase14.class.getMethod("test", double.class);
		tc.appendMethod(null, method, double1);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase14() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase14();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(22, branch_conditions.size());
	}

	// double double0 = ConcolicMarker.mark(DOUBLE_CONSTANT, "double0");
	private DefaultTestCase buildTestCase15() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double1 = tc
				.appendDoublePrimitive(TestCase15.DOUBLE_CONSTANT);
		Method method = TestCase15.class.getMethod("test", double.class);
		tc.appendMethod(null, method, double1);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase15() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase15();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(4, branch_conditions.size());
	}

	// int int0 = ConcolicMarker.mark(-99100191, "int0");
	// int int1 = ConcolicMarker.mark(99100191, "int1");
	// long long0 = ConcolicMarker.mark(-991001911414177541L, "long0");
	// long long1 = ConcolicMarker.mark(991001911414177541L, "long1");
	// float float0 = ConcolicMarker.mark(-0.0099100191F, "float0");
	// float float1 = ConcolicMarker.mark(+0.0099100191F, "float1");
	// double double0 = ConcolicMarker.mark(-0.0099100191F, "double0");
	// double double1 = ConcolicMarker.mark(+0.0099100191F, "double1");
	private DefaultTestCase buildTestCase16() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(-99100191);
		VariableReference int1 = tc.appendIntPrimitive(99100191);

		VariableReference long0 = tc.appendLongPrimitive(-991001911414177541L);
		VariableReference long1 = tc.appendLongPrimitive(991001911414177541L);

		VariableReference float0 = tc.appendFloatPrimitive(-0.0099100191F);
		VariableReference float1 = tc.appendFloatPrimitive(+0.0099100191F);

		VariableReference double0 = tc.appendDoublePrimitive(-0.0099100191F);
		VariableReference double1 = tc.appendDoublePrimitive(+0.0099100191F);

		Method method = TestCase16.class.getMethod("test", int.class,
				int.class, long.class, long.class, float.class, float.class,
				double.class, double.class);
		tc.appendMethod(null, method, int0, int1, long0, long1, float0, float1,
				double0, double1);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase16() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase16();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(4, branch_conditions.size());
	}

	// float float0 = ConcolicMarker.mark(FLOAT_VALUE, "float0");
	// double double0 = ConcolicMarker.mark(DOUBLE_VALUE, "double0");
	private DefaultTestCase buildTestCase17() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference float0 = tc
				.appendFloatPrimitive(TestCase17.FLOAT_VALUE);
		VariableReference double0 = tc
				.appendDoublePrimitive(TestCase17.DOUBLE_VALUE);

		Method method = TestCase17.class.getMethod("test", float.class,
				double.class);
		tc.appendMethod(null, method, float0, double0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase17() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase17();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(2, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase18() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference float0 = tc
				.appendFloatPrimitive(TestCase18.FLOAT_VALUE);
		VariableReference double0 = tc
				.appendDoublePrimitive(TestCase18.DOUBLE_VALUE);

		Method method = TestCase18.class.getMethod("test", float.class,
				double.class);
		tc.appendMethod(null, method, float0, double0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase18() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase18();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(2, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase19() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference float0 = tc
				.appendFloatPrimitive(TestCase19.FLOAT_VALUE);
		VariableReference double0 = tc
				.appendDoublePrimitive(TestCase19.DOUBLE_VALUE);

		Method method = TestCase19.class.getMethod("test", float.class,
				double.class);
		tc.appendMethod(null, method, float0, double0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase19() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase19();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(2, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase20() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference float0 = tc
				.appendFloatPrimitive(TestCase20.FLOAT_VALUE);
		VariableReference double0 = tc
				.appendDoublePrimitive(TestCase20.DOUBLE_VALUE);

		Method method = TestCase20.class.getMethod("test", float.class,
				double.class);
		tc.appendMethod(null, method, float0, double0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase20() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase20();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(2, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase21() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference float0 = tc
				.appendFloatPrimitive(TestCase21.FLOAT_VALUE);
		VariableReference double0 = tc
				.appendDoublePrimitive(TestCase21.DOUBLE_VALUE);

		Method method = TestCase21.class.getMethod("test", float.class,
				double.class);
		tc.appendMethod(null, method, float0, double0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase21() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase21();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(2, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase22() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference float0 = tc
				.appendFloatPrimitive(TestCase22.FLOAT_VALUE_1);
		VariableReference double0 = tc
				.appendDoublePrimitive(TestCase22.DOUBLE_VALUE_1);

		Method method = TestCase22.class.getMethod("test", float.class,
				double.class);
		tc.appendMethod(null, method, float0, double0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase22() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase22();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(2, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase23() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc
				.appendStringPrimitive(TestCase23.STRING_VALUE);

		Method method = TestCase23.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase23() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase23();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase24() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc
				.appendStringPrimitive(TestCase24.STRING_VALUE);

		Method method = TestCase24.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase24() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase24();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(3, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase25() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc
				.appendStringPrimitive(TestCase25.STRING_VALUE);

		Method method = TestCase25.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase25() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase25();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(8, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase26() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc
				.appendStringPrimitive(TestCase26.STRING_VALUE_PART_1);

		Method method = TestCase26.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase26() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase26();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase27() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc
				.appendStringPrimitive(TestCase27.STRING_VALUE);

		Method method = TestCase27.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase27() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase27();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(8, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase28() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc
				.appendStringPrimitive(TestCase28.STRING_VALUE);

		Method method = TestCase28.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase28() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase28();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(5, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase29() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference boolean0 = tc.appendBooleanPrimitive(true);
		VariableReference boolean1 = tc.appendBooleanPrimitive(false);
		Method method = TestCase29.class.getMethod("test", boolean.class,
				boolean.class);
		tc.appendMethod(null, method, boolean0, boolean1);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase29() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase29();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(2, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase30() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc
				.appendStringPrimitive(TestCase30.STRING_VALUE);

		Method method = TestCase30.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase30() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase30();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase31() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc
				.appendStringPrimitive(TestCase31.STRING_VALUE);

		Method method = TestCase31.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase31() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase31();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(2, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase32() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc
				.appendStringPrimitive(TestCase32.STRING_VALUE);

		Method method = TestCase32.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase32() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase32();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(2, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase33() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("foo");

		Method method = TestCase33.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase33() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase33();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	// String string0 = ConcolicMarker.mark("Togliere sta roba", "string0");
	private DefaultTestCase buildTestCase34() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc
				.appendStringPrimitive("Togliere sta roba");

		Method method = TestCase34.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase34() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase34();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}
}
