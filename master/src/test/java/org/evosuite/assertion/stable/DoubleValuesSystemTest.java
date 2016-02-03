package org.evosuite.assertion.stable;

import com.examples.with.different.packagename.assertion.DoubleWrapperExample;
import com.examples.with.different.packagename.stable.CloneMe;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testcase.TestCase;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by gordon on 03/02/2016.
 */
public class DoubleValuesSystemTest extends SystemTestBase {

    private final boolean DEFAULT_RESET_STATIC_FIELDS = Properties.RESET_STATIC_FIELDS;
    private final boolean DEFAULT_JUNIT_CHECK = Properties.JUNIT_CHECK;
    private final boolean DEFAULT_JUNIT_TESTS = Properties.JUNIT_TESTS;
    private final boolean DEFAULT_PURE_INSPECTORS = Properties.PURE_INSPECTORS;
    private final boolean DEFAULT_JUNIT_CHECK_ON_SEPARATE_PROCESS = Properties.JUNIT_CHECK_ON_SEPARATE_PROCESS;
    private final boolean DEFAULT_SANDBOX = Properties.SANDBOX;

    @Before
    public void before() {
        Properties.RESET_STATIC_FIELDS=true;
        Properties.SANDBOX = true;
        Properties.JUNIT_CHECK = true;
        Properties.JUNIT_TESTS = true;
        Properties.PURE_INSPECTORS = true;
        Properties.JUNIT_CHECK_ON_SEPARATE_PROCESS = false;
    }

    @After
    public void after() {
        Properties.RESET_STATIC_FIELDS = DEFAULT_RESET_STATIC_FIELDS;
        Properties.SANDBOX = DEFAULT_SANDBOX;
        Properties.JUNIT_CHECK = DEFAULT_JUNIT_CHECK;
        Properties.JUNIT_TESTS = DEFAULT_JUNIT_TESTS;
        Properties.PURE_INSPECTORS = DEFAULT_PURE_INSPECTORS;
        Properties.JUNIT_CHECK_ON_SEPARATE_PROCESS = DEFAULT_JUNIT_CHECK_ON_SEPARATE_PROCESS;
    }
    @Test
    public void testDoubleWrappers() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = DoubleWrapperExample.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        String[] command = new String[] { "-generateSuite", "-class",
                targetClass };

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        List<TestCase> tests = best.getTests();
        boolean allStable = TestStabilityChecker.checkStability(tests);
        assertTrue(allStable);
    }
}
