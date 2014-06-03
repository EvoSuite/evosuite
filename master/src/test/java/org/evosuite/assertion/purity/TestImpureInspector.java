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

import com.examples.with.different.packagename.purity.ImpureInspector;

public class TestImpureInspector extends SystemTest {
	private final boolean DEFAULT_RESET_STATIC_FIELDS = Properties.RESET_STATIC_FIELDS;
	private final boolean DEFAULT_JUNIT_CHECK = Properties.JUNIT_CHECK;
	private final boolean DEFAULT_JUNIT_TESTS = Properties.JUNIT_TESTS;
	private final boolean DEFAULT_PURE_INSPECTORS = Properties.PURE_INSPECTORS;
	private final boolean DEFAULT_SANDBOX = Properties.SANDBOX;

	@Before
	public void saveProperties() {
		Properties.SANDBOX = true;
		Properties.RESET_STATIC_FIELDS = true;
		Properties.JUNIT_CHECK = true;
		Properties.JUNIT_TESTS = true;
		Properties.PURE_INSPECTORS = true;
	}

	@After
	public void restoreProperties() {
		Properties.RESET_STATIC_FIELDS = DEFAULT_RESET_STATIC_FIELDS;
		Properties.JUNIT_CHECK = DEFAULT_JUNIT_CHECK;
		Properties.JUNIT_TESTS = DEFAULT_JUNIT_TESTS;
		Properties.PURE_INSPECTORS = DEFAULT_PURE_INSPECTORS;
		Properties.SANDBOX = DEFAULT_SANDBOX;
	}

	@Test
	public void test() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ImpureInspector.class.getCanonicalName();
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

		boolean recursivePureFunction = purityAnalyzer.isPure(targetClass,
				"recursivePureFunction",
				Type.getMethodDescriptor(Type.INT_TYPE, Type.INT_TYPE));
		assertTrue(recursivePureFunction);

		boolean getImpureValue = purityAnalyzer.isPure(targetClass,
				"getImpureValue", descriptor);
		assertFalse(getImpureValue);

		boolean getPureValue = purityAnalyzer.isPure(targetClass,
				"getPureValue", descriptor);
		assertTrue(getPureValue);

		boolean getImpureValueFromCall = purityAnalyzer.isPure(targetClass,
				"getImpureValueFromCall", descriptor);
		assertFalse(getImpureValueFromCall);

		boolean getPureValueFromCall = purityAnalyzer.isPure(targetClass,
				"getPureValueFromCall", descriptor);
		assertTrue(getPureValueFromCall);

		boolean recursivePureInspector = purityAnalyzer.isPure(targetClass,
				"recursivePureInspector", descriptor);
		assertTrue(recursivePureInspector);

		boolean recursiveImpureInspector = purityAnalyzer.isPure(targetClass,
				"recursiveImpureInspector", descriptor);
		assertFalse(recursiveImpureInspector);

		StatisticEntry entry = SearchStatistics.getInstance()
				.getLastStatisticEntry();
		assertFalse(entry.hadUnstableTests);

	}

}
