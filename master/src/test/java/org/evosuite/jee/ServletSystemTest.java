/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.jee;

import com.examples.with.different.packagename.jee.servlet.PostPutGetServlet;
import com.examples.with.different.packagename.jee.servlet.SimpleHttpServlet;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Andrea Arcuri on 01/07/15.
 */
public class ServletSystemTest extends SystemTestBase {

    @Before()
    public void init(){
        /*
            likely we ll not need handling of Servlets... however, too early to remove all
             the code written so far...
         */
        Assume.assumeTrue(Properties.HANDLE_SERVLETS);
    }

    @Test
    public void testSimpleCase_noJEE(){
        EvoSuite evosuite = new EvoSuite();

        String targetClass = SimpleHttpServlet.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.LINE};
        Properties.JEE = false;

        String[] command = new String[] { "-generateSuite", "-class", targetClass };

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        Assert.assertTrue(best.getCoverage() < 1);
    }

    @Test
    public void testCombination() {
        testSimpleCase_noJEE();
        super.resetStaticVariables(); //After
        super.setDefaultPropertiesForTestCases(); //Before
        testSimpleCase_withJEE();
    }

    @Test
    public void testInversedCombination() {
        testSimpleCase_withJEE();
        super.resetStaticVariables(); //After
        super.setDefaultPropertiesForTestCases(); //Before
        testSimpleCase_noJEE();
    }


    @Test
    public void testSimpleCase_withJEE(){
        Properties.JEE = true;
        do100percentLineTest(SimpleHttpServlet.class);
    }

    @Test
    public void testPostPutGetServlet(){
        Properties.JEE = true;
        do100percentLineTest(PostPutGetServlet.class);
    }

}
