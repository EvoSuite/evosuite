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

import com.examples.with.different.packagename.errorbranch.VectorAccess;
import com.examples.with.different.packagename.errorbranch.VectorAccessIndex;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

public class VectorInstrumentationSystemTest extends SystemTestBase {

	@Test
	public void testVectorWithErrorBranches() {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = VectorAccess.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ERROR_BRANCHES = true;
		Properties.CRITERION = new Properties.Criterion[] {Properties.Criterion.BRANCH, Properties.Criterion.TRYCATCH};

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		Assert.assertEquals(3, TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size());
		Assert.assertEquals(5, TestGenerationStrategy.getFitnessFactories().get(1).getCoverageGoals().size());

		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testVectorIndexWithErrorBranches() {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = VectorAccessIndex.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ERROR_BRANCHES = true;
		Properties.CRITERION = new Properties.Criterion[] {Properties.Criterion.BRANCH, Properties.Criterion.TRYCATCH};

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		Assert.assertEquals(3, TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size());
		Assert.assertEquals(6, TestGenerationStrategy.getFitnessFactories().get(1).getCoverageGoals().size());
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
}
