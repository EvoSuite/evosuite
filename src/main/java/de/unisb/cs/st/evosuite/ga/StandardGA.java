/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of the GA library.
 * 
 * GA is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * GA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * GA. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.ga;

import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.utils.Randomness;

/**
 * Standard GA implementation
 * 
 * @author Gordon Fraser
 * 
 */
public class StandardGA extends GeneticAlgorithm {

	private static final long serialVersionUID = 5043503777821916152L;

	/**
	 * Constructor
	 * 
	 * @param factory
	 */
	public StandardGA(ChromosomeFactory factory) {
		super(factory);
	}

	@Override
	protected void evolve() {

		List<Chromosome> new_generation = new ArrayList<Chromosome>();

		// Elitism
		new_generation.addAll(elitism());

		// new_generation.size() < population_size
		while (new_generation.size() < Properties.POPULATION) {

			Chromosome parent1 = selection_function.select(population);
			Chromosome parent2 = selection_function.select(population);

			Chromosome offspring1 = parent1.clone();
			Chromosome offspring2 = parent2.clone();

			try {
				if (Randomness.nextDouble() <= Properties.CROSSOVER_RATE) {
					crossover_function.crossOver(offspring1, offspring2);
				}

				notifyMutation(offspring1);
				offspring1.mutate();
				notifyMutation(offspring2);
				offspring2.mutate();
			} catch (ConstructionFailedException e) {
				logger.info("CrossOver/Mutation failed.");
				continue;
			}

			if (!isTooLong(offspring1))
				new_generation.add(offspring1);
			else
				new_generation.add(parent1);

			if (!isTooLong(offspring2))
				new_generation.add(offspring2);
			else
				new_generation.add(parent2);
		}

		population = new_generation;

		current_iteration++;
	}

	@Override
	public void initializePopulation() {
		notifySearchStarted();
		current_iteration = 0;

		// Set up initial population
		generateInitialPopulation(Properties.POPULATION);
		// Determine fitness
		calculateFitness();
		this.notifyIteration();
	}

	@Override
	public void generateSolution() {
		if (population.isEmpty())
			initializePopulation();

		while (!isFinished()) {
			logger.debug("Current population: " + getAge() + "/" + Properties.GENERATIONS);
			logger.info("Best fitness: " + getBestIndividual().getFitness());

			evolve();
			// Determine fitness
			calculateFitness();

			this.notifyIteration();
		}

		notifySearchFinished();
	}

}
