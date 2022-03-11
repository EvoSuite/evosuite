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

package org.evosuite.javaagent;

import org.evosuite.instrumentation.error.ErrorConditionChecker;
import org.junit.Test;
import org.objectweb.asm.Opcodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Gordon Fraser
 */
public class ErrorConditionCheckerTest {

    @Test
    public void testScaleTo() {
        int x = ErrorConditionChecker.scaleTo(100, Integer.MAX_VALUE);
        int y = ErrorConditionChecker.scaleTo(200, Integer.MAX_VALUE);
        assertTrue(x < y);

        final int HALFWAY = Integer.MAX_VALUE / 2;
        x = ErrorConditionChecker.scaleTo(100, HALFWAY);
        y = ErrorConditionChecker.scaleTo(200, HALFWAY);
        assertTrue(x < y);
    }

    @Test
    public void testIntAddOverflow() {
        int distance = ErrorConditionChecker.overflowDistance(Integer.MAX_VALUE, 2,
                Opcodes.IADD);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.overflowDistance(Integer.MAX_VALUE,
                Integer.MAX_VALUE, Opcodes.IADD);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.overflowDistance(Integer.MAX_VALUE, 1,
                Opcodes.IADD);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.overflowDistance(Integer.MAX_VALUE, 0,
                Opcodes.IADD);
        assertEquals(1, distance);

        distance = ErrorConditionChecker.overflowDistance(0, Integer.MAX_VALUE,
                Opcodes.IADD);
        assertEquals(1, distance);

        distance = ErrorConditionChecker.overflowDistance(Integer.MIN_VALUE,
                Integer.MAX_VALUE, Opcodes.IADD);
        assertEquals(Integer.MAX_VALUE, distance);

        int distance1 = ErrorConditionChecker.overflowDistance(10, 10, Opcodes.IADD);
        assertTrue("Expected value greater 0 but got " + distance1, distance1 > 0);

        int distance2 = ErrorConditionChecker.overflowDistance(1000, 1000, Opcodes.IADD);
        assertTrue("Expected value greater 0 but got " + distance2, distance2 > 0);
        assertTrue("Invalid ranking: " + distance1 + ", " + distance2,
                distance1 > distance2);

        distance1 = ErrorConditionChecker.overflowDistance(100, -100, Opcodes.IADD);
        assertTrue("Expected value greater 0 but got " + distance1, distance1 > 0);

        distance2 = ErrorConditionChecker.overflowDistance(-100, 100, Opcodes.IADD);
        assertTrue("Expected value greater 0 but got " + distance2, distance2 > 0);
        assertEquals(distance1, distance2);

        int distance3 = ErrorConditionChecker.overflowDistance(-100, 10, Opcodes.IADD);
        assertTrue("Expected value greater 0 but got " + distance3, distance3 > 0);
        assertEquals(distance1, distance2);

        distance3 = ErrorConditionChecker.overflowDistance(-50, 10, Opcodes.IADD);
        assertTrue("Expected value greater 0 but got " + distance3, distance3 > 0);
        assertTrue(distance3 < distance2);

    }

    @Test
    public void testIntAddUnderflow() {
        int distance = ErrorConditionChecker.underflowDistance(Integer.MIN_VALUE, -1,
                Opcodes.IADD);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.underflowDistance(-1, Integer.MIN_VALUE,
                Opcodes.IADD);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.underflowDistance(Integer.MIN_VALUE / 2,
                Integer.MIN_VALUE / 2,
                Opcodes.IADD);
        assertEquals(1, distance);

        distance = ErrorConditionChecker.underflowDistance((Integer.MIN_VALUE / 2) - 1,
                (Integer.MIN_VALUE / 2) - 1,
                Opcodes.IADD);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.underflowDistance((Integer.MIN_VALUE + 1) / 2,
                (Integer.MIN_VALUE + 1) / 2,
                Opcodes.IADD);
        assertTrue(distance > 0);

        int distance1 = ErrorConditionChecker.underflowDistance(10, 10, Opcodes.IADD);
        assertTrue("Expected value greater 0 but got " + distance1, distance1 > 0);

        int distance2 = ErrorConditionChecker.underflowDistance(1000, 1000, Opcodes.IADD);
        assertTrue("Expected value greater 0 but got " + distance2, distance2 > 0);
        assertTrue("Invalid ranking: " + distance1 + ", " + distance2,
                distance1 < distance2);
    }

    @Test
    public void testIntSubOverflow() {
        int distance = ErrorConditionChecker.overflowDistance(Integer.MAX_VALUE, -2,
                Opcodes.ISUB);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.overflowDistance(Integer.MAX_VALUE,
                Integer.MIN_VALUE, Opcodes.ISUB);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.overflowDistance(Integer.MAX_VALUE, -1,
                Opcodes.ISUB);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.overflowDistance(Integer.MAX_VALUE, 0,
                Opcodes.ISUB);
        assertEquals(1, distance);

        distance = ErrorConditionChecker.overflowDistance(0, Integer.MIN_VALUE,
                Opcodes.ISUB);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.overflowDistance(-1, Integer.MIN_VALUE,
                Opcodes.ISUB);
        assertTrue(distance > 0);

        distance = ErrorConditionChecker.overflowDistance(Integer.MIN_VALUE,
                Integer.MIN_VALUE, Opcodes.ISUB);
        assertEquals(Integer.MAX_VALUE - 1, distance);

        int distance1 = ErrorConditionChecker.overflowDistance(10, 10, Opcodes.ISUB);
        assertTrue("Expected value greater 0 but got " + distance1, distance1 > 0);

        int distance2 = ErrorConditionChecker.overflowDistance(10, 1000, Opcodes.ISUB);
        assertTrue("Expected value greater 0 but got " + distance2, distance2 > 0);
        assertTrue("Invalid ranking: " + distance1 + ", " + distance2,
                distance2 > distance1);

        distance1 = ErrorConditionChecker.overflowDistance(100, -100, Opcodes.ISUB);
        assertTrue("Expected value greater 0 but got " + distance1, distance1 > 0);

        distance2 = ErrorConditionChecker.overflowDistance(-100, 100, Opcodes.ISUB);
        assertTrue("Expected value greater 0 but got " + distance2, distance2 > 0);
        assertTrue(distance1 < distance2);

        int distance3 = ErrorConditionChecker.overflowDistance(-100, 10, Opcodes.ISUB);
        assertTrue("Expected value greater 0 but got " + distance3, distance3 > 0);
        assertTrue(distance3 < distance2);

        distance3 = ErrorConditionChecker.overflowDistance(-50, 10, Opcodes.ISUB);
        assertTrue("Expected value greater 0 but got " + distance3, distance3 > 0);
        assertTrue(distance3 < distance2);

    }

    @Test
    public void testIntSubUnderflow() {
        int distance = ErrorConditionChecker.underflowDistance(Integer.MIN_VALUE, 2,
                Opcodes.ISUB);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.underflowDistance(Integer.MIN_VALUE, -2,
                Opcodes.ISUB);
        assertTrue(distance > 0);

        distance = ErrorConditionChecker.underflowDistance(-2, Integer.MAX_VALUE,
                Opcodes.ISUB);
        assertTrue(distance <= 0);

        int distance1 = ErrorConditionChecker.underflowDistance(10, 10, Opcodes.ISUB);
        assertTrue("Expected value greater 0 but got " + distance1, distance1 > 0);

        int distance2 = ErrorConditionChecker.underflowDistance(1000, 1000, Opcodes.ISUB);
        assertTrue("Expected value greater 0 but got " + distance2, distance2 > 0);

        assertTrue("Invalid ranking: " + distance1 + ", " + distance2,
                distance1 < distance2);

        distance1 = ErrorConditionChecker.underflowDistance(10, -10, Opcodes.ISUB);
        assertTrue("Expected value greater 0 but got " + distance1, distance1 > 0);

        distance2 = ErrorConditionChecker.underflowDistance(1000, -1000, Opcodes.ISUB);
        assertTrue("Expected value greater 0 but got " + distance2, distance2 > 0);

        assertTrue("Invalid ranking: " + distance1 + ", " + distance2,
                distance1 < distance2);
    }

    @Test
    public void testIntMulOverflow() {
        int distance = ErrorConditionChecker.overflowDistance(Integer.MAX_VALUE, 2,
                Opcodes.IMUL);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.overflowDistance(Integer.MAX_VALUE / 2, 3,
                Opcodes.IMUL);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.overflowDistance(Integer.MIN_VALUE, -2,
                Opcodes.IMUL);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.overflowDistance(Integer.MIN_VALUE, -1,
                Opcodes.IMUL);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.overflowDistance(-15000000, -15000000,
                Opcodes.IMUL);
        assertTrue(distance <= 0);

        int distance1 = ErrorConditionChecker.overflowDistance(10, 10, Opcodes.IMUL);
        assertTrue("Expected value greater 0 but got " + distance1, distance1 > 0);

        int distance2 = ErrorConditionChecker.overflowDistance(10, 1000, Opcodes.IMUL);
        assertTrue("Expected value greater 0 but got " + distance2, distance2 > 0);
        assertTrue("Invalid ranking: " + distance1 + ", " + distance2,
                distance2 < distance1);

        distance2 = ErrorConditionChecker.overflowDistance(-10, 1000, Opcodes.IMUL);
        assertTrue("Expected value greater 0 but got " + distance2, distance2 > 0);
        assertTrue("Invalid ranking: " + distance1 + ", " + distance2,
                distance2 > distance1);

        int distance3 = ErrorConditionChecker.overflowDistance(-100, 1000, Opcodes.IMUL);
        assertTrue("Expected value greater 0 but got " + distance3, distance3 > 0);
        assertTrue("Invalid ranking: " + distance1 + ", " + distance2,
                distance3 > distance2);

        distance1 = ErrorConditionChecker.overflowDistance(-100, -100, Opcodes.IMUL);
        assertTrue("Expected value greater 0 but got " + distance1, distance1 > 0);

        distance2 = ErrorConditionChecker.overflowDistance(100, 1000, Opcodes.IMUL);
        assertTrue("Expected value greater 0 but got " + distance2, distance2 > 0);
        assertTrue("Invalid ranking: " + distance1 + ", " + distance2,
                distance2 < distance1);
    }

    @Test
    public void testIntMulUnderflow() {
        int distance = ErrorConditionChecker.underflowDistance(Integer.MAX_VALUE, -2,
                Opcodes.IMUL);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.underflowDistance(-2, Integer.MAX_VALUE,
                Opcodes.IMUL);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.underflowDistance(2, Integer.MIN_VALUE,
                Opcodes.IMUL);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.underflowDistance(Integer.MIN_VALUE,
                Integer.MIN_VALUE,
                Opcodes.IMUL);
        assertTrue(distance > 0);

        int distance1 = ErrorConditionChecker.underflowDistance(10, 10, Opcodes.IMUL);
        assertTrue("Expected value greater 0 but got " + distance1, distance1 > 0);

        int distance2 = ErrorConditionChecker.underflowDistance(20, 1000, Opcodes.IMUL);
        assertTrue("Expected value greater 0 but got " + distance2, distance2 > 0);
        assertTrue("Invalid ranking: " + distance1 + ", " + distance2,
                distance1 < distance2);

        distance2 = ErrorConditionChecker.underflowDistance(3, 1000000, Opcodes.IMUL);
        assertTrue("Expected value greater 0 but got " + distance2, distance2 > 0);
        assertTrue("Invalid ranking: " + distance1 + ", " + distance2,
                distance2 < distance1);
    }

    @Test
    public void testIntDivOverflow() {
        int distance = ErrorConditionChecker.overflowDistance(Integer.MIN_VALUE, -1,
                Opcodes.IDIV);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.overflowDistance(Integer.MIN_VALUE, 1,
                Opcodes.IDIV);
        assertTrue("Expected > 0 but got " + distance, distance > 0);
    }

    @Test
    public void testFloatAddOverflow() {
        int distance = ErrorConditionChecker.overflowDistance(Float.MAX_VALUE, 2F,
                Opcodes.FADD);
        assertEquals(1, distance);

        distance = ErrorConditionChecker.overflowDistance(Float.MAX_VALUE,
                Float.MAX_VALUE, Opcodes.FADD);
        assertTrue(distance <= 0);

        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.overflowDistance(Float.MAX_VALUE, 0F,
                Opcodes.FADD);
        assertEquals(1, distance);

        distance = ErrorConditionChecker.overflowDistance(0F, Float.MAX_VALUE,
                Opcodes.FADD);
        assertEquals(1, distance);

        distance = ErrorConditionChecker.overflowDistance(-Float.MAX_VALUE,
                Float.MAX_VALUE, Opcodes.FADD);
        assertEquals(Integer.MAX_VALUE - 1, distance);

        int distance1 = ErrorConditionChecker.overflowDistance(10F, 10F, Opcodes.FADD);
        assertTrue("Expected value greater 0 but got " + distance1, distance1 > 0);

        int distance2 = ErrorConditionChecker.overflowDistance(1000F, 1000F, Opcodes.FADD);
        assertTrue("Expected value greater 0 but got " + distance2, distance2 > 0);
        assertTrue("Invalid ranking: " + distance1 + ", " + distance2,
                distance1 > distance2);

        distance1 = ErrorConditionChecker.overflowDistance(100F, -100F, Opcodes.FADD);
        assertTrue("Expected value greater 0 but got " + distance1, distance1 > 0);

        distance2 = ErrorConditionChecker.overflowDistance(-100F, 100F, Opcodes.FADD);
        assertTrue("Expected value greater 0 but got " + distance2, distance2 > 0);
        assertEquals(distance1, distance2);

        int distance3 = ErrorConditionChecker.overflowDistance(-100F, 10F, Opcodes.FADD);
        assertTrue("Expected value greater 0 but got " + distance3, distance3 > 0);
        assertEquals(distance1, distance2);

        distance3 = ErrorConditionChecker.overflowDistance(-50F, 10F, Opcodes.FADD);
        assertTrue("Expected value greater 0 but got " + distance3, distance3 > 0);
        assertTrue(distance3 < distance2);

    }

    @Test
    public void testFloatAddUnderflow() {
        int distance = ErrorConditionChecker.underflowDistance(-Float.MAX_VALUE,
                -Float.MAX_VALUE,
                Opcodes.FADD);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.underflowDistance(-Float.MAX_VALUE / 2F,
                -Float.MAX_VALUE / 2F,
                Opcodes.FADD);
        assertEquals(1, distance);

        distance = ErrorConditionChecker.underflowDistance((-Float.MAX_VALUE / 2F) - 1F,
                (-Float.MAX_VALUE / 2F) - 1F,
                Opcodes.FADD);
        assertEquals(1, distance);

        distance = ErrorConditionChecker.underflowDistance((-Float.MAX_VALUE + 1F) / 2F,
                (-Float.MAX_VALUE + 1F) / 2F,
                Opcodes.FADD);
        assertTrue(distance > 0);

        int distance1 = ErrorConditionChecker.underflowDistance(10F, 10F, Opcodes.FADD);
        assertTrue("Expected value greater 0 but got " + distance1, distance1 > 0);

        int distance2 = ErrorConditionChecker.underflowDistance(1000F, 1000F,
                Opcodes.FADD);
        assertTrue("Expected value greater 0 but got " + distance2, distance2 > 0);
        assertTrue("Invalid ranking: " + distance1 + ", " + distance2,
                distance1 < distance2);
    }

    @Test
    public void testFloatSubOverflow() {
        int distance = ErrorConditionChecker.overflowDistance(Float.MAX_VALUE,
                -Float.MAX_VALUE,
                Opcodes.FSUB);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.overflowDistance(Float.MAX_VALUE,
                -Float.MAX_VALUE, Opcodes.FSUB);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.overflowDistance(Float.MAX_VALUE, -1F,
                Opcodes.FSUB);
        assertEquals(1, distance);

        distance = ErrorConditionChecker.overflowDistance(Float.MAX_VALUE, 0F,
                Opcodes.FSUB);
        assertEquals(1, distance);

        distance = ErrorConditionChecker.overflowDistance(0F, -Float.MAX_VALUE,
                Opcodes.FSUB);
        assertEquals(1, distance);

        distance = ErrorConditionChecker.overflowDistance(-1F, -Float.MAX_VALUE,
                Opcodes.FSUB);
        assertTrue(distance > 0);

        distance = ErrorConditionChecker.overflowDistance(-Float.MAX_VALUE,
                -Float.MAX_VALUE, Opcodes.FSUB);
        assertEquals(Integer.MAX_VALUE - 1, distance);

        int distance1 = ErrorConditionChecker.overflowDistance(10F, 10F, Opcodes.FSUB);
        assertTrue("Expected value greater 0 but got " + distance1, distance1 > 0);

        int distance2 = ErrorConditionChecker.overflowDistance(10F, 1000F, Opcodes.FSUB);
        assertTrue("Expected value greater 0 but got " + distance2, distance2 > 0);
        assertTrue("Invalid ranking: " + distance1 + ", " + distance2,
                distance2 > distance1);

        distance1 = ErrorConditionChecker.overflowDistance(100F, -100F, Opcodes.FSUB);
        assertTrue("Expected value greater 0 but got " + distance1, distance1 > 0);

        distance2 = ErrorConditionChecker.overflowDistance(-100F, 100F, Opcodes.FSUB);
        assertTrue("Expected value greater 0 but got " + distance2, distance2 > 0);
        assertTrue(distance1 < distance2);

        int distance3 = ErrorConditionChecker.overflowDistance(-100F, 10F, Opcodes.FSUB);
        assertTrue("Expected value greater 0 but got " + distance3, distance3 > 0);
        assertTrue(distance3 < distance2);

        distance3 = ErrorConditionChecker.overflowDistance(-50F, 10F, Opcodes.FSUB);
        assertTrue("Expected value greater 0 but got " + distance3, distance3 > 0);
        assertTrue(distance3 < distance2);

    }

    @Test
    public void testFloatSubUnderflow() {
        int distance = ErrorConditionChecker.underflowDistance(-Float.MAX_VALUE,
                Float.MAX_VALUE,
                Opcodes.FSUB);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.underflowDistance(-Float.MAX_VALUE, -2F,
                Opcodes.FSUB);
        assertTrue(distance > 0);

        distance = ErrorConditionChecker.underflowDistance(-Float.MAX_VALUE,
                Float.MAX_VALUE, Opcodes.FSUB);
        assertTrue(distance <= 0);

        int distance1 = ErrorConditionChecker.underflowDistance(10F, 10F, Opcodes.FSUB);
        assertTrue("Expected value greater 0 but got " + distance1, distance1 > 0);

        int distance2 = ErrorConditionChecker.underflowDistance(1000F, 1000F,
                Opcodes.FSUB);
        assertTrue("Expected value greater 0 but got " + distance2, distance2 > 0);

        assertTrue("Invalid ranking: " + distance1 + ", " + distance2,
                distance1 < distance2);

        distance1 = ErrorConditionChecker.underflowDistance(10F, -10F, Opcodes.FSUB);
        assertTrue("Expected value greater 0 but got " + distance1, distance1 > 0);

        distance2 = ErrorConditionChecker.underflowDistance(1000F, -1000F, Opcodes.FSUB);
        assertTrue("Expected value greater 0 but got " + distance2, distance2 > 0);

        assertTrue("Invalid ranking: " + distance1 + ", " + distance2,
                distance1 < distance2);
    }

    @Test
    public void testFloatMulOverflow() {
        int distance = ErrorConditionChecker.overflowDistance(Float.MAX_VALUE, 2F,
                Opcodes.FMUL);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.overflowDistance(Float.MAX_VALUE / 2F, 3F,
                Opcodes.FMUL);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.overflowDistance(-Float.MAX_VALUE, -2F,
                Opcodes.FMUL);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.overflowDistance(-Float.MAX_VALUE, -1,
                Opcodes.FMUL);
        assertEquals(1, distance);

        distance = ErrorConditionChecker.overflowDistance(-150000000000000000000000.0000000002F,
                -150000000000000000000000.000000001F,
                Opcodes.FMUL);
        assertTrue(distance <= 0);

        int distance1 = ErrorConditionChecker.overflowDistance(10F, 10F, Opcodes.FMUL);
        assertTrue("Expected value greater 0 but got " + distance1, distance1 > 0);

        int distance2 = ErrorConditionChecker.overflowDistance(10F, 1000F, Opcodes.FMUL);
        assertTrue("Expected value greater 0 but got " + distance2, distance2 > 0);
        assertTrue("Invalid ranking: " + distance1 + ", " + distance2,
                distance2 < distance1);

        distance2 = ErrorConditionChecker.overflowDistance(-10F, 1000F, Opcodes.FMUL);
        assertTrue("Expected value greater 0 but got " + distance2, distance2 > 0);
        assertTrue("Invalid ranking: " + distance1 + ", " + distance2,
                distance2 > distance1);

        int distance3 = ErrorConditionChecker.overflowDistance(-100F, 1000F, Opcodes.FMUL);
        assertTrue("Expected value greater 0 but got " + distance3, distance3 > 0);
        assertTrue("Invalid ranking: " + distance1 + ", " + distance2,
                distance3 > distance2);

        distance1 = ErrorConditionChecker.overflowDistance(-100F, -100F, Opcodes.FMUL);
        assertTrue("Expected value greater 0 but got " + distance1, distance1 > 0);

        distance2 = ErrorConditionChecker.overflowDistance(100F, 1000F, Opcodes.FMUL);
        assertTrue("Expected value greater 0 but got " + distance2, distance2 > 0);
        assertTrue("Invalid ranking: " + distance1 + ", " + distance2,
                distance2 < distance1);
    }

    @Test
    public void testFloatMulUnderflow() {
        int distance = ErrorConditionChecker.underflowDistance(Float.MAX_VALUE, -2F,
                Opcodes.FMUL);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.underflowDistance(-2F, Float.MAX_VALUE,
                Opcodes.FMUL);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.underflowDistance(2F, -Float.MAX_VALUE,
                Opcodes.FMUL);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.underflowDistance(-Float.MAX_VALUE,
                -Float.MAX_VALUE, Opcodes.FMUL);
        assertTrue(distance > 0);

        int distance1 = ErrorConditionChecker.underflowDistance(10F, 10F, Opcodes.FMUL);
        assertTrue("Expected value greater 0 but got " + distance1, distance1 > 0);

        int distance2 = ErrorConditionChecker.underflowDistance(20F, 1000F, Opcodes.FMUL);
        assertTrue("Expected value greater 0 but got " + distance2, distance2 > 0);
        assertTrue("Invalid ranking: " + distance1 + ", " + distance2,
                distance1 < distance2);

        distance2 = ErrorConditionChecker.underflowDistance(3F, 1000000F, Opcodes.FMUL);
        assertTrue("Expected value greater 0 but got " + distance2, distance2 > 0);
        assertTrue("Invalid ranking: " + distance1 + ", " + distance2,
                distance2 < distance1);
    }

    @Test
    public void testFloatDivOverflow() {
        int distance = ErrorConditionChecker.overflowDistance(-Float.MAX_VALUE, -1F,
                Opcodes.FDIV);
        assertTrue(distance <= 0);

        distance = ErrorConditionChecker.overflowDistance(-Float.MAX_VALUE, 1F,
                Opcodes.FDIV);
        assertTrue("Expected > 0 but got " + distance, distance > 0);
    }

}
