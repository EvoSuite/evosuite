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

import java.util.regex.PatternSyntaxException;

import static com.examples.with.different.packagename.concolic.Assertions.checkEquals;

public class TestCase67 {

	public static void test(String string1) {
		String string0 = "Togliere sta roba";

		int catchCount = 0;

		try {
			string1.regionMatches(true, 0, null, 0, 0);
		} catch (NullPointerException ex) {
			catchCount++;
		}

		try {
			string1.regionMatches(true, -1, "sto", 0, 0);
		} catch (StringIndexOutOfBoundsException ex) {
			catchCount++;
		}

		try {
			string1.regionMatches(true, 0, "sto", -1, 0);
		} catch (StringIndexOutOfBoundsException ex) {
			catchCount++;
		}

		try {
			string1.regionMatches(true, 0, "sto", 0, -1);
		} catch (StringIndexOutOfBoundsException ex) {
			catchCount++;
		}

		try {
			string1.regionMatches(true, 0, "sto", 0, Integer.MAX_VALUE);
		} catch (StringIndexOutOfBoundsException ex) {
			catchCount++;
		}

		checkEquals(1, catchCount);

		boolean boolean0 = string1.regionMatches(true, 0, "sto", 0,
				Integer.MAX_VALUE);
		boolean boolean1 = string0.regionMatches(true, 0, "sto", 0,
				Integer.MAX_VALUE);

		checkEquals(boolean1, boolean0);

	}
}
