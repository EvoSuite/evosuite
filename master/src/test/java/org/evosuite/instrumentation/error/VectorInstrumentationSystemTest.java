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
package org.evosuite.instrumentation.error;

import java.util.List;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.errorbranch.VectorAccess;
import com.examples.with.different.packagename.errorbranch.VectorAccessIndex;

public class VectorInstrumentationSystemTest extends SystemTestBase {

	@Test
	public void testVectorWithoutErrorBranches() {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = VectorAccess.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ERROR_BRANCHES = false;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals("Wrong number of goals: ", 3, goals);
		// One infeasible error branch
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testVectorWithErrorBranches() {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = VectorAccess.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ERROR_BRANCHES = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		List<? extends TestFitnessFunction> goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals(); // assuming single fitness function
		Assert.assertEquals("Wrong number of goals: ", 13, goals.size());

		// Not sure why we have to do this:
		TestCaseExecutor.initExecutor();

		for (TestFitnessFunction goal : goals) {
			BranchCoverageTestFitness branchGoal = (BranchCoverageTestFitness) goal;
			if (branchGoal.getBranch() != null
			        && !branchGoal.getBranch().isInstrumented()) {
				Assert.assertTrue(branchGoal.isCoveredBy(best));
			}
		}
	}

	@Test
	public void testVectorIndexWithoutErrorBranches() {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = VectorAccessIndex.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ERROR_BRANCHES = false;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals("Wrong number of goals: ", 3, goals);
		// One infeasible error branch
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testVectorIndexWithErrorBranches() {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = VectorAccessIndex.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ERROR_BRANCHES = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		List<? extends TestFitnessFunction> goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals(); // assuming single fitness function
		Assert.assertEquals("Wrong number of goals: ", 15, goals.size());

		// Not sure why we have to do this:
		TestCaseExecutor.initExecutor();

		for (TestFitnessFunction goal : goals) {
			BranchCoverageTestFitness branchGoal = (BranchCoverageTestFitness) goal;
			if (branchGoal.getBranch() != null
			        && !branchGoal.getBranch().isInstrumented()) {
				Assert.assertTrue(branchGoal.isCoveredBy(best));
			}
		}
	}
}
