package org.evosuite.junit;

import junit.framework.Assert;

import org.evosuite.Properties;
import org.evosuite.junit.JUnitTestReader;
import org.evosuite.testcase.TestCase;
import org.junit.Test;


public class JUnitTestReaderComplexTest {
	private static final String SRCDIR = "src/test/java/";

	@Test
	public void testReadParentTestExample() {
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
	public void testReadTestExample() {
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
				"testExample_MockingBird0.executeCmd(int7);\n";
		Assert.assertEquals(result, code);
	}
	
	@Test
	public void testReadInheritanceExample() {
		Properties.PROJECT_PREFIX = "de.unisb.cs.st.evosuite.junit";
		JUnitTestReader reader = new JUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(InheritanceExample.class.getName() + "#testInheritance");
		testCase.clone();
		String code = testCase.toCode();
		String result = "int int0 = 0;\n" + //
				"int int1 = 5;\n" + //
				"int int2 = 7;\n" + //
				"int int3 = 10;\n" + //
				"int int4 = 4;\n" + //
				"int int5 = 42;\n" + //
				"int int6 = -5;\n" + //
				"int int7 = 11;\n" + //
				"int int8 = 42;\n" + //
				"int int9 = 5;\n" + //
				"int int10 = 47;\n" + //
				"String string0 = null;\n" + //
				"String string1 = \"break free!\";\n" + //
				"int int11 = 38;\n" + //
				"int int12 = 5;\n" + //
				"int int13 = 5;\n" + //
				"TestExample.doCalc(int8, int12);\n" + //
				"int int14 = 3;\n" + //
				"String string2 = \"convert\";\n" + //
				"String string3 = \"killSelf\";\n" + //
				"String string4 = \"killSelf\";\n" + //
				"String string5 = \"me\";\n" + //
				"String string6 = string4 + string5;\n" + //
				"TestExample.MockingBird testExample_MockingBird0 = new TestExample.MockingBird(string6);\n" + //
				"int int15 = int14 - int11;\n" + //
				"testExample_MockingBird0.executeCmd(int15);\n"; 
		Assert.assertEquals(result, code);
	}
}
