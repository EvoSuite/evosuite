package org.evosuite.jdk17;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

public class SmallExSystemTest extends JDK17SystemTestBase {

    @Test
    public void testEx() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = getJDK17CUTCanonicalName("SmallEx");

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateMOSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        TestSuiteChromosome solution = getTestSuiteFromResult(result);
        System.out.println("EvolvedTestSuite:\n" + solution);

        Assert.assertTrue(solution.getCoverage() > 0);
    }
}
