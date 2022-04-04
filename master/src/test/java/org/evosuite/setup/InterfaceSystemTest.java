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
package org.evosuite.setup;

import com.examples.with.different.packagename.interfaces.InterfaceWithDefaultMethods;
import com.examples.with.different.packagename.interfaces.InterfaceWithStaticMethods;
import com.examples.with.different.packagename.interfaces.InterfaceWithoutSubclasses;
import com.examples.with.different.packagename.interfaces.StandardInterface;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

public class InterfaceSystemTest extends SystemTestBase {

    @Test
    public void testInterfaceWithoutSubclasses() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = InterfaceWithoutSubclasses.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        String[] command = new String[]{"-generateSuite", "-class",
                targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Assert.assertEquals(0.0, best.getFitness(), 0.0);

        for (TestFitnessFactory<? extends TestFitnessFunction> ff : TestGenerationStrategy.getFitnessFactories()) {
            Assert.assertEquals(0, ff.getCoverageGoals().size());
        }
    }

    @Test
    public void testInterfaceWithSubclasses() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = StandardInterface.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        String[] command = new String[]{"-generateSuite", "-class",
                targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Assert.assertEquals(0.0, best.getFitness(), 0.0);

        for (TestFitnessFactory<? extends TestFitnessFunction> ff : TestGenerationStrategy.getFitnessFactories()) {
            Assert.assertEquals(0, ff.getCoverageGoals().size());
        }
    }

    @Test
    public void testInterfaceWithStaticMethods() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = InterfaceWithStaticMethods.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.METHOD, Properties.Criterion.BRANCH, Properties.Criterion.LINE};
        String[] command = new String[]{"-generateSuite", "-class",
                targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Assert.assertEquals(0.0, best.getFitness(), 0.0);

        for (TestFitnessFactory<? extends TestFitnessFunction> ff : TestGenerationStrategy.getFitnessFactories()) {
            Assert.assertEquals(1, ff.getCoverageGoals().size());
        }
    }

    @Test
    public void testInterfaceWithDefaultMethods() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = InterfaceWithDefaultMethods.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.METHOD, Properties.Criterion.BRANCH, Properties.Criterion.LINE};
        String[] command = new String[]{"-generateSuite", "-class",
                targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Assert.assertEquals(0.0, best.getFitness(), 0.0);

        for (TestFitnessFactory<? extends TestFitnessFunction> ff : TestGenerationStrategy.getFitnessFactories()) {
            Assert.assertEquals(1, ff.getCoverageGoals().size());
        }
    }
}
