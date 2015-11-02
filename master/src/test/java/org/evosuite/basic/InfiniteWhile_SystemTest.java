/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.basic;

import com.examples.with.different.packagename.InfiniteWhile;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.runtime.TooManyResourcesException;
import org.evosuite.runtime.instrumentation.EvoClassLoader;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.fail;

/**
 * Created by Andrea Arcuri on 29/03/15.
 */
public class InfiniteWhile_SystemTest  extends SystemTest {

    @Test(timeout = 5000)
    public void testLoading() throws Exception{
        EvoClassLoader loader = new EvoClassLoader();
        Class<?> clazz = loader.loadClass(InfiniteWhile.class.getCanonicalName());

        try {
            clazz.newInstance();
            fail();
        }catch(TooManyResourcesException e){
            //expected
        }
    }

    @Test//(timeout = 30_000)
    public void systemTest(){

        EvoSuite evosuite = new EvoSuite();

        String targetClass = InfiniteWhile.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.SEARCH_BUDGET = 10;
        Properties.TIMEOUT = 5000;
        Properties.STOPPING_CONDITION = Properties.StoppingCondition.MAXTIME;
        Properties.JUNIT_TESTS = true;
        String[] command = new String[] { "-generateSuite", "-class", targetClass };

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

        System.out.println("EvolvedTestSuite:\n" + best);

        Assert.assertEquals("Should contain one test: ", 1, best.size());
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }
}
