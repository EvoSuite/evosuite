package org.evosuite.testsuite;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.SystemTest;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.coverage.MethodReturnsPrimitive;

public class TestTestSuiteMinimizer extends SystemTest
{
	@Test
    public void testWithOne()
	{
		Properties.CRITERION = new Criterion[1];
        Properties.CRITERION[0] = Criterion.ONLYBRANCH;

        Properties.MINIMIZE = true;
        Properties.MINIMIZE_VALUES = true;
        Properties.INLINE = true;

        Properties.RESET_STATIC_FIELDS = false;
        Properties.SHOW_PROGRESS = false;
        Properties.ENABLE_ASSERTS_FOR_EVOSUITE = false;
        Properties.SANDBOX = true;

        Properties.HTML = false;
        Properties.PLOT = false;
        Properties.NEW_STATISTICS = false;
        Properties.SAVE_ALL_DATA = false;
        Properties.COVERAGE = false;

        Properties.ASSERTIONS = false;
        Properties.TEST_COMMENTS = false;
        Properties.JUNIT_TESTS = false;
        Properties.JUNIT_CHECK = false;

	    EvoSuite evosuite = new EvoSuite();

        String targetClass = MethodReturnsPrimitive.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[] {
            "-generateSuite",
            "-class", targetClass
        };

        Object result = evosuite.parseCommandLine(command);
        Assert.assertNotNull(result);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome c = (TestSuiteChromosome) ga.getBestIndividual();
        
        Assert.assertEquals(0.0, c.getFitness(), 0.0);
        Assert.assertEquals(1.0, c.getCoverage(), 0.0);
        Assert.assertEquals(6.0, c.getNumOfCoveredGoals(ga.getFitnessFunction()), 0.0);
        Assert.assertEquals(5, c.size());
	}

	@SuppressWarnings("rawtypes")
	@Test
    public void testWithTwo()
	{
		Properties.CRITERION = new Criterion[2];
        Properties.CRITERION[0] = Criterion.ONLYBRANCH;
        Properties.CRITERION[1] = Criterion.LINE;

        Properties.MINIMIZE = true;
        Properties.MINIMIZE_VALUES = true;
        Properties.INLINE = true;

        Properties.RESET_STATIC_FIELDS = false;
        Properties.SHOW_PROGRESS = false;
        Properties.ENABLE_ASSERTS_FOR_EVOSUITE = false;
        Properties.SANDBOX = true;

        Properties.HTML = false;
        Properties.PLOT = false;
        Properties.NEW_STATISTICS = false;
        Properties.SAVE_ALL_DATA = false;
        Properties.COVERAGE = false;

        Properties.ASSERTIONS = false;
        Properties.TEST_COMMENTS = false;
        Properties.JUNIT_TESTS = false;
        Properties.JUNIT_CHECK = false;

	    EvoSuite evosuite = new EvoSuite();

        String targetClass = MethodReturnsPrimitive.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[] {
            "-generateSuite",
            "-class", targetClass
        };

        Object result = evosuite.parseCommandLine(command);
        Assert.assertNotNull(result);

        GeneticAlgorithm<?> ga = getGAFromResult(result);

        TestSuiteChromosome c = (TestSuiteChromosome) ga.getBestIndividual();

        final FitnessFunction onlybranch = ga.getFitnessFunctions().get(0);
        final FitnessFunction line = ga.getFitnessFunctions().get(1);

        Assert.assertEquals(0.0, c.getFitness(onlybranch), 0.0);
        Assert.assertEquals(0.0, c.getFitness(line), 0.0);

        Assert.assertEquals(1.0, c.getCoverage(onlybranch), 0.0);
        Assert.assertEquals(1.0, c.getCoverage(line), 0.0);

        Assert.assertEquals(6.0, c.getNumOfCoveredGoals(onlybranch), 0.0);
        Assert.assertEquals(9.0, c.getNumOfCoveredGoals(line), 0.0);

        Assert.assertEquals(8, c.size());
	}
}
