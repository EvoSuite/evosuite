package de.unisb.cs.st.evosuite.junit;

import junit.framework.Assert;

import org.junit.Test;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.testcase.TestCase;

public class JUnitTestReaderComplexTest {
	private static final String SRCDIR = "src/test/java/";

	@Test
	public void testReadSimpleJUnitTestCase01() {
		Properties.PROJECT_PREFIX = "de.unisb.cs.st.evosuite.junit";
		JUnitTestReader reader = new JUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCaseMethod(ParentTestExample.class.getName() + "#test01");
		testCase.clone();
		String code = testCase.toCode();
		String result = "String string0 = null;\n" + //
				"int int0 = 0;\n" + //
				"TestExample.MockingBird testExample_MockingBird0 = MockingBird.create(string0);\n" + //
				"testExample_MockingBird0.executeCmd(int0);\n";
		Assert.assertEquals(result, code);
	}
}
