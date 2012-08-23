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
}
