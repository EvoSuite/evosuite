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
package org.evosuite.ga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gordon Fraser
 * 
 */
public class RandomSearch extends GeneticAlgorithm {

	private static Logger logger = LoggerFactory.getLogger(RandomSearch.class);

	/**
	 * @param factory
	 */
	public RandomSearch(ChromosomeFactory<? extends Chromosome> factory) {
		super(factory);
	}

	private static final long serialVersionUID = -7685015421245920459L;

	/* (non-Javadoc)
	 * @see org.evosuite.ga.GeneticAlgorithm#evolve()
	 */
	@Override
	protected void evolve() {
		Chromosome newChromosome = chromosomeFactory.getChromosome();
		fitnessFunction.getFitness(newChromosome);
		notifyEvaluation(newChromosome);
		if (newChromosome.compareTo(getBestIndividual()) <= 0) {
			logger.info("New fitness: " + newChromosome.getFitness());
			population.set(0, newChromosome);
		}
		currentIteration++;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.GeneticAlgorithm#initializePopulation()
	 */
	@Override
	public void initializePopulation() {
		generateRandomPopulation(1);
		calculateFitness();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.GeneticAlgorithm#generateSolution()
	 */
	@Override
	public void generateSolution() {
		notifySearchStarted();
		if (population.isEmpty())
			initializePopulation();

		currentIteration = 0;
		while (!isFinished()) {
			evolve();
			this.notifyIteration();
		}
		notifySearchFinished();
	}

}
