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

public class TestCaseRegex {

	public static boolean testConcat(String str) {
		if (str != null && str.matches("a*b")) {
			return true;
		}
		return false;
	}

	public static boolean testUnion(String str) {
		if (str != null && str.matches("a|b")) {
			return true;
		}
		return false;
	}

	public static boolean testOptional(String str) {
		if (str != null && str.matches("(a)?")) {
			return true;
		}
		return false;
	}

	public static boolean testString(String str) {
		if (str != null && str.matches("hello")) {
			return true;
		}
		return false;
	}

	public static boolean testAnyChar(String str) {
		if (str != null && str.matches(".")) {
			return true;
		}
		return false;
	}

	public static boolean testEmpty(String str) {
		if (str != null && str.matches("")) {
			return true;
		}
		return false;
	}

	public static boolean testCross(String str) {
		if (str != null && str.matches("a+")) {
			return true;
		}
		return false;
	}

	public static boolean testRepeatMin(String str) {
		if (str != null && str.matches("a{3,}")) {
			return true;
		}
		return false;
	}

	public static boolean testRepeatMinMax(String str) {
		if (str != null && str.matches("a{3,5}")) {
			return true;
		}
		return false;
	}
	
	public static boolean testRepeatN(String str) {
		if (str != null && str.matches("a{5}")) {
			return true;
		}
		return false;
	}
	
	public static boolean testIntersection(String str) {
		if (str != null && str.matches("[0-9&&[345]]")) {
			return true;
		}
		return false;
	}
	
	public static boolean testChoice(String str) {
		if (str != null && str.matches("[abc]")) {
			return true;
		}
		return false;
	}
	
	public static boolean testRange(String str) {
		if (str != null && str.matches("[a-z]")) {
			return true;
		}
		return false;
	}
}
