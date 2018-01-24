/**
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
package org.evosuite.ga.metaheuristics;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.evosuite.Properties;
import org.evosuite.TimeController;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.operators.crossover.UniformCrossOver;
import org.evosuite.runtime.util.AtMostOnceLogger;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Differential Evolution EA described in the paper "Differential Evolution -
 * A Simple and Efficient Heuristic for Global Optimization over Continuous Spaces" by Rainer Storn
 * and Kenneth Price, Journal of Global Optimization, December 1997, Volume 11, Issue 4, pp 341-359
 * 
 * @author José Campos
 */
public class DifferentialEvolutionEA<T extends Chromosome> extends GeneticAlgorithm<T> {

  private static final long serialVersionUID = 8300035515426994349L;

  private static final Logger logger = LoggerFactory.getLogger(DifferentialEvolutionEA.class);

  /**
   * Constructor
   *
   * @param factory a {@link org.evosuite.ga.ChromosomeFactory} object.
   */
  public DifferentialEvolutionEA(ChromosomeFactory<T> factory) {
    super(factory);
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  @Override
  protected void evolve() {
    List<T> newGeneration = new ArrayList<T>(this.population.size());

    for (int i = 0; i < this.population.size(); i++) {
      T x = (T) this.population.get(i);
      T xClone = (T) x.clone();

      // select 3 distinct individuals
      List<T> individuals = this.selectNumDifferentIndividuals(i, 3);
      T a = (T) individuals.get(0).clone();
      T b = (T) individuals.get(1).clone();
      T c = (T) individuals.get(2).clone();

      // apply differential evolution => a + F * (b - c)

      // difference calculation => b - c
      b.subtract(c);
      // amplify the differential variation => F * (b - c)
      b.amplify();
      // a + b => a + F * (b - c)
      a.add(b);

      if (isTooLong(a) || a.size() == 0) {
        logger.info("discarding donor as it has length zero or it is too long");
        continue;
      }

      // combine x and a by applying, e.g., uniform crossover (see {@link
      // org.evosuite.ga.operators.crossover.UniformCrossOver}) to create x'
      try {
        this.crossoverFunction.crossOver(xClone, a);
      } catch (ConstructionFailedException e) {
        logger.info("crossOver has failed.");
        continue;
      }

      if (xClone.isChanged()) {
        xClone.updateAge(this.currentIteration);
      }

      // is x' better than x?

      for (FitnessFunction<T> fitnessFunction : this.fitnessFunctions) {
        fitnessFunction.getFitness(xClone);
        this.notifyEvaluation(xClone);
      }

      if (this.isBetterOrEqual(x, xClone)) {
        newGeneration.add(x);
      } else {
        newGeneration.add(xClone);
      }
    }

    this.population = newGeneration;
    this.currentIteration++;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void initializePopulation() {
    this.notifySearchStarted();
    this.currentIteration = 0;

    logger.debug("Set up initial population");
    this.generateInitialPopulation(Properties.POPULATION);
    if (this.population.size() < 4) {
      AtMostOnceLogger.error(logger,
          "The number of individuals in the initial population is not enough to perform"
              + " differential evolution, consider increasing the population size (e.g., >= 4)");
    }

    // update fitness values of all individuals
    this.calculateFitnessAndSortPopulation();

    if (!(this.crossoverFunction instanceof UniformCrossOver)) {
      AtMostOnceLogger.warn(logger,
          "Ideally, differential evolution algorithm must be instantiated with uniform crossover");
    }

    this.notifyIteration();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void generateSolution() {
    if (this.population.isEmpty()) {
      this.initializePopulation();
      assert !this.population
          .isEmpty() : "Initial population is empty, i.e., EvoSuite could not create any test!";
    }

    if (Properties.ENABLE_SECONDARY_OBJECTIVE_AFTER > 0
        || Properties.ENABLE_SECONDARY_OBJECTIVE_STARVATION) {
      this.disableFirstSecondaryCriterion();
    }

    logger.debug("Starting evolution");
    while (!this.isFinished()) {
      this.evolve();
      this.applyLocalSearch();

      logger.debug("Updating fitness values");
      this.updateFitnessFunctionsAndValues();

      logger.debug("Current iteration: " + this.currentIteration);
      this.notifyIteration();
    }

    TimeController.execute(this::updateBestIndividualFromArchive, "Update from archive", 5_000);
    this.notifySearchFinished();
  }

  /**
   * 
   * @param forbiddenIndex
   * @param numIndividuals
   * @return
   */
  private List<T> selectNumDifferentIndividuals(int forbiddenIndex, int numIndividuals) {
    List<Integer> indexes =
        IntStream.range(0, this.population.size()).boxed().collect(Collectors.toList());
    indexes.remove(forbiddenIndex);
    assert indexes.size() == this.population.size() - 1;

    Randomness.shuffle(indexes);

    List<T> individuals = new ArrayList<T>(numIndividuals);
    for (int i = 0; i < numIndividuals; i++) {
      individuals.add(this.population.get(indexes.get(i)));
    }

    assert individuals.size() == numIndividuals;
    return individuals;
  }
}
