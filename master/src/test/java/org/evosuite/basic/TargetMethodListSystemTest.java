package org.evosuite.basic;

import com.examples.with.different.packagename.TargetMethod;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

public class TargetMethodListSystemTest extends SystemTestBase {
    @Test
    public void testTargetMethodWithBranchCoverage() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "foo(Ljava/lang/Integer;)Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD_LIST = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.BRANCH, Properties.Criterion.WEAKMUTATION};
        String[] command = new String[]{"-generateMOSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Assert.assertTrue(best.toString().contains("fwo"));
        Assert.assertFalse(best.toString().contains("foo"));
    }
}
