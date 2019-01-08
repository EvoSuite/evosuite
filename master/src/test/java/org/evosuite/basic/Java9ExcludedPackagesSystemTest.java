package org.evosuite.basic;

import com.examples.with.different.packagename.Java9ExcludedPackage;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

public class Java9ExcludedPackagesSystemTest extends SystemTestBase {
    @Test
    public void testSunGraphics2DPackage() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = Java9ExcludedPackage.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.print(best.toString());
        Assert.assertFalse(best.toString().contains("SunGraphics2D"));
        Assert.assertTrue(best.toString().contains("testMe"));
    }
}
