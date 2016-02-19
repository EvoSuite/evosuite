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
/**
 * 
 */
package org.evosuite.testsuite;

import junit.framework.Assert;

import org.evosuite.Properties;
import org.evosuite.junit.JUnitTestReader;
import org.evosuite.testcase.TestCase;
import org.junit.Test;

/**
 * @author Gordon Fraser
 * 
 */
public class TestTestCaseExpander {

	private static final String SRCDIR = "src/test/java/";

	@Test
	public void testNoAssignment() {
		Properties.PROJECT_PREFIX = "org.evosuite.testsuite";
		JUnitTestReader reader = new JUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(ArrayTestExample1.class.getName()
		        + "#test1");
		testCase.clone();
		String code = testCase.toCode();

		TestCaseExpander expander = new TestCaseExpander();
		TestCase expandedTest = expander.expandTestCase(testCase);
		String expandedCode = expandedTest.toCode();

		Assert.assertEquals(testCase.size() + 20, expandedTest.size());
		Assert.assertFalse(code.equals(expandedCode));

	}

	@Test
	public void testSomeAssignments() {
		Properties.PROJECT_PREFIX = "org.evosuite.testsuite";
		JUnitTestReader reader = new JUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(ArrayTestExample1.class.getName()
		        + "#test2");
		testCase.clone();
		String code = testCase.toCode();

		TestCaseExpander expander = new TestCaseExpander();
		TestCase expandedTest = expander.expandTestCase(testCase);
		String expandedCode = expandedTest.toCode();

		Assert.assertEquals(testCase.size() + 14, expandedTest.size());
		Assert.assertFalse(code.equals(expandedCode));
	}

	@Test
	public void testHiddenAssignments() {
		Properties.PROJECT_PREFIX = "org.evosuite.testsuite";
		JUnitTestReader reader = new JUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(ArrayTestExample1.class.getName()
		        + "#test3");
		testCase.clone();
		String code = testCase.toCode();

		TestCaseExpander expander = new TestCaseExpander();
		TestCase expandedTest = expander.expandTestCase(testCase);
		String expandedCode = expandedTest.toCode();

		Assert.assertEquals("Wrong length of test: " + expandedCode,
		                    testCase.size() + 24, expandedTest.size());
		Assert.assertFalse(code.equals(expandedCode));
	}

	@Test
	public void testOneAssignments() {
		Properties.PROJECT_PREFIX = "org.evosuite.testsuite";
		JUnitTestReader reader = new JUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(ArrayTestExample1.class.getName()
		        + "#test4");
		testCase.clone();
		String code = testCase.toCode();

		TestCaseExpander expander = new TestCaseExpander();
		TestCase expandedTest = expander.expandTestCase(testCase);
		String expandedCode = expandedTest.toCode();

		Assert.assertEquals("Wrong length of test: " + expandedCode + ", original test: "
		        + code, testCase.size() + 2, expandedTest.size());
		Assert.assertFalse(code.equals(expandedCode));
	}

	@Test
	public void testArrayAssignment() {
		Properties.PROJECT_PREFIX = "org.evosuite.testsuite";
		JUnitTestReader reader = new JUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(ArrayTestExample1.class.getName()
		        + "#test5");
		testCase.clone();
		String code = testCase.toCode();

		TestCaseExpander expander = new TestCaseExpander();
		TestCase expandedTest = expander.expandTestCase(testCase);
		String expandedCode = expandedTest.toCode();

		Assert.assertEquals("Wrong length of test: " + expandedCode + ", original test: "
		        + code, testCase.size() + 1, expandedTest.size());
		Assert.assertFalse(code.equals(expandedCode));
	}
}
