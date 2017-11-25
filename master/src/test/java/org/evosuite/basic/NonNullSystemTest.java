package org.evosuite.basic;

import com.examples.with.different.packagename.NonNull;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

public class NonNullSystemTest extends SystemTestBase {

    @Test
    public void testNonNull() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = NonNull.class.getCanonicalName();
        Properties.NULL_PROBABILITY = 1.0;
        Properties.TARGET_CLASS = targetClass;
        Properties.HONOUR_DATA_ANNOTATIONS = true;
        String[] command = new String[] { "-generateSuite", "-class", targetClass };

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        Assert.assertFalse(best.toString().contains("(Object) null"));
        Assert.assertFalse(best.toString().contains("null"));

    }
}
