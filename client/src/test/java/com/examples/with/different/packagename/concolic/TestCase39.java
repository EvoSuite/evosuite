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

public class TestCase39 {

	private static final String TOGLIERE = "Togliere";
	private static final String STA = "sta";
	public static final String ROBA = "roba";

	/**
	 * @param args
	 */
	public static void test(int int0, String string2) {
		String[] stringArray0 = new String[int0];
		String string0 = TOGLIERE;
		String string1 = STA;
		stringArray0[5] = string0;
		stringArray0[6] = string1;
		stringArray0[7] = string2;

		boolean boolean0 = stringArray0[7].equalsIgnoreCase(ROBA.toUpperCase());
		boolean boolean1 = ROBA.equalsIgnoreCase(ROBA.toUpperCase());

		checkEquals(boolean0, boolean1);

		stringArray0[5] = string2.concat(string2);

		boolean boolean2 = stringArray0[5].equals("robaroba");
		boolean boolean3 = (ROBA.concat(ROBA)).equals("robaroba");

		checkEquals(boolean2, boolean3);

	}
}
