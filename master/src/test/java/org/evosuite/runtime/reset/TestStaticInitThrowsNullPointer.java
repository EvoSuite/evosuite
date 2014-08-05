package org.evosuite.runtime.reset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.junit.Test;

import com.examples.with.different.packagename.reset.StaticInitThrowsNullPointer;

public class TestStaticInitThrowsNullPointer extends SystemTest {

	/*
	 * These tests are based on issues found on project 44_summa, which is using the lucene API.
	 * those have issues when for example classes uses org.apache.lucene.util.Constants which has:
	 * 
	  try {
	    Collections.class.getMethod("emptySortedSet");
	  } catch (NoSuchMethodException nsme) {
	    v8 = false;
	  }
	  *
	  * in its static initializer
	 */

	@Test
	public void testWithNoReset() {
		Properties.RESET_STATIC_FIELDS = false;

		EvoSuite evosuite = new EvoSuite();

		String targetClass = StaticInitThrowsNullPointer.class
				.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);
		assertTrue(result instanceof List);
		List<?> list = (List<?>)result;
		assertEquals(0, list.size());
	}

	@Test
	public void testWithReset() {
		Properties.RESET_STATIC_FIELDS = true;

		EvoSuite evosuite = new EvoSuite();

		String targetClass = StaticInitThrowsNullPointer.class
				.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);
		assertTrue(result instanceof List);
		List<?> list = (List<?>)result;
		assertEquals(0, list.size());
	}
}
