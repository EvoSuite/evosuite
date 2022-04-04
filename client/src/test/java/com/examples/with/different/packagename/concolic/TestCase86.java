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

import org.junit.Test;

public class TestCase86 {

    @Test
    public void runTest() {
        int int0 = Integer.MAX_VALUE;
        test(int0);
    }

    public static void test(int int0) {
        int[] intArray0 = new int[10];
        intArray0[0] = int0;
        int[] intArray1 = intArray0.clone();
        int int1 = intArray1[0];
        Assertions.checkEquals(int0, int1);
        int int2 = intArray0.length;
        int int3 = intArray1.length;
        Assertions.checkEquals(int2, int3);

        TestCase86[] objectArray0 = new TestCase86[10];
        TestCase86 instance = new TestCase86();
        objectArray0[0] = instance;
        TestCase86[] objectArray1 = objectArray0.clone();
        int int4 = objectArray0.length;
        int int5 = objectArray1.length;
        Assertions.checkEquals(int4, int5);
    }
}
