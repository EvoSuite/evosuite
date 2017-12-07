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

import org.evosuite.Properties;
import org.evosuite.TimeController;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Many Independent Objective (MIO) algorithm
 * 
 * @author José Campos
 */
public class MIO<T extends Chromosome> extends GeneticAlgorithm<T> {

  private static final long serialVersionUID = -5660970130698891194L;

  private final Logger logger = LoggerFactory.getLogger(MIO.class);

  /**
   * Constructor.
   * 
   * @param factory a {@link org.evosuite.ga.ChromosomeFactory} object.
   */
  public MIO(ChromosomeFactory<T> factory) {
    super(factory);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void evolve() {

    // TODO

    this.currentIteration++;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void initializePopulation() {
    this.notifySearchStarted();
    this.currentIteration = 0;

    this.logger.debug("Set up initial population of size one");
    this.generateInitialPopulation(1);

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
      assert !this.population
          .isEmpty() : "Initial population is empty, i.e., EvoSuite could not create any test!";
    }

    if (Properties.ENABLE_SECONDARY_OBJECTIVE_AFTER > 0
        || Properties.ENABLE_SECONDARY_OBJECTIVE_STARVATION) {
      this.disableFirstSecondaryCriterion();
    }

    this.logger.debug("Starting evolution");
    while (!this.isFinished()) {
      this.evolve();
      this.applyLocalSearch();

      this.logger.info("Updating fitness values");
      this.updateFitnessFunctionsAndValues();

      this.logger.info("Current iteration: " + currentIteration);
      this.notifyIteration();
    }

    TimeController.execute(this::updateBestIndividualFromArchive, "Update from archive", 5_000);
    this.notifySearchFinished();
  }
}
