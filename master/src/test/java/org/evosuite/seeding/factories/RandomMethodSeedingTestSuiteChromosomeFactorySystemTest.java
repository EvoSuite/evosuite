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
package org.evosuite.seeding.factories;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testcase.TestCase;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.staticusage.Class1;


public class RandomMethodSeedingTestSuiteChromosomeFactorySystemTest extends SystemTestBase {

	ChromosomeSampleFactory defaultFactory = new ChromosomeSampleFactory();
	TestSuiteChromosome bestIndividual;
	GeneticAlgorithm<TestSuiteChromosome> ga;

	@Before
	public void setup() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = Class1.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);

		ga = (GeneticAlgorithm<TestSuiteChromosome>) getGAFromResult(result);
		bestIndividual = (TestSuiteChromosome) ga.getBestIndividual();
	}

	@Test
	public void testNotSeed() {
		Properties.SEED_PROBABILITY = 0;
		RandomMethodSeedingTestSuiteChromosomeFactory bicf = new RandomMethodSeedingTestSuiteChromosomeFactory(
				defaultFactory, ga);
		TestSuiteChromosome chromosome = bicf.getChromosome();
		
		boolean containsSeededMethod = false;
		for (int i = 0; i < chromosome.getTests().size(); i++){
			if (!chromosome.getTests().get(i).equals(ChromosomeSampleFactory.CHROMOSOME.getTests().get(i))){
				containsSeededMethod = true;
			}
		
		}
		assertFalse(containsSeededMethod);
	}

	@Test
	public void testRandomMethod() {
		//probability is SEED_PROBABILITY/test cases, so 10 guarentees a seed
		Properties.SEED_PROBABILITY = 10;
		RandomMethodSeedingTestSuiteChromosomeFactory factory = new RandomMethodSeedingTestSuiteChromosomeFactory(
				defaultFactory, ga);
		TestSuiteChromosome chromosome = factory.getChromosome();
		boolean containsSeededMethod = false;
		for (TestCase t : chromosome.getTests()) {
			for (TestSuiteChromosome tsc : ga.getPopulation()){
				for (TestCase t2 : tsc.getTests()) {
					if (t.equals(t2)) {
						containsSeededMethod = true;
					}
				}
			}
		}
		assertTrue(containsSeededMethod);
	}

}
