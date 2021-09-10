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
 * <p>StandardChemicalReactionSystemTest</p>
 *
 * @author José Campos
 */
public class StandardChemicalReactionSystemTest extends SystemTestBase {

    private GeneticAlgorithm<?> test(double molecular_collision_rate, int decomposition_threshold,
                                     int synthesis_threshold) {
        Properties.ALGORITHM = Properties.Algorithm.STANDARD_CHEMICAL_REACTION;
        Properties.TEST_ARCHIVE = false;
        Properties.SEARCH_BUDGET = 15_000;

        Properties.POPULATION = 10;
        Properties.MOLECULAR_COLLISION_RATE = molecular_collision_rate;
        Properties.DECOMPOSITION_THRESHOLD = decomposition_threshold;
        Properties.SYNTHESIS_THRESHOLD = synthesis_threshold;

        String targetClass = BMICalculator.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        EvoSuite evoSuite = new EvoSuite();
        GeneticAlgorithm<?> ga = getGAFromResult(evoSuite.parseCommandLine(command));
        Assert.assertEquals(StandardChemicalReaction.class, ga.getClass());

        return ga;
    }

    @Test
    public void testOnwallIneffectiveCollision() {
        GeneticAlgorithm<?> ga = test(0.0, 1_000_000, -1);

        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);
        Assert.assertTrue(Double.compare(best.getCoverage(), 7.0 / 9.0) > 0);
    }

    @Test
    public void testDecomposition() {
        GeneticAlgorithm<?> ga = test(0.0, 1, -1);

        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);
        Assert.assertTrue(Double.compare(best.getCoverage(), 6.0 / 9.0) > 0);
    }

    @Test
    public void testIntermolecularIneffectiveCollision() {
        GeneticAlgorithm<?> ga = test(1.0, -1, -1);

        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);
        Assert.assertTrue(Double.compare(best.getCoverage(), 6.0 / 9.0) > 0);
    }

    @Test
    public void testSynthesis() {
        GeneticAlgorithm<?> ga = test(1.0, -1, 1_000_000);

        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);
        Assert.assertTrue(Double.compare(best.getCoverage(), 7.0 / 9.0) > 0);
    }

    @Test
    public void testIntegration() {
        GeneticAlgorithm<?> ga = test(0.2, 500, 10);

        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);
        // the integration test must covers all goals as it uses all functionalities
        Assert.assertEquals(0.0, best.getFitness(), 0.0);
        Assert.assertEquals(1d, best.getCoverage(), 0.001);
    }
}
