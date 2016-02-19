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
package org.evosuite.ga.metaheuristics;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;

/**
 * (1+1)EA
 *
 * @author Gordon Fraser
 */
public class OnePlusOneEA<T extends Chromosome> extends GeneticAlgorithm<T> {

	private static final long serialVersionUID = 5229089847512798127L;

	private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OnePlusOneEA.class);

	/**
	 * Constructor
	 *
	 * @param factory a {@link org.evosuite.ga.ChromosomeFactory} object.
	 */
	public OnePlusOneEA(ChromosomeFactory<T> factory) {
		super(factory);
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	protected void evolve() {

		T parent = population.get(0);
		T offspring = (T)parent.clone();
		offspring.updateAge(currentIteration);

		notifyMutation(offspring);
		do {
			offspring.mutate();
		} while (!offspring.isChanged());

	    for (FitnessFunction<T> fitnessFunction : fitnessFunctions) {
	        fitnessFunction.getFitness(offspring);
	        notifyEvaluation(offspring);
	    } 
		notifyEvaluation(offspring);
		//logger.info("New individual: " + offspring);

		if (isBetterOrEqual(offspring, parent)) {
			//logger.info("Replacing old population");
			//if(isBetter(offspring, parent))
			//	applyAdaptiveLocalSearch(offspring);

			population.set(0, offspring);
		} else {
			//logger.info("Keeping old population");
		}
		currentIteration++;
	}

	/** {@inheritDoc} */
	@Override
	public void initializePopulation() {
		notifySearchStarted();
		currentIteration = 0;

		// Only one parent
		generateRandomPopulation(1);
		getFitnessFunction().getFitness(population.get(0));
		updateFitnessFunctionsAndValues();
		this.notifyIteration();
		logger.info("Initial fitness: " + population.get(0).getFitness());
	}

	/** {@inheritDoc} */
	@Override
	public void generateSolution() {
		if (population.isEmpty())
			initializePopulation();

		double fitness = population.get(0).getFitness();
		while (!isFinished()) {
			if ((getFitnessFunction().isMaximizationFunction() && getBestIndividual().getFitness() > fitness)
			        || (!getFitnessFunction().isMaximizationFunction() && getBestIndividual().getFitness() < fitness)) {
				logger.info("Current generation: " + getAge());
				logger.info("Best fitness: " + getBestIndividual().getFitness());
				fitness = population.get(0).getFitness();
			}
			evolve();

			applyLocalSearch();

			updateFitnessFunctionsAndValues();

			this.notifyIteration();
		}
		
		updateBestIndividualFromArchive();
		notifySearchFinished();
	}
}
