package org.evosuite.basic;

import com.examples.with.different.packagename.ArrayReference;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

public class ArrayReferenceSystemTest extends SystemTestBase {

    public TestSuiteChromosome generateTest(boolean minimize){
        EvoSuite evosuite = new EvoSuite();

        String targetClass = ArrayReference.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.ASSERTIONS = false;
        Properties.JUNIT_CHECK = false;
        Properties.MINIMIZE = minimize;
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        return best;
    }

    @Test
    public void testArrayReferenceWithoutMinimization() {
        TestSuiteChromosome best = generateTest(false);
        Assert.assertFalse("Array reference should not be assigned to its first element", best.toString().contains("constructorArray0[0] = (Constructor<Insets>) constructorArray0;"));
    }

    @Test
    public void testArrayReferenceWithMinimization() {
        TestSuiteChromosome best = generateTest(true);
        Assert.assertFalse("Array reference should not be assigned to its first element", best.toString().contains("constructorArray0[0] = (Constructor<Insets>) constructorArray0;"));
    }

}
