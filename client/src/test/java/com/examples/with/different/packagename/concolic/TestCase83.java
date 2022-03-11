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

import java.util.regex.Pattern;

import static com.examples.with.different.packagename.concolic.Assertions.checkEquals;

public class TestCase83 {

    /**
     * @param args
     */
    // String string0 = ConcolicMarker.mark("aaaaaab", "string0");
    // String string1 = ConcolicMarker.mark("bbbb", "string1");
    // int catchCount = ConcolicMarker.mark(0, "catchCount");
    public static void test(String string0, String string1, int catchCount) {
        String regex = "a*b";
        boolean boolean0 = Pattern.matches(regex, string0);
        checkEquals(boolean0, true);

        boolean boolean1 = Pattern.matches(regex, string1);
        checkEquals(boolean1, false);

        StringBuffer stringBuffer0 = new StringBuffer("aaaaaab");
        boolean boolean2 = Pattern.matches(regex, stringBuffer0);
        checkEquals(boolean2, true);

        try {
            boolean boolean3 = Pattern.matches(regex, null);
            checkEquals(boolean3, false);
        } catch (NullPointerException ex) {
            catchCount++;
        }

        try {
            boolean boolean3 = Pattern.matches(null, string0);
            checkEquals(boolean3, false);
        } catch (NullPointerException ex) {
            catchCount++;
        }

        try {
            boolean boolean3 = Pattern.matches(null, null);
            checkEquals(boolean3, false);
        } catch (NullPointerException ex) {
            catchCount++;
        }
        checkEquals(catchCount, 3);
    }
}
