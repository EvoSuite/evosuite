package org.evosuite.symbolic;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.testcase.ArrayIndex;
import org.evosuite.testcase.ArrayReference;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testcase.VariableReference;
import org.junit.Test;

public class SymbolicObserverTest {

	private static void printConstraints(List<BranchCondition> branch_conditions) {
		System.out.println("Constraints=");
		for (BranchCondition branchCondition : branch_conditions) {
			for (Constraint<?> constr : branchCondition
					.listOfLocalConstraints()) {
				System.out.println(constr.toString());
			}
		}
	}

	public static class MemoryCell {

		private final int intVal;

		public MemoryCell(int int0) {
			intVal = int0;
		}

		public MemoryCell anotherCell;

		public int getValue() {
			return intVal;
		}
	}

	public static class Calculator {

		private final String operation;

		private static final String ADD = "add";
		private static final String SUB = "sub";
		private static final String DIV = "add";
		private static final String REM = "add";
		private static final String MUL = "add";

		public Calculator(String op) {
			this.operation = op;
		}

		public double compute(double l, double r) {

			if (operation.equals(ADD))
				return l + r;
			else if (operation.equals(SUB))
				return l - r;
			else if (operation.equals(DIV))
				return l / r;
			else if (operation.equals(REM))
				return l % r;
			else if (operation.equals(MUL))
				return l * r;

			return 0.0;
		}

	}

	public static class StringHandler {

		private String str;

		public StringHandler(String str) {
			this.str = str;
		}

		public boolean equals(String otherString) {
			return this.str.equals(otherString);
		}

		public void toUpperCase() {
			str = str.toUpperCase();
		}

		public static boolean stringMatches(String string, String regex) {
			return string.matches(regex);
		}

		public static void checkEquals(boolean l, boolean r) {
			if (l != r) {
				throw new RuntimeException();
			}

		}
	}

	private static void test_input1() {
		String string0 = "aaaaaaaaaaab";
		String string1 = "a*b";
		boolean boolean0 = SymbolicObserverTest.StringHandler.stringMatches(
				string0, string1);
		boolean boolean1 = true;
		SymbolicObserverTest.StringHandler.checkEquals(boolean0, boolean1);
	}

	private static DefaultTestCase build_test_input_1()
			throws SecurityException, NoSuchMethodException {

		test_input1();

		Method string_matches_method = StringHandler.class.getMethod(
				"stringMatches", String.class, String.class);
		Method checkEquals_method = StringHandler.class.getMethod(
				"checkEquals", boolean.class, boolean.class);

		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("aaaaaaaaaaab");
		VariableReference string1 = tc.appendStringPrimitive("a*b");
		VariableReference boolean0 = tc.appendMethod(null,
				string_matches_method, string0, string1);
		VariableReference boolean1 = tc.appendBooleanPrimitive(true);
		tc.appendMethod(null, checkEquals_method, boolean0, boolean1);

		return tc.getDefaultTestCase();
	}

	@Test
	public void test1() throws SecurityException, NoSuchMethodException {
		Properties.CLIENT_ON_THREAD = true;
		Properties.PRINT_TO_SYSTEM = true;
		Properties.TIMEOUT = 5000000;

		DefaultTestCase tc = build_test_input_1();

		System.out.println("TestCase=");
		System.out.println(tc.toCode());

		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic(tc);

		printConstraints(branch_conditions);
		assertEquals(1, branch_conditions.size());
	}

	private static void test_input2() {
		String string0 = "Togliere sta roba";
		StringHandler stringHandler0 = new StringHandler(string0);
		String string1 = "TOGLIERE STA ROBA";
		stringHandler0.toUpperCase();
		boolean boolean0 = stringHandler0.equals(string1);
		boolean boolean1 = true;
		StringHandler.checkEquals(boolean0, boolean1);
	}

	private static DefaultTestCase build_test_input_2()
			throws SecurityException, NoSuchMethodException {

		test_input2();

		Constructor<?> constructor = StringHandler.class
				.getConstructor(String.class);

		Method toUpperCase_method = StringHandler.class
				.getMethod("toUpperCase");
		Method equals_method = StringHandler.class.getMethod("equals",
				String.class);

		Method checkEquals_method = StringHandler.class.getMethod(
				"checkEquals", boolean.class, boolean.class);

		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc
				.appendStringPrimitive("Togliere sta roba");
		VariableReference stringHandler0 = tc.appendConstructor(constructor,
				string0);
		VariableReference string1 = tc
				.appendStringPrimitive("TOGLIERE STA ROBA");
		tc.appendMethod(stringHandler0, toUpperCase_method);
		VariableReference boolean0 = tc.appendMethod(stringHandler0,
				equals_method, string1);
		VariableReference boolean1 = tc.appendBooleanPrimitive(true);
		tc.appendMethod(null, checkEquals_method, boolean0, boolean1);

		return tc.getDefaultTestCase();
	}

	@Test
	public void test2() throws SecurityException, NoSuchMethodException {
		Properties.CLIENT_ON_THREAD = true;
		Properties.PRINT_TO_SYSTEM = true;
		Properties.TIMEOUT = 5000000;

		DefaultTestCase tc = build_test_input_2();

		System.out.println("TestCase=");
		System.out.println(tc.toCode());

		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic(tc);

		printConstraints(branch_conditions);
		assertEquals(1, branch_conditions.size());
	}

	private static void test_input3() {
		String string0 = "add";
		Calculator calculator0 = new Calculator(string0);
		double double0 = 1.5;
		double double1 = -1.5;
		double double2 = calculator0.compute(double0, double1);
		double double3 = 0.0;
		Assertions.checkEquals(double2, double3);
	}

	private static DefaultTestCase build_test_input_3()
			throws SecurityException, NoSuchMethodException {

		test_input3();

		Constructor<?> constructor = Calculator.class
				.getConstructor(String.class);

		Method compute_method = Calculator.class.getMethod("compute",
				double.class, double.class);
		Method checkEquals_method = Assertions.class.getMethod("checkEquals",
				double.class, double.class);

		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive("add");
		VariableReference calculator0 = tc.appendConstructor(constructor,
				string0);
		VariableReference double0 = tc.appendDoublePrimitive(1.5);
		VariableReference double1 = tc.appendDoublePrimitive(-1.5);
		VariableReference double2 = tc.appendMethod(calculator0,
				compute_method, double0, double1);
		VariableReference double3 = tc.appendDoublePrimitive(0.0);
		tc.appendMethod(null, checkEquals_method, double2, double3);
		return tc.getDefaultTestCase();
	}

	@Test
	public void test3() throws SecurityException, NoSuchMethodException {
		Properties.CLIENT_ON_THREAD = true;
		Properties.PRINT_TO_SYSTEM = true;
		Properties.TIMEOUT = 5000000;

		DefaultTestCase tc = build_test_input_3();

		System.out.println("TestCase=");
		System.out.println(tc.toCode());

		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic(tc);

		printConstraints(branch_conditions);
		assertEquals(2, branch_conditions.size());
	}

	private static void test_input4() {
		// IntPrimitiveStmt
		int int0 = Integer.MAX_VALUE;
		// ConstructorStmt
		MemoryCell memoryCell0 = new MemoryCell(int0);
		// Assignment Stmt (non-static)
		memoryCell0.anotherCell = memoryCell0;
		// FieldStmt (non-static)
		MemoryCell memoryCell1 = memoryCell0.anotherCell;
		// MethodStatement
		int int1 = memoryCell0.getValue();
		// MethodStatement
		int int2 = memoryCell1.getValue();
		// MethodStatement
		Assertions.checkEquals(int1, int2);
	}

	private static DefaultTestCase build_test_input_4()
			throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {

		test_input4();

		Constructor<?> constructor = MemoryCell.class.getConstructor(int.class);

		Field anotherCell_field = MemoryCell.class.getField("anotherCell");

		Method getValue_method = MemoryCell.class.getMethod("getValue");

		Method checkEquals_method = Assertions.class.getMethod("checkEquals",
				int.class, int.class);

		TestCaseBuilder tc = new TestCaseBuilder();

		VariableReference int0 = tc.appendIntPrimitive(Integer.MAX_VALUE);

		VariableReference memoryCell0 = tc.appendConstructor(constructor, int0);

		tc.appendAssignment(memoryCell0, anotherCell_field, memoryCell0);

		VariableReference memoryCell1 = tc.appendFieldStmt(memoryCell0,
				anotherCell_field);

		VariableReference int1 = tc.appendMethod(memoryCell0, getValue_method);

		VariableReference int2 = tc.appendMethod(memoryCell1, getValue_method);

		tc.appendMethod(null, checkEquals_method, int1, int2);
		return tc.getDefaultTestCase();
	}

	@Test
	public void test4() throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {
		Properties.CLIENT_ON_THREAD = true;
		Properties.PRINT_TO_SYSTEM = true;
		Properties.TIMEOUT = 5000000;

		DefaultTestCase tc = build_test_input_4();

		System.out.println("TestCase=");
		System.out.println(tc.toCode());

		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic(tc);

		printConstraints(branch_conditions);
		assertEquals(1, branch_conditions.size());
	}

	public static class IntHolder {
		public int intValue;

		public IntHolder(int myInt) {
			this.intValue = myInt;
		}

		public int getValue() {
			return intValue;
		}
	}

	private static void test_input5() {
		// IntPrimitiveStmt
		int int0 = Integer.MAX_VALUE;
		// IntPrimitiveStmt
		int int1 = Integer.MIN_VALUE;
		// ConstructorStmt
		IntHolder intHolder0 = new IntHolder(int0);
		// ConstructorStmt
		IntHolder intHolder1 = new IntHolder(int1);
		// FieldStmt (non-static)
		int int2 = intHolder0.intValue;
		// Assignment Stmt (non-static)
		intHolder1.intValue = int2;
		// MethodStatement
		int int3 = intHolder0.getValue();
		// MethodStatement
		int int4 = intHolder0.getValue();
		// MethodStatement
		Assertions.checkEquals(int3, int4);
	}

	private static DefaultTestCase build_test_input_5()
			throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {

		test_input5();

		Constructor<?> newIntHolder = IntHolder.class.getConstructor(int.class);

		Field intValue = IntHolder.class.getField("intValue");

		Method getValue = IntHolder.class.getMethod("getValue");

		Method checkEquals = Assertions.class.getMethod("checkEquals",
				int.class, int.class);

		TestCaseBuilder tc = new TestCaseBuilder();

		VariableReference int0 = tc.appendIntPrimitive(Integer.MAX_VALUE);

		VariableReference int1 = tc.appendIntPrimitive(Integer.MIN_VALUE);

		VariableReference intHolder0 = tc.appendConstructor(newIntHolder, int0);

		VariableReference intHolder1 = tc.appendConstructor(newIntHolder, int1);

		VariableReference int2 = tc.appendFieldStmt(intHolder0, intValue);

		tc.appendAssignment(intHolder1, intValue, int2);

		VariableReference int3 = tc.appendMethod(intHolder0, getValue);

		VariableReference int4 = tc.appendMethod(intHolder1, getValue);

		tc.appendMethod(null, checkEquals, int3, int4);

		return tc.getDefaultTestCase();
	}

	@Test
	public void test5() throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {
		Properties.CLIENT_ON_THREAD = true;
		Properties.PRINT_TO_SYSTEM = true;
		Properties.TIMEOUT = 5000000;

		DefaultTestCase tc = build_test_input_5();

		System.out.println("TestCase=");
		System.out.println(tc.toCode());

		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic(tc);

		printConstraints(branch_conditions);
		assertEquals(1, branch_conditions.size());
	}

	public static abstract class StaticFields {

		public static String string_field;

		public static Object object_field;

		public static boolean equals(String left, String right) {
			return left.equals(right);
		}
	}

	private static void test_input6() {

		String string0 = "Togliere sta roba";
		StaticFields.string_field = string0;
		String string1 = StaticFields.string_field;
		boolean boolean0 = StaticFields.equals(string0, string1);
		boolean boolean1 = true;
		Assertions.checkEquals(boolean0, boolean1);

	}

	private static DefaultTestCase build_test_input_6()
			throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {

		test_input6();

		Field string_field = StaticFields.class.getField("string_field");

		Method equals = StaticFields.class.getMethod("equals", String.class,
				String.class);

		Method checkEquals = Assertions.class.getMethod("checkEquals",
				boolean.class, boolean.class);

		TestCaseBuilder tc = new TestCaseBuilder();

		VariableReference string0 = tc
				.appendStringPrimitive("Togliere sta roba");

		tc.appendAssignment(null, string_field, string0);

		VariableReference string1 = tc.appendFieldStmt(null, string_field);

		VariableReference boolean0 = tc.appendMethod(null, equals, string0,
				string1);

		VariableReference boolean1 = tc.appendBooleanPrimitive(true);

		tc.appendMethod(null, checkEquals, boolean0, boolean1);

		return tc.getDefaultTestCase();
	}

	@Test
	public void test6() throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {
		Properties.CLIENT_ON_THREAD = true;
		Properties.PRINT_TO_SYSTEM = true;
		Properties.TIMEOUT = 5000000;

		DefaultTestCase tc = build_test_input_6();

		System.out.println("TestCase=");
		System.out.println(tc.toCode());

		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic(tc);

		printConstraints(branch_conditions);
		assertEquals(1, branch_conditions.size());
	}

	private static void test_input7() {
		// NullStmt
		String string0 = null;
		// Assignment Stmt
		String string1 = string0;
	}

	public enum MyEnum {
		VALUE1, VALUE2
	}

	private static void test_input8() {
		// EnumPrimitiveStmt
		MyEnum myEnum0 = MyEnum.VALUE1;
		MyEnum myEnum1 = MyEnum.VALUE1;
		MyEnum myEnum2 = MyEnum.VALUE2;
	}

	private static DefaultTestCase build_test_input_7()
			throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {

		test_input7();

		TestCaseBuilder tc = new TestCaseBuilder();

		VariableReference string0 = tc.appendNull(String.class);

		return tc.getDefaultTestCase();
	}

	@Test
	public void test7() throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {
		Properties.CLIENT_ON_THREAD = true;
		Properties.PRINT_TO_SYSTEM = true;
		Properties.TIMEOUT = 5000000;

		DefaultTestCase tc = build_test_input_7();

		System.out.println("TestCase=");
		System.out.println(tc.toCode());

		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic(tc);

		printConstraints(branch_conditions);
		assertEquals(0, branch_conditions.size());
	}

	private static DefaultTestCase build_test_input_8()
			throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {

		test_input8();

		TestCaseBuilder tc = new TestCaseBuilder();

		VariableReference myEnum0 = tc.appendEnumPrimitive(MyEnum.VALUE1);
		VariableReference myEnum1 = tc.appendEnumPrimitive(MyEnum.VALUE1);
		VariableReference myEnum2 = tc.appendEnumPrimitive(MyEnum.VALUE2);

		return tc.getDefaultTestCase();
	}

	@Test
	public void test8() throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {
		Properties.CLIENT_ON_THREAD = true;
		Properties.PRINT_TO_SYSTEM = true;
		Properties.TIMEOUT = 5000000;

		DefaultTestCase tc = build_test_input_8();

		System.out.println("TestCase=");
		System.out.println(tc.toCode());

		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic(tc);

		printConstraints(branch_conditions);
		assertEquals(0, branch_conditions.size());
	}

	private static void test_input9() {
		// ArrayStmt
		int[] intArray0 = new int[10];
		// ArrayStmt
		double[] doubleArray0 = new double[10];
		// ArrayStmt
		String[] stringArray0 = new String[10];
		// ArrayStmt
		int[][] intMatrix0 = new int[3][3];
		// IntPrimitiveStmt
		int int0 = Integer.MAX_VALUE;
		// store
		intArray0[1] = int0;
		// IntPrimitiveStmt
		int int1 = Integer.MIN_VALUE;
		// load
		int1 = intArray0[1];
		// checkEquals
		Assertions.checkEquals(int0, int1);
	}

	private static DefaultTestCase build_test_input_9()
			throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {

		test_input9();

		Method checkEquals = Assertions.class.getMethod("checkEquals",
				int.class, int.class);

		TestCaseBuilder tc = new TestCaseBuilder();

		ArrayReference intArray0 = tc.appendArrayStmt(int[].class, 10);
		ArrayReference doubleArray0 = tc.appendArrayStmt(double[].class, 11);
		ArrayReference stringArray0 = tc.appendArrayStmt(String[].class, 12);
		ArrayReference intMatrix0 = tc.appendArrayStmt(int[].class, 3, 3);
		VariableReference int0 = tc.appendIntPrimitive(Integer.MAX_VALUE);
		tc.appendAssignment(intArray0, 1, int0);
		VariableReference int1 = tc.appendIntPrimitive(Integer.MIN_VALUE);
		tc.appendAssignment(int1, intArray0, 1);
		tc.appendMethod(null, checkEquals, int0, int1);

		return tc.getDefaultTestCase();
	}

	@Test
	public void test9() throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {
		Properties.CLIENT_ON_THREAD = true;
		Properties.PRINT_TO_SYSTEM = true;
		Properties.TIMEOUT = 5000000;

		DefaultTestCase tc = build_test_input_9();

		System.out.println("TestCase=");
		System.out.println(tc.toCode());

		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic(tc);

		printConstraints(branch_conditions);
		assertEquals(1, branch_conditions.size());
	}

	private static void test_input10() {
		// IntPrimitiveStmt
		int int0 = Integer.MAX_VALUE;
		// IntPrimitiveStmt
		int int1 = Integer.MIN_VALUE;
		// MethodStmt
		int int2 = Math.max(int0, int1);
		// MethodStmt
		Assertions.checkEquals(int0, int2);
	}

	private static DefaultTestCase build_test_input_10()
			throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {

		test_input10();

		Method checkEquals = Assertions.class.getMethod("checkEquals",
				int.class, int.class);

		Method max = Math.class.getMethod("max", int.class, int.class);

		TestCaseBuilder tc = new TestCaseBuilder();

		VariableReference int0 = tc.appendIntPrimitive(Integer.MAX_VALUE);
		VariableReference int1 = tc.appendIntPrimitive(Integer.MIN_VALUE);
		VariableReference int2 = tc.appendMethod(null, max, int0, int1);
		tc.appendMethod(null, checkEquals, int0, int2);

		return tc.getDefaultTestCase();
	}

	@Test
	public void test10() throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {
		Properties.CLIENT_ON_THREAD = true;
		Properties.PRINT_TO_SYSTEM = true;
		Properties.TIMEOUT = 5000000;

		DefaultTestCase tc = build_test_input_10();

		System.out.println("TestCase=");
		System.out.println(tc.toCode());

		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic(tc);

		printConstraints(branch_conditions);
		assertEquals(1, branch_conditions.size());
	}

}
