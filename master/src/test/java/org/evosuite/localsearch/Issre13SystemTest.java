package org.evosuite.localsearch;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.LocalSearchBudgetType;
import org.evosuite.Properties.StoppingCondition;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.Randomness;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.localsearch.DseBar;

/**
 * Created by Andrea Arcuri on 19/03/15.
 */
public class Issre13SystemTest extends SystemTest {

	private static final double DEFAULT_LS_PROBABILITY = Properties.LOCAL_SEARCH_PROBABILITY;
	private static final int DEFAULT_LS_RATE = Properties.LOCAL_SEARCH_RATE;
	private static final LocalSearchBudgetType DEFAULT_LS_BUDGET_TYPE = Properties.LOCAL_SEARCH_BUDGET_TYPE;
	private static final long DEFAULT_LS_BUDGET = Properties.LOCAL_SEARCH_BUDGET;
	private static final long DEFAULT_SEARCH_BUDGET = Properties.SEARCH_BUDGET;

	@Before
	public void init() {
		Properties.LOCAL_SEARCH_PROBABILITY = 1.0;
		Properties.LOCAL_SEARCH_RATE = 1;
		Properties.LOCAL_SEARCH_BUDGET_TYPE = Properties.LocalSearchBudgetType.TESTS;
		Properties.LOCAL_SEARCH_BUDGET = 100;
		Properties.SEARCH_BUDGET = 50000;
	}

	@After
	public void restoreProperties() {
		Properties.LOCAL_SEARCH_PROBABILITY = DEFAULT_LS_PROBABILITY;
		Properties.LOCAL_SEARCH_RATE = DEFAULT_LS_RATE;
		Properties.LOCAL_SEARCH_BUDGET_TYPE = DEFAULT_LS_BUDGET_TYPE;
		Properties.LOCAL_SEARCH_BUDGET = DEFAULT_LS_BUDGET;
		Properties.SEARCH_BUDGET = DEFAULT_SEARCH_BUDGET;
	}

//	@Test
	public void testLocalSearch() {

		// it should be trivial for LS

		EvoSuite evosuite = new EvoSuite();
		String targetClass = DseBar.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;

		Properties.DSE_PROBABILITY = 0.0; // force using only LS, no DSE

		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(),
				0.001);
	}

	@Test
	public void testDSE() {

		// should it be trivial for DSE ?

		EvoSuite evosuite = new EvoSuite();
		String targetClass = DseBar.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;

		Properties.MINIMIZE = false;
		
		Properties.DSE_PROBABILITY = 1.0; // force using only DSE, no LS

		// Properties.DSE_SOLVER = SolverType.Z3_STR_SOLVER;
		// Properties.Z3_STR_PATH = "/home/galeotti/Z3-str/str";

		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(),
				0.001);
	}

	@Test
	public void testDSEMultiObjective() {

		Randomness.setSeed(1545420);

		// should it be trivial for DSE ?
		Properties.SEARCH_BUDGET = 20;
		Properties.TIMEOUT = Integer.MAX_VALUE;
		Properties.CONCOLIC_TIMEOUT = Integer.MAX_VALUE;
		Properties.LOCAL_SEARCH_RATE = 1;
		Properties.DSE_PROBABILITY = 1.0;
//		Properties.LOCAL_SEARCH_BUDGET_TYPE = LocalSearchBudgetType.TIME;
		Properties.LOCAL_SEARCH_BUDGET_TYPE = LocalSearchBudgetType.TESTS;
		Properties.LOCAL_SEARCH_BUDGET = 5;
		Properties.CRITERION = new Criterion[] { Criterion.LINE,
				Criterion.BRANCH, Criterion.EXCEPTION, Criterion.WEAKMUTATION,
				Criterion.OUTPUT, Criterion.METHOD };
		Properties.STOPPING_CONDITION = StoppingCondition.MAXTIME;

		EvoSuite evosuite = new EvoSuite();
		String targetClass = DseBar.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;

		Properties.DSE_PROBABILITY = 1.0; // force using only DSE, no LS

		// Properties.DSE_SOLVER = SolverType.Z3_STR_SOLVER;
		// Properties.Z3_STR_PATH = "/home/galeotti/Z3-str/str";

		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(),
				0.001);
	}

}
