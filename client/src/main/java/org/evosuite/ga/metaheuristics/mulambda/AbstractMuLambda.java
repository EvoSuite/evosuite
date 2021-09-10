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

import org.evosuite.Properties;
import org.evosuite.TimeController;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>AbstractMuLambda</p>
 *
 * @author José Campos
 */
public abstract class AbstractMuLambda<T extends Chromosome<T>> extends GeneticAlgorithm<T> {

    private static final long serialVersionUID = 2738004761503761376L;

    private static final Logger logger = LoggerFactory.getLogger(AbstractMuLambda.class);

    protected final int mu;

    protected final int lambda;

    public AbstractMuLambda(ChromosomeFactory<T> factory, int mu, int lambda) {
        super(factory);
        this.mu = mu;
        this.lambda = lambda;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializePopulation() {
        this.notifySearchStarted();
        this.currentIteration = 0;

        // set up initial population
        this.generateRandomPopulation(this.mu);
        assert this.population.size() == this.mu;

        // update fitness values of all individuals
        this.calculateFitnessAndSortPopulation();

        this.notifyIteration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateSolution() {
        if (this.population.isEmpty()) {
            this.initializePopulation();
        }

        if (Properties.ENABLE_SECONDARY_OBJECTIVE_AFTER > 0
                || Properties.ENABLE_SECONDARY_OBJECTIVE_STARVATION) {
            this.disableFirstSecondaryCriterion();
        }

        int starvationCounter = 0;
        double bestFitness = Double.MAX_VALUE;
        double lastBestFitness = Double.MAX_VALUE;

        if (getFitnessFunction().isMaximizationFunction()) {
            bestFitness = 0.0;
            lastBestFitness = 0.0;
        }

        while (!isFinished()) {
            logger.debug("Current population: " + getAge() + "/" + Properties.SEARCH_BUDGET);
            logger.info("Best fitness: " + getBestIndividual().getFitness());
            this.evolve();

            this.applyLocalSearch();

            double newFitness = getBestIndividual().getFitness();
            if (getFitnessFunction().isMaximizationFunction()) {
                assert (newFitness >= bestFitness) : "best fitness was: " + bestFitness
                        + ", now best fitness is " + newFitness;
            } else {
                assert (newFitness <= bestFitness) : "best fitness was: " + bestFitness
                        + ", now best fitness is " + newFitness;
            }
            bestFitness = newFitness;

            if (Double.compare(bestFitness, lastBestFitness) == 0) {
                starvationCounter++;
            } else {
                logger.info("reset starvationCounter after " + starvationCounter + " iterations");
                starvationCounter = 0;
                lastBestFitness = bestFitness;
            }

            // update fitness values of all individuals
            this.updateFitnessFunctionsAndValues();

            this.notifyIteration();
        }

        TimeController.execute(this::updateBestIndividualFromArchive, "update from archive", 5_000);
        this.notifySearchFinished();
    }
}
