/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.junit;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.testcase.TestCase;

public class JUnitTestReaderSimpleTest {

	private static final String SRCDIR = "src/test/java/";

	@Ignore
	@Test
	public void testReadComplexJUnitTestCase01() {
		Properties.PROJECT_PREFIX = "de.unisb.cs.st.evosuite.junit";
		JUnitTestReader reader = new JUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(SimpleTestExample01.class.getName()
		        + "#test");
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
		Properties.PROJECT_PREFIX = "de.unisb.cs.st.evosuite.junit";
		JUnitTestReader reader = new JUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(SimpleTestExample02.class.getName()
		        + "#test");
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
		Properties.PROJECT_PREFIX = "de.unisb.cs.st.evosuite.junit";
		JUnitTestReader reader = new JUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(SimpleTestExample03.class.getName()
		        + "#test");
		// TODO Implement correct cloning of BoundVariableReferences: testCase =
		testCase.clone();
		String code = testCase.toCode();
		String result = "String var0 = \"dd.MMM.yyyy\";\n" + //
		        "Locale var1 = Locale.FRENCH;\n" + //
		        "SimpleDateFormat formatter = new SimpleDateFormat(var0, var1);\n" + //
		        "long var3 = System.currentTimeMillis();\n" + //
		        "String var4 = formatter.format((Object) var3);\n" + //
		        "PrintStream var5 = System.out;\n" + //
		        "var5.println(var4);\n" + //
		        "String var7 = \"11.sept..2007\";\n" + //
		        "PrintStream var8 = System.out;\n" + //
		        "var8.println(var7);\n" + //
		        "Date result = formatter.parse(var7);\n";
		Assert.assertEquals(result, code);
	}

	@Ignore
	@Test
	public void testReadComplexJUnitTestCase04() {
		Properties.PROJECT_PREFIX = "de.unisb.cs.st.evosuite.junit";
		JUnitTestReader reader = new JUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(SimpleTestExample04.class.getName()
		        + "#test");
		// TODO Implement correct cloning of BoundVariableReferences: testCase =
		// testCase.clone();
		String code = testCase.toCode();
		System.out.println(code);
		String result = "String var0 = \"killSelf\";\n" + //
		        "String var1 = new String(var0);\n" + //
		        "TestExample.MockingBird bird = new TestExample.MockingBird(var1);\n" + //
		        "String var3 = \"You\";\n" + //
		        "String var4 = new String(var3);\n" + //
		        "TestExample.MockingBird var5 = bird.doIt(var4);\n" + //
		        "String var6 = \"Me\";\n" + //
		        "TestExample.MockingBird var7 = var5.doIt(var6);\n" + //
		        "String var8 = \"Them\";\n" + //
		        "TestExample.MockingBird var9 = var7.doIt(var8);\n" + //
		        "String var10 = \"Everybody!\";\n" + //
		        "TestExample.MockingBird var11 = var9.doIt(var10);\n";
		Assert.assertEquals(result, code);
	}

	// TODO Create a test that test reusing a variable:
	// a = new Something();
	// a = new SomethingElse();
	// a.doSomething();
}
