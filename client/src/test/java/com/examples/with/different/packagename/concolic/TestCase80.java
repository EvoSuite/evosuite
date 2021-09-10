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

public class TestCase80 {

    /**
     * @param args
     */
    // float float0 = ConcolicMarker.mark(1.5f, "float0");
    public static void test(float float0) {
        // box integer
        Float float_instance0 = box(float0);
        // unbox integer
        float float1 = unbox(float_instance0);
        float float2 = 1.5f;
        checkEquals(float1, float2);
    }

    public static Float box(Float i) {
        return i;
    }

    public static float unbox(float i) {
        return i;
    }

}
