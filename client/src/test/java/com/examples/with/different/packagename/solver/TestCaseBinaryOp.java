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

public class TestCaseBinaryOp {

	public static boolean testAdd(int x, int y) {
		int z = x + y;
		if (x == z) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean testSub(int x, int y) {
		int z = y - 10;
		if (x == z) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean testMul(int x, int y) {
		if (x != 0 && y == x * 2) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean testMul2(int x, int y) {
		if (10 == x * y) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean testDiv(int x, int y) {
		if (x == y / 5) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean testMod(int x, int y) {
		int z = x % y;
		if (x == z) {
			return true;
		} else {
			return false;
		}
	}

}
