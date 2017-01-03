package org.evosuite.testcase;

import com.examples.with.different.packagename.staticfield.StaticFieldUninitialized;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.statistics.backend.DebugStatisticsBackend;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

/**
 * Created by gordon on 30/11/2016.
 */
public class StaticUninitializedFieldSystemTest extends SystemTestBase {


    @Before
    public void setUpProperties() {
        Properties.RESET_STATIC_FIELDS = true;
        Properties.RESET_STATIC_FIELD_GETS = true;
        Properties.SANDBOX = true;
        Properties.JUNIT_CHECK = true;
        Properties.JUNIT_TESTS = true;
        Properties.PURE_INSPECTORS = true;
        Properties.OUTPUT_VARIABLES = "" + RuntimeVariable.HadUnstableTests;
    }

    @Test
    public void test() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = StaticFieldUninitialized.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        String[] command = new String[] { "-generateSuite", "-class", targetClass };

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println(best.toString());
        Map<String, OutputVariable<?>> map = DebugStatisticsBackend.getLatestWritten();
        Assert.assertNotNull(map);
        OutputVariable unstable = map.get(RuntimeVariable.HadUnstableTests.toString());
        Assert.assertNotNull(unstable);
        Assert.assertEquals("Unexpected unstabled test cases were generated",Boolean.FALSE, unstable.getValue());

        double best_fitness = best.getFitness();
        Assert.assertTrue("Optimal coverage was not achieved ", best_fitness == 0.0);


    }

}
