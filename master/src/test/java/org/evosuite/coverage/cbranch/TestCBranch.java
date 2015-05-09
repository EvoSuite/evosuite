package org.evosuite.coverage.cbranch;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.Properties.Criterion;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.SystemTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.coverage.IndirectlyCoverableBranches;

public class TestCBranch extends SystemTest {
	
    private static final Criterion[] defaultCriterion = Properties.CRITERION;
    
    private static boolean defaultArchive = Properties.TEST_ARCHIVE;

	@After
	public void resetProperties() {
		Properties.CRITERION = defaultCriterion;
		Properties.TEST_ARCHIVE = defaultArchive;
	}

	@Before
	public void beforeTest() {
        Properties.CRITERION[0] = Criterion.CBRANCH;
	}

	@Test
	public void testCBranchFitnessWithArchive() {
		Properties.TEST_ARCHIVE = true;
		testBranchFitness();
	}

	@Test
	public void testCBranchFitnessWithoutArchive() {
		Properties.TEST_ARCHIVE = false;
		Properties.SEARCH_BUDGET = 50000;
		testBranchFitness();
	}

	public void testBranchFitness() {
		EvoSuite evosuite = new EvoSuite();
		
		String targetClass = IndirectlyCoverableBranches.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		System.out.println("EvolvedTestSuite:\n" + best);
		int goals = TestSuiteGenerator.getFitnessFactory().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals(7, goals);
		Assert.assertEquals(5, best.size());
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

}
