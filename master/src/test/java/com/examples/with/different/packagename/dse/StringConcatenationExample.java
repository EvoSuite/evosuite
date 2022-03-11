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
package com.examples.with.different.packagename.dse;

/**
 * Simple String concatenation test
 *
 * @author Ignacio Lebrero
 */
public class StringConcatenationExample {

    private static final int H_SIZE = 7;
    private static final int W_SIZE = 11;

    public static int test(int a) throws IllegalArgumentException {
        String string1 = "test string 1... " + a + "," + H_SIZE + "\n";
        String string2 = "test string 2: " + a + "\n" + string1;
        System.out.print(string2);

        if (string2.equals("test string 2: 65\ntest string 1... 65,7\n")) {
            System.out.print("Correct value! -> " + a + "\n");
            return 1;
        } else {
            System.out.print("Not correct value -> " + a + "\n");
            return 0;
        }
    }

}