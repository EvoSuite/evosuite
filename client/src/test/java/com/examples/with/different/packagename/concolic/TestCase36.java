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
package com.examples.with.different.packagename.concolic;

import static com.examples.with.different.packagename.concolic.Assertions.checkEquals;

public class TestCase36 {

	public static final String STRING_VALUE = "Togliere sta roba";

	private Object objectField;

	public static void test(String string0) {

		String string1 = STRING_VALUE;
		TestCase36 testCase35 = new TestCase36();
		testCase35.objectField = new StringBuffer("ere");
		CharSequence charSequence0 = (CharSequence) testCase35.objectField;
		Object object1 = "q";
		CharSequence charSequence1 = (CharSequence) object1;

		{
			boolean boolean0 = string0.equals(testCase35.objectField);
			boolean boolean1 = string1.equals(testCase35.objectField);
			checkEquals(boolean0, boolean1);
		}

		{
			boolean boolean0 = string0.contains(charSequence0);
			boolean boolean1 = string1.contains(charSequence0);
			checkEquals(boolean0, boolean1);
		}

		{
			String string2 = string0.replace(charSequence0, charSequence1);
			String string3 = string1.replace(charSequence0, charSequence1);
			int int0 = string2.length();
			int int1 = string3.length();
			checkEquals(int0, int1);
		}
	}
}
