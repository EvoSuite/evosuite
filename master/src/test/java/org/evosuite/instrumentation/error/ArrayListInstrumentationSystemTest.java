package org.evosuite.instrumentation.error;

import com.examples.with.different.packagename.errorbranch.ArrayListAccess;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

public class ArrayListInstrumentationSystemTest extends SystemTestBase {
    @Test
    public void testArrayListAccessWithErrorBranches() {

        EvoSuite evosuite = new EvoSuite();

        String targetClass = ArrayListAccess.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.ERROR_BRANCHES = true;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.BRANCH, Properties.Criterion.TRYCATCH};

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);

        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

        Assert.assertEquals(2, TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size());
        Assert.assertEquals(4, TestGenerationStrategy.getFitnessFactories().get(1).getCoverageGoals().size());

        Assert.assertEquals("Non-optimal coverage: ", 0.625d, best.getCoverage(), 0.001);
    }

    // ConcurrentModificationException for ArrayList testCase?
}
