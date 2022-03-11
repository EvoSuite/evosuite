/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.ga.metaheuristics.mulambda;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.ConstructionFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 1+(lambda,lambda) GA
 *
 * @author Yan Ge
 */
public class OnePlusLambdaLambdaGA<T extends Chromosome<T>> extends AbstractMuLambda<T> {

    private static final long serialVersionUID = 529089847512798127L;

    private static final Logger logger = LoggerFactory.getLogger(OnePlusLambdaLambdaGA.class);

    public OnePlusLambdaLambdaGA(ChromosomeFactory<T> factory, int lambda) {
        super(factory, 1, lambda);
    }

    @Override
    protected void evolve() {

        List<T> mutants = new ArrayList<>();

        T parent = population.get(0).clone();

        while (mutants.size() < this.lambda) {
            // clone firstly offspring from parent
            T MutationOffspring = parent.clone();
            notifyMutation(MutationOffspring);

            // perform mutation operation with high probability
            MutationOffspring.mutate();
            mutants.add(MutationOffspring);
        }

        // mutants are evaluated as current population so that the best mutant
        // can be selected
        population = mutants;

        updateFitnessFunctionsAndValues();
        calculateFitnessAndSortPopulation();

        // obtain the best mutant
        T bestMutantOffspring = getBestIndividual();

        // start to execute uniform crossover operator
        List<T> crossoverOffspring = new ArrayList<>();

        while (crossoverOffspring.size() < this.lambda) {
            try {
                T p1 = parent.clone();
                T p2 = bestMutantOffspring.clone();

                crossoverFunction.crossOver(p1, p2);

                crossoverOffspring.add(p1);
                crossoverOffspring.add(p2);
            } catch (ConstructionFailedException e) {
                logger.info("CrossOver failed.");
            }
        }

        population = crossoverOffspring;
        updateFitnessFunctionsAndValues();
        T bestCrossoverOffspring = getBestIndividual();

        T so_far_best_individual;
        // compare bestCrossover offspring with parent and select the better one
        if (isBetterOrEqual(bestCrossoverOffspring, parent)) {
            so_far_best_individual = bestCrossoverOffspring;
        } else {
            so_far_best_individual = parent;
        }

        // compare the so_far_best_individual with best mutant, and select the better one to be the
        // parent for next iteration.
        if (isBetterOrEqual(so_far_best_individual, bestMutantOffspring)) {
            population.set(0, so_far_best_individual);
        } else {
            population.set(0, bestMutantOffspring);
        }

        currentIteration++;
    }
}
