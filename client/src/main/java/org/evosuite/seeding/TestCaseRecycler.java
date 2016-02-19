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

import java.util.LinkedHashSet;
import java.util.Set;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.SearchListener;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

/**
 * 
 * This singleton class serves as a pool for TestChromosomes that are worth
 * recycling
 * 
 * Whenever a TestFitnessFunction detects, that a TestChromosome covers it, the
 * TestFitnessFunction will notify this Class by calling
 * testIsInterestingForGoal()
 * 
 * Then whenever a genetic algorithm fills it's initial population it will ask
 * this class for interesting TestChromosomes concerning it's current
 * fitness_function getRecycableChromosomes() then returns to the GA a set of
 * all TestChromosomes that were interesting for TestFitnessFunctions that were
 * similar to the given fitness_function - for more information look at
 * TestFitnessFunction.isSimilarTo(), .isCovered() and
 * GeneticAlgorithm.recycleChromosomes()
 * 
 * @author Andre Mis
 */
public final class TestCaseRecycler implements SearchListener {

	private static TestCaseRecycler instance;

	private final Set<TestCase> testPool;

	/**
	 * <p>
	 * Getter for the field <code>instance</code>.
	 * </p>
	 * 
	 * @return a {@link org.evosuite.seeding.TestCaseRecycler} object.
	 */
	public static TestCaseRecycler getInstance() {
		if (instance == null)
			instance = new TestCaseRecycler();
		return instance;
	}

	private TestCaseRecycler() {
		testPool = new LinkedHashSet<TestCase>();
	}


	@Override
	public void searchStarted(GeneticAlgorithm<?> algorithm) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void iteration(GeneticAlgorithm<?> algorithm) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void searchFinished(GeneticAlgorithm<?> algorithm) {
		Chromosome individual = algorithm.getBestIndividual();
		if(individual instanceof TestChromosome) {
			TestChromosome testChromosome = (TestChromosome)individual;
			testPool.add(testChromosome.getTestCase());
		} else if(individual instanceof TestSuiteChromosome) {
			TestSuiteChromosome testSuiteChromosome = (TestSuiteChromosome) individual;
			testPool.addAll(testSuiteChromosome.getTests());
		}
	}

	@Override
	public void fitnessEvaluation(Chromosome individual) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void modification(Chromosome individual) {
		// TODO Auto-generated method stub
		
	}
}
