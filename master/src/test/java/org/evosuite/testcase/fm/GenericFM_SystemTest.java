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
package org.evosuite.testcase.fm;

import com.examples.with.different.packagename.fm.GenericFM_ConstrainedType;
import com.examples.with.different.packagename.fm.GenericFM_GenericMethod;
import com.examples.with.different.packagename.fm.GenericFM_StringType;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by gordon on 19/04/2017.
 */
public class GenericFM_SystemTest extends SystemTestBase {

    @Before
    public void init() {
        Properties.P_FUNCTIONAL_MOCKING = 0.5;
        Properties.FUNCTIONAL_MOCKING_PERCENT = 0.0;
    }

    @Test
    public void testGenericsWithTypeParameter() {
        do100percentLineTest(GenericFM_StringType.class);
    }

    @Test
    public void testGenericsMethod() {
        do100percentLineTest(GenericFM_GenericMethod.class);
    }

    @Test
    public void testGenericsWithConstrainedTypeParameter() {
        GeneticAlgorithm<?> ga = do100percentLineTest(GenericFM_ConstrainedType.class);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);
        assertFalse(best.toString().contains("any(java.lang.Number.class)"));
        assertTrue(best.toString().contains("anyInt()"));
    }
}
