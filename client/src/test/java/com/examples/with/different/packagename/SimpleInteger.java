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

public class SimpleInteger {

    public int testInt(int x, int y) {
        return x + y;
    }

    public short testShort(short x, short y) {
        return (short) (x + y);
    }

    public byte testByte(byte x, byte y) {
        return (byte) (x + y);
    }

    public long testLong(long x, long y) {
        return (x + y);
    }

    public float testFloat(float x, float y) {
        return (x + y);
    }

    public double testDouble(double x, double y) {
        return x + y;
    }

    public char testChar(char x, char y) {
        return (char) (x + y);
    }

    public int[] testIntArray(int x, int y) {
        return new int[]{x, y};
    }

}
