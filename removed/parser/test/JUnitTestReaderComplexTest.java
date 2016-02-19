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
package org.evosuite.junit;

import junit.framework.Assert;

import org.evosuite.Properties;
import org.evosuite.testcase.TestCase;
import org.junit.Ignore;
import org.junit.Test;

import com.examples.with.different.packagename.testreader.InheritanceExample;
import com.examples.with.different.packagename.testreader.ParentTestExample;
import com.examples.with.different.packagename.testreader.TestExample;

public class JUnitTestReaderComplexTest {
	private static final String SRCDIR = "src/test/java/";

	@Ignore
	@Test
	public void testReadParentTestExample() {
		Properties.PROJECT_PREFIX = "org.evosuite.junit";
		JUnitTestReader reader = new JUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(ParentTestExample.class.getName()
		        + "#test01");
		testCase.clone();
		String code = testCase.toCode();
		String result = "int int0 = 0;\n"
		        + //
		        "int int1 = 5;\n"
		        + //
		        "int int2 = Integer.MAX_VALUE;\n"
		        + //
		        "int int3 = 7;\n"
		        + //
		        "String string0 = null;\n"
		        + //
		        "String string1 = \"break free!\";\n"
		        + //
		        "int int4 = 3;\n"
		        + //
		        "String string2 = \"escape\";\n"
		        + //
		        "TestExample.MockingBird testExample_MockingBird0 = MockingBird.create(string2);\n"
		        + //
		        "testExample_MockingBird0.executeCmd(int4);\n";
		Assert.assertEquals(result, code);
	}

	@Ignore
	@Test
	public void testReadTestExample() {
		Properties.PROJECT_PREFIX = "org.evosuite.junit";
		JUnitTestReader reader = new JUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(TestExample.class.getName()
		        + "#test01");
		testCase.clone();
		String code = testCase.toCode();
		String result = "int int0 = 0;\n"
		        + //
		        "int int1 = 5;\n"
		        + //
		        "int int2 = 7;\n"
		        + //
		        "int int3 = 10;\n"
		        + //
		        "int int4 = 4;\n"
		        + //
		        "int int5 = 42;\n"
		        + //
		        "int int6 = -5;\n"
		        + //
		        "String string0 = null;\n"
		        + //
		        "String string1 = \"break free!\";\n"
		        + //
		        "int int7 = 38;\n"
		        + //
		        "int int8 = 3;\n"
		        + //
		        "String string2 = \"convert\";\n"
		        + //
		        "String string3 = \"killSelf\";\n"
		        + //
		        "TestExample.MockingBird testExample_MockingBird0 = new TestExample.MockingBird(string3);\n"
		        + //
		        "testExample_MockingBird0.executeCmd(int7);\n";
		Assert.assertEquals(result, code);
	}

	@Ignore
	@Test
	public void testReadInheritanceExample() {
		Properties.PROJECT_PREFIX = "org.evosuite.junit";
		JUnitTestReader reader = new JUnitTestReader(null, new String[] { SRCDIR });
		TestCase testCase = reader.readJUnitTestCase(InheritanceExample.class.getName()
		        + "#testInheritance");
		testCase.clone();
		String code = testCase.toCode();
		String result = "int int0 = 0;\n"
		        + //
		        "int int1 = 5;\n"
		        + //
		        "int int2 = 7;\n"
		        + //
		        "int int3 = 10;\n"
		        + //
		        "int int4 = 4;\n"
		        + //
		        "int int5 = 42;\n"
		        + //
		        "int int6 = -5;\n"
		        + //
		        "int int7 = 11;\n"
		        + //
		        "int int8 = 42;\n"
		        + //
		        "int int9 = 5;\n"
		        + //
		        "int int10 = 47;\n"
		        + //
		        "String string0 = null;\n"
		        + //
		        "String string1 = \"break free!\";\n"
		        + //
		        "int int11 = 38;\n"
		        + //
		        "int int12 = 5;\n"
		        + //
		        "int int13 = 5;\n"
		        + //
		        "TestExample.doCalc(int8, int12);\n"
		        + //
		        "int int14 = 3;\n"
		        + //
		        "String string2 = \"convert\";\n"
		        + //
		        "String string3 = \"killSelf\";\n"
		        + //
		        "String string4 = \"killSelf\";\n"
		        + //
		        "String string5 = \"me\";\n"
		        + //
		        "String string6 = string4 + string5;\n"
		        + //
		        "TestExample.MockingBird testExample_MockingBird0 = new TestExample.MockingBird(string6);\n"
		        + //
		        "int int15 = int14 - int11;\n" + //
		        "testExample_MockingBird0.executeCmd(int15);\n";
		Assert.assertEquals(result, code);
	}
}
