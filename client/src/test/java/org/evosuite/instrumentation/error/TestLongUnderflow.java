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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class TestLongUnderflow {

    private long x;
    private long y;

    // Creates the test data
    @Parameters
    public static Collection<Object[]> data() {
        Object[] values = new Object[]{Long.MIN_VALUE, Long.MIN_VALUE / 2, 0, Long.MAX_VALUE / 2, Long.MAX_VALUE};
        List<Object[]> valuePairs = new ArrayList<>();
        for (Object val1 : values) {
            for (Object val2 : values) {
                valuePairs.add(new Object[]{val1, val2});
            }
        }
        return valuePairs;
    }

    public TestLongUnderflow(long x, long y) {
        this.x = x;
        this.y = y;
    }


    private void assertUnderflow(BigDecimal preciseResult, int distance, long longResult) {
        BigDecimal maxResult = new BigDecimal(Long.MIN_VALUE);
        if (preciseResult.compareTo(maxResult) < 0) {
            assertTrue("Expected negative value for " + x + " and " + y + ": " + distance + " for " + longResult, distance < 0);
        } else {
            assertTrue("Expected positive value for " + x + " and " + y + ": " + distance, distance >= 0);
        }
    }


    @Test
    public void testAddUnderflow() {
        int result = ErrorConditionChecker.underflowDistance(x, y, Opcodes.LADD);
        assertUnderflow(new BigDecimal(x).add(new BigDecimal(y)), result, x + y);
    }

    @Test
    public void testSubUnderflow() {
        int result = ErrorConditionChecker.underflowDistance(x, y, Opcodes.LSUB);
        assertUnderflow(new BigDecimal(x).subtract(new BigDecimal(y)), result, x - y);
    }

    @Test
    public void testMulUnderflow() {
        int result = ErrorConditionChecker.underflowDistance(x, y, Opcodes.LMUL);
        assertUnderflow(new BigDecimal(x).multiply(new BigDecimal(y)), result, x * y);
    }

    @Test
    public void testDivUnderflow() {
        Assume.assumeTrue(y != 0L);

        int result = ErrorConditionChecker.underflowDistance(x, y, Opcodes.LDIV);
        assertUnderflow(new BigDecimal(x).divide(new BigDecimal(y), 10, RoundingMode.HALF_UP), result, x / y);
    }
}
