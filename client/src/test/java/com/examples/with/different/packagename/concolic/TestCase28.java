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

public class TestCase28 {

    public static final String STRING_VALUE = "Togliere sta roba";

    public static void test(String string0) {

        String string1 = STRING_VALUE;
        {
            boolean boolean0 = string0.equals(STRING_VALUE);
            boolean boolean1 = string1.equals(STRING_VALUE);
            checkEquals(boolean0, boolean1);
        }

        {
            boolean boolean0 = string0.equalsIgnoreCase(STRING_VALUE);
            boolean boolean1 = string1.equalsIgnoreCase(STRING_VALUE);
            checkEquals(boolean0, boolean1);
        }

        {
            boolean boolean0 = string0.endsWith(STRING_VALUE);
            boolean boolean1 = string1.endsWith(STRING_VALUE);
            checkEquals(boolean0, boolean1);
        }

        {
            boolean boolean0 = string0.startsWith(STRING_VALUE, 0);
            boolean boolean1 = string1.startsWith(STRING_VALUE, 0);
            checkEquals(boolean0, boolean1);
        }

        {
            boolean ignoresCase = true;
            int toffset = 10;
            String other = "STA";
            int ooffset = 0;
            int len = 9;
            boolean boolean0 = string0.regionMatches(ignoresCase, toffset,
                    other, ooffset, len);
            boolean boolean1 = string1.regionMatches(ignoresCase, toffset,
                    other, ooffset, len);
            checkEquals(boolean0, boolean1);
        }
    }
}
