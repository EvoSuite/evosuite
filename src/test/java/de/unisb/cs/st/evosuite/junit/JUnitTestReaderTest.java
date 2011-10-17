package de.unisb.cs.st.evosuite.junit;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import de.unisb.cs.st.evosuite.testcase.TestCase;

public class JUnitTestReaderTest {

	private static final String SRCDIR = "src/test/java/";

	@Ignore
	@Test
	public void testReadComplexJUnitTestCase01() {
		JUnitTestReader reader = new ComplexJUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(SimpleTestExample01.class.getName() + "#test");
		testCase.clone();
		String code = testCase.toCode();
		String result = "String var0 = \"killSelf\";\n" + //
				"TestExample.MockingBird bird = new TestExample.MockingBird(var0);\n" + //
				"int var2 = 10;\n" + //
				"bird.executeCmd(var2);\n";
		Assert.assertEquals(result, code);
	}

	@Ignore
	@Test
	public void testReadComplexJUnitTestCase02() {
		JUnitTestReader reader = new ComplexJUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(SimpleTestExample02.class.getName() + "#test");
		testCase.clone();
		String code = testCase.toCode();
		String result = "String var0 = \"killSelf\";\n" + //
				"TestExample.MockingBird bird = MockingBird.create(var0);\n" + //
				"int var2 = 10;\n" + //
				"bird.executeCmd(var2);\n";
		Assert.assertEquals(result, code);
	}

	@Ignore
	@Test
	public void testReadComplexJUnitTestCase03() {
		JUnitTestReader reader = new ComplexJUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(SimpleTestExample03.class.getName() + "#test");
		// TODO Implement correct cloning of BoundVariableReferences: testCase = 
			testCase.clone();
		String code = testCase.toCode();
		String result = "String var0 = \"dd.MMM.yyyy\";\n" + //
				"Locale var1 = Locale.FRENCH;\n" + //
				"SimpleDateFormat formatter = new SimpleDateFormat(var0, var1);\n" + //
				"long var3 = System.currentTimeMillis();\n" + //
				"String var4 = formatter.format((Object) var3);\n" + //
				"PrintStream var5 = System.out;\n" + //
				"var5.println((Object) var4);\n" + //
				"String var7 = \"11.sept..2007\";\n" + //
				"Date result = formatter.parse(var7);\n";
		Assert.assertEquals(result, code);
	}

}
