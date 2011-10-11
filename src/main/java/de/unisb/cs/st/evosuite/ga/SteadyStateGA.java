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
import de.unisb.cs.st.evosuite.ma.Connector;
import de.unisb.cs.st.evosuite.utils.Randomness;

/**
 * Implementation of steady state GA
 * 
 * @author Gordon Fraser
 * 
 */
public class SteadyStateGA extends GeneticAlgorithm {

	private static final long serialVersionUID = 7846967347821123201L;

	protected ReplacementFunction replacement_function;

	/**
	 * Constructor
	 * 
	 * @param factory
	 */
	public SteadyStateGA(ChromosomeFactory<? extends Chromosome> factory) {
		super(factory);

		setReplacementFunction(new FitnessReplacementFunction(selection_function));
	}

	protected boolean keepOffspring(Chromosome parent1, Chromosome parent2,
	        Chromosome offspring1, Chromosome offspring2) {
		//return replacement_function.keepOffspring(parent1, parent2, offspring1,
		//                                          offspring2);

		return (isBetterOrEqual(offspring1, parent1) && isBetterOrEqual(offspring1,
		                                                                parent2))
		        || (isBetterOrEqual(offspring2, parent1) && isBetterOrEqual(offspring2,
		                                                                    parent2));
	}

	@Override
	protected void evolve() {
		List<Chromosome> new_generation = new ArrayList<Chromosome>();

		// Elitism
		logger.debug("Elitism");
		new_generation.addAll(elitism());

		// Add random elements
		// new_generation.addAll(randomism());

		while (new_generation.size() < Properties.POPULATION && !isFinished()) {
			logger.debug("Generating offspring");

			Chromosome parent1 = selection_function.select(population);
			Chromosome parent2 = selection_function.select(population);

			Chromosome offspring1 = parent1.clone();
			Chromosome offspring2 = parent2.clone();

			try {
				// Crossover
				if (Randomness.nextDouble() <= Properties.CROSSOVER_RATE) {
					crossover_function.crossOver(offspring1, offspring2);
				}

			} catch (ConstructionFailedException e) {
				logger.info("CrossOver failed");
				continue;
			}

			// Mutation
			notifyMutation(offspring1);
			offspring1.mutate();
			notifyMutation(offspring2);
			offspring2.mutate();

			// The two offspring replace the parents if and only if one of
			// the offspring is not worse than the best parent.

			fitness_function.getFitness(offspring1);
			notifyEvaluation(offspring1);

			fitness_function.getFitness(offspring2);
			notifyEvaluation(offspring2);

			// local search

			// DSE

			if (keepOffspring(parent1, parent2, offspring1, offspring2)) {
				logger.debug("Keeping offspring");

				// Reject offspring straight away if it's too long
				int rejected = 0;
				if (isTooLong(offspring1) || offspring1.size() == 0) {
					rejected++;
				} else
					new_generation.add(offspring1);

				if (isTooLong(offspring2) || offspring2.size() == 0) {
					rejected++;
				} else
					new_generation.add(offspring2);

				if (rejected == 1)
					new_generation.add(Randomness.choice(parent1, parent2));
				else if (rejected == 2) {
					new_generation.add(parent1);
					new_generation.add(parent2);
				}
			} else {
				logger.debug("Keeping parents");
				new_generation.add(parent1);
				new_generation.add(parent2);
			}
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
		logger.debug("Calculating fitness of initial population");
		calculateFitness();
		this.notifyIteration();
	}

	@Override
	public void generateSolution() {
		if (population.isEmpty())
			initializePopulation();

		logger.debug("Starting evolution");
		double bestFitness = Double.MAX_VALUE;
		while (!isFinished()) {
			logger.info("Population size before: " + population.size());
			evolve();

			if (shouldApplyDSE())
				applyDSE();

			if (shouldApplyLocalSearch())
				applyLocalSearch();

			sortPopulation();
			double newFitness = getBestIndividual().getFitness();
			assert (newFitness <= bestFitness);
			bestFitness = newFitness;
			logger.info("Current iteration: " + current_iteration);
			this.notifyIteration();
			logger.info("Population size: " + population.size());
			logger.info("Best individual has fitness: " + population.get(0).getFitness());
			logger.info("Worst individual has fitness: "
			        + population.get(population.size() - 1).getFitness());

			if (Properties.MA_ACTIVE) {
				// call manual algorithm
				Connector.externalCall(this);
			}
		}

		notifySearchFinished();
	}

	public void setReplacementFunction(ReplacementFunction replacement_function) {
		this.replacement_function = replacement_function;
	}

	public ReplacementFunction getReplacementFunction() {
		return replacement_function;
	}

}
