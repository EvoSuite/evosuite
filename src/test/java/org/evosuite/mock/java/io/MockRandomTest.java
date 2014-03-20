package org.evosuite.mock.java.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Random;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.SearchStatistics;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.ReportGenerator.StatisticEntry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.mock.java.util.RandomUser;

public class MockRandomTest extends SystemTest {

	private static final boolean REPLACE_CALLS = Properties.REPLACE_CALLS;
	private static final boolean JUNIT_TESTS = Properties.JUNIT_TESTS;
	private static final boolean JUNIT_CHECK = Properties.JUNIT_CHECK;

	@Before
	public void setProperties() {
		Properties.REPLACE_CALLS = true;
		Properties.JUNIT_TESTS = true;
		Properties.JUNIT_CHECK = true;
	}

	@After
	public void restoreProperties() {
		Properties.REPLACE_CALLS = REPLACE_CALLS;
		Properties.JUNIT_TESTS = JUNIT_TESTS;
		Properties.JUNIT_CHECK = JUNIT_CHECK;
	}

	@Test
	public void testRandomUser() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = RandomUser.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SEARCH_BUDGET = 20000;

		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);
		Assert.assertTrue(result != null);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(),
				0.001);

		StatisticEntry entry = SearchStatistics.getInstance()
				.getLastStatisticEntry();
		assertFalse(entry.hadUnstableTests);
	}

}
