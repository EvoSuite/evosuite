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

import java.math.BigInteger;

public class TestCase91 extends TestCase90 {

    /**
     * @param string0    .equals("135")
     * @param string1    .equals("20")
     * @param catchCount ==0
     */
    public static void test(String string0, String string1, int catchCount) {

        try {
            new BigInteger("Togliere sta roba");
        } catch (NumberFormatException ex) {
            catchCount++;
        }

        try {
            new BigInteger((String) null);
        } catch (NullPointerException ex) {
            catchCount++;
        }

        Assertions.checkEquals(2, catchCount);

        BigInteger bigInteger0 = new BigInteger(string0);
        BigInteger bigInteger1 = new BigInteger(string1);

        int int0 = bigInteger0.intValue();
        int int1 = bigInteger1.intValue();

        Assertions.checkEquals(135, int0);
        Assertions.checkEquals(20, int1);

        BigInteger[] bigIntegerArray0 = bigInteger0
                .divideAndRemainder(bigInteger1);

        BigInteger quotient = bigIntegerArray0[0];
        BigInteger remainder = bigIntegerArray0[1];

        int quotientInteger = quotient.intValue();
        int remainderInteger = remainder.intValue();

        Assertions.checkEquals(6, quotientInteger);
        Assertions.checkEquals(15, remainderInteger);

        BigInteger min = quotient.min(remainder);
        Assertions.checkEquals(min.intValue(), quotient.intValue());

    }

}
