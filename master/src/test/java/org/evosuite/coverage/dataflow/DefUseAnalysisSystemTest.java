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
package org.evosuite.coverage.dataflow;

import java.util.Arrays;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.defuse.DefUseExample1;
import com.examples.with.different.packagename.defuse.GCD;

public class DefUseAnalysisSystemTest extends SystemTestBase {

    private final Criterion[] oldCriterion = Arrays.copyOf(Properties.CRITERION, Properties.CRITERION.length);
	private final boolean oldAssertions = Properties.ASSERTIONS;
	private final boolean DEFAULT_SANDBOX = Properties.SANDBOX;
	private final String analysisCriteria = Properties.ANALYSIS_CRITERIA;

	@Before
	public void beforeTest() {
		Properties.SANDBOX = true;
		Properties.CRITERION = new Criterion[] { Criterion.DEFUSE };
		//Properties.ANALYSIS_CRITERIA = "Branch,DefUse";
		Properties.TARGET_CLASS = DefUseExample1.class.getCanonicalName();
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

		// Need to deactivate assertions, otherwise classloader is chanaged 
		// and DefUseCoverageFactory is reset
		Properties.ASSERTIONS = false;
		//Properties.ANALYSIS_CRITERIA = "Branch,DefUse";

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

		Properties.ASSERTIONS = false;
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		
		System.out.println("Def: "+DefUsePool.getDefCounter());
		//DefUseCoverageFactory.computeGoals();
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);
		Assert.assertEquals(0, DefUseCoverageFactory.getInterMethodGoalsCount());
		Assert.assertEquals(0, DefUseCoverageFactory.getIntraClassGoalsCount());
		Assert.assertEquals(4, DefUseCoverageFactory.getParamGoalsCount()); // 3 or 4?
		Assert.assertEquals(6, DefUseCoverageFactory.getIntraMethodGoalsCount());
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
}
