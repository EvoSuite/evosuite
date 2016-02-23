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
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.SearchListener;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * MutationTestPool class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class MutationTestPool implements SearchListener {

	private final static Logger logger = LoggerFactory.getLogger(MutationTestPool.class);

	/** Test cases for mutants we have already covered */
	private static Map<Mutation, TestChromosome> testMap = new HashMap<Mutation, TestChromosome>();

	/** All known mutants */
	private final static List<Mutation> allMutants = MutationPool.getMutants();

	/** Complete set of fitness functions */
	private final static List<MutationTestFitness> allMutantFitnessFunctions = new ArrayList<MutationTestFitness>();

	static {
		for (Mutation m : allMutants) {
		    if (ArrayUtil.contains(Properties.CRITERION, Criterion.WEAKMUTATION))
				allMutantFitnessFunctions.add(new WeakMutationTestFitness(m));
			else {
				allMutantFitnessFunctions.add(new StrongMutationTestFitness(m));
			}
		}
		Randomness.shuffle(allMutantFitnessFunctions);
	}

	/**
	 * <p>
	 * getUncoveredMutants
	 * </p>
	 * 
	 * @return a {@link java.util.Set} object.
	 */
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

	/**
	 * <p>
	 * isCovered
	 * </p>
	 * 
	 * @param mutation
	 *            a {@link org.evosuite.coverage.mutation.Mutation} object.
	 * @return a boolean.
	 */
	public static boolean isCovered(Mutation mutation) {
		return testMap.containsKey(mutation);
	}

	/**
	 * <p>
	 * getUncoveredFitnessFunctions
	 * </p>
	 * 
	 * @return a {@link java.util.Set} object.
	 */
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

	/**
	 * <p>
	 * getCoveredMutants
	 * </p>
	 * 
	 * @return a int.
	 */
	public static int getCoveredMutants() {
		return testMap.size();
	}

	/**
	 * Keep a copy of a test that covers a mutant
	 * 
	 * @param mutation
	 *            a {@link org.evosuite.coverage.mutation.Mutation} object.
	 * @param test
	 *            a {@link org.evosuite.testcase.TestChromosome} object.
	 */
	public static void addTest(Mutation mutation, TestChromosome test) {
		testMap.put(mutation, test);		
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.SearchListener#searchStarted(org.evosuite.ga.GeneticAlgorithm)
	 */
	/** {@inheritDoc} */
	@Override
	public void searchStarted(GeneticAlgorithm<?> algorithm) {

	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.SearchListener#iteration(org.evosuite.ga.GeneticAlgorithm)
	 */
	/** {@inheritDoc} */
	@Override
	public void iteration(GeneticAlgorithm<?> algorithm) {

	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.SearchListener#searchFinished(org.evosuite.ga.GeneticAlgorithm)
	 */
	/** {@inheritDoc} */
	@Override
	public void searchFinished(GeneticAlgorithm<?> algorithm) {
		TestSuiteChromosome solution = (TestSuiteChromosome) algorithm.getBestIndividual();

		logger.info("Search finished with size " + solution.size());
		for (TestChromosome test : testMap.values())
			solution.addTest(test);
		logger.info("Adding mutation tests to size " + solution.size());

	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.SearchListener#fitnessEvaluation(org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public void fitnessEvaluation(Chromosome individual) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.SearchListener#modification(org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public void modification(Chromosome individual) {
		// TODO Auto-generated method stub

	}

}
