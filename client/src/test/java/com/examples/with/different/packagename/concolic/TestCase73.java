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

public class TestCase73 {

	/**
	 * @param args
	 */
	// String string0 = ConcolicMarker.mark("enci", "string0");
	// String string1 = ConcolicMarker.mark("c", "string1");
	public static void test(String string0, String string1) {

		String result = "";
		result += string0.charAt(2);

		// StringBuilder sb = new StringBuilder(result);
		// sb.append(string0.charAt(2));
		// result = sb.toString();

		boolean boolean0 = result.equals(string1);
		checkEquals(boolean0, true);

	}

}
