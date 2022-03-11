/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package com.examples.with.different.packagename.localsearch;

public class ExampleHardForGA {

    /**
     * Solution a==600, b==54000
     *
     * @param a
     * @param b
     * @return
     */
    public boolean coverMe(int a, int b) {
        if (a < 0 || b < 0) {
            return false;
        }
        if (a == 0 || b == 0) {
            return false;
        }
        final int c1 = 2 * 3 * 5;
        final int c2 = 4 * 27 * 25;
        if (a == c1 || a == c2) {
            return false;
        }
        if (b == c1 || b == c2) {
            return false;
        }

        if ((a % c1) != 0)
            return false;
        if ((b % c2) != 0)
            return false;
        final int c3 = a / c1;
        final int c4 = b / c2;
        if (c3 == c4)
            return true;

        return false;
    }

//	public static void main(String[] args) {
//		IntegerDSE dse = new IntegerDSE();
//		dse.coverMe(600, 54000);
//	}
}
