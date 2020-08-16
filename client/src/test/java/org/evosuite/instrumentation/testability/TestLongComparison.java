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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by gordon on 03/02/2016.
 */
public class TestLongComparison {

    @Test
    public void testLongComparison() {
        long l1 = 0L;
        long l2 = 10L;
        assertEquals(0, BooleanHelper.longSub(l1, l1));
        assertTrue(BooleanHelper.longSub(l2, l1) > 0);
        assertTrue(BooleanHelper.longSub(l1, l2) < 0);
    }

    @Test
    public void testLongExampleFromMath() {
        long l1 = -9223372036854775808L;
        long l2 = 1528L;
        assertEquals(0, BooleanHelper.longSub(l1, l1));
        assertTrue(BooleanHelper.longSub(l2, l1) > 0);
        assertTrue(BooleanHelper.longSub(l1, l2) < 0);
    }


}
