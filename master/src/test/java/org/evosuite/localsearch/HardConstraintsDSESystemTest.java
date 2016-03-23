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
import org.evosuite.Properties.StoppingCondition;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.concolic.HardConstraints;

/**
 * Created by Andrea Arcuri on 19/03/15.
 */
public class HardConstraintsDSESystemTest extends SystemTestBase {

	@Before 
	public void prepareTest() {
		Properties.LOCAL_SEARCH_PROBABILITY = 1.0;
		Properties.LOCAL_SEARCH_RATE = 1;
		Properties.LOCAL_SEARCH_BUDGET_TYPE = Properties.LocalSearchBudgetType.TESTS;
		Properties.LOCAL_SEARCH_BUDGET = 100;
		Properties.SEARCH_BUDGET = 50000;
		Properties.STOPPING_CONDITION = StoppingCondition.MAXTIME;
		Properties.SEARCH_BUDGET = 60;
		Properties.MINIMIZE = true;
		Properties.ASSERTIONS = true;
		Properties.RESET_STATIC_FIELD_GETS = true;

	}
	
	@Test
	public void testZ3Str2() {
		String z3StrPath = System.getenv("z3_str2_path");
		Assume.assumeTrue(z3StrPath != null);
		Properties.Z3_STR2_PATH = z3StrPath;
		Properties.DSE_SOLVER = Properties.SolverType.Z3_STR2_SOLVER;

		EvoSuite evosuite = new EvoSuite();
		String targetClass = HardConstraints.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;

		Properties.CRITERION = new Criterion[] { Criterion.BRANCH, Criterion.EXCEPTION };

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);
		
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(),
				0.001);

	}

	@Test
	public void testCVC4() {
		String cvc4Path = System.getenv("cvc4_path");
		Assume.assumeTrue(cvc4Path != null);
		Properties.CVC4_PATH = cvc4Path;
		Properties.DSE_SOLVER = Properties.SolverType.CVC4_SOLVER;

		EvoSuite evosuite = new EvoSuite();
		String targetClass = HardConstraints.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;

		Properties.CRITERION = new Criterion[] { Criterion.BRANCH, Criterion.EXCEPTION };

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);
		
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(),
				0.001);

	}

	@Test
	public void testZ3() {
		String z3Path = System.getenv("z3_path");
		Assume.assumeTrue(z3Path != null);
		Properties.Z3_PATH = z3Path;
		Properties.DSE_SOLVER = Properties.SolverType.Z3_SOLVER;

		EvoSuite evosuite = new EvoSuite();
		String targetClass = HardConstraints.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;

		Properties.CRITERION = new Criterion[] { Criterion.BRANCH, Criterion.EXCEPTION };

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);
		
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(),
				0.001);

	}

}
