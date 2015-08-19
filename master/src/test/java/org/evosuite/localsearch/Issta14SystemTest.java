/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.localsearch;

import com.examples.with.different.packagename.localsearch.DseBar;
import com.examples.with.different.packagename.localsearch.IntegerLocalSearchExample;
import com.examples.with.different.packagename.localsearch.IsstaFoo;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.DSEType;
import org.evosuite.Properties.LocalSearchBudgetType;
import org.evosuite.Properties.SolverType;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Andrea Arcuri on 19/03/15.
 */
public class Issta14SystemTest extends SystemTest {

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

	@Test
	public void testLocalSearch() {

		// it should be trivial for LS

		EvoSuite evosuite = new EvoSuite();
		String targetClass = IsstaFoo.class.getCanonicalName();
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
		String targetClass = IsstaFoo.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;

		Properties.DSE_PROBABILITY = 1.0; // force using only DSE, no LS

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
