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


public class TestCase25 {

	public static final String STRING_VALUE = "Togliere sta roba";

	public static final String SUB_STRING_VALUE = " sta";

	public static final char SUB_CHAR_VALUE = 's';

	/**
	 * @param args
	 */
	public static void test(String string0) {

		String string1 = STRING_VALUE;
		{
			int int0 = string0.compareTo(string0);
			int int1 = string1.compareTo(string1);
			checkEquals(int0, int1);
		}
		{
			int int0 = string0.compareToIgnoreCase(string0);
			int int1 = string1.compareToIgnoreCase(string1);
			checkEquals(int0, int1);
		}
		{
			int int0 = string0.indexOf(SUB_STRING_VALUE);
			int int1 = string1.indexOf(SUB_STRING_VALUE);
			checkEquals(int0, int1);
		}
		{
			int int0 = string0.lastIndexOf(SUB_STRING_VALUE);
			int int1 = string1.lastIndexOf(SUB_STRING_VALUE);
			checkEquals(int0, int1);
		}
		{
			int int0 = string0.indexOf(SUB_CHAR_VALUE);
			int int1 = string1.indexOf(SUB_CHAR_VALUE);
			checkEquals(int0, int1);
		}
		{
			int int0 = string0.lastIndexOf(SUB_CHAR_VALUE);
			int int1 = string1.lastIndexOf(SUB_CHAR_VALUE);
			checkEquals(int0, int1);
		}
		{
			int int0 = string0.indexOf(SUB_STRING_VALUE);
			int int1 = string1.indexOf(SUB_STRING_VALUE);
			checkEquals(int0, int1);
		}
		{
			char char0 = string0.charAt(5);
			char char1 = string1.charAt(5);
			checkEquals(char0, char1);
		}
	}

}
