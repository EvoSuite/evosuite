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

import com.examples.with.different.packagename.sette.L4_Collections;

/**
 * Created by Andrea Arcuri on 19/03/15.
 */
public class L4CollectionsSystemTest extends SystemTest {

	private static final double DEFAULT_LS_PROBABILITY = Properties.LOCAL_SEARCH_PROBABILITY;
	private static final int DEFAULT_LS_RATE = Properties.LOCAL_SEARCH_RATE;
	private static final LocalSearchBudgetType DEFAULT_LS_BUDGET_TYPE = Properties.LOCAL_SEARCH_BUDGET_TYPE;
	private static final long DEFAULT_LS_BUDGET = Properties.LOCAL_SEARCH_BUDGET;
	private static final long DEFAULT_SEARCH_BUDGET = Properties.SEARCH_BUDGET;
	private static final boolean DEFAULT_MINIMIZE = Properties.MINIMIZE;
	private static final boolean DEFAULT_ASSERTIONS = Properties.ASSERTIONS;
	private static final String DEFAULT_Z3_PATH = Properties.Z3_PATH;
	private static final SolverType DEFAULT_DSE_SOLVER = Properties.DSE_SOLVER;
	private static final Criterion[] DEFAULT_CRITERION = Properties.CRITERION;
	private static final int DEFAULT_CONCOLIC_TIMEOUT = Properties.CONCOLIC_TIMEOUT;
	private static final int DEFAULT_TIMEOUT = Properties.TIMEOUT;
	
	
	@Before
	public void init() {
		Properties.LOCAL_SEARCH_PROBABILITY = 1.0;
		Properties.LOCAL_SEARCH_RATE = 1;
		Properties.LOCAL_SEARCH_BUDGET_TYPE = Properties.LocalSearchBudgetType.TESTS;
		Properties.LOCAL_SEARCH_BUDGET = 100;
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
		Properties.Z3_PATH = DEFAULT_Z3_PATH;
		Properties.DSE_SOLVER = DEFAULT_DSE_SOLVER;
		Properties.CRITERION = DEFAULT_CRITERION;
		Properties.CONCOLIC_TIMEOUT = DEFAULT_CONCOLIC_TIMEOUT;
		Properties.TIMEOUT = DEFAULT_TIMEOUT;
	}

	@Test
	public void testZ3DSE() {

		if (System.getenv("z3_path")==null) {
			System.out.println("z3_path should be configured for running this test case");
			return;
		}
		
		Properties.Z3_PATH = System.getenv("z3_path");
		Properties.DSE_SOLVER = SolverType.Z3_SOLVER;
		
		Properties.STOPPING_CONDITION = StoppingCondition.MAXTIME;
		Properties.SEARCH_BUDGET = 120;
		
		// should it be trivial for DSE ?

		EvoSuite evosuite = new EvoSuite();
		String targetClass = L4_Collections.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;

		Properties.CRITERION = new Criterion[] {Criterion.LINE, Criterion.BRANCH, Criterion.EXCEPTION, Criterion.WEAKMUTATION, 
				Criterion.OUTPUT, Criterion.METHOD, Criterion.METHODNOEXCEPTION, Criterion.CBRANCH};
		
		Properties.MINIMIZE = false;
		Properties.ASSERTIONS = false;
		
		Properties.DSE_PROBABILITY = 1.0; // force using only DSE, no LS

		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);


	}
}
