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


public class TestCase90 {

    public static void test(char char0) {
        int int0 = Character.getNumericValue(char0);
        int int1 = Character.getNumericValue('a');

        Assertions.checkEquals(int0, int1);

        boolean boolean0 = Character.isDigit(char0);
        boolean boolean1 = Character.isDigit('a');

        Assertions.checkEquals(boolean0, boolean1);

        boolean boolean2 = Character.isLetter(char0);
        boolean boolean3 = Character.isLetter('a');

        Assertions.checkEquals(boolean2, boolean3);


    }

}
