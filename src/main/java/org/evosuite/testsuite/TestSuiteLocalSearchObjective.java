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
package org.evosuite.testsuite;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.LocalSearchBudget;
import org.evosuite.ga.LocalSearchObjective;
import org.evosuite.testcase.TestChromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>TestSuiteLocalSearchObjective class.</p>
 *
 * @author Gordon Fraser
 */
public class TestSuiteLocalSearchObjective implements LocalSearchObjective {

	private static Logger logger = LoggerFactory.getLogger(TestSuiteLocalSearchObjective.class);

	private final TestSuiteFitnessFunction fitness;

	private final TestSuiteChromosome suite;

	private final int testIndex;

	private double lastFitness;

	private double lastCoverage;

	/**
	 * <p>Constructor for TestSuiteLocalSearchObjective.</p>
	 *
	 * @param fitness a {@link org.evosuite.testsuite.TestSuiteFitnessFunction} object.
	 * @param suite a {@link org.evosuite.testsuite.TestSuiteChromosome} object.
	 * @param index a int.
	 */
	public TestSuiteLocalSearchObjective(TestSuiteFitnessFunction fitness,
	        TestSuiteChromosome suite, int index) {
		this.fitness = fitness;
		this.suite = suite;
		this.testIndex = index;
		this.lastFitness = suite.getFitness();
		this.lastCoverage = suite.getCoverage();

		/*
		for (TestChromosome test : suite.getTestChromosomes()) {
			test.setChanged(true);
			test.setLastExecutionResult(null);
		}

		double fit = fitness.getFitness(suite);
		assert (fit == this.lastFitness);
		*/
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.LocalSearchObjective#hasImproved(org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean hasImproved(Chromosome individual) {
		return hasChanged(individual) < 0;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.LocalSearchObjective#hasNotWorsened(org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean hasNotWorsened(Chromosome individual) {
		return hasChanged(individual) < 1;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.LocalSearchObjective#hasChanged(org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public int hasChanged(Chromosome individual) {
		individual.setChanged(true);
		suite.setTestChromosome(testIndex, (TestChromosome) individual);
		LocalSearchBudget.evaluation();
		double newFitness = fitness.getFitness(suite);
		if (newFitness < lastFitness) { // TODO: Maximize
			logger.info("Local search improved fitness from " + lastFitness + " to "
			        + newFitness);
			lastFitness = newFitness;
			lastCoverage = suite.getCoverage();
			suite.setFitness(lastFitness);
			return -1;
		} else if (newFitness > lastFitness) {
			suite.setFitness(lastFitness);
			suite.setCoverage(lastCoverage);
			return 1;
		} else {
			lastCoverage = suite.getCoverage();
			return 0;
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.LocalSearchObjective#getFitnessFunction()
	 */
	/** {@inheritDoc} */
	@Override
	public FitnessFunction getFitnessFunction() {
		return fitness;
	}

}
