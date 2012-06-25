/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.javaagent;

import junit.framework.Assert;

import org.evosuite.Properties;
import org.evosuite.TestUtil;
import org.evosuite.javaagent.InstrumentingClassLoader;
import org.evosuite.testcase.ExecutionTrace;
import org.evosuite.testcase.ExecutionTracer;
import org.junit.Ignore;
import org.junit.Test;


public class InstrumentingClassLoaderTest {

	/*
	 * Tests the child-first/parent-last property of the classloader.
	 */
	@Ignore
	@Test
	public void testDependingInstrumentation() throws Exception {
		Class<?> originalClass = DependentClassLoaderTestSubject.class;
		Properties.TARGET_CLASS = originalClass.getName();
		Properties.PROJECT_PREFIX = originalClass.getPackage().getName();
		Properties.TARGET_CLASS_PREFIX = Properties.PROJECT_PREFIX;
		InstrumentingClassLoader instrumentingClassLoader = new InstrumentingClassLoader();
		Class<?> changedClass = instrumentingClassLoader.loadClass(ClassLoaderTestSubject.class.getName());
		Assert.assertEquals(instrumentingClassLoader, changedClass.getClassLoader());
		Object changed = changedClass.getConstructor().newInstance();
		ExecutionTracer.enable();
		ExecutionTracer.getExecutionTracer().clear();
		TestUtil.invokeMethod(changed, "trySomethingElse");
		ExecutionTrace execTrace = ExecutionTracer.getExecutionTracer().getTrace();
		execTrace = ExecutionTracer.getExecutionTracer().getTrace();
		Assert.assertFalse(execTrace.getTrueDistances().isEmpty());
		Assert.assertFalse(execTrace.getFalseDistances().isEmpty());
		ExecutionTracer.getExecutionTracer().clear();
	}

	@Ignore
	@Test
	public void testDirectInstrumentation() throws Exception {
		Class<?> originalClass = ClassLoaderTestSubject.class;
		Properties.TARGET_CLASS = originalClass.getName();
		Properties.PROJECT_PREFIX = originalClass.getPackage().getName();
		ClassLoaderTestSubject original = new ClassLoaderTestSubject();
		ExecutionTracer.enable();
		ExecutionTracer.getExecutionTracer().clear();
		original.assess(6);
		ExecutionTrace execTrace = ExecutionTracer.getExecutionTracer().getTrace();
		Assert.assertTrue(execTrace.getTrueDistances().isEmpty());
		Assert.assertTrue(execTrace.getFalseDistances().isEmpty());

		InstrumentingClassLoader instrumentingClassLoader = new InstrumentingClassLoader();
		Class<?> changedClass = instrumentingClassLoader.loadClass(ClassLoaderTestSubject.class.getName());
		Assert.assertEquals(instrumentingClassLoader, changedClass.getClassLoader());
		Assert.assertTrue(changedClass.hashCode() != originalClass.hashCode());
		Assert.assertFalse(changedClass.equals(originalClass));
		Object changed = changedClass.getConstructor().newInstance();
		try {
			@SuppressWarnings("unused")
			ClassLoaderTestSubject casted = (ClassLoaderTestSubject) changed;
			Assert.fail();
		} catch (ClassCastException exc) {
			// expected
		}
		ExecutionTracer.getExecutionTracer().clear();
		TestUtil.invokeMethod(changed, "assess", Integer.valueOf(6));
		execTrace = ExecutionTracer.getExecutionTracer().getTrace();
		Assert.assertFalse(execTrace.getTrueDistances().isEmpty());
		Assert.assertFalse(execTrace.getFalseDistances().isEmpty());
		ExecutionTracer.getExecutionTracer().clear();
	}

	@Ignore
	@Test
	public void testInnerClasses() throws Exception {
		Class<? extends InnerClassesTestSubject> originalClass = InnerClassesTestSubject.class;

		Properties.TARGET_CLASS = originalClass.getName();
		Properties.PROJECT_PREFIX = originalClass.getPackage().getName();
		InstrumentingClassLoader instrumentingClassLoader = new InstrumentingClassLoader();

		Class<?> changedClass = instrumentingClassLoader.loadClass(InnerClassesTestSubject.class.getName());

		Assert.assertEquals(instrumentingClassLoader, changedClass.getClassLoader());
		Assert.assertTrue(changedClass.hashCode() != originalClass.hashCode());

		InnerClassesTestSubject original = originalClass.newInstance();
		Assert.assertEquals("abcd", original.toString());

		Object modified = changedClass.newInstance();
		Assert.assertEquals("abcd", modified.toString());
	}
}
