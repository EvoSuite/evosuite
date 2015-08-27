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
package org.evosuite.coverage.input;

import com.examples.with.different.packagename.coverage.MethodWithSeveralInputArguments;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * @author Jose Miguel Rojas
 *
 */
public class TestInputCoverageFitnessFunction extends SystemTest {

    private static final Criterion[] defaultCriterion = Properties.CRITERION;
    
    private static boolean defaultArchive = Properties.TEST_ARCHIVE;

	@After
	public void resetProperties() {
		Properties.CRITERION = defaultCriterion;
		Properties.TEST_ARCHIVE = defaultArchive;
	}

	@Before
	public void beforeTest() {
        Properties.CRITERION[0] = Criterion.INPUT;
	}

	@Test
	public void testInputCoverageWithoutMinimization() {
		Properties.SEARCH_BUDGET = 10; // low budget, some goals will not be covered
		Properties.MINIMIZE = false; // MINIMIZE = true; makes this test fail miserably
		Properties.ASSERTIONS = false; // ASSERTIONS = true; kills goal comments
		Properties.JUNIT_TESTS = true;
		testInputCoverage();
	}

	@Test
	public void testInputCoverageWithMinimisation() {
		Properties.SEARCH_BUDGET = 10;
		Properties.MINIMIZE = true;
		testInputCoverage();
	}
		
	public void testInputCoverage() {
		EvoSuite evosuite = new EvoSuite();
		
		String targetClass = MethodWithSeveralInputArguments.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		List<?> goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals();
		Assert.assertEquals(13, goals.size());
		//Assert.assertEquals("Non-optimal fitness: ", 0.0, best.getFitness(), 0.001);
		//Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

}
