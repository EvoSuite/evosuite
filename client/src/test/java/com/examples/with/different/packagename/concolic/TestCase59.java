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

public class TestCase59 {

	/**
	 * @param args
	 */
//	String string1 = ConcolicMarker.mark(string0, "string1");
//	int int1 = ConcolicMarker.mark(5, "int1");
	public static void test(String string1, int int1) {

		String string0 = "Togliere sta roba";

		int int0 = 5;
		char char0 = string0.charAt(int0);
		char char1 = string1.charAt(int1);

		checkEquals(char0, char1);

		// negative index throws exception
		try {
			string1.charAt(-1);
		} catch (StringIndexOutOfBoundsException ex) {
			// index too small
		}

		// too big index throws exception
		try {
			string1.charAt(Integer.MAX_VALUE);
		} catch (StringIndexOutOfBoundsException ex) {
			// index too small
		}
		
		// check everything still works here
		char char2 = string0.charAt(int1);
		char char3 = string1.charAt(int0);

		checkEquals(char2, char3);

	}
}
