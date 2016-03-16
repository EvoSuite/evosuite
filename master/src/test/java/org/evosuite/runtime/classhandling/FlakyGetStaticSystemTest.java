package org.evosuite.runtime.classhandling;

import com.examples.with.different.packagename.reset.ClassWithMutableStatic;
import com.examples.with.different.packagename.reset.SingletonObjectReset;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.TestGenerationContext;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.statistics.backend.DebugStatisticsBackend;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.Randomness;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Created by gordon on 20/02/2016.
 */
public class FlakyGetStaticSystemTest extends SystemTestBase {

    @Test
    public void testResetGetStatic() {

        Properties.RESET_STATIC_FIELDS = true;
        Properties.RESET_STATIC_FIELD_GETS = true;
        Properties.JUNIT_CHECK = true;
        Properties.JUNIT_TESTS = true;
        Properties.SANDBOX = true;
        Properties.ASSERTION_STRATEGY = Properties.AssertionStrategy.ALL;

        EvoSuite evosuite = new EvoSuite();

        String targetClass = ClassWithMutableStatic.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.OUTPUT_VARIABLES = "" + RuntimeVariable.HadUnstableTests;
        String[] command = new String[] { "-generateSuite", "-class", targetClass };

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);
        double best_fitness = best.getFitness();
        Assert.assertTrue("Optimal coverage was not achieved ", best_fitness == 0.0);

        Map<String, OutputVariable<?>> map = DebugStatisticsBackend.getLatestWritten();
        Assert.assertNotNull(map);
        OutputVariable<?> unstable = map.get(RuntimeVariable.HadUnstableTests.toString());
        Assert.assertNotNull(unstable);
        Assert.assertEquals(Boolean.FALSE, unstable.getValue());
    }
}
