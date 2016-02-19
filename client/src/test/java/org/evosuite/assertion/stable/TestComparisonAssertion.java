/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.assertion.stable;

import org.evosuite.assertion.ComparisonTraceEntry;
import org.evosuite.assertion.ComparisonTraceObserver;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by gordon on 01/02/2016.
 */
public class TestComparisonAssertion {

    @Test
    public void testInt() {
        int x = 42;
        int y = 42;
        int z = 43;
        assertTrue(ComparisonTraceEntry.equals(x, y));
        assertFalse(ComparisonTraceEntry.equals(x, z));
    }

    @Test
    public void testIntWrapper() {
        Integer x = new Integer(42);
        Integer y = new Integer(42);
        Integer z = new Integer(43);
        assertTrue(ComparisonTraceEntry.equals(x, y));
        assertFalse(ComparisonTraceEntry.equals(x, z));
    }

    @Test
    public void testFloat() {
        float x = 42.0F;
        float y = 42F;
        float z = 43F;
        assertTrue(ComparisonTraceEntry.equals(x, y));
        assertFalse(ComparisonTraceEntry.equals(x, z));
    }

    @Test
    public void testFloatEpsilon() {
        float x = 42.002F;
        float y = 42.001F;
        float z = 42.1F;
        assertTrue(ComparisonTraceEntry.equals(x, y));
        assertFalse(ComparisonTraceEntry.equals(x, z));
    }

    @Test
    public void testFloatWrapper() {
        Float x = new Float(42.0F);
        Float y = new Float(42.0F);
        Float z = new Float(43.0F);
        assertTrue(ComparisonTraceEntry.equals(x, y));
        assertFalse(ComparisonTraceEntry.equals(x, z));
    }

    @Test
    public void testDouble() {
        double x = 42.0;
        double y = 42.0;
        double z = 43.0;
        assertTrue(ComparisonTraceEntry.equals(x, y));
        assertFalse(ComparisonTraceEntry.equals(x, z));
    }

    @Test
    public void testDoubleNaN() {
        double x = Double.NaN;
        double y = Double.NaN;
        double z = 43.0;
        assertTrue(ComparisonTraceEntry.equals(x, y));
        assertFalse(ComparisonTraceEntry.equals(x, z));
    }

    @Test
    public void testDoubleEpsilon() {
        double x = 42.0001;
        double y = 42.0002;
        double z = 42.1;
        assertTrue(ComparisonTraceEntry.equals(x, y));
        assertFalse(ComparisonTraceEntry.equals(x, z));
    }


    @Test
    public void testDoubleWrapper() {
        Double x = new Double(42);
        Double y = new Double(42);
        Double z = new Double(43);
        assertTrue(ComparisonTraceEntry.equals(x, y));
        assertFalse(ComparisonTraceEntry.equals(x, z));
    }

    @Test
    public void testNonNumeric() {
        String x = "Foo";
        String y = "Foo";
        String z = "Bar";
        assertTrue(ComparisonTraceEntry.equals(x, y));
        assertFalse(ComparisonTraceEntry.equals(x, z));
    }
}
