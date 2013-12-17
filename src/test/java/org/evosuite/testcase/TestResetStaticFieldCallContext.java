package org.evosuite.testcase;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.staticfield.StaticFieldProxy;

public class TestResetStaticFieldCallContext extends SystemTest {

	private boolean reset_statick_field_property;
	private boolean instrument_context_property;

	@Before
	public void saveProperties() {
		reset_statick_field_property = Properties.RESET_STATIC_FIELDS;
		instrument_context_property = Properties.INSTRUMENT_CONTEXT;
		Properties.RESET_STATIC_FIELDS = true;
		Properties.INSTRUMENT_CONTEXT = true;
	}

	@After
	public void restoreProperties() {
		Properties.RESET_STATIC_FIELDS = reset_statick_field_property;
		Properties.INSTRUMENT_CONTEXT = instrument_context_property;
	}

	@Test
	public void test() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = StaticFieldProxy.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);
		double best_fitness = best.getFitness();
		Assert.assertTrue("Optimal coverage is not feasible ",
				best_fitness > 0.0);
	}

}
