package org.evosuite.basic;

import com.examples.with.different.packagename.MethodWithClassParameter;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

public class MethodWithClassParameterSystemTest extends SystemTestBase {

    @Test
    public void testIfClassParameterCanBeInstantiatedCorrectly() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = MethodWithClassParameter.class.getCanonicalName();
        Properties.NULL_PROBABILITY = 1.0;
        Properties.TARGET_CLASS = targetClass;
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

//        System.out.println("EvolvedTestSuite:\n" + best);
        Assert.assertFalse(best.toString().contains("Class<Foo> class0 = Class.class;"));
        Assert.assertTrue(best.toString().contains("Class<Foo> class0 = Foo.class;"));

    }
}
