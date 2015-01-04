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
package org.evosuite.coverage.exception;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.statistics.backend.DebugStatisticsBackend;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.ImplicitExplicitException;

import java.util.Map;


public class TestImplicitExplicitExceptions  extends SystemTest {

    private static final Criterion[] defaultCriterion = Properties.CRITERION;

	@After
	public void resetProperties() {
		Properties.CRITERION = defaultCriterion;
	}

	@Test
	public void testExceptionFitness() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ImplicitExplicitException.class.getCanonicalName();
		
		Properties.TARGET_CLASS = targetClass;
		Properties.CRITERION[0] = Properties.Criterion.EXCEPTION;
		Properties.OUTPUT_VARIABLES = ""+
				RuntimeVariable.Explicit_MethodExceptions + "," +
				RuntimeVariable.Explicit_TypeExceptions + "," +
				RuntimeVariable.Implicit_MethodExceptions +"," +
				RuntimeVariable.Implicit_TypeExceptions;
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		double fitness = best.getFitness();
		/*
		 * there are 2 undeclared exceptions (both implicit and explicit),
		 * and 3 declared: so fit = 1 / (1+5)
		 */
		Assert.assertEquals("Wrong fitness: ", 1d / 6d, fitness, 0.001);

        Map<String, OutputVariable<?>> map = DebugStatisticsBackend.getLatestWritten();
        Assert.assertNotNull(map);
        Assert.assertEquals(1 , map.get(RuntimeVariable.Explicit_MethodExceptions.toString()).getValue());
        Assert.assertEquals(1 , map.get(RuntimeVariable.Explicit_TypeExceptions.toString()).getValue());
        Assert.assertEquals(1 , map.get(RuntimeVariable.Implicit_MethodExceptions.toString()).getValue());
        Assert.assertEquals(1 , map.get(RuntimeVariable.Implicit_TypeExceptions.toString()).getValue());
	}

}
