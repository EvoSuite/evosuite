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
package org.evosuite.coverage.statement;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;
import com.examples.with.different.packagename.IntExample;

public class StatementCoverageFitnessFunctionSystemTest extends SystemTestBase {

    private void test() {
        Properties.CRITERION = new Properties.Criterion[]{Criterion.STATEMENT};

        String targetClass = IntExample.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        EvoSuite evosuite = new EvoSuite();
        String[] command = new String[]{"-class", targetClass, "-generateSuite"};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size();
        Assert.assertEquals(20, goals);
        Assert.assertEquals(goals, best.getNumOfCoveredGoals());
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void test100StatementCoverageWithArchive() {
        Properties.TEST_ARCHIVE = true;
        this.test();
    }

    @Test
    public void test100StatementCoverageWithoutArchive() {
        Properties.TEST_ARCHIVE = false;
        this.test();
    }
}
