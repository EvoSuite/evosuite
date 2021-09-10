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


public class TestCase68 {

    public static void test(String string1) {
        String string0 = "Togliere sta roba";

        int catchCount = 0;

        try {
            string1.contains(null);
        } catch (NullPointerException ex) {
            catchCount++;
        }
        checkEquals(1, catchCount);

        boolean boolean0 = string1.contains(new StringBuffer().toString());

        checkEquals(true, boolean0);

        boolean boolean1 = string1.contains("sta");
        boolean boolean2 = string0.contains("sta");

        checkEquals(boolean1, boolean2);

        boolean boolean3 = string1.contains(string1);
        boolean boolean4 = string0.contains(string1);

        checkEquals(boolean4, boolean3);

    }
}
