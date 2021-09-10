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
package org.evosuite.coverage.exception;

import com.examples.with.different.packagename.coverage.ImplicitAndExplicitExceptionInSameMethod;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.statistics.backend.DebugStatisticsBackend;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.ImplicitExplicitException;

import java.util.Map;


public class ImplicitExplicitExceptionsSystemTest extends SystemTestBase {

    private static final Criterion[] defaultCriterion = Properties.CRITERION;

    private static final boolean defaultArchive = Properties.TEST_ARCHIVE;

    @After
    public void resetProperties() {
        Properties.CRITERION = defaultCriterion;
        Properties.TEST_ARCHIVE = defaultArchive;
    }

    @Test
    public void testExceptionFitness_NoArchive() {
        //archive should have no impact
        Properties.TEST_ARCHIVE = false;
        testExceptionFitness();
    }

    @Test
    public void testExceptionFitness_WithArchive() {
        Properties.TEST_ARCHIVE = true;
        testExceptionFitness();
    }


    private void testExceptionFitness() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = ImplicitExplicitException.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.CRITERION = new Criterion[]{Properties.Criterion.EXCEPTION};
        Properties.OUTPUT_VARIABLES = "" +
                RuntimeVariable.Explicit_MethodExceptions + "," +
                RuntimeVariable.Explicit_TypeExceptions + "," +
                RuntimeVariable.Implicit_MethodExceptions + "," +
                RuntimeVariable.Implicit_TypeExceptions;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        double fitness = best.getFitness();
        /*
         * there are 2 undeclared exceptions (both implicit and explicit),
         * and 3 declared: so fit = 1 / (1+5)
         */
        Assert.assertEquals("Wrong fitness: ", 1d / 6d, fitness, 0.0000001);

        Map<String, OutputVariable<?>> map = DebugStatisticsBackend.getLatestWritten();
        Assert.assertNotNull(map);
        Assert.assertEquals(1, map.get(RuntimeVariable.Explicit_MethodExceptions.toString()).getValue());
        Assert.assertEquals(1, map.get(RuntimeVariable.Explicit_TypeExceptions.toString()).getValue());
        Assert.assertEquals(1, map.get(RuntimeVariable.Implicit_MethodExceptions.toString()).getValue());
        Assert.assertEquals(1, map.get(RuntimeVariable.Implicit_TypeExceptions.toString()).getValue());
    }

    @Test
    public void testImplicitAndExplicitExceptionInSameMethod() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = ImplicitAndExplicitExceptionInSameMethod.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.CRITERION = new Criterion[]{Properties.Criterion.EXCEPTION};

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        double fitness = best.getFitness();
        /*
         * there are 2 undeclared exceptions (both implicit and explicit).
         * there are also 2 declared, but same type and same method, so count as 1
         *
         * fit = 1 / (1+3)
         *
         */
        Assert.assertEquals("Wrong fitness: ", 1d / 4d, fitness, 0.001);
    }

}
