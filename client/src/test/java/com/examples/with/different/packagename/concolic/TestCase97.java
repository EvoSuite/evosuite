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

import java.io.IOException;
import java.io.StringReader;

public class TestCase97 {

    public static void test(String string0) {
        StringReader reader = new StringReader(string0);
        try {
            int int0 = reader.read();
            int int2 = "Togliere sta roba".charAt(0);
            Assertions.checkEquals(int0, int2);

            int int1 = reader.read();
            int int3 = "Togliere sta roba".charAt(1);
            Assertions.checkEquals(int1, int3);

            // consume remaining reader
            int int4 = reader.read();
            while (int4 != -1) {
                int4 = reader.read();
            }

        } catch (IOException e) {

        }

    }
}
