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

public class MathInt {

    public int divide(int a, int b) {
        return a / b;
    }

    public int remainder(int a, int b) {
        return a % b;
    }

    public int multiply(int a, int b) {
        return a * b;
    }

    public int sum(int a, int b) {
        return a + b;
    }

    public int substract(int a, int b) {
        return a - b;
    }

    private Object f = new Object();

    public void unreach() {
        if (f == null) {
            f = new Object();
        }
    }

    public int castToInt(int f) {
        return f;
    }

    public long castToLong(int f) {
        return f;
    }

    public char castToChar(int f) {
        return (char) f;
    }

    public short castToShort(int f) {
        return (short) f;
    }

    public short castToByte(int f) {
        return (byte) f;
    }

    public int shiftLeft(int a, int b) {
        return a << b;
    }

    public int shiftRight(int a, int b) {
        return a >> b;
    }

    public int unsignedShiftRight(int a, int b) {
        return a >>> b;
    }
}
