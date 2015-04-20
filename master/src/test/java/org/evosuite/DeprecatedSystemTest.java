package org.evosuite;

import com.examples.with.different.packagename.DeprecatedMethods;
import com.examples.with.different.packagename.NullInteger;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

/**
 * By default, we should be able to test @Deprecated methods
 *
 * Created by Andrea Arcuri on 13/03/15.
 */
public class DeprecatedSystemTest extends SystemTest {

    @Test
    public void testDeprecatedMethods() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = DeprecatedMethods.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[] { "-generateSuite", "-class", targetClass };

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        int goals = TestSuiteGenerator.getFitnessFactory().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals("Wrong number of goals: ", 2, goals); //default constructor and deprecated method
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }
}
