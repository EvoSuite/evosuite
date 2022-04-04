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

public class StringArrayAssignmentExample {

    public static int test1(String b, String[] array) {
        String a = array[0];
        String[] arr = new String[3];
        arr[2] = array[3];

        if (arr[2].length() > 2) {
            array[0] = b;

            if (array[2].contains("aaasdd")) {
                return -1;
            } else {
                return 0;
            }
        } else {
            return 2;
        }
    }

}
