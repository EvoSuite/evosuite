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
/**
 *
 */
package org.evosuite.basic;

import static org.junit.Assert.assertEquals;

import com.examples.with.different.packagename.Compositional;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Algorithm;
import org.evosuite.Properties.Criterion;
import org.evosuite.SystemTestBase;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.coverage.exception.ExceptionCoverageSuiteFitness;
import org.evosuite.coverage.method.MethodTraceCoverageSuiteFitness;
import org.evosuite.coverage.method.MethodNoExceptionCoverageSuiteFitness;
import org.evosuite.coverage.io.output.OutputCoverageSuiteFitness;
import org.evosuite.coverage.line.LineCoverageSuiteFitness;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.defuse.DefUseExample1;
import com.examples.with.different.packagename.defuse.GCD;

import java.util.Map;

/**
 * @author Jose Miguel Rojas
 */
public class CompositionalFitnessSystemTest extends SystemTestBase {

    private final double ANY_DOUBLE_1 = 2.0;
    private final double ANY_DOUBLE_2 = 5.0;
    private final double ANY_DOUBLE_3 = 6.0;
    private final double ANY_DOUBLE_4 = 3.0;

    private static final Criterion[] defaultCriterion = Properties.CRITERION;

    @Before
    public void beforeTest() {
        Properties.ALGORITHM = Algorithm.MONOTONICGA;
        Properties.LOG_LEVEL = "debug";
        Properties.PRINT_TO_SYSTEM = true;
        Properties.CLIENT_ON_THREAD = true;
        Properties.ASSERTIONS = false;
    }

    @After
    public void afterTest() {
        Properties.CRITERION = defaultCriterion;
    }

    @Test
    public void testCompositionalTwoFunction() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = DefUseExample1.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.CRITERION = new Properties.Criterion[2];
        Properties.CRITERION[0] = Criterion.DEFUSE;
        Properties.CRITERION[1] = Criterion.METHODNOEXCEPTION;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        Map<FitnessFunction<?>, Double> fitnesses = best.getFitnessValues();
        double sum = 0.0;
        double cov = 0.0;
        for (FitnessFunction<?> fitness : fitnesses.keySet()) {
            sum += fitnesses.get(fitness);
            cov += best.getCoverage(fitness);
            assert (fitnesses.get(fitness) == best.getFitness(fitness));
        }
        cov = cov / best.getCoverageValues().size();
        Assert.assertEquals("Inconsistent fitness: ", sum, best.getFitness(), 0.001);
        Assert.assertEquals("Inconsistent coverage: ", cov, best.getCoverage(), 0.001);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGCDExample() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GCD.class.getCanonicalName();

        Properties.TEST_ARCHIVE = true;
        Properties.TARGET_CLASS = targetClass;
        Properties.CRITERION = new Properties.Criterion[2];
        Properties.CRITERION[0] = Criterion.ONLYBRANCH;
        Properties.CRITERION[1] = Criterion.ONLYMUTATION;
        Properties.ANALYSIS_CRITERIA = "OnlyBranch,ONLYMUTATION,METHOD,exception";

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGetFitnessForNoFunctionNoCompositional() {
        TestSuiteChromosome c = new TestSuiteChromosome();
        assertEquals(0.0, c.getFitness(), 0.001);
    }

    @Test
    public void testCompositionalGetFitnessForNoFunction() {
        TestSuiteChromosome c = new TestSuiteChromosome();
        assertEquals(0.0, c.getFitness(), 0.001);
    }

    @Test
    public void testCompositionalGetFitnessForOneFunction() {
        TestSuiteChromosome c = new TestSuiteChromosome();
        LineCoverageSuiteFitness f1 = new LineCoverageSuiteFitness();
        c.addFitness(f1);
        c.setFitness(f1, ANY_DOUBLE_1);
        assertEquals(ANY_DOUBLE_1, c.getFitness(), 0.001);
    }

    @Test
    public void testCompositionalGetFitnessForTwoFunctions() {
        TestSuiteChromosome c = new TestSuiteChromosome();
        LineCoverageSuiteFitness f1 = new LineCoverageSuiteFitness();
        c.addFitness(f1);
        c.setFitness(f1, ANY_DOUBLE_1);
        BranchCoverageSuiteFitness f2 = new BranchCoverageSuiteFitness();
        c.addFitness(f2);
        c.setFitness(f2, ANY_DOUBLE_2);
        assertEquals(ANY_DOUBLE_1 + ANY_DOUBLE_2, c.getFitness(), 0.001);
    }

    @Test
    public void testCompositionalGetFitnessForSeveralFunctions() {
        Properties.TARGET_CLASS = Compositional.class.getCanonicalName();

        TestSuiteChromosome c = new TestSuiteChromosome();
        MethodTraceCoverageSuiteFitness f1 = new MethodTraceCoverageSuiteFitness();
        c.addFitness(f1);
        c.setFitness(f1, ANY_DOUBLE_1);
        MethodNoExceptionCoverageSuiteFitness f2 = new MethodNoExceptionCoverageSuiteFitness();
        c.addFitness(f2);
        c.setFitness(f2, ANY_DOUBLE_2);
        OutputCoverageSuiteFitness f3 = new OutputCoverageSuiteFitness();
        c.addFitness(f3);
        c.setFitness(f3, ANY_DOUBLE_3);
        ExceptionCoverageSuiteFitness f4 = new ExceptionCoverageSuiteFitness();
        c.addFitness(f4);
        c.setFitness(f4, ANY_DOUBLE_4);
        double sum = ANY_DOUBLE_1 + ANY_DOUBLE_2 + ANY_DOUBLE_3 + ANY_DOUBLE_4;
        assertEquals(sum, c.getFitness(), 0.001);
    }
}
