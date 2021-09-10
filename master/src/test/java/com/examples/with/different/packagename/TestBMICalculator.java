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
package com.examples.with.different.packagename;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class TestBMICalculator {

    @Test
    public void testConstructor() throws Throwable {
        assertNotNull(new BMICalculator());
    }

    @Test
    public void testVeryObese() throws Throwable {
        String string0 = BMICalculator.calculateBMICategory((-1.0), 2138.41);
        assertEquals("very obese", string0);
    }

    @Test
    public void testUnderweight() throws Throwable {
        String string0 = BMICalculator.calculateBMICategory(2010.42781, 40.0);
        assertEquals("underweight", string0);
    }

    @Test
    public void testOverweight() throws Throwable {
        String string0 = BMICalculator.calculateBMICategory((-1.0), 25.0);
        assertEquals("overweight", string0);
    }

    @Test
    public void testObese() throws Throwable {
        String string0 = BMICalculator.calculateBMICategory((-1.0), 30.0);
        assertEquals("obese", string0);
    }

    @Test
    public void testHealthy() throws Throwable {
        String string0 = BMICalculator.calculateBMICategory((-1.0), 18.5);
        assertEquals("healthy", string0);
    }
}
