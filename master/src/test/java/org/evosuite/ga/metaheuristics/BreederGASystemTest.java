/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.ga.metaheuristics;

import com.examples.with.different.packagename.BMICalculator;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

public class BreederGASystemTest extends SystemTestBase {

    @Test
    public void testLambdaGAIntegration() {
        Properties.ALGORITHM = Properties.Algorithm.BREEDER_GA;
        Properties.TRUNCATION_RATE = 0.1;

        EvoSuite evoSuite = new EvoSuite();

        String targetClass = BMICalculator.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evoSuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);

        Assert.assertEquals(BreederGA.class, ga.getClass());

        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);
        Assert.assertEquals(0.0, best.getFitness(), 0.0);
        Assert.assertEquals(1d, best.getCoverage(), 0.001);
    }
}
