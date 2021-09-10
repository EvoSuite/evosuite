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
package org.evosuite.ga.metaheuristics;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;
import com.examples.with.different.packagename.BMICalculator;

/**
 * System tests for the implementation of the Many Independent Objective (MIO) algorithm
 *
 * @author José Campos
 */
public class MIOSystemTest extends SystemTestBase {

    private void test(double exploitation_starts_at_percent) {
        Properties.ALGORITHM = Properties.Algorithm.MIO;
        Properties.ARCHIVE_TYPE = Properties.ArchiveType.MIO;

        Properties.NUMBER_OF_TESTS_PER_TARGET = 3;
        Properties.EXPLOITATION_STARTS_AT_PERCENT = exploitation_starts_at_percent;

        String targetClass = BMICalculator.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateMOSuite", "-Dalgorithm=MIO", "-Dstrategy=MOSuite",
                "-class", targetClass};

        EvoSuite evoSuite = new EvoSuite();

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(evoSuite.parseCommandLine(command));
        Assert.assertEquals(TestSuiteAdapter.class, ga.getClass());

        @SuppressWarnings("unchecked")
        TestSuiteAdapter<MIO> mio = (TestSuiteAdapter<MIO>) ga;

        Assert.assertEquals(MIO.class, mio.getAlgorithm().getClass());

        TestSuiteChromosome best = mio.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);
        Assert.assertEquals(0.0, best.getFitness(), 0.0);
        Assert.assertEquals(1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testRandomSampling() {
        test(1.0);
    }

    @Test
    public void testArchiveSampling() {
        test(0.0);
    }

    @Test
    public void testIntegration() {
        test(0.5);
    }

}
