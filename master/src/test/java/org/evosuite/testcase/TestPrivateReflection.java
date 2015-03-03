package org.evosuite.testcase;

import com.examples.with.different.packagename.pool.DependencyClass;
import com.examples.with.different.packagename.reflection.OnlyPrivateMethods;
import com.examples.with.different.packagename.reflection.PrivateFieldInPrivateMethod;
import com.examples.with.different.packagename.reflection.PrivateFieldInPublicMethod;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by Andrea Arcuri on 02/03/15.
 */
public class TestPrivateReflection extends SystemTest {

    @Test
    public void testPrivateFieldInPrivateMethod() throws IOException {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = PrivateFieldInPrivateMethod.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.P_REFLECTION_ON_PRIVATE = 0.9;
        Properties.REFLECTION_START_PERCENT = 0.0;

        String[] command = new String[] { "-generateSuite", "-class", targetClass };
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }


    @Test
    public void testPrivateFieldInPublicMethod() throws IOException {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = PrivateFieldInPublicMethod.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.P_REFLECTION_ON_PRIVATE = 0.9;
        Properties.REFLECTION_START_PERCENT = 0.0;

        String[] command = new String[] { "-generateSuite", "-class", targetClass };
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }


    @Test
    public void testOnlyPrivateMethods() throws IOException {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = OnlyPrivateMethods.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.P_REFLECTION_ON_PRIVATE = 0.9;
        Properties.REFLECTION_START_PERCENT = 0.0;

        String[] command = new String[] { "-generateSuite", "-class", targetClass };
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testOnlyPrivateMethods_noReflection() throws IOException {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = OnlyPrivateMethods.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.P_REFLECTION_ON_PRIVATE = 0.0;
        Properties.REFLECTION_START_PERCENT = 0.0;

        String[] command = new String[] { "-generateSuite", "-class", targetClass };
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        Assert.assertTrue(best.getCoverage() < 1d);
    }

    @Test
    public void testOnlyPrivateMethods_noTime() throws IOException {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = OnlyPrivateMethods.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.P_REFLECTION_ON_PRIVATE = 0.9;
        Properties.REFLECTION_START_PERCENT = 1.0; //would never start

        String[] command = new String[] { "-generateSuite", "-class", targetClass };
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        Assert.assertTrue( best.getCoverage() < 1d);
    }

}
