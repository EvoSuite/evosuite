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
package com.examples.with.different.packagename.ncs;

public class Triangle {

    public enum TriangleType {
        INVALID, SCALENE, EQUILATERAL, ISOSCELES
    }

    public static TriangleType exe(int a, int b, int c) {
        int trian;
        if (a <= 0 || b <= 0 || c <= 0)
            return TriangleType.INVALID;
        trian = 0;
        if (a == b)
            trian = trian + 1;
        if (a == c)
            trian = trian + 2;
        if (b == c)
            trian = trian + 3;

        // if (trian == 0) {
        // if (a + b < c || a + c < b || b + c < a) {
        // return TriangleType.INVALID;
        // } else {
        // return TriangleType.SCALENE;
        // }
        // }
        //
        // if (trian > 3) {
        // return TriangleType.EQUILATERAL;
        // }

        if (trian == 1 && a + b > c) {
            return TriangleType.ISOSCELES;
        } else if (trian == 2 && a + c > b) {
            return TriangleType.ISOSCELES;
        } else if (trian == 3 && b + c > a) {
            return TriangleType.ISOSCELES;
        }

        return TriangleType.INVALID;
    }

}