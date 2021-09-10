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
package org.evosuite.coverage.cbranch;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.SystemTestBase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.coverage.IndirectlyCoverableBranches;

public class CBranchSystemTest extends SystemTestBase {

    private static final Criterion[] defaultCriterion = Properties.CRITERION;

    private static final boolean defaultArchive = Properties.TEST_ARCHIVE;

    @After
    public void resetProperties() {
        Properties.CRITERION = defaultCriterion;
        Properties.TEST_ARCHIVE = defaultArchive;
    }

    @Before
    public void beforeTest() {
        Properties.CRITERION[0] = Criterion.CBRANCH;
    }

    @Test
    public void testCBranchFitnessWithArchive() {
        Properties.TEST_ARCHIVE = true;
        testBranchFitness();
    }

    @Test
    public void testCBranchFitnessWithoutArchive() {
        Properties.TEST_ARCHIVE = false;
        Properties.SEARCH_BUDGET = 50000;
        testBranchFitness();
    }

    public void testBranchFitness() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = IndirectlyCoverableBranches.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();

        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(7, goals);
        Assert.assertEquals(5, best.size());
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

}
