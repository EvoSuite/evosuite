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

package org.evosuite.runtime;

import java.util.UUID;

/**
 * <p>
 * Random class.
 * </p>
 *
 * @author Gordon Fraser
 */
public class Random {

    private static boolean wasAccessed = false;

    /**
     * We have a unique number that is increased every time a new random number
     * is accessed
     */
    private static int currentNumber = 0;

    /**
     * Replacement function for nextInt
     *
     * @return a int.
     */
    public static int nextInt() {
        wasAccessed = true;
        return currentNumber++;
    }

    /**
     * Replacement function for nextInt
     *
     * @param max a int.
     * @return a int.
     */
    public static int nextInt(int max) {
        wasAccessed = true;
        return currentNumber % max;
    }

    /**
     * Replacement function for nextFloat
     *
     * @return a float.
     */
    public static float nextFloat() {
        wasAccessed = true;
        return (currentNumber++ % 10F) / 10F;
    }


    /**
     * Replacement function for nextBytes
     *
     * @param bytes
     */
    public static void nextBytes(byte[] bytes) {
        wasAccessed = true;

        for (int i = 0; i < bytes.length; )
            for (int rnd = nextInt(), n = Math.min(bytes.length - i, 4);
                 n-- > 0; rnd >>= 8)
                bytes[i++] = (byte) rnd;
    }


    /**
     * Replacement function for nextDouble
     *
     * @return a double.
     */
    public static double nextDouble() {
        wasAccessed = true;
        return (currentNumber++ % 10.0) / 10.0;
    }

    /**
     * Replacement function for nextGaussian
     *
     * @return a double.
     */
    public static double nextGaussian() {
        wasAccessed = true;
        return nextDouble();
    }

    /**
     * Replacement function for nextBoolean
     *
     * @return a boolean.
     */
    public static boolean nextBoolean() {
        wasAccessed = true;
        return nextInt(1) != 0;
    }


    /**
     * Replacement function for nextLong
     *
     * @return a long.
     */
    public static long nextLong() {
        wasAccessed = true;
        return currentNumber++;
    }

    /**
     * Set the next random number to a value
     *
     * @param number a int.
     */
    public static void setNextRandom(int number) {
        currentNumber = Math.abs(number);
    }

    /**
     * Reset runtime to initial state
     */
    public static void reset() {
        currentNumber = 0;
        wasAccessed = false;
    }

    /**
     * Getter to check whether this runtime replacement was accessed during test
     * execution
     *
     * @return a boolean.
     */
    public static boolean wasAccessed() {
        return wasAccessed;
    }

    /**
     * Replacement for function java.util.UUID.randomUUID()
     *
     * @return
     */
    public static UUID randomUUID() {
        wasAccessed = true;

        byte[] randomBytes = new byte[16];
        nextBytes(randomBytes);
        randomBytes[6] &= 0x0f;  /* clear version        */
        randomBytes[6] |= 0x40;  /* set to version 4     */
        randomBytes[8] &= 0x3f;  /* clear variant        */
        randomBytes[8] |= 0x80;  /* set to IETF variant  */

        UUID newUUID = buildNewUUID(randomBytes);
        return newUUID;
    }

    /**
     * Replacement for creation of new UUID using byte[]
     *
     * @param data
     * @return
     */
    private static UUID buildNewUUID(byte[] data) {
        long msb = 0;
        long lsb = 0;
        assert data.length == 16 : "data must be 16 bytes in length";
        for (int i = 0; i < 8; i++)
            msb = (msb << 8) | (data[i] & 0xff);
        for (int i = 8; i < 16; i++)
            lsb = (lsb << 8) | (data[i] & 0xff);
        UUID newUUID = new UUID(msb, lsb);
        return newUUID;
    }

    public static int getCurrentNumber() {
        return currentNumber;
    }
}
