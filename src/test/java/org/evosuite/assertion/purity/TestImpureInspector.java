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

import com.examples.with.different.packagename.inspector.ImpureInspector;

public class TestImpureInspector extends SystemTest {
	private boolean reset_statick_field__property;
	private boolean junit_check_property;
	private boolean junit_tests_property;
	private boolean pure_inspectors_property;

	@Before
	public void saveProperties() {
		reset_statick_field__property = Properties.RESET_STATIC_FIELDS;
		junit_check_property = Properties.JUNIT_CHECK;
		junit_tests_property = Properties.JUNIT_TESTS;
		pure_inspectors_property = Properties.PURE_INSPECTORS;

		Properties.RESET_STATIC_FIELDS = true;
		Properties.JUNIT_CHECK = true;
		Properties.JUNIT_TESTS = true;
		Properties.PURE_INSPECTORS = true;
	}

	@After
	public void restoreProperties() {
		Properties.RESET_STATIC_FIELDS = reset_statick_field__property;
		Properties.JUNIT_CHECK = junit_check_property;
		Properties.JUNIT_TESTS = junit_tests_property;
		Properties.PURE_INSPECTORS = pure_inspectors_property;
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
