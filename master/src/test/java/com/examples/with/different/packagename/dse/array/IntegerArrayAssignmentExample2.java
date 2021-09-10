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
package com.examples.with.different.packagename.dse.array;

public class IntegerArrayAssignmentExample2 {

    public static int test1(int b, int[] array, int c) {
        int[] arr = new int[3];
        arr[2] = array[3];

        if (arr[2] > 2) {
            array[2] = b;

            if (array[2] == 0) {
                return -1;
            } else if (array[c] > 6) {
                return 0;
            } else {
                return -2;
            }
        } else {
            return 2;
        }
    }

}
