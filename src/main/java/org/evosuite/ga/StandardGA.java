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
package org.evosuite.ga;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.utils.Randomness;


/**
 * Standard GA implementation
 *
 * @author Gordon Fraser
 */
public class StandardGA<T extends Chromosome> extends GeneticAlgorithm<T> {

	private static final long serialVersionUID = 5043503777821916152L;
	
	private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StandardGA.class);

	/**
	 * Constructor
	 *
	 * @param factory a {@link org.evosuite.ga.ChromosomeFactory} object.
	 */
	public StandardGA(ChromosomeFactory<T> factory) {
		super(factory);
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	protected void evolve() {

		List<T> newGeneration = new ArrayList<T>();

		// Elitism
		newGeneration.addAll(elitism());

		// new_generation.size() < population_size
		while (!isNextPopulationFull(newGeneration)) {

			T parent1 = selectionFunction.select(population);
			T parent2 = selectionFunction.select(population);

			T offspring1 = (T)parent1.clone();
			T offspring2 = (T)parent2.clone();

			try {
				if (Randomness.nextDouble() <= Properties.CROSSOVER_RATE) {
					crossoverFunction.crossOver(offspring1, offspring2);
				}

				notifyMutation(offspring1);
				offspring1.mutate();
				notifyMutation(offspring2);
				offspring2.mutate();
				
				if(offspring1.isChanged()) {
					offspring1.updateAge(currentIteration);
				}
				if(offspring2.isChanged()) {
					offspring2.updateAge(currentIteration);
				}
			} catch (ConstructionFailedException e) {
				logger.info("CrossOver/Mutation failed.");
				continue;
			}

			if (!isTooLong(offspring1))
				newGeneration.add(offspring1);
			else
				newGeneration.add(parent1);

			if (!isTooLong(offspring2))
				newGeneration.add(offspring2);
			else
				newGeneration.add(parent2);
		}

		population = newGeneration;

		currentIteration++;
	}

	/** {@inheritDoc} */
	@Override
	public void initializePopulation() {
		notifySearchStarted();
		currentIteration = 0;

		// Set up initial population
		generateInitialPopulation(Properties.POPULATION);
		// Determine fitness
		calculateFitness();
		this.notifyIteration();
	}

	/** {@inheritDoc} */
	@Override
	public void generateSolution() {
		if (population.isEmpty())
			initializePopulation();

		while (!isFinished()) {
			logger.debug("Current population: " + getAge() + "/" + Properties.SEARCH_BUDGET);
			logger.info("Best fitness: " + getBestIndividual().getFitness());

			evolve();
			// Determine fitness
			calculateFitness();

			if (shouldApplyDSE())
				applyDSE();

			if (shouldApplyLocalSearch())
				applyLocalSearch();

			this.notifyIteration();
		}

		notifySearchFinished();
	}

}
