package org.evosuite.mock.java.lang;

import com.examples.with.different.packagename.mock.java.lang.ExtendingThread;
import com.examples.with.different.packagename.mock.java.lang.LongSleep;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by foo on 16/02/15.
 */
public class MockThreadSystemTest extends SystemTest{

    @Test
    public void testCallingOfRun(){
        String targetClass = ExtendingThread.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.REPLACE_CALLS = true;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.LINE};

        EvoSuite evosuite = new EvoSuite();
        String[] command = new String[] { "-generateSuite", "-class", targetClass };
        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        Assert.assertNotNull(best);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }


    @Test
    public void testLongSleep(){
        String targetClass = LongSleep.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.REPLACE_CALLS = true;

        EvoSuite evosuite = new EvoSuite();
        String[] command = new String[] { "-generateSuite", "-class", targetClass };
        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        Assert.assertNotNull(best);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }
}
