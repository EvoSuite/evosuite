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
package com.examples.with.different.packagename;

public class IF_CMP_Test {

    public void greaterEqual_IF_CMPLT(Integer val1, Integer val2) {
        if (val1 >= val2) {
            System.out.println(val1 + " >= " + val2);
            return;
        }
        System.out.println(val1 + " < " + val2);
    }

    public void greaterThan_IF_CMPLE(Integer val1, Integer val2) {
        if (val1 > val2) {
            System.out.println(val1 + " > " + val2);
            return;
        }
        System.out.println(val1 + " <= " + val2);
    }

    public void lesserEqual_IF_CMPGT(Integer val1, Integer val2) {
        if (val1 <= val2) {
            System.out.println(val1 + " <= " + val2);
            return;
        }
        System.out.println(val1 + " > " + val2);
    }

    public void lesserThan_IF_CMPGE(Integer val1, Integer val2) {
        if (val1 < val2) {
            System.out.println(val1 + " < " + val2);
            return;
        }
        System.out.println(val1 + " >= " + val2);
    }

}
