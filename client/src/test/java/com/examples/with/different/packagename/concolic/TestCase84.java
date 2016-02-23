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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.examples.with.different.packagename.concolic.Assertions.checkEquals;

public class TestCase84 {

	/**
	 * @param args
	 */
	// String string0 = ConcolicMarker.mark("aaaaaaaaaaab", "string0");
	public static void test(String string0) {

		String regex = "a*b";

		Pattern pattern0 = Pattern.compile(regex);
		Matcher matcher0 = pattern0.matcher(string0);
		boolean boolean0 = matcher0.matches();

		checkEquals(boolean0, true);
	}

}
