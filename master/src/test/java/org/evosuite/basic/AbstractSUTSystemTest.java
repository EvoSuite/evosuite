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
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.AbstractClassWithStaticFactory;
import com.examples.with.different.packagename.AbstractSuperClass;

import java.util.ArrayList;
import java.util.List;

public class AbstractSUTSystemTest extends SystemTestBase {

    @Test
    public void testAbstractSUT() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = AbstractSuperClass.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        Assert.assertNotNull(result);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testAbstractSUTWithOnlyStaticFactory() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = AbstractClassWithStaticFactory.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        // Just using line coverage because it's not clear if covering default constructors of anonymous and abstract classes makes sense...
        List<Properties.Criterion> criteria = new ArrayList<>();
        criteria.add(Properties.Criterion.LINE);
        Properties.CRITERION = criteria.toArray(Properties.CRITERION);

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        Assert.assertNotNull(result);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

}
