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
package com.examples.with.different.packagename.stable;

public class FinalSingletonArrayUser {

    final static byte[] CONSTANT_ARRAY;

    static int counter = 0;

    static {
        CONSTANT_ARRAY = "\r\n".getBytes();
        counter++;
    }

    private final byte[] myArray;

    public FinalSingletonArrayUser() {
        myArray = new byte[2];
        System.arraycopy(CONSTANT_ARRAY, 0, myArray, 0, CONSTANT_ARRAY.length);
    }

    public boolean isEqualToFirst(byte myFloat) {
        if (myFloat == CONSTANT_ARRAY[0])
            return true;
        else
            return false;
    }

    public boolean isEqualToSecond(byte myFloat) {
        if (myFloat == CONSTANT_ARRAY[1])
            return true;
        else
            return false;
    }

    public boolean isFirstZero() {
        if (CONSTANT_ARRAY[0] == (byte) 0)
            return true;
        else
            return false;
    }

    public boolean isSecondZero() {
        if (CONSTANT_ARRAY[1] == (byte) 0)
            return true;
        else
            return false;
    }

    public static void clear() {
        CONSTANT_ARRAY[0] = -1;
        CONSTANT_ARRAY[1] = -1;
    }

}
