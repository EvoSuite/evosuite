package org.evosuite;

import com.examples.with.different.packagename.InfiniteWhile;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.runtime.TooManyResourcesException;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Andrea Arcuri on 29/03/15.
 */
public class InfiniteWhile_SystemTest  extends SystemTest{

    @Test(timeout = 10_000)
    public void test(){

        EvoSuite evosuite = new EvoSuite();

        String targetClass = InfiniteWhile.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        String[] command = new String[] { "-generateSuite", "-class", targetClass };

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        String code = best.toString();
        System.out.println("EvolvedTestSuite:\n" + best);

        //the test should catch the exception
        Assert.assertTrue(code.contains(""+ TooManyResourcesException.class.getSimpleName()));
    }
}
