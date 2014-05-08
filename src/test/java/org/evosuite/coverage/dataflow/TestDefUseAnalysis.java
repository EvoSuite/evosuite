package org.evosuite.coverage.dataflow;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.SystemTest;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.defuse.DefUseExample1;
import com.examples.with.different.packagename.defuse.GCD;

public class TestDefUseAnalysis extends SystemTest {

	private final Criterion oldCriterion = Properties.CRITERION;
	private final boolean oldAssertions = Properties.ASSERTIONS;
	private final boolean DEFAULT_SANDBOX = Properties.SANDBOX;
	private final String analysisCriteria = Properties.ANALYSIS_CRITERIA;

	@Before
	public void beforeTest() {
		Properties.SANDBOX = true;
	}

	@After
	public void afterTest() {
		Properties.CRITERION = oldCriterion;
		Properties.ASSERTIONS = oldAssertions;
		Properties.SANDBOX = DEFAULT_SANDBOX;
		Properties.ANALYSIS_CRITERIA = analysisCriteria;
	}

	@Test
	public void testSimpleExample() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DefUseExample1.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.CRITERION = Criterion.DEFUSE;

		// Need to deactivate assertions, otherwise classloader is chanaged 
		// and DefUseCoverageFactory is reset
		Properties.ASSERTIONS = false;
		Properties.ANALYSIS_CRITERIA = "Branch,DefUse";

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals(0, DefUseCoverageFactory.getInterMethodGoalsCount());
		Assert.assertEquals(0, DefUseCoverageFactory.getIntraClassGoalsCount());
		Assert.assertEquals(1, DefUseCoverageFactory.getParamGoalsCount());
		Assert.assertEquals(3, DefUseCoverageFactory.getIntraMethodGoalsCount());
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testGCDExample() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = GCD.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.CRITERION = Criterion.DEFUSE;
		Properties.ASSERTIONS = false;
		Properties.ANALYSIS_CRITERIA = "Branch,DefUse";

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals(0, DefUseCoverageFactory.getInterMethodGoalsCount());
		Assert.assertEquals(0, DefUseCoverageFactory.getIntraClassGoalsCount());
		Assert.assertEquals(4, DefUseCoverageFactory.getParamGoalsCount());
		Assert.assertEquals(6, DefUseCoverageFactory.getIntraMethodGoalsCount());
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
}
