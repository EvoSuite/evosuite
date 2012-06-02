/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.ga;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;

/**
 * 
 * This singleton class serves as a pool for TestChromosomes that are worth recycling
 * 
 * Whenever a TestFitnessFunction detects, that a TestChromosome covers it,
 * the TestFitnessFunction will notify this Class by calling testIsInterestingForGoal()
 * 
 * Then whenever a genetic algorithm fills it's initial population it will ask
 * this class for interesting TestChromosomes concerning it's current fitness_function
 * getRecycableChromosomes() then returns to the GA a set of all TestChromosomes
 * that were interesting for TestFitnessFunctions that were similar to the given fitness_function
 *  - for more information look at TestFitnessFunction.isSimilarTo(), .isCovered() 
 *  	and GeneticAlgorithm.recycleChromosomes()
 * 
 * 
 * @author Andre Mis
 */
public class ChromosomeRecycler {

	private static ChromosomeRecycler instance;
	
	// TODO TestChromosome and TestFitnessFunction .equals() and hashCode() ?
	private Map<TestFitnessFunction,Set<TestChromosome>> chromosomePool;
	
	public static ChromosomeRecycler getInstance() {
		if(instance==null)
			instance = new ChromosomeRecycler();
		return instance;
	}
	
	private ChromosomeRecycler() {
		chromosomePool = new HashMap<TestFitnessFunction,Set<TestChromosome>>();
	}

	/**
	 * Can be called whenever a TestChromosome was valuable for a
	 * given TestFitnessFunction to reuse the TestChromosome later
	 * when looking for similar goals
	 * 
	 * Called by TestFitnessFunction.isCovered() whenever it detects that
	 * that the testFitness was covered by a test. 
	 */
	public void testIsInterestingForGoal(TestChromosome test,
			TestFitnessFunction fitnessFunction) {
		
//		System.out.println("Found interesting test for "+fitnessFunction.toString());
		if(chromosomePool.get(fitnessFunction) == null)
			chromosomePool.put(fitnessFunction, new HashSet<TestChromosome>());
		chromosomePool.get(fitnessFunction).add(test);
	}

	/**
	 * Returns all interesting Chromosomes for fitness functions that are
	 * similar to the given one in the sense of TestFitnessFunction.isSimilarTo()
	 * 
	 * Called by GeneticAlgorithm.recycleChromosomes() whenever a new initial
	 * population is to be contructed
	 */
	public Set<Chromosome> getRecycableChromosomes(
			FitnessFunction fitnessFunction) {
		
		Set<Chromosome> r = new HashSet<Chromosome>();
		TestFitnessFunction testFitnessFunction = null;
		try {
			testFitnessFunction = (TestFitnessFunction)fitnessFunction;
		} catch(ClassCastException e) {
			return r;
		}
		for(TestFitnessFunction goal : chromosomePool.keySet()) {
			if(goal.isSimilarTo(testFitnessFunction))
				r.addAll(chromosomePool.get(goal));
		}
//		System.out.println("returned "+r.size());//+" recycables for "+fitnessFunction.toString());
		return r;
	}
}
