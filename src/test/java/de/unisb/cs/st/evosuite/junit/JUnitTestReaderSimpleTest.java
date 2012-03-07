package de.unisb.cs.st.evosuite.junit;

import org.junit.Assert;
import org.junit.Test;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.testcase.TestCase;

public class JUnitTestReaderSimpleTest {

	private static final String SRCDIR = "src/test/java/";

	@Test
	public void testReadNonexistingJUnitTestCase() {
		try {
			Properties.PROJECT_PREFIX = "de.unisb.cs.st.evosuite.junit";
			JUnitTestReader reader = new JUnitTestReader(null, new String[] { SRCDIR });
			TestCase testCase = reader.readJUnitTestCase(SimpleTestExample.class.getName() + "#nonexistent");
			Assert.fail("Expected exception on nonexisting test.");
		} catch (RuntimeException exc) {
			// Expecting exception stating that test was not found
		}
	}

	@Test
	public void testReadSimpleJUnitTestCase01() {
		Properties.PROJECT_PREFIX = "de.unisb.cs.st.evosuite.junit";
		JUnitTestReader reader = new JUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(SimpleTestExample.class.getName() + "#test01");
		testCase.clone();
		String code = testCase.toCode();
		String result = "String string0 = \"killSelf\";\n" + //
				"TestExample.MockingBird testExample_MockingBird0 = new TestExample.MockingBird(string0);\n" + //
				"int int0 = 10;\n" + //
				"testExample_MockingBird0.executeCmd(int0);\n";
		Assert.assertEquals(result, code);
	}

	@Test
	public void testReadSimpleJUnitTestCase02() {
		Properties.PROJECT_PREFIX = "de.unisb.cs.st.evosuite.junit";
		JUnitTestReader reader = new JUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(SimpleTestExample.class.getName() + "#test02");
		testCase.clone();
		String code = testCase.toCode();
		String result = "String string0 = \"killSelf\";\n" + //
				"TestExample.MockingBird testExample_MockingBird0 = MockingBird.create(string0);\n" + //
				"int int0 = 10;\n" + //
				"testExample_MockingBird0.executeCmd(int0);\n";
		Assert.assertEquals(result, code);
	}

	@Test
	public void testReadSimpleJUnitTestCase03() {
		Properties.PROJECT_PREFIX = "de.unisb.cs.st.evosuite.junit";
		JUnitTestReader reader = new JUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(SimpleTestExample.class.getName() + "#test03");
		testCase = testCase.clone();
		String code = testCase.toCode();
		String result = "String string0 = \"dd.MMM.yyyy\";\n" + //
				"Locale locale0 = Locale.FRENCH;\n" + //
				"SimpleDateFormat simpleDateFormat0 = new SimpleDateFormat(string0, locale0);\n" + //
				"long long0 = System.currentTimeMillis();\n" + //
				"String string1 = simpleDateFormat0.format(long0);\n" + //
				"PrintStream printStream0 = System.out;\n" + //
				"printStream0.println(string1);\n" + //
				"String string2 = \"11.sept..2007\";\n" + //
				"PrintStream printStream1 = System.out;\n" + //
				"printStream1.println(string2);\n" + //
				"simpleDateFormat0.parse(string2);\n";
		Assert.assertEquals(result, code);
	}

	@Test
	public void testReadSimpleJUnitTestCase04() {
		Properties.PROJECT_PREFIX = "de.unisb.cs.st.evosuite.junit";
		JUnitTestReader reader = new JUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(SimpleTestExample.class.getName() + "#test04");
		testCase = testCase.clone();
		String code = testCase.toCode();
		String result = "String string0 = \"killSelf\";\n" + //
				"String string1 = new String(string0);\n" + //
				"TestExample.MockingBird testExample_MockingBird0 = new TestExample.MockingBird(string1);\n" + //
				"String string2 = \"You\";\n" + //
				"String string3 = new String(string2);\n" + //
				"TestExample.MockingBird testExample_MockingBird1 = testExample_MockingBird0.doIt(string3);\n" + //
				"String string4 = \"Me\";\n" + //
				"TestExample.MockingBird testExample_MockingBird2 = testExample_MockingBird1.doIt(string4);\n" + //
				"String string5 = \"Them\";\n" + //
				"TestExample.MockingBird testExample_MockingBird3 = testExample_MockingBird2.doIt(string5);\n" + //
				"String string6 = \"Everybody!\";\n" + //
				"testExample_MockingBird3.doIt(string6);\n";
		Assert.assertEquals(result, code);
	}

	@Test
	public void testReadSimpleJUnitTestCase05() {
		Properties.PROJECT_PREFIX = "de.unisb.cs.st.evosuite.junit";
		JUnitTestReader reader = new JUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(SimpleTestExample.class.getName() + "#test05");
		testCase.clone();
		String code = testCase.toCode();
		String result = "String string0 = \"killSelf\";\n" + //
				"String string1 = \"flyAway\";\n" + //
				"TestExample.MockingBird testExample_MockingBird0 = MockingBird.create(string1);\n" + //
				"int int0 = 10;\n" + //
				"testExample_MockingBird0.executeCmd(int0);\n";
		Assert.assertEquals(result, code);
	}
}
