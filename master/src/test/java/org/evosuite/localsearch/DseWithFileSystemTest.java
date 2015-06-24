package org.evosuite.localsearch;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.LocalSearchBudgetType;
import org.evosuite.Properties.SolverType;
import org.evosuite.Properties.StoppingCondition;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.dse.DseWithFile;

/**
 * Created by Andrea Arcuri on 19/03/15.
 */
public class DseWithFileSystemTest extends SystemTest {

	private static final double DEFAULT_LS_PROBABILITY = Properties.LOCAL_SEARCH_PROBABILITY;
	private static final int DEFAULT_LS_RATE = Properties.LOCAL_SEARCH_RATE;
	private static final LocalSearchBudgetType DEFAULT_LS_BUDGET_TYPE = Properties.LOCAL_SEARCH_BUDGET_TYPE;
	private static final long DEFAULT_LS_BUDGET = Properties.LOCAL_SEARCH_BUDGET;
	private static final long DEFAULT_SEARCH_BUDGET = Properties.SEARCH_BUDGET;
	private static final boolean DEFAULT_MINIMIZE = Properties.MINIMIZE;
	private static final boolean DEFAULT_ASSERTIONS = Properties.ASSERTIONS;
	private static final SolverType DEFAULT_DSE_SOLVER = Properties.DSE_SOLVER;
	private static final Criterion[] DEFAULT_CRITERION = Properties.CRITERION;
	private static final int DEFAULT_CONCOLIC_TIMEOUT = Properties.CONCOLIC_TIMEOUT;
	
	
	@Before
	public void init() {
		Properties.VIRTUAL_FS = true;
		Properties.VIRTUAL_NET= true;
		Properties.LOCAL_SEARCH_PROBABILITY = 1.0;
		Properties.LOCAL_SEARCH_RATE = 1;
		Properties.LOCAL_SEARCH_BUDGET_TYPE = Properties.LocalSearchBudgetType.TESTS;
		Properties.LOCAL_SEARCH_BUDGET = 100;
		Properties.SEARCH_BUDGET = 50000;
//		Properties.CONCOLIC_TIMEOUT = Integer.MAX_VALUE;
	}

	@After
	public void restoreProperties() {
		Properties.LOCAL_SEARCH_PROBABILITY = DEFAULT_LS_PROBABILITY;
		Properties.LOCAL_SEARCH_RATE = DEFAULT_LS_RATE;
		Properties.LOCAL_SEARCH_BUDGET_TYPE = DEFAULT_LS_BUDGET_TYPE;
		Properties.LOCAL_SEARCH_BUDGET = DEFAULT_LS_BUDGET;
		Properties.SEARCH_BUDGET = DEFAULT_SEARCH_BUDGET;
		Properties.MINIMIZE = DEFAULT_MINIMIZE;
		Properties.ASSERTIONS = DEFAULT_ASSERTIONS;
		Properties.DSE_SOLVER = DEFAULT_DSE_SOLVER;
		Properties.CRITERION = DEFAULT_CRITERION;
		Properties.CONCOLIC_TIMEOUT = DEFAULT_CONCOLIC_TIMEOUT;
	}

	@Test
	public void testDSE() {

		Properties.DSE_SOLVER = SolverType.EVOSUITE_SOLVER;
		
		Properties.STOPPING_CONDITION = StoppingCondition.MAXTIME;
		Properties.SEARCH_BUDGET = 120;
		
		// should it be trivial for DSE ?

		EvoSuite evosuite = new EvoSuite();
		String targetClass = DseWithFile.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;

		Properties.CRITERION = new Criterion[] {Criterion.BRANCH, Criterion.EXCEPTION};
		
		Properties.MINIMIZE = true;
		Properties.ASSERTIONS = true;
		
		Properties.DSE_PROBABILITY = 1.0; // force using only DSE, no LS

		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);


	}
}
