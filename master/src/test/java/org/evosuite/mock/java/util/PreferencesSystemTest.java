package org.evosuite.mock.java.util;

import com.examples.with.different.packagename.mock.java.util.prefs.PrefsNode;
import com.examples.with.different.packagename.mock.java.util.prefs.PrefsSystem;
import com.examples.with.different.packagename.mock.java.util.prefs.PrefsUser;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by gordon on 26/12/2016.
 */
public class PreferencesSystemTest extends SystemTestBase {

    @Test
    public void testUserPreferences() throws Exception{
        String targetClass = PrefsUser.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        EvoSuite evosuite = new EvoSuite();
        String[] command = new String[] { "-generateSuite", "-class", targetClass };
        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

        System.out.println(best.toString());

        Assert.assertNotNull(best);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testSystemPreferences() throws Exception{
        String targetClass = PrefsSystem.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        EvoSuite evosuite = new EvoSuite();
        String[] command = new String[] { "-generateSuite", "-class", targetClass };
        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

        System.out.println(best.toString());

        Assert.assertNotNull(best);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }


    @Test
    public void testNodePreferences() throws Exception{
        String targetClass = PrefsNode.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        EvoSuite evosuite = new EvoSuite();
        String[] command = new String[] { "-generateSuite", "-class", targetClass };
        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

        System.out.println(best.toString());

        Assert.assertNotNull(best);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

}
