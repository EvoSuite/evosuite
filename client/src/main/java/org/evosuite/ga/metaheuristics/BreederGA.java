/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.ga.metaheuristics;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.runtime.util.AtMostOnceLogger;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a BreederGA as described in:
 * H. Muëhlenbein and D. Schlierkamp-Voosen,
 * "Predictive models for the breeder genetic algorithm. contiunous parameter optimization,”
 * Evolutionary Computation, vol. 1, no. 1, pp. 25–49, 1993.
 * <p>
 * This uses standard mutation and crossover.
 *
 * @param <T>
 */
public class BreederGA<T extends Chromosome<T>> extends StandardGA<T> {

    private static final long serialVersionUID = 8305884372813786175L;
    private final Logger logger = LoggerFactory.getLogger(BreederGA.class);

    /**
     * Constructor
     *
     * @param factory a {@link org.evosuite.ga.ChromosomeFactory} object.
     */
    public BreederGA(ChromosomeFactory<T> factory) {
        super(factory);
    }

    @Override
    protected void evolve() {

        // Elitism
        List<T> newGeneration = new ArrayList<>(elitism());

        // Truncation selection
        List<T> candidates = population.subList(0, (int) (population.size() * Properties.TRUNCATION_RATE));

        // If there are no candidates, the parameters are not set optimally,
        if (candidates.size() <= 1) {
            candidates.addAll(population);
            AtMostOnceLogger.warn(logger, "Not sufficient candidates for reproduction, consider increasing the population size, or the truncation rate");
        }

        // new_generation.size() < population_size
        while (!isNextPopulationFull(newGeneration)) {

            T parent1 = Randomness.choice(candidates);
            T parent2 = Randomness.choice(candidates);

            // Self-breeding is nor allowed
            if (parent1 == parent2) {
                continue;
            }

            T offspring1 = parent1.clone();
            T offspring2 = parent2.clone();

            try {
                crossoverFunction.crossOver(offspring1, offspring2);
            } catch (ConstructionFailedException e) {
                logger.info("CrossOver/Mutation failed.");
                continue;
            }


            T offspring = Randomness.choice(offspring1, offspring2);

            notifyMutation(offspring);
            offspring.mutate();

            if (offspring.isChanged()) {
                offspring.updateAge(currentIteration);
            }
            if (!isTooLong(offspring)) {
                newGeneration.add(offspring1);
            }
        }

        population = newGeneration;
        //archive
        updateFitnessFunctionsAndValues();
        //
        currentIteration++;
    }

}
