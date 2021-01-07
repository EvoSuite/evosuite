package org.evosuite.dse;

import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.junit.Before;

import static org.junit.Assume.assumeTrue;

public class DSESystemTestBase extends SystemTestBase {
    @Before
	public void init() {
		Properties.VIRTUAL_FS = true;
		Properties.VIRTUAL_NET = true;
		Properties.LOCAL_SEARCH_PROBABILITY = 1.0;
		Properties.LOCAL_SEARCH_RATE = 1;
		Properties.LOCAL_SEARCH_BUDGET_TYPE = Properties.LocalSearchBudgetType.TESTS;
		Properties.LOCAL_SEARCH_BUDGET = 100;
		Properties.SEARCH_BUDGET = 50000;
		// Properties.CONCOLIC_TIMEOUT = Integer.MAX_VALUE;
		Properties.RESET_STATIC_FIELD_GETS = true;

		String cvc4_path = System.getenv("CVC4_PATH");
		if (cvc4_path != null) {
			Properties.CVC4_PATH = cvc4_path;
		}

		Properties.DSE_SOLVER = Properties.SolverType.CVC4_SOLVER;

		Properties.STOPPING_CONDITION = Properties.StoppingCondition.MAXTIME;
		Properties.SEARCH_BUDGET = 60 * 60 * 10; // 10 hours
		Properties.MINIMIZATION_TIMEOUT = 60 * 60;
		Properties.ASSERTION_TIMEOUT = 60 * 60;
		// Properties.TIMEOUT = Integer.MAX_VALUE;

		Properties.STRATEGY = Properties.Strategy.DSE;
		Properties.SELECTED_DSE_ARRAYS_MEMORY_MODEL_VERSION = Properties.DSE_ARRAYS_MEMORY_MODEL_VERSION.SELECT_STORE_EXPRESSIONS;

		Properties.CRITERION = new Properties.Criterion[] { Properties.Criterion.BRANCH };

		Properties.MINIMIZE = true;
		Properties.ASSERTIONS = true;

		assumeTrue(Properties.CVC4_PATH != null);
	}
}
