/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite;

import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.DivisionByZero;

public class TestSUTDivisionByZero extends SystemTest {

	/*
	 * To avoid side effects on test cases that we will run afterwards,
	 * if we modify some values in Properties, then we need to re-int them after
	 * each test case execution
	 */
	public static final double defaultPrimitivePool = Properties.PRIMITIVE_POOL;
	public static final boolean defaultErrorBranches = Properties.ERROR_BRANCHES;

	@After
	public void resetProperties() {
		Properties.PRIMITIVE_POOL = defaultPrimitivePool;
		Properties.ERROR_BRANCHES = defaultErrorBranches;
	}

	@Test
	public void testDivisonByZero() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DivisionByZero.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.PRIMITIVE_POOL = 0.99;
		Properties.ERROR_BRANCHES = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		Assert.assertTrue(result != null);
		Assert.assertTrue("Invalid result type :" + result.getClass(),
		                  result instanceof GeneticAlgorithm);

		GeneticAlgorithm ga = (GeneticAlgorithm) result;
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ", 6, goals);
		Assert.assertEquals("Non-optimal coverage: ", 5d / 6d, best.getCoverage(), 0.001);
	}
}
