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
/**
 * 
 */
package org.evosuite.coverage.mutation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.ga.SearchListener;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author fraser
 * 
 */
public class MutationTestPool implements SearchListener {

	private final Logger logger = LoggerFactory.getLogger(MutationTestPool.class);

	private static Map<Mutation, TestChromosome> testMap = new HashMap<Mutation, TestChromosome>();

	private final static List<Mutation> allMutants = MutationPool.getMutants();

	private final static List<MutationTestFitness> allMutantFitnessFunctions = new ArrayList<MutationTestFitness>();

	static {
		for (Mutation m : allMutants) {
			if (Properties.CRITERION == Criterion.WEAKMUTATION)
				allMutantFitnessFunctions.add(new WeakMutationTestFitness(m));
			else {
				allMutantFitnessFunctions.add(new StrongMutationTestFitness(m));
			}
		}
		Randomness.shuffle(allMutantFitnessFunctions);
	}

	public static Set<Mutation> getUncoveredMutants() {
		Set<Mutation> mutants = new HashSet<Mutation>();
		for (Mutation m : allMutants) {
			if (MutationTimeoutStoppingCondition.isDisabled(m))
				continue;
			if (!isCovered(m)) {
				mutants.add(m);
			}
		}
		return mutants;
	}

	public static boolean isCovered(Mutation mutation) {
		return testMap.containsKey(mutation);
	}

	public static Set<MutationTestFitness> getUncoveredFitnessFunctions() {
		Set<MutationTestFitness> mutants = new HashSet<MutationTestFitness>();
		//int num = 0;
		for (MutationTestFitness m : allMutantFitnessFunctions) {
			if (MutationTimeoutStoppingCondition.isDisabled(m.getMutation()))
				continue;
			if (!isCovered(m.getMutation())) {
				//if (num++ < Properties.MAX_MUTANTS)
				mutants.add(m);
			}
		}
		return mutants;
	}

	public static int getCoveredMutants() {
		return testMap.size();
	}

	/**
	 * Keep a copy of a test that covers a mutant
	 * 
	 * @param mutation
	 * @param test
	 */
	public static void addTest(Mutation mutation, TestChromosome test) {
		testMap.put(mutation, test);
		//System.out.println("Covered mutants: " + testMap.size());
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#searchStarted(de.unisb.cs.st.evosuite.ga.GeneticAlgorithm)
	 */
	@Override
	public void searchStarted(GeneticAlgorithm algorithm) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#iteration(de.unisb.cs.st.evosuite.ga.GeneticAlgorithm)
	 */
	@Override
	public void iteration(GeneticAlgorithm algorithm) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#searchFinished(de.unisb.cs.st.evosuite.ga.GeneticAlgorithm)
	 */
	@Override
	public void searchFinished(GeneticAlgorithm algorithm) {
		TestSuiteChromosome solution = (TestSuiteChromosome) algorithm.getBestIndividual();

		logger.info("Search finished with size " + solution.size());
		for (TestChromosome test : testMap.values())
			solution.addTest(test);
		logger.info("Adding mutation tests to size " + solution.size());

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#fitnessEvaluation(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public void fitnessEvaluation(Chromosome individual) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#modification(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public void modification(Chromosome individual) {
		// TODO Auto-generated method stub

	}

}
