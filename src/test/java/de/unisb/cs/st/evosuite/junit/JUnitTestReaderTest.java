package de.unisb.cs.st.evosuite.junit;

import junit.framework.Assert;

import org.junit.Test;

import de.unisb.cs.st.evosuite.testcase.TestCase;

public class JUnitTestReaderTest {

	private static final String SRCDIR = "src/test/java/";

	@Test
	public void testReadComplexJUnitTestCase01() {
		JUnitTestReader reader = new ComplexJUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(SimpleTestExample01.class.getName() + "#test");
		testCase.clone();
		String code = testCase.toCode();
		String result = "String var0 = \"killSelf\";\n" + //
				"TestExample.MockingBird bird = new TestExample.MockingBird(var0);\n" + //
				"int var2 = 10;\n" + //
				"TestExample.MockingBird var3 = (TestExample.MockingBird)bird.executeCmd(var2);\n";
		Assert.assertEquals(result, code);
	}

	@Test
	public void testReadComplexJUnitTestCase02() {
		JUnitTestReader reader = new ComplexJUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(SimpleTestExample02.class.getName() + "#test");
		testCase.clone();
		String code = testCase.toCode();
		String result = "String var0 = \"killSelf\";\n" + //
				"TestExample.MockingBird bird = MockingBird.create(var0);\n" + //
				"int var2 = 10;\n" + //
				"TestExample.MockingBird var3 = (TestExample.MockingBird)bird.executeCmd(var2);\n";
		Assert.assertEquals(result, code);
	}
}
