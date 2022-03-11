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

public class RealArrayAssignmentExample2 {

    public static int realAssignment(double b, double[] array) {
        double a = array[0];

        if (a > 2.3d) {
            array[0] = b;

            if (array[0] > 5.1d && array[0] < 5.8d) {
                if (array[0] == 5.6d) {
                    return 6;
                } else {
                    return 7;
                }
            }

            if (array[2] == 0.2d) {
                return -1;
            } else {
                return 0;
            }
        } else {
            return 2;
        }
    }

}
