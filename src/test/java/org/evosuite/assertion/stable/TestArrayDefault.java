package org.evosuite.assertion.stable;

import static org.junit.Assert.assertFalse;

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

import com.examples.with.different.packagename.purity.ImpureEqualsTarget;
import com.examples.with.different.packagename.stable.BooleanArrayDefault;
import com.examples.with.different.packagename.stable.DoubleArrayDefault;
import com.examples.with.different.packagename.stable.FloatArrayDefault;
import com.examples.with.different.packagename.stable.IntegerArrayDefault;
import com.examples.with.different.packagename.stable.ObjectArrayDefault;

public class TestArrayDefault extends SystemTest {
	private final boolean DEFAULT_RESET_STATIC_FIELDS = Properties.RESET_STATIC_FIELDS;
	private final boolean DEFAULT_JUNIT_CHECK = Properties.JUNIT_CHECK;
	private final boolean DEFAULT_JUNIT_TESTS = Properties.JUNIT_TESTS;
	private final boolean DEFAULT_PURE_INSPECTORS = Properties.PURE_INSPECTORS;
	private final boolean DEFAULT_SANDBOX = Properties.SANDBOX;
	private final boolean DEFAULT_PURE_EQUALS = Properties.PURE_EQUALS;
	private final int DEFAULT_MINIMIZATION_TO = Properties.MINIMIZATION_TIMEOUT;
	private final int EXTRA_TIMEOUT = Properties.EXTRA_TIMEOUT;
	

	@Before
	public void saveProperties() {
		Properties.SANDBOX = true;
		Properties.RESET_STATIC_FIELDS = true;
		Properties.JUNIT_CHECK = true;
		Properties.JUNIT_TESTS = true;
		Properties.PURE_INSPECTORS = true;
		Properties.PURE_EQUALS = true;
		
		Properties.MINIMIZATION_TIMEOUT = 800;
		Properties.EXTRA_TIMEOUT = 200;
	}

	@After
	public void restoreProperties() {
		Properties.SANDBOX = DEFAULT_SANDBOX;
		Properties.RESET_STATIC_FIELDS = DEFAULT_RESET_STATIC_FIELDS;
		Properties.JUNIT_CHECK = DEFAULT_JUNIT_CHECK;
		Properties.JUNIT_TESTS = DEFAULT_JUNIT_TESTS;
		Properties.PURE_INSPECTORS = DEFAULT_PURE_INSPECTORS;
		Properties.PURE_EQUALS = DEFAULT_PURE_EQUALS;
		
		Properties.MINIMIZATION_TIMEOUT = DEFAULT_MINIMIZATION_TO;
		Properties.EXTRA_TIMEOUT = EXTRA_TIMEOUT;
	}

	@Test
	public void testFloatDefault() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = FloatArrayDefault.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		StatisticEntry entry = SearchStatistics.getInstance()
				.getLastStatisticEntry();
		assertFalse(entry.hadUnstableTests);


	}

	@Test
	public void testIntegerDefault() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = IntegerArrayDefault.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		StatisticEntry entry = SearchStatistics.getInstance()
				.getLastStatisticEntry();
		assertFalse(entry.hadUnstableTests);


	}

	@Test
	public void testObjectDefault() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ObjectArrayDefault.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		StatisticEntry entry = SearchStatistics.getInstance()
				.getLastStatisticEntry();
		assertFalse(entry.hadUnstableTests);


	}

	@Test
	public void testBooleanDefault() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = BooleanArrayDefault.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		StatisticEntry entry = SearchStatistics.getInstance()
				.getLastStatisticEntry();
		assertFalse(entry.hadUnstableTests);

//		Assert.assertTrue("Optimal coverage was not achieved ",
//				best_fitness == 0.0);

	}

	@Test
	public void testDoubleDefault() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DoubleArrayDefault.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		StatisticEntry entry = SearchStatistics.getInstance()
				.getLastStatisticEntry();
		assertFalse(entry.hadUnstableTests);

//		Assert.assertTrue("Optimal coverage was not achieved ",
//				best_fitness == 0.0);

	}

}
