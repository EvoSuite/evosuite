/**
 * Copyright (C) 2010-2017 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.ConstructionFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 1+(lambda,lambda) GA
 * 
 * @author Yan Ge
 */
public class OnePlusLambdaLambdaGA<T extends Chromosome> extends GeneticAlgorithm<T> {

  private static final long serialVersionUID = 529089847512798127L;

  private static final Logger logger = LoggerFactory.getLogger(OnePlusLambdaLambdaGA.class);

  public OnePlusLambdaLambdaGA(ChromosomeFactory<T> factory) {
    super(factory);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void evolve() {

    List<T> mutants = new ArrayList<T>();

    T parent = (T) population.get(0).clone();

    while (!isNextPopulationFull(mutants)) {
      // clone firstly offspring from parent
      T MutationOffspring = (T) parent.clone();
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
    List<T> crossoverOffspring = new ArrayList<T>();

    while (!isNextPopulationFull(crossoverOffspring)) {
      try {
        T p1 = (T) parent.clone();
        T p2 = (T) bestMutantOffspring.clone();

        crossoverFunction.crossOver(p1, p2);

        crossoverOffspring.add(p1);
        crossoverOffspring.add(p2);
      } catch (ConstructionFailedException e) {
        logger.info("CrossOver failed.");
        continue;
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

  @Override
  public void initializePopulation() {
    notifySearchStarted();
    currentIteration = 0;
    // Initialize one size parent
    generateRandomPopulation(1);
    // Determine fitness
    calculateFitnessAndSortPopulation();
    this.notifyIteration();
    logger.info("Initial fitness: " + population.get(0).getFitness());
  }

  @Override
  public void generateSolution() {
    if (Properties.ENABLE_SECONDARY_OBJECTIVE_AFTER > 0
        || Properties.ENABLE_SECONDARY_OBJECTIVE_STARVATION) {
      disableFirstSecondaryCriterion();
    }

    if (population.isEmpty()) {
      initializePopulation();
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

      evolve();

      applyLocalSearch();

      double newFitness = getBestIndividual().getFitness();

      if (getFitnessFunction().isMaximizationFunction())
        assert (newFitness >= bestFitness) : "best fitness was: " + bestFitness
            + ", now best fitness is " + newFitness;
      else
        assert (newFitness <= bestFitness) : "best fitness was: " + bestFitness
            + ", now best fitness is " + newFitness;
      bestFitness = newFitness;

      if (Double.compare(bestFitness, lastBestFitness) == 0) {
        starvationCounter++;
      } else {
        logger.info("reset starvationCounter after " + starvationCounter + " iterations");
        starvationCounter = 0;
        lastBestFitness = bestFitness;
      }

      updateSecondaryCriterion(starvationCounter);

      this.notifyIteration();
    }
    updateBestIndividualFromArchive();
    notifySearchFinished();
  }

}
