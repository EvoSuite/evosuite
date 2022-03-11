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
package org.evosuite.junit;

import org.evosuite.junit.examples.*;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CoverageAnalysisTest {

    @Test
    public void isTest() {
        assertFalse(CoverageAnalysis.isTest(Not_A_Test.class));

        assertTrue(CoverageAnalysis.isTest(JUnit3Test.class));
        assertFalse(CoverageAnalysis.isTest(JUnit3Suite.class));
        assertFalse(CoverageAnalysis.isTest(AbstractJUnit3Test.class));

        assertTrue(CoverageAnalysis.isTest(JUnit4Test.class));
        assertTrue(CoverageAnalysis.isTest(JUnit4EvoSuiteTest.class));
        assertFalse(CoverageAnalysis.isTest(JUnit4Suite.class));
        assertFalse(CoverageAnalysis.isTest(JUnit4Categories.class));
        assertTrue(CoverageAnalysis.isTest(JUnit4ParameterizedTest.class));
    }
}
