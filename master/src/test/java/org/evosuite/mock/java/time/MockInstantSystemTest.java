package org.evosuite.mock.java.time;

import com.examples.with.different.packagename.mock.java.time.InstantExample;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by gordon on 25/01/2016.
 */
public class MockInstantSystemTest extends SystemTestBase {

    @Test
    public void testInstant() throws Exception{
        String targetClass = InstantExample.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.JUNIT_TESTS = true;
        Properties.JUNIT_CHECK = true;
        Properties.REPLACE_CALLS = true;
        Properties.OUTPUT_VARIABLES=""+ RuntimeVariable.HadUnstableTests;

        EvoSuite evosuite = new EvoSuite();
        String[] command = new String[] { "-generateSuite", "-class", targetClass };
        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

        Assert.assertNotNull(best);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);

        checkUnstable();
    }
}
