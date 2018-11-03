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

        String[] command = new String[] {"-generateSuite", "-class", targetClass};

        Object result = evoSuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);

        Assert.assertEquals(BreederGA.class, ga.getClass());

        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);
        Assert.assertEquals(0.0, best.getFitness(), 0.0);
        Assert.assertEquals(1d, best.getCoverage(), 0.001);
    }
}
