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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.junit.Test;

import com.examples.with.different.packagename.concolic.Assertions;
import com.examples.with.different.packagename.concolic.Boxer;
import com.examples.with.different.packagename.concolic.Calculator;
import com.examples.with.different.packagename.concolic.Fraction;
import com.examples.with.different.packagename.concolic.IntHolder;
import com.examples.with.different.packagename.concolic.MemoryCell;
import com.examples.with.different.packagename.concolic.MyEnum;
import com.examples.with.different.packagename.concolic.StaticFields;
import com.examples.with.different.packagename.concolic.StringHandler;
import com.examples.with.different.packagename.concolic.TestCase86;

public class SymbolicObserverTest {

	public static void printConstraints(List<BranchCondition> branch_conditions) {
		System.out.println("Constraints=");
		for (BranchCondition branchCondition : branch_conditions) {

			for (Constraint<?> constr : branchCondition
					.getSupportingConstraints()) {
				System.out.println(constr.toString());
			}
			System.out.println(branchCondition.getConstraint().toString());

		}
	}

	private static void test_input1() {
		String string0 = "aaaaaaaaaaab";
		String string1 = "a*b";
		boolean boolean0 = StringHandler.stringMatches(string0, string1);
		boolean boolean1 = true;
		StringHandler.checkEquals(boolean0, boolean1);
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

		List<BranchCondition> branch_conditions = ConcolicExecution
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

		List<BranchCondition> branch_conditions = ConcolicExecution
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

		List<BranchCondition> branch_conditions = ConcolicExecution
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

		List<BranchCondition> branch_conditions = ConcolicExecution
				.executeConcolic(tc);

		printConstraints(branch_conditions);
		assertEquals(1, branch_conditions.size());
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

		List<BranchCondition> branch_conditions = ConcolicExecution
				.executeConcolic(tc);

		printConstraints(branch_conditions);
		assertEquals(1, branch_conditions.size());
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

		VariableReference string1 = tc.appendStaticFieldStmt(string_field);

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

		List<BranchCondition> branch_conditions = ConcolicExecution
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

		tc.appendNull(String.class);

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

		List<BranchCondition> branch_conditions = ConcolicExecution
				.executeConcolic(tc);

		printConstraints(branch_conditions);
		assertEquals(0, branch_conditions.size());
	}

	private static DefaultTestCase build_test_input_8()
			throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {

		test_input8();

		TestCaseBuilder tc = new TestCaseBuilder();

		tc.appendEnumPrimitive(MyEnum.VALUE1);
		tc.appendEnumPrimitive(MyEnum.VALUE1);
		tc.appendEnumPrimitive(MyEnum.VALUE2);

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

		List<BranchCondition> branch_conditions = ConcolicExecution
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
		tc.appendArrayStmt(double[].class, 11);
		tc.appendArrayStmt(String[].class, 12);
		tc.appendArrayStmt(int[].class, 3, 3);
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

		List<BranchCondition> branch_conditions = ConcolicExecution
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

		List<BranchCondition> branch_conditions = ConcolicExecution
				.executeConcolic(tc);

		printConstraints(branch_conditions);
		assertEquals(1, branch_conditions.size());
	}

	private static DefaultTestCase build_test_input_11()
			throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {

		Constructor<?> newBoolean = Boolean.class.getConstructor(boolean.class);
		Constructor<?> newInteger = Integer.class.getConstructor(int.class);
		Constructor<?> newByte = Byte.class.getConstructor(byte.class);
		Constructor<?> newShort = Short.class.getConstructor(short.class);
		Constructor<?> newChar = Character.class.getConstructor(char.class);
		Constructor<?> newLong = Long.class.getConstructor(long.class);
		Constructor<?> newFloat = Float.class.getConstructor(float.class);
		Constructor<?> newDouble = Double.class.getConstructor(double.class);

		Method booleanValue = Boolean.class.getMethod("booleanValue");
		Method intValue = Integer.class.getMethod("intValue");
		Method byteValue = Byte.class.getMethod("byteValue");
		Method shortValue = Short.class.getMethod("shortValue");
		Method charValue = Character.class.getMethod("charValue");
		Method longValue = Long.class.getMethod("longValue");
		Method floatValue = Float.class.getMethod("floatValue");
		Method doubleValue = Double.class.getMethod("doubleValue");

		Method checkBooleanEquals = Assertions.class.getMethod("checkEquals",
				boolean.class, boolean.class);
		Method checkIntEquals = Assertions.class.getMethod("checkEquals",
				int.class, int.class);
		Method checkByteEquals = Assertions.class.getMethod("checkEquals",
				byte.class, byte.class);
		Method checkShortEquals = Assertions.class.getMethod("checkEquals",
				short.class, short.class);
		Method checkCharEquals = Assertions.class.getMethod("checkEquals",
				char.class, char.class);
		Method checkLongEquals = Assertions.class.getMethod("checkEquals",
				long.class, long.class);
		Method checkFloatEquals = Assertions.class.getMethod("checkEquals",
				float.class, float.class);
		Method checkDoubleEquals = Assertions.class.getMethod("checkEquals",
				double.class, double.class);

		TestCaseBuilder tc = new TestCaseBuilder();

		VariableReference int0 = tc.appendIntPrimitive(Integer.MAX_VALUE);
		VariableReference integer0 = tc.appendConstructor(newInteger, int0);
		VariableReference int1 = tc.appendMethod(integer0, intValue);

		VariableReference byte0 = tc.appendBytePrimitive(Byte.MAX_VALUE);
		VariableReference byte_instance0 = tc.appendConstructor(newByte, byte0);
		VariableReference byte1 = tc.appendMethod(byte_instance0, byteValue);

		VariableReference short0 = tc.appendShortPrimitive(Short.MAX_VALUE);
		VariableReference short_instance0 = tc.appendConstructor(newShort,
				short0);
		VariableReference short1 = tc.appendMethod(short_instance0, shortValue);

		VariableReference char0 = tc.appendCharPrimitive(Character.MAX_VALUE);
		VariableReference character0 = tc.appendConstructor(newChar, char0);
		VariableReference char1 = tc.appendMethod(character0, charValue);

		VariableReference long0 = tc.appendLongPrimitive(Long.MAX_VALUE);
		VariableReference long_instance0 = tc.appendConstructor(newLong, long0);
		VariableReference long1 = tc.appendMethod(long_instance0, longValue);

		VariableReference float1 = tc.appendFloatPrimitive(Float.MAX_VALUE);
		VariableReference float_instance1 = tc.appendConstructor(newFloat,
				float1);
		VariableReference float2 = tc.appendMethod(float_instance1, floatValue);

		VariableReference double0 = tc.appendDoublePrimitive(Double.MAX_VALUE);
		VariableReference double_instance1 = tc.appendConstructor(newDouble,
				double0);
		VariableReference double1 = tc.appendMethod(double_instance1,
				doubleValue);

		VariableReference boolean0 = tc.appendBooleanPrimitive(Boolean.TRUE);
		VariableReference boolean_instance0 = tc.appendConstructor(newBoolean,
				boolean0);
		VariableReference boolean1 = tc.appendMethod(boolean_instance0,
				booleanValue);

		tc.appendMethod(null, checkIntEquals, int0, int1);
		tc.appendMethod(null, checkByteEquals, byte0, byte1);
		tc.appendMethod(null, checkShortEquals, short0, short1);
		tc.appendMethod(null, checkCharEquals, char0, char1);
		tc.appendMethod(null, checkFloatEquals, float1, float2);
		tc.appendMethod(null, checkLongEquals, long0, long1);
		tc.appendMethod(null, checkDoubleEquals, double0, double1);
		tc.appendMethod(null, checkBooleanEquals, boolean0, boolean1);

		return tc.getDefaultTestCase();
	}

	@Test
	public void test11() throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {
		Properties.CLIENT_ON_THREAD = true;
		Properties.PRINT_TO_SYSTEM = true;
		Properties.TIMEOUT = 5000000;

		DefaultTestCase tc = build_test_input_11();

		System.out.println("TestCase=");
		System.out.println(tc.toCode());

		List<BranchCondition> branch_conditions = ConcolicExecution
				.executeConcolic(tc);

		printConstraints(branch_conditions);
		assertEquals(8, branch_conditions.size());
	}

	private static DefaultTestCase build_test_input_12()
			throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {

		Constructor<?> newString = String.class.getConstructor(String.class);
		Method length = String.class.getMethod("length");
		Method checkIntEquals = Assertions.class.getMethod("checkEquals",
				int.class, int.class);
		TestCaseBuilder tc = new TestCaseBuilder();

		VariableReference string0 = tc
				.appendStringPrimitive("Togliere sta roba");
		VariableReference string1 = tc.appendConstructor(newString, string0);
		VariableReference int0 = tc.appendMethod(string0, length);
		VariableReference int1 = tc.appendMethod(string1, length);
		tc.appendMethod(null, checkIntEquals, int0, int1);

		return tc.getDefaultTestCase();
	}

	@Test
	public void test12() throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {
		Properties.CLIENT_ON_THREAD = true;
		Properties.PRINT_TO_SYSTEM = true;
		Properties.TIMEOUT = 5000000;

		DefaultTestCase tc = build_test_input_12();

		System.out.println("TestCase=");
		System.out.println(tc.toCode());

		List<BranchCondition> branch_conditions = ConcolicExecution
				.executeConcolic(tc);

		printConstraints(branch_conditions);
		assertEquals(1, branch_conditions.size());
	}

	private static DefaultTestCase build_test_input_13()
			throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {

		Method checkDoubleEquals = Assertions.class.getMethod("checkEquals",
				double.class, double.class);
		TestCaseBuilder tc = new TestCaseBuilder();

		VariableReference int0 = tc.appendIntPrimitive(10);
		VariableReference int1 = tc.appendIntPrimitive(20);
		tc.appendMethod(null, checkDoubleEquals, int0, int1);

		return tc.getDefaultTestCase();
	}

	@Test
	public void test13() throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {
		Properties.CLIENT_ON_THREAD = true;
		Properties.PRINT_TO_SYSTEM = true;
		Properties.TIMEOUT = 5000000;

		DefaultTestCase tc = build_test_input_13();

		System.out.println("TestCase=");
		System.out.println(tc.toCode());

		List<BranchCondition> branch_conditions = ConcolicExecution
				.executeConcolic(tc);

		printConstraints(branch_conditions);
		assertEquals(1, branch_conditions.size());
	}

	@Test
	public void test_input_14() {
		int int0 = Integer.MAX_VALUE;
		Integer integer0 = Boxer.boxInteger(int0);
		int int1 = Boxer.unboxInteger(integer0);
		Assertions.checkEquals(int0, int1);
	}

	private static DefaultTestCase build_test_input_14()
			throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {

		Method checkIntEquals = Assertions.class.getMethod("checkEquals",
				int.class, int.class);

		Method boxInteger = Boxer.class.getMethod("boxInteger", Integer.class);

		Method unboxInteger = Boxer.class.getMethod("unboxInteger", int.class);

		TestCaseBuilder tc = new TestCaseBuilder();

		VariableReference int0 = tc.appendIntPrimitive(Integer.MAX_VALUE);
		VariableReference integer0 = tc.appendMethod(null, boxInteger, int0);
		VariableReference int1 = tc.appendMethod(null, unboxInteger, integer0);
		tc.appendMethod(null, checkIntEquals, int0, int1);

		return tc.getDefaultTestCase();
	}

	@Test
	public void test14() throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {
		Properties.CLIENT_ON_THREAD = true;
		Properties.PRINT_TO_SYSTEM = true;
		Properties.TIMEOUT = 5000000;

		DefaultTestCase tc = build_test_input_14();

		System.out.println("TestCase=");
		System.out.println(tc.toCode());

		List<BranchCondition> branch_conditions = ConcolicExecution
				.executeConcolic(tc);

		printConstraints(branch_conditions);
		assertEquals(1, branch_conditions.size());
	}

	// Fraction fraction0 = Fraction.ONE_FIFTH;
	private static DefaultTestCase build_test_input_15()
			throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {

		Field one_fifth = Fraction.class.getField("ONE_FIFTH");

		TestCaseBuilder tc = new TestCaseBuilder();

		tc.appendStaticFieldStmt(one_fifth);
		return tc.getDefaultTestCase();
	}

	@Test
	public void test15() throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {
		Properties.CLIENT_ON_THREAD = true;
		Properties.PRINT_TO_SYSTEM = true;
		Properties.TIMEOUT = 5000000;

		DefaultTestCase tc = build_test_input_15();

		System.out.println("TestCase=");
		System.out.println(tc.toCode());

		List<BranchCondition> branch_conditions = ConcolicExecution
				.executeConcolic(tc);

		printConstraints(branch_conditions);
		assertEquals(0, branch_conditions.size());
	}

	// Double double0 = null;
	// Double double1 = Double.valueOf((double) double0);
	private static DefaultTestCase build_test_input_16()
			throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {

		Method valueOf = Double.class.getMethod("valueOf", double.class);

		TestCaseBuilder tc = new TestCaseBuilder();

		VariableReference double0 = tc.appendNull(Double.class);
		tc.appendMethod(null, valueOf, double0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void test16() throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {
		Properties.CLIENT_ON_THREAD = true;
		Properties.PRINT_TO_SYSTEM = true;
		Properties.TIMEOUT = 5000000;

		DefaultTestCase tc = build_test_input_16();

		System.out.println("TestCase=");
		System.out.println(tc.toCode());

		List<BranchCondition> branch_conditions = ConcolicExecution
				.executeConcolic(tc);

		printConstraints(branch_conditions);
		assertEquals(0, branch_conditions.size());
	}

	private static DefaultTestCase build_test_input_17()
			throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {

		Method test = TestCase86.class.getMethod("test", int.class);

		TestCaseBuilder tc = new TestCaseBuilder();

		VariableReference int0 = tc.appendIntPrimitive(Integer.MAX_VALUE);
		tc.appendMethod(null, test, int0);

		return tc.getDefaultTestCase();
	}

	@Test
	public void test17() throws SecurityException, NoSuchMethodException,
			NoSuchFieldException {
		Properties.CLIENT_ON_THREAD = true;
		Properties.PRINT_TO_SYSTEM = true;
		Properties.TIMEOUT = 5000000;

		DefaultTestCase tc = build_test_input_17();

		System.out.println("TestCase=");
		System.out.println(tc.toCode());

		List<BranchCondition> branch_conditions = ConcolicExecution
				.executeConcolic(tc);

		printConstraints(branch_conditions);
		assertEquals(1, branch_conditions.size());
	}
}
