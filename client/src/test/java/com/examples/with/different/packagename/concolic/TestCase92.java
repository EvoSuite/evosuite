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


public class TestCase92 {

    public static void test(String string0, int catchCount, boolean boolean0) {

        try {
            new StringBuffer(null);
        } catch (NullPointerException ex) {
            catchCount++;
        }

        Assertions.checkEquals(1, catchCount);

        StringBuffer sb = new StringBuffer(string0);
        String string1 = sb.toString();

        int int0 = "Togliere sta roba".length();
        int int1 = string1.length();

        Assertions.checkEquals(int0, int1);

        sb.append(true);
        sb.append(false);
        String string2 = sb.toString();

        int int2 = "Togliere sta robatruefalse".length();
        int int3 = string2.length();

        Assertions.checkEquals(int2, int3);

        StringBuffer stringBuffer1 = new StringBuffer(new String(
                "Togliere sta roba"));
        stringBuffer1.append(boolean0);
        String string3 = stringBuffer1.toString();

        int int4 = string3.length();
        int int5 = "Togliere sta robatrue".length();

        Assertions.checkEquals(int4, int5);

        // append char
        {
            char char0 = 'x';
            stringBuffer1.append(char0);
            String string4 = stringBuffer1.toString();

            int int6 = string4.length();
            int int7 = "Togliere sta robatruex".length();
            Assertions.checkEquals(int6, int7);
        }
        // append int
        {
            int myInt = 125;
            stringBuffer1.append(myInt);
            String string5 = stringBuffer1.toString();

            int int8 = string5.length();
            int int9 = "Togliere sta robatruex125".length();
            Assertions.checkEquals(int8, int9);
        }
        // append long
        {
            long myLong = 999L;
            stringBuffer1.append(myLong);
            String string6 = stringBuffer1.toString();

            int int10 = string6.length();
            int int11 = "Togliere sta robatruex125999".length();
            Assertions.checkEquals(int10, int11);
        }
        // append float
        {
            float myFloat = 1.0f;
            stringBuffer1.append(myFloat);
            String string7 = stringBuffer1.toString();

            int int12 = string7.length();
            int int13 = "Togliere sta robatruex1259991.0".length();
            Assertions.checkEquals(int12, int13);
        }
        // append double
        {
            double myDouble = 2.0;
            stringBuffer1.append(myDouble);
            String string7 = stringBuffer1.toString();

            int int14 = string7.length();
            int int15 = "Togliere sta robatruex1259991.02.0".length();
            Assertions.checkEquals(int14, int15);
        }
        // append null String
        {
            stringBuffer1.append((String) null);
            String string8 = stringBuffer1.toString();

            int int16 = string8.length();
            int int17 = "Togliere sta robatruex1259991.02.0null".length();
            Assertions.checkEquals(int16, int17);
        }
        // append non-null symbolic String
        {
            String string9 = stringBuffer1.toString();
            String string10 = stringBuffer1.append(string9).toString();

            int int18 = ("Togliere sta robatruex1259991.02.0null" + "Togliere sta robatruex1259991.02.0null").length();
            int int19 = string10.length();
            Assertions.checkEquals(int18, int19);
        }

    }

}
