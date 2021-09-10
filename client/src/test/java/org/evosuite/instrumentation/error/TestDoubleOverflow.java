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
package org.evosuite.instrumentation.error;

import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.objectweb.asm.Opcodes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class TestDoubleOverflow {

    private double x;
    private double y;

    // Creates the test data
    @Parameters
    public static Collection<Object[]> data() {
        Object[] values = new Object[]{-Double.MAX_VALUE, -Double.MIN_VALUE / 2.0, 0.0, Double.MAX_VALUE / 2.0, Double.MAX_VALUE};
        List<Object[]> valuePairs = new ArrayList<>();
        for (Object val1 : values) {
            for (Object val2 : values) {
                valuePairs.add(new Object[]{val1, val2});
            }
        }
        return valuePairs;
    }

    public TestDoubleOverflow(double x, double y) {
        this.x = x;
        this.y = y;
    }


    private void assertOverflow(BigDecimal preciseResult, int distance, double doubleResult) {
        BigDecimal maxResult = new BigDecimal(Double.MAX_VALUE);
        if (preciseResult.compareTo(maxResult) > 0) {
            assertTrue("Expected negative value for " + x + " and " + y + ": " + distance, distance <= 0);
            assertEquals(Double.POSITIVE_INFINITY, doubleResult, 0.0);
        } else {
            assertTrue("Expected positive value for " + x + " and " + y + ": " + distance, distance > 0);
        }
    }


    @Test
    public void testAddOverflow() {
        int result = ErrorConditionChecker.overflowDistance(x, y, Opcodes.DADD);
        assertOverflow(new BigDecimal(x).add(new BigDecimal(y)), result, x + y);
    }

    @Test
    public void testSubOverflow() {
        int result = ErrorConditionChecker.overflowDistance(x, y, Opcodes.DSUB);
        assertOverflow(new BigDecimal(x).subtract(new BigDecimal(y)), result, x - y);
    }

    @Test
    public void testMulOverflow() {
        int result = ErrorConditionChecker.overflowDistance(x, y, Opcodes.DMUL);
        assertOverflow(new BigDecimal(x).multiply(new BigDecimal(y)), result, x * y);
    }

    @Test
    public void testDivOverflow() {
        Assume.assumeTrue(y != 0D);

        int result = ErrorConditionChecker.overflowDistance(x, y, Opcodes.DDIV);
        assertOverflow(new BigDecimal(x).divide(new BigDecimal(y)), result, x / y);
    }
}
