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

public class TestCase75 {

	/**
	 * @param args
	 */
	// String string0 = ConcolicMarker.mark("Togliere ", "string0");
	// String string1 = ConcolicMarker.mark("sta ", "string1");
	// String string2 = ConcolicMarker.mark("roba", "string2");
	public static void test(String string0, String string1, String string2) {

		String string3 = "Togliere sta roba";

		StringBuffer charSequence = new StringBuffer(string0);
		StringBuilder sb = new StringBuilder(charSequence);

		String string4 = sb.toString();
		boolean boolean0 = string4.equals(string3);
		checkEquals(boolean0, false);

		String string5 = sb.append(string1).toString();
		boolean boolean1 = string5.equals(string3);
		checkEquals(boolean1, false);

		String string6 = sb.append(string2).toString();
		boolean boolean2 = string6.equals(string3);
		checkEquals(boolean2, true);
	}

}
