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
				"Date date0 = simpleDateFormat0.parse(string2);\n" + //
				"PrintStream printStream2 = System.out;\n" + //
				"printStream2.println(date0);\n";
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

	@Test
	public void testReadSimpleJUnitTestCase06() {
		Properties.PROJECT_PREFIX = "de.unisb.cs.st.evosuite.junit";
		JUnitTestReader reader = new JUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(SimpleTestExample.class.getName() + "#test06");
		testCase.clone();
		String code = testCase.toCode();
		String result = "int int0 = 2;\n" + //
				"int int1 = 5;\n" + //
				"int[][] intArray0 = new int[2][5];\n" + //
				"int int2 = 0;\n" + //
				"int int3 = 0;\n" + //
				"int int4 = int2 * int3;\n" + //
				"intArray0[0][0] = int4;\n" + //
				"int int5 = 0;\n" + //
				"int int6 = 1;\n" + //
				"int int7 = int5 * int6;\n" + //
				"intArray0[0][1] = int7;\n" + //
				"int int8 = 0;\n" + //
				"int int9 = 2;\n" + //
				"int int10 = int8 * int9;\n" + //
				"intArray0[0][2] = int10;\n" + //
				"int int11 = 0;\n" + //
				"int int12 = 3;\n" + //
				"int int13 = int11 * int12;\n" + //
				"intArray0[0][3] = int13;\n" + //
				"int int14 = 0;\n" + //
				"int int15 = 4;\n" + //
				"int int16 = int14 * int15;\n" + //
				"intArray0[0][4] = int16;\n" + //
				"int int17 = 1;\n" + //
				"int int18 = 0;\n" + //
				"int int19 = int17 * int18;\n" + //
				"intArray0[1][0] = int19;\n" + //
				"int int20 = 1;\n" + //
				"int int21 = 1;\n" + //
				"int int22 = int20 * int21;\n" + //
				"intArray0[1][1] = int22;\n" + //
				"int int23 = 1;\n" + //
				"int int24 = 2;\n" + //
				"int int25 = int23 * int24;\n" + //
				"intArray0[1][2] = int25;\n" + //
				"int int26 = 1;\n" + //
				"int int27 = 3;\n" + //
				"int int28 = int26 * int27;\n" + //
				"intArray0[1][3] = int28;\n" + //
				"int int29 = 1;\n" + //
				"int int30 = 4;\n" + //
				"int int31 = int29 * int30;\n" + //
				"intArray0[1][4] = int31;\n";
		Assert.assertEquals(result, code);
	}

	@Test
	public void testReadSimpleJUnitTestCase07() {
		Properties.PROJECT_PREFIX = "de.unisb.cs.st.evosuite.junit";
		JUnitTestReader reader = new JUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(SimpleTestExample.class.getName() + "#test07");
		testCase.clone();
		String code = testCase.toCode();
		String result = "int int0 = 5;\n" + //
				"int int1 = 5;\n" + //
				"int int2 = 0;\n" + //
				"BufferedImage bufferedImage0 = TestExample.createImage(int0, int1, int2);\n" + //
				"int[][] intArray0 = new int[5][5];\n" + //
				"bufferedImage0.getWidth();\n" + //
				"bufferedImage0.getHeight();\n" + //
				"intArray0[0][0] = intArray0[1][4];\n" + //
				"int int3 = 4;\n" + //
				"int int4 = 5;\n" + //
				"int int5 = TestExample.doCalc(int3, int4);\n" + //
				"int int6 = 3;\n" + //
				"int int7 = int6 + int5;\n" + //
				"intArray0[0][1] = int7;\n";
		Assert.assertEquals(result, code);
	}

	@Test
	public void testReadSimpleJUnitTestCase08() {
		Properties.PROJECT_PREFIX = "de.unisb.cs.st.evosuite.junit";
		JUnitTestReader reader = new JUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(SimpleTestExample.class.getName() + "#test08");
		testCase.clone();
		String code = testCase.toCode();
		String result = "TestExample.sysoutArray();\n" + //
				"String[] stringArray0 = new String[]{\"Test\"};\n" + //
				"TestExample.sysoutArray(stringArray0);\n" + //
				"String[] stringArray1 = new String[]{\"This \", \"is \", \"a \", \"Test\", \"!\"};\n" + //
				"TestExample.sysoutArray(stringArray1);\n";
		Assert.assertEquals(result, code);
	}
}
