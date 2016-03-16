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
package org.evosuite.utils;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.ReadFromSystemIn;

public class SystemInUtilSystemTest extends SystemTestBase {

	private static final boolean defaultSystemIn = Properties.REPLACE_SYSTEM_IN;
	
	/*
	 * We consider VFS has it might mess up System.in handling (eg, these test cases were
	 * created to debug such issue that actually happened) 
	 */
	private static final boolean defaultVFS = Properties.VIRTUAL_FS;
	
	@After
	public void tearDown(){
		Properties.REPLACE_SYSTEM_IN = defaultSystemIn;
		Properties.VIRTUAL_FS = defaultVFS;
	}
	
	@Test
	public void testWithNoVFS(){
		Properties.VIRTUAL_FS = false;
		_test();
	}
	
	@Test
	public void testWithVFS(){
		Properties.VIRTUAL_FS = true;
		_test();
	}
	
	private void _test(){

		EvoSuite evosuite = new EvoSuite();

		String targetClass = ReadFromSystemIn.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_SYSTEM_IN= true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals(3, goals );
		double coverage = best.getCoverage();
		Assert.assertTrue("Not good enough coverage: "+coverage, coverage > 0.99d);

	}
}
