package org.evosuite.testcase;

import static org.junit.Assert.assertFalse;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.SearchStatistics;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.ReportGenerator.StatisticEntry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.staticfield.UnstableAssertion;

public class TestUnstableAssertion extends SystemTest {

	private final boolean DEFAULT_JUNIT_CHECK_ON_SEPARATE_PROCESS = Properties.JUNIT_CHECK_ON_SEPARATE_PROCESS;
	private final boolean DEFAULT_RESET_STATIC_FIELDS = Properties.RESET_STATIC_FIELDS;
	private final boolean DEFAULT_JUNIT_CHECK = Properties.JUNIT_CHECK;
	private final boolean DEFAULT_JUNIT_TESTS = Properties.JUNIT_TESTS;
	private final boolean DEFAULT_SANDBOX = Properties.SANDBOX;

	@Before
	public void saveProperties() {
		Properties.JUNIT_CHECK_ON_SEPARATE_PROCESS = true;
		Properties.RESET_STATIC_FIELDS = true;
		Properties.JUNIT_CHECK = true;
		Properties.JUNIT_TESTS = true;
		Properties.SANDBOX = true;
	}

	@After
	public void restoreProperties() {
		Properties.JUNIT_CHECK_ON_SEPARATE_PROCESS = DEFAULT_JUNIT_CHECK_ON_SEPARATE_PROCESS;
		Properties.RESET_STATIC_FIELDS = DEFAULT_RESET_STATIC_FIELDS;
		Properties.JUNIT_CHECK = DEFAULT_JUNIT_CHECK;
		Properties.JUNIT_TESTS = DEFAULT_JUNIT_TESTS;
		Properties.SANDBOX = DEFAULT_SANDBOX;
	}

	@Test
	public void test() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = UnstableAssertion.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);
		double best_fitness = best.getFitness();
		Assert.assertTrue("Optimal coverage was not achieved ",
				best_fitness == 0.0);

		StatisticEntry entry = SearchStatistics.getInstance()
				.getLastStatisticEntry();
		assertFalse(entry.hadUnstableTests);
	}

}
