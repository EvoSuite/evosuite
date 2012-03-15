package de.unisb.cs.st.evosuite.junit;

import junit.framework.Assert;

import org.junit.Test;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.testcase.TestCase;

public class JUnitTestReaderComplexTest {
	private static final String SRCDIR = "src/test/java/";

	@Test
	public void testReadComplexJUnitTestCase01() {
		Properties.PROJECT_PREFIX = "de.unisb.cs.st.evosuite.junit";
		JUnitTestReader reader = new JUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(ParentTestExample.class.getName() + "#test01");
		testCase.clone();
		String code = testCase.toCode();
		String result = "int int0 = 0;\n" + //
				"int int1 = 5;\n" + //
				"int int2 = Integer.MAX_VALUE;\n" + //
				"int int3 = 7;\n" + //
				"String string0 = null;\n" + //
				"String string1 = \"break free!\";\n" + //
				"int int4 = 3;\n" + //
				"String string2 = \"escape\";\n" + //
				"TestExample.MockingBird testExample_MockingBird0 = MockingBird.create(string2);\n" + //
				"testExample_MockingBird0.executeCmd(int4);\n";
		Assert.assertEquals(result, code);
	}

	@Test
	public void testReadComplexJUnitTestCase02() {
		Properties.PROJECT_PREFIX = "de.unisb.cs.st.evosuite.junit";
		JUnitTestReader reader = new JUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(TestExample.class.getName() + "#test01");
		testCase.clone();
		String code = testCase.toCode();
		String result = "int int0 = 0;\n" + //
				"int int1 = 5;\n" + //
				"int int2 = 7;\n" + //
				"int int3 = 10;\n" + //
				"int int4 = 4;\n" + //
				"int int5 = 42;\n" + //
				"int int6 = -5;\n" + //
				"String string0 = null;\n" + //
				"String string1 = \"break free!\";\n" + //
				"int int7 = 38;\n" + //
				"int int8 = 3;\n" + //
				"String string2 = \"convert\";\n" + //
				"String string3 = \"killSelf\";\n" + //
				"TestExample.MockingBird testExample_MockingBird0 = new TestExample.MockingBird(string3);\n" + //
				"int int9 = int8 - int7;\n" + //
				"testExample_MockingBird0.executeCmd(int9);\n";
		Assert.assertEquals(result, code);
	}
}
