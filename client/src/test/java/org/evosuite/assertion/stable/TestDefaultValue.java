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
package org.evosuite.assertion.stable;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.evosuite.Properties;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestDefaultValue {

	private final boolean DEFAULT_SANDBOX = Properties.SANDBOX;

	@Before
	public void setUp() {
		Properties.SANDBOX = false;
	}

	@After
	public void reset() {
		Properties.SANDBOX = DEFAULT_SANDBOX;
		ClassPathHandler.resetSingleton();
	}

	@Test
	public void testDouble() throws SecurityException, NoSuchMethodException {
		TestCaseBuilder builder = new TestCaseBuilder();
		ArrayReference doubleArray0 = builder.appendArrayStmt(Double[].class,
				10);
		VariableReference double0 = builder.appendNull(Double.class);
		builder.appendAssignment(doubleArray0, 0, double0);
		builder.appendAssignment(double0, doubleArray0, 0);
		builder.appendMethod(double0, Double.class.getMethod("floatValue"));
		DefaultTestCase tc = builder.getDefaultTestCase();
		ExecutionResult ret_val = TestCaseExecutor.runTest(tc);
		assertNotNull(ret_val);
		assertFalse(ret_val.explicitExceptions.isEmpty());
	}

	@Test
	public void testFloat() throws SecurityException, NoSuchMethodException {
		TestCaseBuilder builder = new TestCaseBuilder();
		ArrayReference floatArray0 = builder.appendArrayStmt(Float[].class, 10);
		VariableReference float0 = builder.appendNull(Float.class);
		builder.appendAssignment(floatArray0, 0, float0);
		builder.appendAssignment(float0, floatArray0, 0);
		builder.appendMethod(float0, Float.class.getMethod("toString"));
		DefaultTestCase tc = builder.getDefaultTestCase();
		System.out.println(tc.toCode());
		ExecutionResult ret_val = TestCaseExecutor.runTest(tc);
		assertNotNull(ret_val);
		assertFalse(ret_val.explicitExceptions.isEmpty());
	}
	
	@Test
	public void testInteger() throws SecurityException, NoSuchMethodException {
		TestCaseBuilder builder = new TestCaseBuilder();
		ArrayReference integerArray0 = builder.appendArrayStmt(Integer[].class, 10);
		VariableReference integer0 = builder.appendNull(Integer.class);
		builder.appendAssignment(integerArray0, 0, integer0);
		builder.appendAssignment(integer0, integerArray0, 0);
		builder.appendMethod(integer0, Integer.class.getMethod("toString"));
		DefaultTestCase tc = builder.getDefaultTestCase();
		ExecutionResult ret_val = TestCaseExecutor.runTest(tc);
		assertNotNull(ret_val);
		assertFalse(ret_val.explicitExceptions.isEmpty());
	}
	
	@Test
	public void testLong() throws SecurityException, NoSuchMethodException {
		TestCaseBuilder builder = new TestCaseBuilder();
		ArrayReference longArray0 = builder.appendArrayStmt(Long[].class, 10);
		VariableReference long0 = builder.appendNull(Long.class);
		builder.appendAssignment(longArray0, 0, long0);
		builder.appendAssignment(long0, longArray0, 0);
		builder.appendMethod(long0, Long.class.getMethod("toString"));
		DefaultTestCase tc = builder.getDefaultTestCase();
		ExecutionResult ret_val = TestCaseExecutor.runTest(tc);
		assertNotNull(ret_val);
		assertFalse(ret_val.explicitExceptions.isEmpty());
	}
	
	@Test
	public void testCharacter() throws SecurityException, NoSuchMethodException {
		TestCaseBuilder builder = new TestCaseBuilder();
		ArrayReference characterArray0 = builder.appendArrayStmt(Character[].class, 10);
		VariableReference character0 = builder.appendNull(Character.class);
		builder.appendAssignment(characterArray0, 0, character0);
		builder.appendAssignment(character0, characterArray0, 0);
		builder.appendMethod(character0, Character.class.getMethod("toString"));
		DefaultTestCase tc = builder.getDefaultTestCase();
		ExecutionResult ret_val = TestCaseExecutor.runTest(tc);
		assertNotNull(ret_val);
		assertFalse(ret_val.explicitExceptions.isEmpty());
	}
	
	@Test
	public void testByte() throws SecurityException, NoSuchMethodException {
		TestCaseBuilder builder = new TestCaseBuilder();
		ArrayReference integerArray0 = builder.appendArrayStmt(Byte[].class, 10);
		VariableReference integer0 = builder.appendNull(Byte.class);
		builder.appendAssignment(integerArray0, 0, integer0);
		builder.appendAssignment(integer0, integerArray0, 0);
		builder.appendMethod(integer0, Byte.class.getMethod("toString"));
		DefaultTestCase tc = builder.getDefaultTestCase();
		ExecutionResult ret_val = TestCaseExecutor.runTest(tc);
		assertNotNull(ret_val);
		assertFalse(ret_val.explicitExceptions.isEmpty());
	}
	
	@Test
	public void testShort() throws SecurityException, NoSuchMethodException {
		TestCaseBuilder builder = new TestCaseBuilder();
		ArrayReference integerArray0 = builder.appendArrayStmt(Short[].class, 10);
		VariableReference integer0 = builder.appendNull(Short.class);
		builder.appendAssignment(integerArray0, 0, integer0);
		builder.appendAssignment(integer0, integerArray0, 0);
		builder.appendMethod(integer0, Short.class.getMethod("toString"));
		DefaultTestCase tc = builder.getDefaultTestCase();
		ExecutionResult ret_val = TestCaseExecutor.runTest(tc);
		assertNotNull(ret_val);
		assertFalse(ret_val.explicitExceptions.isEmpty());
	}
}
