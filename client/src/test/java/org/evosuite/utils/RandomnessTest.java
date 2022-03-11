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
package org.evosuite.utils;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author José Campos
 */
public class RandomnessTest {

    @Test
    public void testNextDoubleWithMinMax() {
        double min = 0.8;
        double max = 0.9;

        for (int i = 0; i < 1_000_000; i++) {
            double r = Randomness.nextDouble(min, max);
            assertTrue(
                    "random double (" + r + ") value has to be in the range [" + min + ", " + max + "]",
                    (Double.compare(r, min) >= 0) && (Double.compare(r, max) <= 0));
        }
    }
}
