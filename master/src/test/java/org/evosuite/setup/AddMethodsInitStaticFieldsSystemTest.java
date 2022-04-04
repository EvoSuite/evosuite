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
package org.evosuite.setup;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.staticusage.Class1;
import com.examples.with.different.packagename.staticusage.DirectAccessStaticField;

public class AddMethodsInitStaticFieldsSystemTest extends SystemTestBase {

    private boolean ADD_METHODS_INITIALIZING_STATIC_FIELDS;

    @Before
    public void prepareTest() {
        ADD_METHODS_INITIALIZING_STATIC_FIELDS = Properties.HANDLE_STATIC_FIELDS;
        Properties.HANDLE_STATIC_FIELDS = true;
    }

    @Test
    public void testStaticMethods() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = Class1.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        String[] command = new String[]{"-generateSuite", "-class",
                targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);
        double best_fitness = best.getFitness();
        Assert.assertEquals("Optimal coverage not reached: " + best_fitness, 0.0, best_fitness, 0.0);
    }

    @Test
    public void testStaticFields() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = DirectAccessStaticField.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        String[] command = new String[]{"-generateSuite", "-class",
                targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);
        double best_fitness = best.getFitness();
        Assert.assertEquals("Optimal coverage not reached: " + best_fitness, 0.0, best_fitness, 0.0);
    }

    @After
    public void restore() {
        Properties.HANDLE_STATIC_FIELDS = ADD_METHODS_INITIALIZING_STATIC_FIELDS;
    }


}
