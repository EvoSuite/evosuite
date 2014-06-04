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

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.ImplicitExplicitException;


public class TestImplicitExplicitExceptions  extends SystemTest {

	private static final Criterion defaultCriterion = Properties.CRITERION;

	@After
	public void resetProperties() {
		Properties.CRITERION = defaultCriterion;
	}

	@Test
	public void testExceptionFitness() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ImplicitExplicitException.class.getCanonicalName();
		
		Properties.TARGET_CLASS = targetClass;
		Properties.CRITERION = Properties.Criterion.EXCEPTION;
		Properties.OUTPUT_VARIABLES = ""+
				RuntimeVariable.Explicit_MethodExceptions + "," +
				RuntimeVariable.Explicit_TypeExceptions + "," +
				RuntimeVariable.Implicit_MethodExceptions +"," +
				RuntimeVariable.Implicit_TypeExceptions;
		
		/*
		 * FIXME: once statistics fully refactored, should check we get data.
		 * Furthermore, we should check if works with and without minimization.
		 * Currently, minimization for exceptions is broken.
		 */
		//Properties.MINIMIZE = false;		
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		double fitness = best.getFitness();
		/*
		 * all targets should be covered. then there should be NullPointer that should be
		 * handled both implicit and explicit, so fit = 1 / (1+2)
		 */
		Assert.assertEquals("Wrong fitness: ", 1d / 3d, fitness, 0.001);
	}

}
