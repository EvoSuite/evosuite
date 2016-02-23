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

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.utils.Randomness;


/**
 * Alternative version of steady state GA
 *
 * @author Gordon Fraser
 */
public class SteadyStateGA<T extends Chromosome> extends MonotonicGA<T> {

	private static final long serialVersionUID = 7301010503732698233L;
	
	private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SteadyStateGA.class);

	/**
	 * Generate a new search object
	 *
	 * @param factory a {@link org.evosuite.ga.ChromosomeFactory} object.
	 */
	public SteadyStateGA(ChromosomeFactory<T> factory) {
		super(factory);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Perform one iteration of the search
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void evolve() {
		logger.debug("Generating offspring");
		currentIteration++;

		T parent1 = selectionFunction.select(population);
		T parent2 = selectionFunction.select(population);
		
		T offspring1 = (T)parent1.clone();
		T offspring2 = (T)parent2.clone();

		try {
			// Crossover
			if (Randomness.nextDouble() <= Properties.CROSSOVER_RATE) {
							crossoverFunction.crossOver(offspring1, offspring2);
}

			// Mutation
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
			logger.info("CrossOver/Mutation failed");
			return;
		} 
		
		// The two offspring replace the parents if and only if one of
		// the offspring is not worse than the best parent.
	    for (FitnessFunction<T> fitnessFunction : fitnessFunctions) {
	        fitnessFunction.getFitness(offspring1);
	        notifyEvaluation(offspring1);
	        fitnessFunction.getFitness(offspring2);
	        notifyEvaluation(offspring2);
	    } 
	    
	    
		// if (replacement_function.keepOffspring(parent1, parent2, offspring1,
		if (!Properties.PARENT_CHECK
		        || keepOffspring(parent1, parent2, offspring1, offspring2)) {
			logger.debug("Keeping offspring");

			if (!isTooLong(offspring1)) {
				population.remove(parent1);
				population.add(offspring1);
			}
			if (!isTooLong(offspring2)) {
				population.remove(parent2);
				population.add(offspring2);
			}
		} else {
			logger.debug("Keeping parents");
		}
		
		updateFitnessFunctionsAndValues();
	}

}
