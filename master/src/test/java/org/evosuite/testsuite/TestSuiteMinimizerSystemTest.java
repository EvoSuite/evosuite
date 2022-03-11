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
package org.evosuite.testsuite;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.coverage.MethodReturnsPrimitive;

public class TestSuiteMinimizerSystemTest extends SystemTestBase {

    private final boolean oldMinimizeValues = Properties.MINIMIZE_VALUES;

    @After
    public void restoreProperties() {
        Properties.MINIMIZE_VALUES = oldMinimizeValues;
    }

    @Test
    public void testWithOneFitnessFunctionNoValueMinimization() {
        Properties.CRITERION = new Criterion[1];
        Properties.CRITERION[0] = Criterion.ONLYBRANCH;

        Properties.MINIMIZE_VALUES = false;

        EvoSuite evosuite = new EvoSuite();

        String targetClass = MethodReturnsPrimitive.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{
                "-generateSuite",
                "-class", targetClass
        };

        Object result = evosuite.parseCommandLine(command);
        Assert.assertNotNull(result);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome c = ga.getBestIndividual();
        System.out.println(c.toString());

        Assert.assertEquals(0.0, c.getFitness(), 0.0);
        Assert.assertEquals(1.0, c.getCoverage(), 0.0);
        Assert.assertEquals(6.0, c.getNumOfCoveredGoals(ga.getFitnessFunction()), 0.0);
        Assert.assertEquals(5, c.size());
    }

    @Test
    public void testWithOneFitnessFunctionWithValueMinimization() {
        Properties.CRITERION = new Criterion[1];
        Properties.CRITERION[0] = Criterion.ONLYBRANCH;

        Properties.MINIMIZE_VALUES = true;
        Properties.MINIMIZE_SKIP_COINCIDENTAL = false;
        Properties.MINIMIZE_SECOND_PASS = false;
        EvoSuite evosuite = new EvoSuite();

        String targetClass = MethodReturnsPrimitive.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{
                "-generateSuite",
                "-class", targetClass
        };

        Object result = evosuite.parseCommandLine(command);
        Assert.assertNotNull(result);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome c = ga.getBestIndividual();
        System.out.println(c.toString());

        Assert.assertEquals(0.0, c.getFitness(), 0.0);
        Assert.assertEquals(1.0, c.getCoverage(), 0.0);
        Assert.assertEquals(6.0, c.getNumOfCoveredGoals(ga.getFitnessFunction()), 0.0);
        Assert.assertEquals(5, c.size());
    }

    @Test
    public void testWithOneFitnessFunctionWithValueMinimizationAndSkippingCoveredGoals() {
        Properties.CRITERION = new Criterion[1];
        Properties.CRITERION[0] = Criterion.ONLYBRANCH;

        Properties.MINIMIZE_VALUES = true;
        Properties.MINIMIZE_SKIP_COINCIDENTAL = true;
        Properties.MINIMIZE_SECOND_PASS = true;
        EvoSuite evosuite = new EvoSuite();

        String targetClass = MethodReturnsPrimitive.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{
                "-generateSuite",
                "-class", targetClass
        };

        Object result = evosuite.parseCommandLine(command);
        Assert.assertNotNull(result);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome c = ga.getBestIndividual();
        System.out.println(c.toString());

        Assert.assertEquals(0.0, c.getFitness(), 0.0);
        Assert.assertEquals(1.0, c.getCoverage(), 0.0);
        Assert.assertEquals(6.0, c.getNumOfCoveredGoals(ga.getFitnessFunction()), 0.0);
        Assert.assertEquals(5, c.size());
    }


    @Test
    public void testWithTwo() {
        Properties.CRITERION = new Criterion[2];
        Properties.CRITERION[0] = Criterion.ONLYBRANCH;
        Properties.CRITERION[1] = Criterion.LINE;

        Properties.MINIMIZE_VALUES = true;

        EvoSuite evosuite = new EvoSuite();

        String targetClass = MethodReturnsPrimitive.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{
                "-generateSuite",
                "-class", targetClass
        };

        Object result = evosuite.parseCommandLine(command);
        Assert.assertNotNull(result);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);

        TestSuiteChromosome c = ga.getBestIndividual();

        final FitnessFunction<TestSuiteChromosome> onlybranch = ga.getFitnessFunctions().get(0);
        final FitnessFunction<TestSuiteChromosome> line = ga.getFitnessFunctions().get(1);

        Assert.assertEquals(0.0, c.getFitness(onlybranch), 0.0);
        Assert.assertEquals(0.0, c.getFitness(line), 0.0);

        Assert.assertEquals(1.0, c.getCoverage(onlybranch), 0.0);
        Assert.assertEquals(1.0, c.getCoverage(line), 0.0);

        Assert.assertEquals(6.0, c.getNumOfCoveredGoals(onlybranch), 0.0);
        Assert.assertEquals(10.0, c.getNumOfCoveredGoals(line), 0.0);
    }
}
