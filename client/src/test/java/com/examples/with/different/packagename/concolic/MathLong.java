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

public class MathLong {

    public long divide(long a, long b) {
        return a / b;
    }

    public long remainder(long a, long b) {
        return a % b;
    }

    public long multiply(long a, long b) {
        return a * b;
    }

    public long sum(long a, long b) {
        return a + b;
    }

    public long substract(long a, long b) {
        return a - b;
    }

    private Long f = 3215155154115L;

    public void unreach() {
        if (f == null) {
            f = 3215155154115L;
        }
    }

    public int castToInt(long f) {
        return (int) f;
    }

    public long castToLong(long f) {
        return f;
    }

    public char castToChar(long f) {
        return (char) f;
    }

    public short castToShort(long f) {
        return (short) f;
    }

    public byte castToByte(long f) {
        return (byte) f;
    }

    public float castToFloat(long f) {
        return (float) f;
    }

    public long shiftLeft(long a, long b) {
        return a << b;
    }

    public long shiftRight(long a, long b) {
        return a >> b;
    }

    public long unsignedShiftRight(long a, long b) {
        return a >>> b;
    }
}
