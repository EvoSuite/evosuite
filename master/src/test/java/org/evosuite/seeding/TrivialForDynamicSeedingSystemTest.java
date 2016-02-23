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
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.examples.with.different.packagename.TrivialForDynamicSeeding;
import com.examples.with.different.packagename.TrivialForDynamicSeedingEndsWith;
import com.examples.with.different.packagename.TrivialForDynamicSeedingRegex;
import com.examples.with.different.packagename.TrivialForDynamicSeedingRegionMatches;
import com.examples.with.different.packagename.TrivialForDynamicSeedingRegionMatchesCase;
import com.examples.with.different.packagename.TrivialForDynamicSeedingStartsWith;

public class TrivialForDynamicSeedingSystemTest extends SystemTestBase {

	public static final double defaultDynamicPool = Properties.DYNAMIC_POOL;

	@After
	public void resetProperties() {
		Properties.DYNAMIC_POOL = defaultDynamicPool;
	}

	@Test
	public void testConcatenatedStringEquals() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = TrivialForDynamicSeeding.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		// Properties.DYNAMIC_POOL = 2d / 3d;
		// Properties.SEARCH_BUDGET = 30000;
		ConstantPoolManager.getInstance().reset();
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testConcatenatedStringEqualsWithoutSeeding() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = TrivialForDynamicSeeding.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.DYNAMIC_POOL = 0.0;
		ConstantPoolManager.getInstance().reset();

		String[] command = new String[] { "-generateSuite", "-class", targetClass }; //, "-Ddynamic_pool=0.0"

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Unexpected coverage: ", 2d / 3d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testStartsWith() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = TrivialForDynamicSeedingStartsWith.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		// Properties.DYNAMIC_POOL = 1d / 3d;
		ConstantPoolManager.getInstance().reset();
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testStartsWithWithoutSeeding() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = TrivialForDynamicSeedingStartsWith.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.DYNAMIC_POOL = 0.0;
		ConstantPoolManager.getInstance().reset();

		String[] command = new String[] { "-generateSuite", "-class", targetClass}; //, "-Ddynamic_pool=0.0" 

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Unexpected coverage: ", 2d / 3d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testEndsWith() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = TrivialForDynamicSeedingEndsWith.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		// Properties.DYNAMIC_POOL = 1d / 3d;
		ConstantPoolManager.getInstance().reset();
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testEndsWithWithoutSeeding() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = TrivialForDynamicSeedingEndsWith.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.DYNAMIC_POOL = 0.0;
		ConstantPoolManager.getInstance().reset();

		String[] command = new String[] { "-generateSuite", "-class", targetClass }; //, "-Ddynamic_pool=0.0"

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Unexpected coverage: ", 2d / 3d, best.getCoverage(), 0.001);
	}

	@Test
	public void testRegionMatches() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = TrivialForDynamicSeedingRegionMatches.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		// Properties.DYNAMIC_POOL = 1d / 3d;
		ConstantPoolManager.getInstance().reset();
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testRegionMatchesWithoutSeeding() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = TrivialForDynamicSeedingRegionMatches.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.DYNAMIC_POOL = 0.0;
		ConstantPoolManager.getInstance().reset();

		String[] command = new String[] { "-generateSuite", "-class", targetClass}; //, "-Ddynamic_pool=0.0" 

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Unexpected coverage: ", 2d / 3d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testRegionMatchesIgnoreCase() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = TrivialForDynamicSeedingRegionMatchesCase.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		// Properties.DYNAMIC_POOL = 1d / 3d;
		ConstantPoolManager.getInstance().reset();
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };//, "-Ddynamic_pool=0.333"

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testRegionMatchesIgnoreCaseWithoutSeeding() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = TrivialForDynamicSeedingRegionMatchesCase.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.DYNAMIC_POOL = 0.0;
		ConstantPoolManager.getInstance().reset();

		String[] command = new String[] { "-generateSuite", "-class", targetClass }; //, "-Ddynamic_pool=0.0"

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Unexpected coverage: ", 2d / 3d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testRegexStringMatches() {
		
		//first check whether the regex is feasible 
		final String example = "-@0.AA"; 
		Assert.assertTrue(example.matches(TrivialForDynamicSeedingRegex.REGEX));
		
		EvoSuite evosuite = new EvoSuite();

		String targetClass = TrivialForDynamicSeedingRegex.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.DYNAMIC_POOL = 0.99d;//1d / 3d;
		ConstantPoolManager.getInstance().reset();
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		ConstantPoolManager foo = ConstantPoolManager.getInstance();
		
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Ignore // It is actually not so difficult to achieve this. Maybe the regex should be more complex?
	@Test
	public void testRegexStringMatchesWithoutSeeding() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = TrivialForDynamicSeedingRegex.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.DYNAMIC_POOL = 0.0;
		ConstantPoolManager.getInstance().reset();

		String[] command = new String[] { "-generateSuite", "-class", targetClass }; //, "-Ddynamic_pool=0.0"

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Unexpected coverage: ", 2d / 3d, best.getCoverage(), 0.001);
	}
}
