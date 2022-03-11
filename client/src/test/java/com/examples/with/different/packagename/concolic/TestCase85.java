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

public class TestCase85 {

    /**
     * @param args
     */
    // String string0 = ConcolicMarker.mark("aaaaaaaaaaab", "string0");
    public static void test(String string0) {

        String regex = "a*b";
        boolean boolean0 = string0.matches(regex);
        checkEquals(boolean0, true);
    }

}
