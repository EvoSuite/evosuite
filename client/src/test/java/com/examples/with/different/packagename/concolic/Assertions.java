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

public class Assertions {

    public static void checkEquals(long l, long r) {
        if (l != r) {
            throw new RuntimeException("check failed!");
        }
    }

    public static void checkEquals(float l, float r) {
        if (l != r) {
            throw new RuntimeException("check failed!");
        }
    }

    public static void checkEquals(int l, int r) {
        if (l != r) {
            throw new RuntimeException("check failed!");
        }
    }

    public static void checkEquals(byte l, byte r) {
        if (l != r) {
            throw new RuntimeException("check failed!");
        }
    }

    public static void checkEquals(double l, double r) {
        if (l != r) {
            throw new RuntimeException("check failed!");
        }
    }

    public static void checkEquals(boolean l, boolean r) {
        if (l != r) {
            throw new RuntimeException("check failed!");
        }
    }

    public static void checkNotEquals(boolean l, boolean r) {
        if (l == r) {
            throw new RuntimeException("check failed!");
        }
    }

    public static void checkEquals(char l, char r) {
        if (l != r) {
            throw new RuntimeException("check failed!");
        }
    }

    public static void checkObjectEquals(Object l, Object r) {
        if (l != r) {
            throw new RuntimeException("check failed!");
        }
    }

    public static void checkEquals(short l, short r) {
        if (l != r) {
            throw new RuntimeException("check failed!");
        }
    }
}
