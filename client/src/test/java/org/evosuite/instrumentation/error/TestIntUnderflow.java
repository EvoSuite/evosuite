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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class TestIntUnderflow {

    private int x;
    private int y;

    // Creates the test data
    @Parameters
    public static Collection<Object[]> data() {
        Object[] values = new Object[]{Integer.MIN_VALUE, Integer.MIN_VALUE / 2, 0, Integer.MAX_VALUE / 2, Integer.MAX_VALUE};
        List<Object[]> valuePairs = new ArrayList<>();
        for (Object val1 : values) {
            for (Object val2 : values) {
                valuePairs.add(new Object[]{val1, val2});
            }
        }
        return valuePairs;
    }

    public TestIntUnderflow(int x, int y) {
        this.x = x;
        this.y = y;
    }

    private void assertUnderflow(long longResult, int intResult) {
        if (longResult < Integer.MIN_VALUE) {
            assertTrue("Expected negative value for " + x + " and " + y + ": " + intResult, intResult < 0);
        } else {
            assertTrue("Expected positive value for " + x + " and " + y + ": " + intResult, intResult >= 0);
        }
    }


    @Test
    public void testAddUnderflow() {
        int result = ErrorConditionChecker.underflowDistance(x, y, Opcodes.IADD);
        assertUnderflow((long) x + (long) y, result);
    }

    @Test
    public void testSubUnderflow() {
        int result = ErrorConditionChecker.underflowDistance(x, y, Opcodes.ISUB);
        assertUnderflow((long) x - (long) y, result);
    }

    @Test
    public void testMulUnderflow() {
        int result = ErrorConditionChecker.underflowDistance(x, y, Opcodes.IMUL);
        assertUnderflow((long) x * (long) y, result);
    }

    @Test
    public void testDivUnderflow() {
        Assume.assumeTrue(y != 0);
        int result = ErrorConditionChecker.underflowDistance(x, y, Opcodes.IDIV);
        assertUnderflow((long) x / (long) y, result);
    }
}
