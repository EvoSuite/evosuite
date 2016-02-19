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
package com.examples.with.different.packagename.solver;

public class TestCaseCastIntToString {
	
	public static boolean test(int int0) {
		String string0 = String.valueOf(int0);
		if (string0.equals("0")) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean test(long long0) {
		String string0 = String.valueOf(long0);
		if (string0.equals("0")) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean test(char char0) {
		String string0 = String.valueOf(char0);
		if (string0.equals("0")) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean test(boolean boolean0) {
		String string0 = String.valueOf(boolean0);
		if (string0.equals("false")) {
			return true;
		} else {
			return false;
		}
	}

}
