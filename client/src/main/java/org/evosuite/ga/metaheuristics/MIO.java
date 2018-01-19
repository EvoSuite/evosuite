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

import org.evosuite.Properties;
import org.evosuite.TimeController;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.archive.Archive;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.factories.RandomLengthTestFactory;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.Randomness;
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

  private final ChromosomeFactory<TestChromosome> randomFactory = new RandomLengthTestFactory();

  private double pr = Properties.P_RANDOM_TEST_OR_FROM_ARCHIVE;

  private int n = Properties.NUMBER_OF_TESTS_PER_TARGET;

  private TestSuiteChromosome solution = null;

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
  @SuppressWarnings("unchecked")
  @Override
  protected void evolve() {

    // From the second step on, MIO will decide to either sample a new test at random
    // (probability Pr), or will choose one existing test in the archive (probability
    // 1 - Pr), copy it, and mutate it.
    //
    // Note: in MIO there is an extra parameter m which controls how many mutations and
    // fitness evaluations should be done on the same individual before sampling a new
    // one.

    if (this.solution == null || (this.solution
        .getNumberOfMutations() >= Properties.MAX_NUM_MUTATIONS_BEFORE_GIVING_UP
        || this.solution
            .getNumberOfEvaluations() >= Properties.MAX_NUM_FITNESS_EVALUATIONS_BEFORE_GIVING_UP)) {

      this.solution = new TestSuiteChromosome(this.randomFactory);

      if (Randomness.nextDouble() < this.pr) {
        TestChromosome test = this.randomFactory.getChromosome();
        assert test.getTestCase().size() > 0;
        this.solution.addTest(test);
      } else {
        TestCase test = Archive.getArchiveInstance().getRandomSolution();
        assert test.size() > 0;
        if (test == null) {
          this.solution.addTest(this.randomFactory.getChromosome());
        } else {
          this.solution.addTest(test);
        }
      }
    }
    assert this.solution != null;

    // mutate it
    notifyMutation(this.solution);
    this.solution.mutate();

    // evaluate it
    for (FitnessFunction<T> fitnessFunction : this.fitnessFunctions) {
      fitnessFunction.getFitness((T) this.solution);
    }

    // after a certain amount of search-budget, focus the search (see Section 3.2)
    double progress = this.progress();
    if (progress >= Properties.EXPLOITATION_STARTS_AT_PERCENT) {
      double dif = Math.abs(Properties.EXPLOITATION_STARTS_AT_PERCENT - progress);
      this.pr = Properties.P_RANDOM_TEST_OR_FROM_ARCHIVE - dif;
      if (this.pr < 0.0) {
        this.pr = 0.0;
      }

      this.n = (int) (Properties.NUMBER_OF_TESTS_PER_TARGET
          - (dif * Properties.NUMBER_OF_TESTS_PER_TARGET));
      if (this.n < 1) {
        this.n = 1;
      }
      Archive.getArchiveInstance().shrinkSolutions(this.n);
    }

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
    // At the beginning of the search, the archive will be empty, and so a new test
    // will be randomly generated.
    this.generateInitialPopulation(1);
    assert this.population.size() == 1;
    this.solution = (TestSuiteChromosome) this.population.get(0).clone();

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
