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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.staticusage.Class1;

public class BIMutatedMethodSeedingTestSuiteChromosomeFactorySystemTest extends SystemTestBase {

	ChromosomeSampleFactory defaultFactory = new ChromosomeSampleFactory();
	TestSuiteChromosome bestIndividual;
	GeneticAlgorithm<TestSuiteChromosome> ga;
	private final static double SEED_PROBABILITY = Properties.SEED_PROBABILITY;
	private final static int SEED_MUTATIONS = Properties.SEED_MUTATIONS;

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
	
	@After
	public void restore() {
		Properties.SEED_PROBABILITY = SEED_PROBABILITY;
		Properties.SEED_MUTATIONS = SEED_MUTATIONS;
	}

	@Test
	public void testNotSeed() {
		Properties.SEED_PROBABILITY = 0;
		BIMutatedMethodSeedingTestSuiteChromosomeFactory bicf = new BIMutatedMethodSeedingTestSuiteChromosomeFactory(
				defaultFactory, bestIndividual);
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
	public void testBIMutatedMethod() {
		//probability is SEED_PROBABILITY/test cases, so 10 guarentees a seed
		Properties.SEED_PROBABILITY = 10;
		Properties.SEED_MUTATIONS = 0; // Test requires configured test cluster otherwise
		BIMutatedMethodSeedingTestSuiteChromosomeFactory factory = new BIMutatedMethodSeedingTestSuiteChromosomeFactory(
				defaultFactory, bestIndividual);
		TestSuiteChromosome chromosome = factory.getChromosome();
		boolean containsMutatedSeededMethod = false;
		for (TestCase t : chromosome.getTests()) {
			for (TestCase t2 : bestIndividual.getTests()) {
				if (!t.equals(t2) && !t.equals(TestSampleFactory.CHROMOSOME)) {
					// test case not from original BI or from sample factory,
					// so must be seeded mutated BI
					containsMutatedSeededMethod = true;
				}
			}
		}
		assertTrue(containsMutatedSeededMethod);
	}

}
