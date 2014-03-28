package org.evosuite.assertion.purity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.assertion.CheapPurityAnalyzer;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.SearchStatistics;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.ReportGenerator.StatisticEntry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Type;

import com.examples.with.different.packagename.purity.ImpureRandomness;

public class TestImpureRandomness extends SystemTest {
	private final boolean DEFAULT_REPLACE_CALLS = Properties.REPLACE_CALLS;
	private final boolean DEFAULT_JUNIT_CHECK = Properties.JUNIT_CHECK;
	private final boolean DEFAULT_JUNIT_TESTS = Properties.JUNIT_TESTS;
	private final boolean DEFAULT_PURE_INSPECTORS = Properties.PURE_INSPECTORS;
	private final boolean DEFAULT_JUNIT_CHECK_ON_SEPARATE_PROCESS = Properties.JUNIT_CHECK_ON_SEPARATE_PROCESS;

	@Before
	public void saveProperties() {
		Properties.REPLACE_CALLS = true;
		Properties.JUNIT_CHECK = true;
		Properties.JUNIT_TESTS = true;
		Properties.PURE_INSPECTORS = true;
		Properties.JUNIT_CHECK_ON_SEPARATE_PROCESS = true;
	}

	@After
	public void restoreProperties() {
		Properties.REPLACE_CALLS = DEFAULT_REPLACE_CALLS;
		Properties.JUNIT_CHECK_ON_SEPARATE_PROCESS = DEFAULT_JUNIT_CHECK_ON_SEPARATE_PROCESS;
		Properties.JUNIT_CHECK = DEFAULT_JUNIT_CHECK;
		Properties.JUNIT_TESTS = DEFAULT_JUNIT_TESTS;
		Properties.PURE_INSPECTORS = DEFAULT_PURE_INSPECTORS;
	}

	@Test
	public void test() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ImpureRandomness.class.getCanonicalName();
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

		CheapPurityAnalyzer purityAnalyzer = CheapPurityAnalyzer.getInstance();

		String descriptor = Type.getMethodDescriptor(Type.INT_TYPE);
		boolean randomNextInt = purityAnalyzer.isPure(targetClass,
				"randomNextInt", descriptor);
		assertFalse(randomNextInt);

		boolean secureRandomNextInt = purityAnalyzer.isPure(
				targetClass, "secureRandomNextInt", descriptor);
		assertFalse(secureRandomNextInt);

		boolean randomUUIDToString = purityAnalyzer.isPure(
				targetClass, "randomUUIDToString", descriptor);
		assertFalse(randomUUIDToString);

		
		StatisticEntry entry = SearchStatistics.getInstance()
				.getLastStatisticEntry();
		assertFalse(entry.hadUnstableTests);
	}

}
