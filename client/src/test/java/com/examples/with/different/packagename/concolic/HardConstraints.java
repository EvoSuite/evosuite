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
package com.examples.with.different.packagename.concolic;

public class HardConstraints {

    public static boolean test2(float float0) {
        // float0 == 3.1415998935699463f
        // 1415926536

        if (float0 >= 4) {
            return false;
        }

        if (float0 <= 3) {
            return false;
        }

        if (float0 * 100 < 314) {
            return false;
        }

        int int0 = (int) (float0 * 10000f) + 1;

        if (int0 % 8 != 0) {
            return false;
        }

        if (int0 % 3 != 0) {
            return false;
        }

        if (int0 % 7 != 0) {
            return false;
        }

        if (int0 % 187 != 0) {
            return false;
        }

        int int1 = (int) (float0 * 100000f) + 1;

        if (int1 == 314151) {
            return false;
        }

        return true;

    }

    public static boolean test1(int int1) {
        if ((int1 % 251) != 0) {
            return false;
        }

        if (int1 >= 26104) {
            return false;
        }

        if (int1 <= 13554) {
            return false;
        }

        if ((int1 % 4) != 0) {
            return false;
        }

        if ((int1 % 25) != 0) {
            return false;
        }

        return true;
    }

    public static boolean test0(int int0) {

        if (int0 <= 0) {
            return false;
        }

        if ((int0 % 523) != 0) {
            return false;
        }

        if ((int0 % 25) != 0) {
            return false;
        }

        if (int0 >= 26150) {
            return false;
        }

        return true;
    }

}
