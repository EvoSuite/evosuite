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
package org.evosuite.instrumentation.testability;

import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.statements.numeric.DoublePrimitiveStatement;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestDoubleFloatComparison {

    @Test
    public void testDoubleComparison() {
        int x = BooleanHelper.doubleSubG(0.0, 0.0);
        int y = BooleanHelper.doubleSubG(10.0, 0.0);
        assertTrue(y > x);

        int z = BooleanHelper.doubleSubG(20.0, 0.0);
        assertTrue(z > x);
        assertTrue(z > y);
    }

    @Test
    public void testDoubleComparison2() {
        assertEquals(Double.compare(1000.0, 1.0) > 0, BooleanHelper.doubleSubG(1000.0, 1.0) > 0);
        assertEquals(Double.compare(1000.0, 1.0) < 0, BooleanHelper.doubleSubG(1000.0, 1.0) < 0);
        assertEquals(Double.compare(1000.0, 1.0) == 0, BooleanHelper.doubleSubG(1000.0, 1.0) == 0);

        assertEquals(Double.compare(1.0, 1000.0) > 0, BooleanHelper.doubleSubG(1.0, 1000.0) > 0);
        assertEquals(Double.compare(1.0, 1000.0) < 0, BooleanHelper.doubleSubG(1.0, 1000.0) < 0);
        assertEquals(Double.compare(1.0, 1000.0) == 0, BooleanHelper.doubleSubG(1.0, 1000.0) == 0);
    }

    @Test
    public void testDoubleNegativeComparison() {
        int x = BooleanHelper.doubleSubG(0.0, 0.0);
        int y = BooleanHelper.doubleSubG(-10.0, 0.0);
        assertTrue(x > y);

        int z = BooleanHelper.doubleSubG(-20.0, 0.0);
        assertTrue(z < x);
        assertTrue(z < y);
    }

    @Test
    public void testLargeDoubleComparison() {
        int x = BooleanHelper.doubleSubG(29380.0, 3266.3);
        int y = BooleanHelper.doubleSubG(23562985.124125, 2938.2525);
        assertTrue(y > x);

        int z = BooleanHelper.doubleSubG(238628629.23423, 2352.2323);
        assertTrue(z > x);
        assertTrue(z > y);
    }

    @Test
    public void testLargeDoubleNegativeComparison() {
        int x = BooleanHelper.doubleSubG(-29380.0, 3266.3);
        int y = BooleanHelper.doubleSubG(-23562985.124125, 2938.2525);
        assertTrue(y < x);

        int z = BooleanHelper.doubleSubG(-238628629.23423, 2352.2323);
        assertTrue(z < x);
        assertTrue(z < y);
    }

    @Test
    public void testExamples() {
        double x1 = -1939.9207985389992;
        double x2 = -1941.2134374492741;
        double y1 = -89.0;
        double y2 = -95.11816569743506;
        double z1 = 291.0;
        double z2 = 291.35140748465363;

        int a1 = BooleanHelper.doubleSubG(x1, 0.0);
        int a2 = BooleanHelper.doubleSubG(x2, 0.0);
        assertTrue(a1 > a2);

        int b1 = BooleanHelper.doubleSubG(y1, 0.0);
        int b2 = BooleanHelper.doubleSubG(y2, 0.0);
        assertTrue(b1 > b2);

        int c1 = BooleanHelper.doubleSubG(z1, 0.0);
        int c2 = BooleanHelper.doubleSubG(z2, 0.0);
        assertTrue(c1 < c2);
    }

    @Test
    public void testDelta() {
        TestCase test = new DefaultTestCase();
        DoublePrimitiveStatement statement1 = new DoublePrimitiveStatement(test);
        DoublePrimitiveStatement statement2 = new DoublePrimitiveStatement(test);

        double d1 = statement1.getValue();
        double d2 = statement2.getValue();
        int val = BooleanHelper.doubleSubG(d1, d2);
        assertEquals(val > 0, d1 > d2);
        assertEquals(val < 0, d1 < d2);
        assertEquals(val == 0, d1 == d2);

        for (int i = 0; i < 100; i++) {
            statement1.delta();
            statement2.delta();
            d1 = statement1.getValue();
            d2 = statement2.getValue();
            val = BooleanHelper.doubleSubG(d1, d2);
            assertEquals(val > 0, d1 > d2);
            assertEquals(val < 0, d1 < d2);
            assertEquals(val == 0, d1 == d2);
        }
        for (int i = 0; i < 100; i++) {
            statement1.randomize();
            statement2.randomize();
            d1 = statement1.getValue();
            d2 = statement2.getValue();
            val = BooleanHelper.doubleSubG(d1, d2);
            assertEquals(val > 0, d1 > d2);
            assertEquals(val < 0, d1 < d2);
            assertEquals(val == 0, d1 == d2);
        }
    }

    @Test
    public void testInfinityAndDouble() {
        assertTrue(BooleanHelper.doubleSubG(Double.POSITIVE_INFINITY, 0.0) != 0);
        assertTrue(BooleanHelper.doubleSubG(Double.NEGATIVE_INFINITY, 0.0) != 0);
        assertTrue(BooleanHelper.doubleSubG(0.0, Double.POSITIVE_INFINITY) != 0);
        assertTrue(BooleanHelper.doubleSubG(0.0, Double.NEGATIVE_INFINITY) != 0);
    }

    @Test
    public void testNaNAndDouble() {
        assertEquals(1, BooleanHelper.doubleSubG(Float.NaN, 0.0));
        assertEquals(1, BooleanHelper.doubleSubG(0.0, Double.NaN));
        assertEquals(-1, BooleanHelper.doubleSubL(Float.NaN, 0.0));
        assertEquals(-1, BooleanHelper.doubleSubL(0.0, Double.NaN));
    }

    // TODO: Double.compare has different semantics wrt NaN, does this test really make sense?
    @Test
    public void testDoubleNaN2() {
        double p = Double.NaN;
        assertEquals(Double.compare(p, Double.NaN) > 0, BooleanHelper.doubleSubL(p, Double.NaN) > 0);
        assertEquals(Double.compare(p, Double.NaN) > 0, BooleanHelper.doubleSubG(p, Double.NaN) < 0);
        // compare is different to dcmpl/g
        // assertEquals(Double.compare(p, Double.NaN) == 0, BooleanHelper.doubleSubG(p, Double.NaN) == 0);
        assertEquals(Double.compare(p, Double.NaN) < 0, BooleanHelper.doubleSubL(p, Double.NaN) > 0);
        assertEquals(Double.compare(p, Double.NaN) < 0, BooleanHelper.doubleSubG(p, Double.NaN) < 0);
        assertEquals(Double.compare(p, 0.0) < 0, BooleanHelper.doubleSubG(p, 0.0) < 0);
        assertEquals(Double.compare(p, 0.0) == 0, BooleanHelper.doubleSubG(p, 0.0) == 0);
        assertEquals(Double.compare(p, 0.0) > 0, BooleanHelper.doubleSubG(p, 0.0) > 0);
        assertEquals(Double.compare(p, 1.0) > 0, BooleanHelper.doubleSubG(p, 1.0) > 0);
        assertEquals(Double.compare(p, 1.0) == 0, BooleanHelper.doubleSubG(p, 1.0) == 0);
        assertEquals(Double.compare(p, 1.0) < 0, BooleanHelper.doubleSubG(p, 1.0) < 0);
        assertTrue(BooleanHelper.doubleSubG(0.0, p) > 0);
        assertEquals(Double.compare(0.0, p) < 0, BooleanHelper.doubleSubG(0.0, p) > 0);
        //assertEquals(Double.compare(0.0, p) == 0, BooleanHelper.doubleSubG(0.0, p) == 0);
        assertEquals(Double.compare(0.0, p) > 0, BooleanHelper.doubleSubG(0.0, p) < 0);
        assertEquals(Double.compare(1.0, p) < 0, BooleanHelper.doubleSubG(1.0, p) > 0);
        //assertEquals(Double.compare(1.0, p) == 0 , BooleanHelper.doubleSubG(1.0, p) == 0);
        assertEquals(Double.compare(1.0, p) < 0, BooleanHelper.doubleSubL(1.0, p) < 0);
    }

    // TODO: Float.compare has different semantics wrt NaN, does this test really make sense?
    @Test
    public void testFloatNaN2() {
        float p = Float.NaN;
        assertEquals(Float.compare(p, Float.NaN) > 0, BooleanHelper.floatSubL(p, Float.NaN) > 0);
        assertEquals(Float.compare(p, Float.NaN) > 0, BooleanHelper.floatSubG(p, Float.NaN) < 0);
        // compare behaves different than fcmpl/fcmpg
        //assertEquals(Float.compare(p, Float.NaN) == 0, BooleanHelper.floatSubG(p, Float.NaN) == 0);
        assertEquals(Float.compare(p, Float.NaN) < 0, BooleanHelper.floatSubL(p, Float.NaN) > 0);
        assertEquals(Float.compare(p, Float.NaN) < 0, BooleanHelper.floatSubG(p, Float.NaN) < 0);
        assertEquals(Float.compare(p, 0.0f) < 0, BooleanHelper.floatSubG(p, 0.0f) < 0);
        assertEquals(Float.compare(p, 0.0f) == 0, BooleanHelper.floatSubG(p, 0.0f) == 0);
        assertEquals(Float.compare(p, 0.0f) > 0, BooleanHelper.floatSubG(p, 0.0f) > 0);
        assertEquals(Float.compare(p, 1.0f) > 0, BooleanHelper.floatSubG(p, 1.0f) > 0);
        assertEquals(Float.compare(p, 1.0f) == 0, BooleanHelper.floatSubG(p, 1.0f) == 0);
        assertEquals(Float.compare(p, 1.0f) < 0, BooleanHelper.floatSubG(p, 1.0f) < 0);
    }


    @Test
    public void testInfinityAndFloat() {
        assertTrue(BooleanHelper.floatSubG(Float.POSITIVE_INFINITY, 0.0F) != 0);
        assertTrue(BooleanHelper.floatSubG(Float.NEGATIVE_INFINITY, 0.0F) != 0);
        assertTrue(BooleanHelper.floatSubG(0.0F, Float.POSITIVE_INFINITY) != 0);
        assertTrue(BooleanHelper.floatSubG(0.0F, Float.NEGATIVE_INFINITY) != 0);
    }

    @Test
    public void testNaNAndFloat() {
        assertEquals(1, BooleanHelper.floatSubG(Float.NaN, 0.0F));
        assertEquals(1, BooleanHelper.floatSubG(0.0F, Float.NaN));
        assertEquals(-1, BooleanHelper.floatSubL(Float.NaN, 0.0F));
        assertEquals(-1, BooleanHelper.floatSubL(0.0F, Float.NaN));
    }

    @Test
    public void testRoundingErrorForSmallNumber() {
        float a = -1.4E-45F;
        assertTrue(BooleanHelper.floatSubG(a, 0.0f) < 0);
    }

}
