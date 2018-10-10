/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import static org.junit.Assert.assertNotNull;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.Properties.Criterion;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.concolic.Pat;

/**
 * Created by Andrea Arcuri on 19/03/15.
 */
public class PatSystemTest extends SystemTestBase {

	@Before
	public void init() {
		Properties.LOCAL_SEARCH_PROBABILITY = 1.0;
		Properties.LOCAL_SEARCH_RATE = 1;
		Properties.LOCAL_SEARCH_BUDGET_TYPE = Properties.LocalSearchBudgetType.TIME;
		Properties.LOCAL_SEARCH_BUDGET = 10;
		Properties.SEARCH_BUDGET = 20;
		Properties.STOPPING_CONDITION = Properties.StoppingCondition.MAXTIME;
		Properties.RESET_STATIC_FIELD_GETS = true;
	}

	@Test
	public void testCVC4() {
		Assume.assumeTrue(System.getenv("cvc4_path")!=null);
		
		Properties.CVC4_PATH =System.getenv("cvc4_path");
		Properties.DSE_SOLVER = Properties.SolverType.CVC4_SOLVER;
		
		EvoSuite evosuite = new EvoSuite();
		String targetClass = Pat.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		

		Properties.DSE_PROBABILITY = 1.0; // force using only DSE, no LS
		Properties.CRITERION = new Criterion[] {
	            //these are basic criteria that should be always on by default
	            Criterion.LINE, Criterion.BRANCH, Criterion.EXCEPTION, Criterion.WEAKMUTATION, Criterion.OUTPUT, Criterion.METHOD, Criterion.METHODNOEXCEPTION, Criterion.CBRANCH  };
		
		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		assertNotNull(best);
	}

	@Test
	public void testZ3() {
		Assume.assumeTrue(System.getenv("z3_path")!=null);
		
		Properties.Z3_PATH =System.getenv("z3_path");
		Properties.DSE_SOLVER = Properties.SolverType.Z3_SOLVER;
		
		EvoSuite evosuite = new EvoSuite();
		String targetClass = Pat.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		
		Properties.DSE_PROBABILITY = 1.0; // force using only DSE, no LS
		Properties.CRITERION = new Criterion[] {
	            //these are basic criteria that should be always on by default
	            Criterion.LINE, Criterion.BRANCH, Criterion.EXCEPTION, Criterion.WEAKMUTATION, Criterion.OUTPUT, Criterion.METHOD, Criterion.METHODNOEXCEPTION, Criterion.CBRANCH  };
		

		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		assertNotNull(best);
	}

}
