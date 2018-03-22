package org.evosuite.instrumentation.error;

import com.examples.with.different.packagename.errorbranch.QueueAccess;
import com.examples.with.different.packagename.errorbranch.QueueRemove;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

public class QueueInstrumentationSystemTest extends SystemTestBase {
    @Test
    public void testQueueElementOperationWithErrorBranches() {

        EvoSuite evosuite = new EvoSuite();

        String targetClass = QueueAccess.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.ERROR_BRANCHES = true;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.BRANCH, Properties.Criterion.TRYCATCH};

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);

        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

        Assert.assertEquals(3, TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size());
        Assert.assertEquals(1, TestGenerationStrategy.getFitnessFactories().get(1).getCoverageGoals().size());

        Assert.assertEquals("Non-optimal coverage: ", 0.6666666666666666d, best.getCoverage(), 0.001);
    }

    @Test
    public void testQueueRemoveOperationWithErrorBranches() {

        EvoSuite evosuite = new EvoSuite();

        String targetClass = QueueRemove.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.ERROR_BRANCHES = true;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.BRANCH, Properties.Criterion.TRYCATCH};

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);

        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

        Assert.assertEquals(3, TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size());
        Assert.assertEquals(1, TestGenerationStrategy.getFitnessFactories().get(1).getCoverageGoals().size());

        Assert.assertEquals("Non-optimal coverage: ", 0.6666666666666666d, best.getCoverage(), 0.001);
    }
}
