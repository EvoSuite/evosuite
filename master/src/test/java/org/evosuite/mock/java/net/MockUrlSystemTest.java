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
package org.evosuite.mock.java.net;

import com.examples.with.different.packagename.mock.java.net.ReadFromInputURL;
import com.examples.with.different.packagename.mock.java.net.ReadFromURL;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.instrumentation.EvoClassLoader;
import org.evosuite.runtime.instrumentation.MethodCallReplacementCache;
import org.evosuite.runtime.testdata.EvoSuiteURL;
import org.evosuite.runtime.testdata.NetworkHandling;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * Created by arcuri on 12/19/14.
 */
public class MockUrlSystemTest extends SystemTestBase {

    @Test(timeout = 5000)
    public void testLoading_ReadFromURL() throws Exception {
        //for some reason, this class failed when using loop limit in the search

        RuntimeSettings.useVNET = true;
        RuntimeSettings.maxNumberOfIterationsPerLoop = 100_000;
        MethodCallReplacementCache.resetSingleton();
        org.evosuite.runtime.Runtime.getInstance().resetRuntime();

        EvoClassLoader loader = new EvoClassLoader();
        Class<?> clazz = loader.loadClass(ReadFromURL.class.getCanonicalName());


        Method m = clazz.getMethod("checkResource");
        boolean b = (Boolean) m.invoke(null);
        Assert.assertFalse(b);

        EvoSuiteURL evoURL = new EvoSuiteURL("http://www.evosuite.org/index.html");
        NetworkHandling.createRemoteTextFile(evoURL, "foo");
        b = (Boolean) m.invoke(null);
        Assert.assertTrue(b);
    }


    @Test
    public void testCheckResource() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = ReadFromURL.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.SEARCH_BUDGET = 20000;
        Properties.VIRTUAL_NET = true;
        Properties.MAX_LOOP_ITERATIONS = 100000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        Assert.assertNotNull(result);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testUrlAsInput() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = ReadFromInputURL.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.SEARCH_BUDGET = 20000;
        Properties.VIRTUAL_NET = true;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        Assert.assertNotNull(result);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals("Wrong number of goals: ", 5, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }


}
