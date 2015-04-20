package org.evosuite.junit.xml;

import static org.junit.Assert.assertFalse;

import java.util.Map;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.assertion.CheapPurityAnalyzer;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.statistics.backend.DebugStatisticsBackend;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Type;

import com.examples.with.different.packagename.junit.Foo;
//import org.evosuite.testsuite.SearchStatistics;

public class SystemTestJUnitOnSeparateProcessProperty extends SystemTest {
	private final boolean DEFAULT_JUNIT_CHECK_ON_SEPARATE_PROCESS = Properties.JUNIT_CHECK_ON_SEPARATE_PROCESS;
	private final boolean DEFAULT_JUNIT_CHECK = Properties.JUNIT_CHECK;
	private final boolean DEFAULT_JUNIT_TESTS = Properties.JUNIT_TESTS;

	@Before
	public void saveProperties() {
		Properties.JUNIT_CHECK_ON_SEPARATE_PROCESS = true;
		Properties.JUNIT_CHECK = true;
		Properties.JUNIT_TESTS = true;
	}

	@After
	public void restoreProperties() {
		Properties.JUNIT_CHECK_ON_SEPARATE_PROCESS = DEFAULT_JUNIT_CHECK_ON_SEPARATE_PROCESS;
		Properties.JUNIT_CHECK = DEFAULT_JUNIT_CHECK;
		Properties.JUNIT_TESTS = DEFAULT_JUNIT_TESTS;
	}

	@Test
	public void test() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = Foo.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		Properties.OUTPUT_VARIABLES=""+RuntimeVariable.HadUnstableTests;
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

		String descriptor = Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
				Type.getType(Object.class));
		boolean equals = purityAnalyzer.isPure(targetClass, "equals",
				descriptor);
		assertFalse(equals);

		Map<String, OutputVariable<?>> map = DebugStatisticsBackend.getLatestWritten();
		Assert.assertNotNull(map);
		OutputVariable unstable = map.get(RuntimeVariable.HadUnstableTests.toString());
		Assert.assertNotNull(unstable);
		Assert.assertEquals(Boolean.FALSE, unstable.getValue());
	}

}
