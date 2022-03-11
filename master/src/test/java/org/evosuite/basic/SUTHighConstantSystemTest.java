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
package org.evosuite.basic;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.*;

import com.examples.with.different.packagename.HighConstant;


/**
 * @author Andrea Arcuri
 */
public class SUTHighConstantSystemTest extends SystemTestBase {

    public static final double defaultPrimitivePool = Properties.PRIMITIVE_POOL;

    @After
    public void resetProperties() {
        Properties.PRIMITIVE_POOL = defaultPrimitivePool;
    }

    @Test
    public void testNoPrimitivePool() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = HighConstant.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.PRIMITIVE_POOL = 0;

        String[] command = new String[]{
                "-generateSuite",
                "-class",
                targetClass
        };

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println(best.toString());
        /*
         * there are 2 branches and one method, so 3 targets, of which we cover only 2
         */
        Assert.assertEquals("Non-expected coverage: ", 2d / 3d, best.getCoverage(), 0.001);

        Assert.assertEquals("Wrong number of test cases: ", 1, best.size());
        /*
         * - Constructor
         * - variable init
         * - method call
         */
        if (Properties.INLINE)
            Assert.assertEquals("Wrong number of statements: ", 2, best.getTestChromosome(0).getTestCase().size());
        else
            Assert.assertEquals("Wrong number of statements: ", 3, best.getTestChromosome(0).getTestCase().size());
    }

    @Test
    public void testUsingPrimitivePool() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = HighConstant.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.PRIMITIVE_POOL = 0.8;

        String[] command = new String[]{
                "-generateSuite",
                "-class",
                targetClass
        };

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);

        Assert.assertEquals("Wrong number of generations: ", 0, ga.getAge());
        TestSuiteChromosome best = ga.getBestIndividual();
        //Assert.assertEquals("Wrong number of test cases: ",2 , best.size());
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
        //Assert.assertEquals("Wrong number of statements: ",3,best.getTestChromosome(0).getTestCase().size());
        //Assert.assertEquals("Wrong number of statements: ",3,best.getTestChromosome(1).getTestCase().size());
    }
}
