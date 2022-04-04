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
package org.evosuite.runtime.vnet;


public class EvoIPAddressUtil {

    public static byte[] textToNumericFormatV4(String src) {
        //TODO byte[] addr = sun.net.util.IPAddressUtil.textToNumericFormatV4(host); //FIXME
        byte[] res = new byte[4];
        long tmpValue = 0L;
        int currByte = 0;
        boolean newOctet = true;
        int len = src.length();
        if (len != 0 && len <= 15) {
            for (int i = 0; i < len; ++i) {
                char c = src.charAt(i);
                if (c == '.') {
                    if (newOctet || tmpValue < 0L || tmpValue > 255L || currByte == 3) {
                        return null;
                    }

                    res[currByte++] = (byte) ((int) (tmpValue & 255L));
                    tmpValue = 0L;
                    newOctet = true;
                } else {
                    int digit = Character.digit(c, 10);
                    if (digit < 0) {
                        return null;
                    }

                    tmpValue *= 10L;
                    tmpValue += digit;
                    newOctet = false;
                }
            }

            if (!newOctet && tmpValue >= 0L && tmpValue < 1L << (4 - currByte) * 8) {
                switch (currByte) {
                    case 0:
                        res[0] = (byte) ((int) (tmpValue >> 24 & 255L));
                    case 1:
                        res[1] = (byte) ((int) (tmpValue >> 16 & 255L));
                    case 2:
                        res[2] = (byte) ((int) (tmpValue >> 8 & 255L));
                    case 3:
                        res[3] = (byte) ((int) (tmpValue >> 0 & 255L));
                    default:
                        return res;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
