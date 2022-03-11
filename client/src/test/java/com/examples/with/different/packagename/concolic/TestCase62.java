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

import static com.examples.with.different.packagename.concolic.Assertions.checkEquals;

public class TestCase62 {

    //	String string0 = ConcolicMarker.mark("Togliere sta roba", "string0");
    public static void test(String string0) {
        String string1 = "Togliere sta roba";

        int catchCount = 0;

        try {
            string0.lastIndexOf(212, -1);
        } catch (StringIndexOutOfBoundsException ex) {
            catchCount++;
        }

        try {
            string0.lastIndexOf(212, Integer.MAX_VALUE);
        } catch (StringIndexOutOfBoundsException ex) {
            catchCount++;
        }

        try {
            string0.lastIndexOf(null);
        } catch (NullPointerException ex) {
            catchCount++;
        }

        try {
            String nullStringRef = null;
            string0.lastIndexOf(nullStringRef, 0);
        } catch (NullPointerException ex) {
            catchCount++;
        }

        checkEquals(2, catchCount);

        int int0 = string0.lastIndexOf('a');
        int int1 = string0.lastIndexOf('a', string0.length() - 1);
        int int2 = string0.lastIndexOf("a");
        int int3 = string0.lastIndexOf("a", string0.length() - 1);
        int int4 = string1.lastIndexOf("a");

        checkEquals(int0, int4);
        checkEquals(int1, int4);
        checkEquals(int2, int4);
        checkEquals(int3, int4);
    }
}
