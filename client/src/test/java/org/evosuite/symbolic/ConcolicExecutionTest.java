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
package org.evosuite.symbolic;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.variable.VariableReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.concolic.TestCase0;
import com.examples.with.different.packagename.concolic.TestCase1;
import com.examples.with.different.packagename.concolic.TestCase10;
import com.examples.with.different.packagename.concolic.TestCase100;
import com.examples.with.different.packagename.concolic.TestCase101;
import com.examples.with.different.packagename.concolic.TestCase11;
import com.examples.with.different.packagename.concolic.TestCase12;
import com.examples.with.different.packagename.concolic.TestCase13;
import com.examples.with.different.packagename.concolic.TestCase14;
import com.examples.with.different.packagename.concolic.TestCase15;
import com.examples.with.different.packagename.concolic.TestCase16;
import com.examples.with.different.packagename.concolic.TestCase17;
import com.examples.with.different.packagename.concolic.TestCase18;
import com.examples.with.different.packagename.concolic.TestCase19;
import com.examples.with.different.packagename.concolic.TestCase2;
import com.examples.with.different.packagename.concolic.TestCase20;
import com.examples.with.different.packagename.concolic.TestCase21;
import com.examples.with.different.packagename.concolic.TestCase22;
import com.examples.with.different.packagename.concolic.TestCase23;
import com.examples.with.different.packagename.concolic.TestCase24;
import com.examples.with.different.packagename.concolic.TestCase25;
import com.examples.with.different.packagename.concolic.TestCase26;
import com.examples.with.different.packagename.concolic.TestCase27;
import com.examples.with.different.packagename.concolic.TestCase28;
import com.examples.with.different.packagename.concolic.TestCase29;
import com.examples.with.different.packagename.concolic.TestCase3;
import com.examples.with.different.packagename.concolic.TestCase30;
import com.examples.with.different.packagename.concolic.TestCase31;
import com.examples.with.different.packagename.concolic.TestCase32;
import com.examples.with.different.packagename.concolic.TestCase33;
import com.examples.with.different.packagename.concolic.TestCase34;
import com.examples.with.different.packagename.concolic.TestCase35;
import com.examples.with.different.packagename.concolic.TestCase36;
import com.examples.with.different.packagename.concolic.TestCase37;
import com.examples.with.different.packagename.concolic.TestCase38;
import com.examples.with.different.packagename.concolic.TestCase39;
import com.examples.with.different.packagename.concolic.TestCase4;
import com.examples.with.different.packagename.concolic.TestCase40;
import com.examples.with.different.packagename.concolic.TestCase41;
import com.examples.with.different.packagename.concolic.TestCase42;
import com.examples.with.different.packagename.concolic.TestCase43;
import com.examples.with.different.packagename.concolic.TestCase44;
import com.examples.with.different.packagename.concolic.TestCase45;
import com.examples.with.different.packagename.concolic.TestCase46;
import com.examples.with.different.packagename.concolic.TestCase47;
import com.examples.with.different.packagename.concolic.TestCase48;
import com.examples.with.different.packagename.concolic.TestCase49;
import com.examples.with.different.packagename.concolic.TestCase5;
import com.examples.with.different.packagename.concolic.TestCase50;
import com.examples.with.different.packagename.concolic.TestCase51;
import com.examples.with.different.packagename.concolic.TestCase52;
import com.examples.with.different.packagename.concolic.TestCase54;
import com.examples.with.different.packagename.concolic.TestCase56;
import com.examples.with.different.packagename.concolic.TestCase57;
import com.examples.with.different.packagename.concolic.TestCase58;
import com.examples.with.different.packagename.concolic.TestCase59;
import com.examples.with.different.packagename.concolic.TestCase6;
import com.examples.with.different.packagename.concolic.TestCase60;
import com.examples.with.different.packagename.concolic.TestCase61;
import com.examples.with.different.packagename.concolic.TestCase62;
import com.examples.with.different.packagename.concolic.TestCase63;
import com.examples.with.different.packagename.concolic.TestCase64;
import com.examples.with.different.packagename.concolic.TestCase65;
import com.examples.with.different.packagename.concolic.TestCase66;
import com.examples.with.different.packagename.concolic.TestCase67;
import com.examples.with.different.packagename.concolic.TestCase68;
import com.examples.with.different.packagename.concolic.TestCase69;
import com.examples.with.different.packagename.concolic.TestCase7;
import com.examples.with.different.packagename.concolic.TestCase71;
import com.examples.with.different.packagename.concolic.TestCase72;
import com.examples.with.different.packagename.concolic.TestCase73;
import com.examples.with.different.packagename.concolic.TestCase74;
import com.examples.with.different.packagename.concolic.TestCase75;
import com.examples.with.different.packagename.concolic.TestCase76;
import com.examples.with.different.packagename.concolic.TestCase77;
import com.examples.with.different.packagename.concolic.TestCase78;
import com.examples.with.different.packagename.concolic.TestCase79;
import com.examples.with.different.packagename.concolic.TestCase8;
import com.examples.with.different.packagename.concolic.TestCase80;
import com.examples.with.different.packagename.concolic.TestCase81;
import com.examples.with.different.packagename.concolic.TestCase82;
import com.examples.with.different.packagename.concolic.TestCase83;
import com.examples.with.different.packagename.concolic.TestCase84;
import com.examples.with.different.packagename.concolic.TestCase85;
import com.examples.with.different.packagename.concolic.TestCase87;
import com.examples.with.different.packagename.concolic.TestCase88;
import com.examples.with.different.packagename.concolic.TestCase89;
import com.examples.with.different.packagename.concolic.TestCase9;
import com.examples.with.different.packagename.concolic.TestCase90;
import com.examples.with.different.packagename.concolic.TestCase91;
import com.examples.with.different.packagename.concolic.TestCase92;
import com.examples.with.different.packagename.concolic.TestCase94;
import com.examples.with.different.packagename.concolic.TestCase95;
import com.examples.with.different.packagename.concolic.TestCase96;
import com.examples.with.different.packagename.concolic.TestCase97;
import com.examples.with.different.packagename.concolic.TestCase98;
import com.examples.with.different.packagename.concolic.TestCase99;

public class ConcolicExecutionTest {

	@After
	public void resetStaticVariables() {
		TestGenerationContext.getInstance().resetContext();
	}

	@Before
	public void initializeExecutor() {
		TestCaseExecutor.getInstance().newObservers();
		TestCaseExecutor.initExecutor();
	}

	@Test
	public void testCase0() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase0();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(0, branch_conditions.size());
	}

	@Test
	public void testCase1() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase1();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	private List<BranchCondition> executeTest(DefaultTestCase tc) {
		Properties.CLIENT_ON_THREAD = true;
		Properties.PRINT_TO_SYSTEM = false;
		Properties.TIMEOUT = 5000;
		Properties.CONCOLIC_TIMEOUT = 5000000;

		List<BranchCondition> branch_conditions = ConcolicExecution
				.executeConcolic(tc);

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
		assertEquals(0, branch_conditions.size());
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
		assertEquals(0, branch_conditions.size());
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

	private DefaultTestCase buildTestCase35() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc
				.appendStringPrimitive(TestCase35.STRING_VALUE);

		Method method = TestCase35.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase35() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase35();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(3, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase36() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc
				.appendStringPrimitive(TestCase36.STRING_VALUE);

		Method method = TestCase36.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase36() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase36();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(0, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase37() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(Integer.MAX_VALUE);

		Method method = TestCase37.class.getMethod("test", int.class);
		tc.appendMethod(null, method, int0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase37() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase37();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase3() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(-1);

		Method method = TestCase3.class.getMethod("test", int.class);
		tc.appendMethod(null, method, int0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase3() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase3();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(2, branch_conditions.size());
	}

	// int int0 = ConcolicMarker.mark(15, "int0");
	// boolean boolean0 = ConcolicMarker.mark(true, "boolean0");
	// short short0 = ConcolicMarker.mark(Short.MAX_VALUE, "short0");
	// byte byte0 = ConcolicMarker.mark(Byte.MAX_VALUE, "byte0");
	// char char0 = ConcolicMarker.mark(Character.MAX_VALUE, "char0");
	// long long0 = ConcolicMarker.mark(Long.MAX_VALUE, "long0");
	// float float0 = ConcolicMarker.mark(Float.MAX_VALUE, "float0");
	// double double0 = ConcolicMarker.mark(Double.MAX_VALUE, "double0");
	private DefaultTestCase buildTestCase38() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(15);
		VariableReference boolean0 = tc.appendBooleanPrimitive(true);
		VariableReference short0 = tc.appendShortPrimitive(Short.MAX_VALUE);
		VariableReference byte0 = tc.appendBytePrimitive(Byte.MAX_VALUE);
		VariableReference char0 = tc.appendCharPrimitive(Character.MAX_VALUE);
		VariableReference long0 = tc.appendLongPrimitive(Long.MAX_VALUE);
		VariableReference float0 = tc.appendFloatPrimitive(Float.MAX_VALUE);
		VariableReference double0 = tc.appendDoublePrimitive(Float.MAX_VALUE);

		Method method = TestCase38.class.getMethod("test", int.class,
				boolean.class, short.class, byte.class, char.class, long.class,
				float.class, double.class);
		tc.appendMethod(null, method, int0, boolean0, short0, byte0, char0,
				long0, float0, double0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase38() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase38();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(7, branch_conditions.size());
	}

	// final int ARRAY_SIZE = 20;
	// int int0 = ConcolicMarker.mark(ARRAY_SIZE, "int0");
	// String string2 = ConcolicMarker.mark(ROBA, "string2");
	private DefaultTestCase buildTestCase39() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(20);
		VariableReference string2 = tc.appendStringPrimitive(TestCase39.ROBA);

		Method method = TestCase39.class.getMethod("test", int.class,
				String.class);
		tc.appendMethod(null, method, int0, string2);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase39() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase39();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(2, branch_conditions.size());
	}

	// int int0 = ConcolicMarker.mark(10, "int0");
	// int int1 = ConcolicMarker.mark(1, "int1");
	// float float0 = ConcolicMarker.mark(Float.POSITIVE_INFINITY, "float0");
	private DefaultTestCase buildTestCase40() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(10);
		VariableReference int1 = tc.appendIntPrimitive(1);
		VariableReference float0 = tc
				.appendFloatPrimitive(Float.POSITIVE_INFINITY);

		Method method = TestCase40.class.getMethod("test", int.class,
				int.class, float.class);
		tc.appendMethod(null, method, int0, int1, float0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase40() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase40();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	// int int0 = ConcolicMarker.mark(0,"var0");
	// int int1 = ConcolicMarker.mark(0,"var1");
	// int int3 = ConcolicMarker.mark(1,"var2");
	// int int4 = ConcolicMarker.mark(1,"var3");
	private DefaultTestCase buildTestCase41() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(0);
		VariableReference int1 = tc.appendIntPrimitive(0);
		VariableReference int3 = tc.appendIntPrimitive(1);
		VariableReference int4 = tc.appendIntPrimitive(1);

		Method method = TestCase41.class.getMethod("test", int.class,
				int.class, int.class, int.class);
		tc.appendMethod(null, method, int0, int1, int3, int4);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase41() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase41();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(8, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase42() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();

		Method method = TestCase42.class.getMethod("test");
		tc.appendMethod(null, method);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase42() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase42();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(0, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase43() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(Integer.MAX_VALUE);

		Method method = TestCase43.class.getMethod("test", int.class);
		tc.appendMethod(null, method, int0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase43() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase43();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	// int int0 = ConcolicMarker.mark(1111, "var0");
	// int int1 = ConcolicMarker.mark(1111, "var1");
	// int int2 = ConcolicMarker.mark(1111, "var2");
	// int int3 = ConcolicMarker.mark(1111, "var3");
	// int int4 = ConcolicMarker.mark(-285, "var4");
	// int int5 = ConcolicMarker.mark(-285, "var5");
	// int int6 = ConcolicMarker.mark(6, "var6");
	// int int7 = ConcolicMarker.mark(302, "var7");
	// int int8 = ConcolicMarker.mark(1565, "var8");
	// int int9 = ConcolicMarker.mark(1893, "var9");
	// int int10 = ConcolicMarker.mark(-1956, "var10");
	private DefaultTestCase buildTestCase44() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(1111);
		VariableReference int1 = tc.appendIntPrimitive(1111);
		VariableReference int2 = tc.appendIntPrimitive(1111);
		VariableReference int3 = tc.appendIntPrimitive(1111);
		VariableReference int4 = tc.appendIntPrimitive(-285);
		VariableReference int5 = tc.appendIntPrimitive(-285);
		VariableReference int6 = tc.appendIntPrimitive(6);
		VariableReference int7 = tc.appendIntPrimitive(302);
		VariableReference int8 = tc.appendIntPrimitive(1565);
		VariableReference int9 = tc.appendIntPrimitive(1893);
		VariableReference int10 = tc.appendIntPrimitive(-1956);

		Method method = TestCase44.class.getMethod("test", int.class,
				int.class, int.class, int.class, int.class, int.class,
				int.class, int.class, int.class, int.class, int.class);
		tc.appendMethod(null, method, int0, int1, int2, int3, int4, int5, int6,
				int7, int8, int9, int10);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase44() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase44();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(10, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase45() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();

		Method method = TestCase45.class.getMethod("test");
		tc.appendMethod(null, method);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase45() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase45();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(0, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase46() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc
				.appendStringPrimitive("\\5b;\u001C,?\u0011\u0010\u001E]\"");
		VariableReference string1 = tc
				.appendStringPrimitive("\\5b;\u001C,?\u0011\u0010\u001E]\"");
		VariableReference string2 = tc
				.appendStringPrimitive("\\5b;\u001C,?\u0011\u0010\u001E]\"");
		VariableReference string3 = tc
				.appendStringPrimitive("\\5b;\u001C,?\u0011\u0010\u001E]\"");
		VariableReference string4 = tc
				.appendStringPrimitive("\\5b;\u001C,?\u0011\u0010\u001E]\"");
		VariableReference string5 = tc
				.appendStringPrimitive("\\5b;\u001C,?\u0011\u0010\u001E]\"");
		VariableReference string6 = tc
				.appendStringPrimitive("\\5b;\u001C,?\u0011\u0010\u001E]\"");
		VariableReference string7 = tc
				.appendStringPrimitive("\\5b;\u001C,?\u0011\u0010\u001E]\"");
		VariableReference string8 = tc.appendStringPrimitive("ness");
		VariableReference string9 = tc.appendStringPrimitive("iciti");
		VariableReference string10 = tc.appendStringPrimitive("iciti");
		VariableReference string11 = tc.appendStringPrimitive("doc");
		VariableReference string12 = tc.appendStringPrimitive("text");
		VariableReference string13 = tc.appendStringPrimitive("text");
		VariableReference string14 = tc.appendStringPrimitive("text");
		VariableReference string15 = tc.appendStringPrimitive("text");
		VariableReference string16 = tc.appendStringPrimitive("text");
		VariableReference string17 = tc.appendStringPrimitive("text");
		VariableReference string18 = tc.appendStringPrimitive("");
		VariableReference string19 = tc.appendStringPrimitive("");
		VariableReference string20 = tc
				.appendStringPrimitive("urVf3T\r\t\u0019\u000B0 eiM I");
		VariableReference string21 = tc
				.appendStringPrimitive("urVf3T\r\t\u0019\u000B0 eiM I");
		VariableReference string22 = tc
				.appendStringPrimitive("urVf3T\r\t\u0019\u000B0 eiM I");
		VariableReference string23 = tc.appendStringPrimitive("ical");
		Method method = TestCase46.class.getMethod("test", String.class,
				String.class, String.class, String.class, String.class,
				String.class, String.class, String.class, String.class,
				String.class, String.class, String.class, String.class,
				String.class, String.class, String.class, String.class,
				String.class, String.class, String.class, String.class,
				String.class, String.class, String.class);

		tc.appendMethod(null, method, string0, string1, string2, string3,
				string4, string5, string6, string7, string8, string9, string10,
				string11, string12, string13, string14, string15, string16,
				string17, string18, string19, string20, string21, string22,
				string23);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase46() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase46();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(10, branch_conditions.size());
	}

	// char char0 = ConcolicMarker.mark('\u0007', "char0");
	// char char1 = ConcolicMarker.mark('\t', "char1");
	// char char2 = ConcolicMarker.mark('\u0015', "char2");
	// char char3 = ConcolicMarker.mark('\u0015', "char3");
	private DefaultTestCase buildTestCase47() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference char0 = tc.appendCharPrimitive('\u0007');
		VariableReference char1 = tc.appendCharPrimitive('\t');
		VariableReference char2 = tc.appendCharPrimitive('\u0015');
		VariableReference char3 = tc.appendCharPrimitive('\u0015');

		Method method = TestCase47.class.getMethod("test", char.class,
				char.class, char.class, char.class);
		tc.appendMethod(null, method, char0, char1, char2, char3);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase47() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase47();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(9, branch_conditions.size());
	}

	// char char0 = ConcolicMarker.mark('y',"char0");
	// char char1 = ConcolicMarker.mark('y',"char1");
	// char char2 = ConcolicMarker.mark('y',"char2");
	// char char3 = ConcolicMarker.mark('y',"char3");
	// char char4 = ConcolicMarker.mark('y',"char4");
	// char char5 = ConcolicMarker.mark('I',"char5");
	// char char6 = ConcolicMarker.mark('I',"char6");
	// char char7 = ConcolicMarker.mark('V',"char7");
	// char char8 = ConcolicMarker.mark('R',"char8");
	private DefaultTestCase buildTestCase48() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference char0 = tc.appendCharPrimitive('y');
		VariableReference char1 = tc.appendCharPrimitive('y');
		VariableReference char2 = tc.appendCharPrimitive('y');
		VariableReference char3 = tc.appendCharPrimitive('y');
		VariableReference char4 = tc.appendCharPrimitive('y');
		VariableReference char5 = tc.appendCharPrimitive('I');
		VariableReference char6 = tc.appendCharPrimitive('I');
		VariableReference char7 = tc.appendCharPrimitive('V');
		VariableReference char8 = tc.appendCharPrimitive('R');

		Method method = TestCase48.class.getMethod("test", char.class,
				char.class, char.class, char.class, char.class, char.class,
				char.class, char.class, char.class);
		tc.appendMethod(null, method, char0, char1, char2, char3, char4, char5,
				char6, char7, char8);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase48() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase48();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(0, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase49() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();

		Method method = TestCase49.class.getMethod("test");
		tc.appendMethod(null, method);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase49() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase49();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(0, branch_conditions.size());
	}

	// char char0 = ConcolicMarker.mark('U', "char0");
	// char char1 = ConcolicMarker.mark('U', "char1");
	// char char2 = ConcolicMarker.mark(')', "char2");
	// char char3 = ConcolicMarker.mark('j', "char3");
	// char char4 = ConcolicMarker.mark('s', "char4");
	// char char5 = ConcolicMarker.mark('\u001A', "char5");
	private DefaultTestCase buildTestCase50() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference char0 = tc.appendCharPrimitive('U');
		VariableReference char1 = tc.appendCharPrimitive('U');
		VariableReference char2 = tc.appendCharPrimitive(')');
		VariableReference char3 = tc.appendCharPrimitive('j');
		VariableReference char4 = tc.appendCharPrimitive('s');
		VariableReference char5 = tc.appendCharPrimitive('\u001A');

		Method method = TestCase50.class.getMethod("test", char.class,
				char.class, char.class, char.class, char.class, char.class);
		tc.appendMethod(null, method, char0, char1, char2, char3, char4, char5);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase50() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase50();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(15, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase51() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();

		Method method = TestCase51.class.getMethod("test");
		tc.appendMethod(null, method);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase51() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase51();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(0, branch_conditions.size());
	}

	// String string0 = ConcolicMarker.mark("Togliere sta roba", "string0");
	// String string1 = ConcolicMarker.mark(" ", "string1");
	private DefaultTestCase buildTestCase52() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc
				.appendStringPrimitive("Togliere sta roba");
		VariableReference string1 = tc.appendStringPrimitive(" ");

		Method method = TestCase52.class.getMethod("test", String.class,
				String.class);
		tc.appendMethod(null, method, string0, string1);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase52() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase52();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(0, branch_conditions.size());
	}

	// int int0 = ConcolicMarker.mark(-756,"var1");
	// int int1 = ConcolicMarker.mark(-542,"var2");
	// int int3 = ConcolicMarker.mark(1,"var3");
	// int int8 = ConcolicMarker.mark(-1480,"var4");
	// int int11 = ConcolicMarker.mark(-1637,"var5");
	private DefaultTestCase buildTestCase2() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(-756);
		VariableReference int1 = tc.appendIntPrimitive(-542);
		VariableReference int3 = tc.appendIntPrimitive(1);
		VariableReference int8 = tc.appendIntPrimitive(-1480);
		VariableReference int11 = tc.appendIntPrimitive(-1637);

		Method method = TestCase2.class.getMethod("test", int.class, int.class,
				int.class, int.class, int.class);
		tc.appendMethod(null, method, int0, int1, int3, int8, int11);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase2() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase2();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(4, branch_conditions.size());
	}

	// long long0 = ConcolicMarker.mark(1554151784714561687L,"var1");
	// int int0 = ConcolicMarker.mark(0, "var2");
	private DefaultTestCase buildTestCase4() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference long0 = tc.appendLongPrimitive(1554151784714561687L);
		VariableReference int0 = tc.appendIntPrimitive(0);

		Method method = TestCase4.class
				.getMethod("test", long.class, int.class);
		tc.appendMethod(null, method, long0, int0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase4() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase4();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(2, branch_conditions.size());
	}

	// float float0 = ConcolicMarker.mark(882.70544F,"var1");
	// float float1 = ConcolicMarker.mark(882.70544F,"var2");
	// float float2 = ConcolicMarker.mark(882.70544F, "var3");
	// float float3 = ConcolicMarker.mark(1.0F,"var4");
	// float float4 = ConcolicMarker.mark(63.534046F,"var5");
	private DefaultTestCase buildTestCase5() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference float0 = tc.appendFloatPrimitive(882.70544F);
		VariableReference float1 = tc.appendFloatPrimitive(882.70544F);
		VariableReference float2 = tc.appendFloatPrimitive(882.70544F);
		VariableReference float3 = tc.appendFloatPrimitive(1.0F);
		VariableReference float4 = tc.appendFloatPrimitive(63.534046F);

		Method method = TestCase5.class.getMethod("test", float.class,
				float.class, float.class, float.class, float.class);
		tc.appendMethod(null, method, float0, float1, float2, float3, float4);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase5() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase5();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	// int int0 = ConcolicMarker.mark(1515, "int0");
	// int int2 = ConcolicMarker.mark(1541, "int2");
	private DefaultTestCase buildTestCase56() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(1515);
		VariableReference int2 = tc.appendIntPrimitive(1541);

		Method method = TestCase56.class
				.getMethod("test", int.class, int.class);
		tc.appendMethod(null, method, int0, int2);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase56() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase56();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(2, branch_conditions.size());
	}

	// String string1 = ConcolicMarker.mark("Togliere sta roba".toUpperCase(),
	// "string1");
	// String string3 = ConcolicMarker.mark("Togliere", "string3");
	private DefaultTestCase buildTestCase57() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string1 = tc
				.appendStringPrimitive("Togliere sta roba".toUpperCase());
		VariableReference string3 = tc.appendStringPrimitive("Togliere");

		Method method = TestCase57.class.getMethod("test", String.class,
				String.class);
		tc.appendMethod(null, method, string1, string3);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase57() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase57();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(2, branch_conditions.size());
	}

	// String string1 = ConcolicMarker.mark("Togliere sta roba".toUpperCase(),
	// "string1");
	private DefaultTestCase buildTestCase58() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string1 = tc
				.appendStringPrimitive("Togliere sta roba".toUpperCase());

		Method method = TestCase58.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string1);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase58() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase58();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(2, branch_conditions.size());
	}

	// String string1 = ConcolicMarker.mark(string0, "string1");
	// int int1 = ConcolicMarker.mark(5, "int1");
	private DefaultTestCase buildTestCase59() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string1 = tc
				.appendStringPrimitive("Togliere sta roba");
		VariableReference int1 = tc.appendIntPrimitive(5);

		Method method = TestCase59.class.getMethod("test", String.class,
				int.class);
		tc.appendMethod(null, method, string1, int1);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase59() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase59();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(2, branch_conditions.size());
	}

	// float float0 = ConcolicMarker.mark(1442.5817F,"var1");
	// float float1 = ConcolicMarker.mark(1.0F,"var2");
	private DefaultTestCase buildTestCase6() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference float0 = tc.appendFloatPrimitive(1442.5817F);
		VariableReference float1 = tc.appendFloatPrimitive(1.0F);

		Method method = TestCase6.class.getMethod("test", float.class,
				float.class);
		tc.appendMethod(null, method, float0, float1);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase6() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase6();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	// String string1 = ConcolicMarker.mark(string0, "string1");
	private DefaultTestCase buildTestCase60() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string1 = tc
				.appendStringPrimitive("Togliere sta roba");

		Method method = TestCase60.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string1);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase60() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase60();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase61() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string1 = tc
				.appendStringPrimitive("Togliere sta roba");

		Method method = TestCase61.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string1);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase61() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase61();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(4, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase62() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string1 = tc
				.appendStringPrimitive("Togliere sta roba");

		Method method = TestCase62.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string1);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase62() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase62();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(4, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase63() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string1 = tc
				.appendStringPrimitive("Togliere sta roba");

		Method method = TestCase63.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string1);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase63() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase63();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(5, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase64() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string1 = tc
				.appendStringPrimitive("Togliere sta roba");

		Method method = TestCase64.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string1);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase64() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase64();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase65() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string1 = tc
				.appendStringPrimitive("Togliere sta roba");

		Method method = TestCase65.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string1);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase65() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase65();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(4, branch_conditions.size());
	}

	// String string1 = ConcolicMarker.mark(string0, "string0");
	private DefaultTestCase buildTestCase66() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string1 = tc
				.appendStringPrimitive("Togliere sta roba");

		Method method = TestCase66.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string1);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase66() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase66();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	// String string1 = ConcolicMarker.mark(string0, "string0");
	private DefaultTestCase buildTestCase67() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string1 = tc
				.appendStringPrimitive("Togliere sta roba");

		Method method = TestCase67.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string1);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase67() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase67();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase68() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string1 = tc
				.appendStringPrimitive("Togliere sta roba");

		Method method = TestCase68.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string1);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase68() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase68();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(3, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase69() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();

		Method method = TestCase69.class.getMethod("test");
		tc.appendMethod(null, method);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase69() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase69();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(0, branch_conditions.size());
	}

	// long long0 = ConcolicMarker.mark(0L,"var1");
	// long long1 = ConcolicMarker.mark(0L, "var2");
	// float float0 = ConcolicMarker.mark(1442.5817F,"var1");
	// float float1 = ConcolicMarker.mark(1.0F,"var2");
	private DefaultTestCase buildTestCase7() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference long0 = tc.appendLongPrimitive(0L);
		VariableReference long1 = tc.appendLongPrimitive(0L);

		Method method = TestCase7.class.getMethod("test", long.class,
				long.class);
		tc.appendMethod(null, method, long0, long1);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase7() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase7();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(0, branch_conditions.size());
	}

	// String string0 = ConcolicMarker.mark("enci", "string0");
	// String string9 = ConcolicMarker.mark("nov", "string9");
	private DefaultTestCase buildTestCase72() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("enci");
		VariableReference string9 = tc.appendStringPrimitive("nov");
		Method method = TestCase72.class.getMethod("test", String.class,
				String.class);
		tc.appendMethod(null, method, string0, string9);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase72() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase72();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(163, branch_conditions.size());
	}

	// String string0 = ConcolicMarker.mark("enci", "string0");
	// String string1 = ConcolicMarker.mark("c", "string1");
	private DefaultTestCase buildTestCase73() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("enci");
		VariableReference string1 = tc.appendStringPrimitive("c");
		Method method = TestCase73.class.getMethod("test", String.class,
				String.class);
		tc.appendMethod(null, method, string0, string1);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase73() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase73();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	// String string0 = ConcolicMarker.mark("Togliere ", "string0");
	// String string1 = ConcolicMarker.mark("sta ", "string1");
	// String string2 = ConcolicMarker.mark("roba ", "string2");
	private DefaultTestCase buildTestCase74() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("Togliere");
		VariableReference string1 = tc.appendStringPrimitive("sta");
		VariableReference string2 = tc.appendStringPrimitive("roba");
		Method method = TestCase74.class.getMethod("test", String.class,
				String.class, String.class);
		tc.appendMethod(null, method, string0, string1, string2);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase74() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase74();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(3, branch_conditions.size());
	}

	// String string0 = ConcolicMarker.mark("Togliere ", "string0");
	// String string1 = ConcolicMarker.mark("sta ", "string1");
	// String string2 = ConcolicMarker.mark("roba ", "string2");
	private DefaultTestCase buildTestCase75() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("Togliere");
		VariableReference string1 = tc.appendStringPrimitive("sta");
		VariableReference string2 = tc.appendStringPrimitive("roba");
		Method method = TestCase75.class.getMethod("test", String.class,
				String.class, String.class);
		tc.appendMethod(null, method, string0, string1, string2);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase75() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase75();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(2, branch_conditions.size());
	}

	// int int0 = ConcolicMarker.mark(10, "int0");
	// int int1 = ConcolicMarker.mark(0, "int1");
	// int int2 = ConcolicMarker.mark(-1, "int2");
	private DefaultTestCase buildTestCase76() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(10);
		VariableReference int1 = tc.appendIntPrimitive(0);
		VariableReference int2 = tc.appendIntPrimitive(-1);

		Method method = TestCase76.class.getMethod("test", int.class,
				int.class, int.class);
		tc.appendMethod(null, method, int0, int1, int2);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase76() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase76();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	// int int0 = ConcolicMarker.mark(10,"int0");
	// int int1 = ConcolicMarker.mark(20,"int1");
	// int int2 = ConcolicMarker.mark(30,"int2");
	private DefaultTestCase buildTestCase77() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(10);
		VariableReference int1 = tc.appendIntPrimitive(20);
		VariableReference int2 = tc.appendIntPrimitive(30);

		Method method = TestCase77.class.getMethod("test", int.class,
				int.class, int.class);
		tc.appendMethod(null, method, int0, int1, int2);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase77() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase77();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(0, branch_conditions.size());
	}

	// int int0 = ConcolicMarker.mark(10, "int0");
	private DefaultTestCase buildTestCase78() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(10);

		Method method = TestCase78.class.getMethod("test", int.class);
		tc.appendMethod(null, method, int0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase78() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase78();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	// long long0 = ConcolicMarker.mark(10L, "long0");
	private DefaultTestCase buildTestCase79() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference long0 = tc.appendLongPrimitive(10L);

		Method method = TestCase79.class.getMethod("test", long.class);
		tc.appendMethod(null, method, long0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase79() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase79();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	// int int0 = ConcolicMarker.mark(-1,"var1");
	private DefaultTestCase buildTestCase8() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(-1);

		Method method = TestCase8.class.getMethod("test", int.class);
		tc.appendMethod(null, method, int0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase8() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase8();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(0, branch_conditions.size());
	}

	// float float0 = ConcolicMarker.mark(1.5f, "float0");
	private DefaultTestCase buildTestCase80() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference float0 = tc.appendFloatPrimitive(1.5f);

		Method method = TestCase80.class.getMethod("test", float.class);
		tc.appendMethod(null, method, float0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase80() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase80();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	// double double0 = ConcolicMarker.mark(1.5, "double0");
	private DefaultTestCase buildTestCase81() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference double0 = tc.appendDoublePrimitive(1.5);

		Method method = TestCase81.class.getMethod("test", double.class);
		tc.appendMethod(null, method, double0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase81() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase81();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	// char char0 = ConcolicMarker.mark('a', "char0");
	// boolean boolean0 = ConcolicMarker.mark(true, "boolean0");
	// short short0 = ConcolicMarker.mark((short) 1, "short0");
	// byte byte0 = ConcolicMarker.mark((byte) 1, "byte0");
	private DefaultTestCase buildTestCase82() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference char0 = tc.appendCharPrimitive('a');
		VariableReference boolean0 = tc.appendBooleanPrimitive(true);
		VariableReference short0 = tc.appendShortPrimitive((short) 1);
		VariableReference byte0 = tc.appendBytePrimitive((byte) 1);

		Method method = TestCase82.class.getMethod("test", char.class,
				boolean.class, short.class, byte.class);
		tc.appendMethod(null, method, char0, boolean0, short0, byte0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase82() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase82();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(4, branch_conditions.size());
	}

	// String string0 = ConcolicMarker.mark("aaaaaab", "string0");
	// String string1 = ConcolicMarker.mark("bbbb", "string1");
	// int catchCount = ConcolicMarker.mark(0, "catchCount");
	private DefaultTestCase buildTestCase83() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("aaaaaab");
		VariableReference string1 = tc.appendStringPrimitive("bbbb");
		VariableReference catchCount = tc.appendIntPrimitive(0);

		Method method = TestCase83.class.getMethod("test", String.class,
				String.class, int.class);
		tc.appendMethod(null, method, string0, string1, catchCount);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase83() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase83();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(3, branch_conditions.size());
	}

	// String string0 = ConcolicMarker.mark("aaaaaaaaaaab", "string0");
	private DefaultTestCase buildTestCase84() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("aaaaaaaaaaab");

		Method method = TestCase84.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase84() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase84();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase85() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("aaaaaaaaaaab");

		Method method = TestCase85.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase85() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase85();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	// int int0 = ConcolicMarker.mark(0,"var1");
	private DefaultTestCase buildTestCase9() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(0);

		Method method = TestCase9.class.getMethod("test", int.class);
		tc.appendMethod(null, method, int0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase9() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase9();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(0, branch_conditions.size());
	}

	// char char0 = ConcolicMarker.mark('Q', "char0");
	// char char1 = ConcolicMarker.mark('\u0007', "char1");
	// char char2 = ConcolicMarker.mark('%', "char2");
	// char char3 = ConcolicMarker.mark('\n', "char3");
	// char char4 = ConcolicMarker.mark('>', "char4");
	// char char5 = ConcolicMarker.mark('7', "char5");
	// char char6 = ConcolicMarker.mark('\u000B', "char6");
	// char char7 = ConcolicMarker.mark('\u001B', "char7");
	// char char8 = ConcolicMarker.mark('l', "char8");
	private DefaultTestCase buildTestCase71() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference char0 = tc.appendCharPrimitive('Q');
		VariableReference char1 = tc.appendCharPrimitive('\u0007');
		VariableReference char2 = tc.appendCharPrimitive('%');
		VariableReference char3 = tc.appendCharPrimitive('\n');
		VariableReference char4 = tc.appendCharPrimitive('>');
		VariableReference char5 = tc.appendCharPrimitive('7');
		VariableReference char6 = tc.appendCharPrimitive('\u000B');
		VariableReference char7 = tc.appendCharPrimitive('\u001B');
		VariableReference char8 = tc.appendCharPrimitive('l');

		Method method = TestCase71.class.getMethod("test", char.class,
				char.class, char.class, char.class, char.class, char.class,
				char.class, char.class, char.class);
		tc.appendMethod(null, method, char0, char1, char2, char3, char4, char5,
				char6, char7, char8);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase71() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase71();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(39, branch_conditions.size());
	}

	// int int0 = ConcolicMarker.mark(-950,"int0");
	// int int1 = ConcolicMarker.mark(-950,"int1");
	// int int2 = ConcolicMarker.mark(-950,"int2");
	// char char0 = ConcolicMarker.mark('(',"char0");
	// char char1 = ConcolicMarker.mark('(',"char1");
	// char char2 = ConcolicMarker.mark('l',"char2");
	// char char3 = ConcolicMarker.mark('Q',"char3");
	// char char4 = ConcolicMarker.mark('\u001F',"char4");
	// char char5 = ConcolicMarker.mark('\u001D',"char5");
	// char char6 = ConcolicMarker.mark('X',"char6");
	// char char7 = ConcolicMarker.mark('r',"char7");
	// char char8 = ConcolicMarker.mark('\u0016',"char8");
	// char char9 = ConcolicMarker.mark('g',"char9");
	// char char10 = ConcolicMarker.mark('M',"char10");
	// char char11 = ConcolicMarker.mark('\b',"char11");
	private DefaultTestCase buildTestCase54() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(-950);
		VariableReference int1 = tc.appendIntPrimitive(-950);
		VariableReference int2 = tc.appendIntPrimitive(-950);

		VariableReference char0 = tc.appendCharPrimitive('(');
		VariableReference char1 = tc.appendCharPrimitive('(');
		VariableReference char2 = tc.appendCharPrimitive('l');
		VariableReference char3 = tc.appendCharPrimitive('Q');
		VariableReference char4 = tc.appendCharPrimitive('\u001F');
		VariableReference char5 = tc.appendCharPrimitive('\u001D');
		VariableReference char6 = tc.appendCharPrimitive('X');
		VariableReference char7 = tc.appendCharPrimitive('r');
		VariableReference char8 = tc.appendCharPrimitive('\u0016');
		VariableReference char9 = tc.appendCharPrimitive('g');
		VariableReference char10 = tc.appendCharPrimitive('M');
		VariableReference char11 = tc.appendCharPrimitive('\b');

		Method method = TestCase54.class.getMethod("test", int.class,
				int.class, int.class, char.class, char.class, char.class,
				char.class, char.class, char.class, char.class, char.class,
				char.class, char.class, char.class, char.class);
		tc.appendMethod(null, method, int0, int1, int2, char0, char1, char2,
				char3, char4, char5, char6, char7, char8, char9, char10, char11);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase54() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase54();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(21, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase87() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("abc_e");

		Method method = TestCase87.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	private DefaultTestCase buildTestCase88() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();

		VariableReference int0 = tc.appendIntPrimitive(10);
		VariableReference int1 = tc.appendIntPrimitive(20);

		Method method = TestCase88.class
				.getMethod("test", int.class, int.class);
		tc.appendMethod(null, method, int0, int1);
		return tc.getDefaultTestCase();
	}

	private DefaultTestCase buildTestCase89() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();

		VariableReference int0 = tc.appendIntPrimitive(200);
		Method method = TestCase89.class.getMethod("test", int.class);
		tc.appendMethod(null, method, int0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase87() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase87();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(3, branch_conditions.size());
	}

	@Test
	public void testCase88() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase88();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(0, branch_conditions.size());
	}

	@Test
	public void testCase89() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase89();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase90() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();

		VariableReference char0 = tc.appendCharPrimitive('a');
		Method method = TestCase90.class.getMethod("test", char.class);
		tc.appendMethod(null, method, char0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase90() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase90();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(3, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase91() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();

		VariableReference string0 = tc.appendStringPrimitive("135");
		VariableReference string1 = tc.appendStringPrimitive("20");
		VariableReference catchCount = tc.appendIntPrimitive(0);
		Method method = TestCase91.class.getMethod("test", String.class,
				String.class, int.class);
		tc.appendMethod(null, method, string0, string1, catchCount);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase91() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase91();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(6, branch_conditions.size());
	}

	private DefaultTestCase buildTestCase92() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();

		VariableReference string0 = tc
				.appendStringPrimitive("Togliere sta roba");
		VariableReference catchCount = tc.appendIntPrimitive(0);
		VariableReference boolean0 = tc.appendBooleanPrimitive(true);
		Method method = TestCase92.class.getMethod("test", String.class,
				int.class, boolean.class);
		tc.appendMethod(null, method, string0, catchCount, boolean0);
		return tc.getDefaultTestCase();
	}

	private DefaultTestCase buildTestCase93() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();

		VariableReference string0 = tc
				.appendStringPrimitive("Togliere sta roba");
		VariableReference catchCount = tc.appendIntPrimitive(0);
		VariableReference boolean0 = tc.appendBooleanPrimitive(true);
		Method method = TestCase92.class.getMethod("test", String.class,
				int.class, boolean.class);
		tc.appendMethod(null, method, string0, catchCount, boolean0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase92() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase92();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(11, branch_conditions.size());
	}

	@Test
	public void testCase93() throws SecurityException, NoSuchMethodException {
		final int old_length = Properties.DSE_CONSTRAINT_LENGTH;
		DefaultTestCase tc = buildTestCase93();

		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(11, branch_conditions.size());

		Properties.DSE_CONSTRAINT_LENGTH = 33;
		List<BranchCondition> branch_conditions1 = executeTest(tc);
		assertEquals(10, branch_conditions1.size());

		Properties.DSE_CONSTRAINT_LENGTH = 5;
		List<BranchCondition> branch_conditions2 = executeTest(tc);
		assertEquals(2, branch_conditions2.size());

		Properties.DSE_CONSTRAINT_LENGTH = old_length;
		List<BranchCondition> branch_conditions3 = executeTest(tc);
		assertEquals(11, branch_conditions3.size());

	}

	private DefaultTestCase buildTestCase94() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();

		VariableReference string0 = tc
				.appendStringPrimitive("Togliere sta roba");
		VariableReference string1 = tc
				.appendStringPrimitive("Togliere sta roba");
		Method method = TestCase94.class.getMethod("test", String.class,
				String.class);
		tc.appendMethod(null, method, string0, string1);

		return tc.getDefaultTestCase();
	}

	private DefaultTestCase buildTestCase95() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();

		VariableReference string0 = tc
				.appendStringPrimitive("Togliere_sta_roba");
		VariableReference string1 = tc.appendStringPrimitive("_");
		Method method = TestCase95.class.getMethod("test", String.class,
				String.class);
		tc.appendMethod(null, method, string0, string1);

		return tc.getDefaultTestCase();
	}

	private DefaultTestCase buildTestCase96() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();

		VariableReference string0 = tc
				.appendStringPrimitive("Toglieresta roba");
		VariableReference int0 = tc.appendIntPrimitive(0);
		Method method = TestCase96.class.getMethod("test", String.class,
				int.class);
		tc.appendMethod(null, method, string0, int0);

		return tc.getDefaultTestCase();
	}

	private DefaultTestCase buildTestCase97() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();

		VariableReference string0 = tc
				.appendStringPrimitive("Togliere sta roba");
		Method method = TestCase97.class.getMethod("test", String.class);
		tc.appendMethod(null, method, string0);

		return tc.getDefaultTestCase();
	}

	private DefaultTestCase buildTestCase98() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();

		Method method = TestCase98.class.getMethod("test");
		tc.appendMethod(null, method);

		return tc.getDefaultTestCase();
	}

	private DefaultTestCase buildTestCase99() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();

		Method method = TestCase99.class.getMethod("test", String.class);

		VariableReference string0 = tc.appendStringPrimitive("10");

		tc.appendMethod(null, method, string0);

		return tc.getDefaultTestCase();
	}

	private DefaultTestCase buildTestCase100() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();

		Method method = TestCase100.class.getMethod("test", String.class,
				int.class);

		VariableReference string0 = tc.appendStringPrimitive("roberto");

		VariableReference int0 = tc.appendIntPrimitive(-1);

		tc.appendMethod(null, method, string0, int0);

		return tc.getDefaultTestCase();
	}

	private DefaultTestCase buildTestCase101() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();

		Method method = TestCase101.class.getMethod("test", Class.class,
				String.class);

		VariableReference clazzPrimitive0 = tc
				.appendClassPrimitive(TestCase101.class);
		VariableReference string0 = tc.appendStringPrimitive(TestCase101.class
				.getName());

		tc.appendMethod(null, method, clazzPrimitive0, string0);

		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase94() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase94();
		List<BranchCondition> branch_conditions = executeTest(tc);
		Set<Variable<?>> variables = new HashSet<Variable<?>>();
		for (BranchCondition branchCondition : branch_conditions) {
			variables.addAll(branchCondition.getConstraint()
					.getVariables());
		}
		assertEquals(2, variables.size());
	}

	@Test
	public void testCase95() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase95();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(7, branch_conditions.size());

	}

	@Test
	public void testCase96() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase96();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(2, branch_conditions.size());

	}

	@Test
	public void testCase97() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase97();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(18, branch_conditions.size());

	}

	@Test
	public void testCase98() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase98();
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(0, branch_conditions.size());

	}

	@Test
	public void testCase99() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase99();

		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	@Test
	public void testCase100() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase100();

		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}

	@Test
	public void testCase101() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase101();

		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(1, branch_conditions.size());
	}
}
