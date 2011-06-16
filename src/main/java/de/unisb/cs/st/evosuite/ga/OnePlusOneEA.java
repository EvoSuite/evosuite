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

/**
 * (1+1)EA
 * 
 * @author Gordon Fraser
 * 
 */
public class OnePlusOneEA extends GeneticAlgorithm {

	private static final long serialVersionUID = 5229089847512798127L;

	/**
	 * Constructor
	 * 
	 * @param factory
	 */
	public OnePlusOneEA(ChromosomeFactory factory) {
		super(factory);
	}

	@Override
	protected void evolve() {

		Chromosome parent = population.get(0);
		Chromosome offspring = parent.clone();

		notifyMutation(offspring);
		do {
			offspring.mutate();
		} while (!offspring.changed);

		fitness_function.getFitness(offspring);
		notifyEvaluation(offspring);

		if (isBetterOrEqual(offspring, parent)) {
			logger.debug("Replacing old population");
			population.set(0, offspring);
		} else {
			logger.debug("Keeping old population");
		}
		current_iteration++;
	}

	@Override
	public void initializePopulation() {
		notifySearchStarted();
		current_iteration = 0;

		// Only one parent
		generateRandomPopulation(1);
		fitness_function.getFitness(population.get(0));
		this.notifyIteration();
		logger.info("Initial fitness: " + population.get(0).getFitness());
	}

	@Override
	public void generateSolution() {
		if (population.isEmpty())
			initializePopulation();

		double fitness = population.get(0).getFitness();
		while (!isFinished()) {
			if ((selection_function.isMaximize() && getBestIndividual().getFitness() > fitness)
			        || (!selection_function.isMaximize() && getBestIndividual().getFitness() < fitness)) {
				logger.info("Current population: " + getAge());
				logger.info("Best fitness: " + getBestIndividual().getFitness());
				fitness = population.get(0).getFitness();
			}
			evolve();

			if (shouldApplyDSE())
				applyDSE();

			if (shouldApplyLocalSearch())
				applyLocalSearch();

			this.notifyIteration();
		}
		notifySearchFinished();
	}
}
