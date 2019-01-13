package org.evosuite.basic;

import com.examples.with.different.packagename.DataUtils;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

public class DataUtilsSystemTest extends SystemTestBase {
    @Test
    public void test2DArrayInstrumentation() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = DataUtils.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.DEFUSE};
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.print(best.toString());
//        Assert.assertEquals(1,best.getNumOfCoveredGoals());
    }
}
