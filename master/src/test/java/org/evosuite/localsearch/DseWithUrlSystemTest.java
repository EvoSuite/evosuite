/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.localsearch;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.SolverType;
import org.evosuite.Properties.StoppingCondition;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.dse.DseWithURL;

/**
 * Created by Andrea Arcuri on 19/03/15.
 */
public class DseWithUrlSystemTest extends SystemTestBase {

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
		Properties.RESET_STATIC_FIELD_GETS = true;
		Properties.P_FUNCTIONAL_MOCKING = 0.0;
		Properties.P_REFLECTION_ON_PRIVATE = 0.0;

	}

	@Test
	public void testDSE() {

		Properties.DSE_SOLVER = SolverType.EVOSUITE_SOLVER;
		
		Properties.STOPPING_CONDITION = StoppingCondition.MAXTIME;
		Properties.SEARCH_BUDGET = 120;
		
		// should it be trivial for DSE ?

		EvoSuite evosuite = new EvoSuite();
		String targetClass = DseWithURL.class.getCanonicalName();
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
