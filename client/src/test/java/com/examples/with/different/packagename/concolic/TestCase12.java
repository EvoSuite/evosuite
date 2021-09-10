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


public class TestCase12 {

    /**
     * @param args
     */
    public static void test(long long0, long long1, long long3, long long5) {

        MathLong mathlong0 = new MathLong();
        long long2 = mathlong0.shiftLeft(long0, long1);
        if (long2 != long3) {
            mathlong0.castToChar(long3);
        }
        long long4 = mathlong0.shiftRight(long0, long1);
        if (long4 != long5) {
            mathlong0.castToChar(long3);
        }
        long long6 = mathlong0.unsignedShiftRight(long0, long1);
        if (long6 != long5) {
            mathlong0.castToChar(long3);
        }
    }

}
