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
import org.evosuite.TimeController;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * (Mu + Lambda) EA
 *
 * @author José Campos
 */
public class MuPlusLambdaEA<T extends Chromosome> extends GeneticAlgorithm<T> {

  private static final long serialVersionUID = -4011708411919957290L;

  public MuPlusLambdaEA(ChromosomeFactory<T> factory) {
    super(factory);
  }

  /** {@inheritDoc} */
  @SuppressWarnings("unchecked")
  @Override
  protected void evolve() {

    List<T> offsprings = new ArrayList<T>(Properties.LAMBDA);

    // create new offsprings by mutating current population
    while (!isNextPopulationFull(offsprings)) {
      for (T parent : this.population) {
        T offspring = (T) parent.clone();
        this.notifyMutation(offspring);

        do {
          offspring.mutate();
        } while (!offspring.isChanged());

        offsprings.add(offspring);
      }
    }

    // update fitness values of offsprings
    for (T offspring : offsprings) {
      for (FitnessFunction<T> fitnessFunction : this.fitnessFunctions) {
        fitnessFunction.getFitness(offspring);
        this.notifyEvaluation(offspring);
      }
    }

    for (int i = 0; i < this.population.size(); i++) {
      T parent = this.population.get(i);
      T bestOffspring = null;

      for (T offspring : offsprings) {
        if (isBetterOrEqual(offspring, parent)) {
          bestOffspring = offspring;
        }
      }

      if (bestOffspring != null) {
        // replace individual with a better one
        this.population.set(i, bestOffspring);
        // to prevent a population with only equal and dominant
        // individuals, here the best offspring is remove so that
        // it cannot be chosen again. in case of 1+1 and 1+Lambda EA
        // this optimization has no effect.
        offsprings.remove(bestOffspring);
      }

      this.population.get(i).updateAge(this.currentIteration);
    }
    assert this.population.size() == Properties.MU;

    this.currentIteration++;
  }

  /** {@inheritDoc} */
  @Override
  public void initializePopulation() {
    this.notifySearchStarted();
    this.currentIteration = 0;
    // set up initial population
    this.generateRandomPopulation(Properties.MU);
    assert this.population.size() == Properties.MU;
    // update fitness values of all individuals
    this.calculateFitnessAndSortPopulation();

    // checks like 'isNextPopulationFull' use POPULATION rather
    // than LAMBDA
    Properties.POPULATION = Properties.LAMBDA;

    this.notifyIteration();
  }

  /** {@inheritDoc} */
  @Override
  public void generateSolution() {
    if (this.population.isEmpty()) {
      this.initializePopulation();
    }

    if (Properties.ENABLE_SECONDARY_OBJECTIVE_AFTER > 0
        || Properties.ENABLE_SECONDARY_OBJECTIVE_STARVATION) {
      this.disableFirstSecondaryCriterion();
    }

    while (!isFinished()) {
      this.evolve();

      this.applyLocalSearch();

      // update fitness values of all individuals
      this.updateFitnessFunctionsAndValues();

      this.notifyIteration();
    }

    TimeController.execute(this::updateBestIndividualFromArchive, "update from archive", 5_000);
    this.notifySearchFinished();
  }
}
