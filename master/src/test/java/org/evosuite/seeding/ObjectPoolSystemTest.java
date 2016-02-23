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
package org.evosuite.seeding;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.TestFactory;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.examples.with.different.packagename.testcarver.ArrayConverterTestCase;
import com.examples.with.different.packagename.testcarver.DifficultClassTest;
import com.examples.with.different.packagename.testcarver.DifficultClassWithoutCarving;
import com.examples.with.different.packagename.testcarver.DifficultClassWithoutCarvingTest;

public class ObjectPoolSystemTest extends SystemTestBase {

	private double P_POOL = Properties.P_OBJECT_POOL;
	private boolean CARVE_POOL = Properties.CARVE_OBJECT_POOL;
	private String SELECTED_JUNIT = Properties.SELECTED_JUNIT;
	private TestFactory FACTORY = Properties.TEST_FACTORY;
	
	@Before
	public void initProperties() {
		Properties.SEARCH_BUDGET = 20000;
	}
	
	@After
	public void restoreProperties() {
		Properties.P_OBJECT_POOL = P_POOL;
		Properties.CARVE_OBJECT_POOL = CARVE_POOL;
		Properties.SELECTED_JUNIT = SELECTED_JUNIT;
		Properties.TEST_FACTORY = FACTORY;
	}
	
	@Ignore
	@Test
	public void testDifficultClassWithoutPoolFails() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DifficultClassWithoutCarving.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		Properties.P_OBJECT_POOL = 0.0;
				
		String[] command = new String[] { "-generateSuite", "-class", targetClass};

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		// Passes now....
		Assert.assertTrue("Did not expect optimal coverage: ", best.getCoverage() < 1d);		
	}
	
	@Ignore
	@Test
	public void testDifficultClassWithWrongPoolFails() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DifficultClassWithoutCarving.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		Properties.P_OBJECT_POOL = 0.8;
		Properties.CARVE_OBJECT_POOL = true;
		Properties.SELECTED_JUNIT = ArrayConverterTestCase.class.getCanonicalName();
				
		String[] command = new String[] { "-generateSuite", "-class", targetClass};

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertTrue("Did not expect optimal coverage: ", best.getCoverage() < 1d);		
	}
	
	@Test
	public void testDifficultClassWithPoolPasses() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DifficultClassWithoutCarving.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		Properties.P_OBJECT_POOL = 0.8;
		Properties.CARVE_OBJECT_POOL = true;
		Properties.SELECTED_JUNIT = DifficultClassTest.class.getCanonicalName();
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass};

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Expected optimal coverage: ", 1d, best.getCoverage(), 0.001);		
	}
	
	@Test
	public void testDifficultClassWithMultipleClassPoolPasses() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DifficultClassWithoutCarving.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		Properties.P_OBJECT_POOL = 0.8;
		Properties.CARVE_OBJECT_POOL = true;
		Properties.SELECTED_JUNIT = DifficultClassWithoutCarvingTest.class.getCanonicalName();
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass};

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Expected optimal coverage: ", 1d, best.getCoverage(), 0.001);		
	}


}
