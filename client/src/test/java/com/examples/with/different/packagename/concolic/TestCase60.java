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

public class TestCase60 {

	/**
	 * @param args
	 */
	// String string1 = ConcolicMarker.mark("Togliere sta roba", "string1");

	public static void test(String string1) {

		String string0 = "Togliere sta roba";

		int catchCount = 0;
		try {
			// begin index too small
			string1.substring(-1, 10);
		} catch (StringIndexOutOfBoundsException ex) {
			catchCount++;
		}

		try {
			// begin index too big
			string1.substring(Integer.MAX_VALUE, 10);
		} catch (StringIndexOutOfBoundsException ex) {
			catchCount++;
		}

		try {
			// end index too small
			string1.substring(0, -1);
		} catch (StringIndexOutOfBoundsException ex) {
			catchCount++;
		}

		try {
			// end index too big
			string1.substring(0, Integer.MAX_VALUE);
		} catch (StringIndexOutOfBoundsException ex) {
			catchCount++;
		}

		try {
			// end index less than begin index
			string1.substring(5, 2);
		} catch (StringIndexOutOfBoundsException ex) {
			catchCount++;
		}

		checkEquals(catchCount, 5); // 1==5! handler code is not symbolically
									// executed?

		String string2 = string0.substring(5, 12);
		String string3 = string1.substring(5, 12);

		checkEquals(string2.length(), string3.length());

	}
}
