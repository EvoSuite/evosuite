package org.evosuite.basic;

import com.examples.with.different.packagename.ATM;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

public class ATMSystemTest  extends SystemTestBase {

    @Test
    public void testNullString() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = ATM.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[] { "-generateSuite", "-class", targetClass , "-criterion" , "branch:mutation"};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals("Wrong number of goals: ", 20, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }
}
