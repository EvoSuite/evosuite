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

public class TestCaseFloatTrigonometry {

	public static int test(double x) {
		int c = 0;

		double acos = Math.acos(x);
		double asin = Math.asin(x);
		double atan = Math.atan(x);
		double cos = Math.cos(x);
		double cosh = Math.cosh(x);
		double tan = Math.tan(x);
		if (acos > 0) {
			c++;
		}
		if (asin > 0) {
			c++;
		}
		if (atan > 0) {
			c++;
		}
		if (cos > 0) {
			c++;
		}
		if (cosh > 0) {
			c++;
		}
		if (tan > 0) {
			c++;
		}
		return c;
	}
}
