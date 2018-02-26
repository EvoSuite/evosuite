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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.evosuite.Properties;
import org.evosuite.TimeController;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Chemical Reaction Optimization as described in the paper "Chemical Reaction
 * Optimization: a tutorial" by Albert Y. S. Lam and Victor O. K. Li, Memetic Computing, March 2012,
 * Volume 4, Issue 1, pp 3-17.
 *
 * @author José Campos
 */
public class StandardChemicalReaction<T extends Chromosome> extends GeneticAlgorithm<T> {

  private static final long serialVersionUID = 2723118789259809773L;

  private static final Logger logger = LoggerFactory.getLogger(StandardChemicalReaction.class);

  private double buffer = 0;

  private double initialEnergy = 0.0;

  private List<T> elite = new ArrayList<T>(Properties.ELITE);

  /**
   * Constructor
   *
   * @param factory a {@link org.evosuite.ga.ChromosomeFactory} object.
   */
  public StandardChemicalReaction(ChromosomeFactory<T> factory) {
    super(factory);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void evolve() {

    if (Randomness.nextDouble() > Properties.MOLECULAR_COLLISION_RATE || this.population.size() == 1) {
      // uni-molecular collision
      logger.debug("an uni-molecular collision has occurred");

      int moleculeIndex = Randomness.nextInt(this.population.size());
      assert moleculeIndex >= 0 && moleculeIndex < this.population.size();
      T molecule = this.population.get(moleculeIndex);
      assert molecule != null;

      // has it been involved an on-wall ineffective collision or a decomposition?
      // (it can be done by check decomposition criterion on the chosen molecule)
      if (this.decompositionCheck(molecule)) {
        // decomposition
        logger.debug("a decomposition has occurred");

        List<T> offsprings = this.decomposition(molecule);
        if (offsprings != null) {
          // remove 'molecule' from population, and add 'offspring1' and 'offspring2' to population
          this.population.remove(moleculeIndex);
          this.population.addAll(offsprings);
        }
      } else {
        // on-wall ineffective collision
        logger.debug("an on-wall ineffective collision has occurred");

        T newMolecule = this.onwallIneffectiveCollision(molecule);
        if (newMolecule != null) {
          this.population.set(moleculeIndex, newMolecule);
        }
      }
    } else {
      // inter-molecular collision
      logger.debug("an inter-molecular collision has occurred");

      int molecule1Index = Randomness.nextInt(this.population.size());
      assert molecule1Index >= 0 && molecule1Index < this.population.size();
      int molecule2Index = Randomness.nextInt(this.population.size());
      while (molecule2Index == molecule1Index) {
        // find a different molecule as an inter-molecular collision involves at least two molecules
        molecule2Index = Randomness.nextInt(this.population.size());
      }
      assert molecule2Index >= 0 && molecule2Index < this.population.size();
      assert molecule1Index != molecule2Index;

      T molecule1 = this.population.get(molecule1Index);
      T molecule2 = this.population.get(molecule2Index);

      // have they been involved an inter-molecular ineffective collision or a synthesis?
      if (this.synthesisCheck(molecule1) && this.synthesisCheck(molecule2)) {
        // synthesis
        logger.debug("a synthesis has occurred");

        T offspring = this.synthesis(molecule1, molecule2);
        if (offspring != null) {
          // remove 'molecule1' and 'molecule2' from population, and add 'offspring'
          this.population.set(molecule1Index, offspring);
          this.population.remove(molecule2Index);
        }
      } else {
        // inter-molecular ineffective collision
        logger.debug("an inter-molecular ineffective collision has occurred");

        Pair<T, T> newMolecules = this.intermolecularIneffectiveCollision(molecule1, molecule2);
        if (newMolecules != null) {
          this.population.set(molecule1Index, newMolecules.getLeft());
          this.population.set(molecule2Index, newMolecules.getRight());
        }
      }
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

    logger.debug("Set up initial population");
    this.generateInitialPopulation(Properties.POPULATION);

    // update fitness values of all individuals
    this.calculateFitnessAndSortPopulation();

    this.initialEnergy = this.getCurrentAmountOfEnergy();
    logger.debug("Initial energy is " + this.initialEnergy);

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
      this.elite = this.elitism();
      this.evolve();
      this.applyLocalSearch();

      logger.debug("Updating fitness values");
      this.updateFitnessFunctionsAndValues();

      logger.debug("Current iteration: " + this.currentIteration);
      this.notifyIteration();

      if (Properties.ELITE > 0) {
        // perform elitism
        for (int i = 0; i < this.elite.size(); i++) {
          T best = (T) this.elite.get(i); // elite already includes a copy of each individual, so no
                                          // need to clone it

          int moleculeIndex = Randomness.nextInt(this.population.size());
          T molecule = this.population.get(moleculeIndex);

          double bestTotalKineticEnergy = best.getKineticEnergy() + best.getFitness();
          double moleculeTotalKineticEnergy = molecule.getKineticEnergy() + molecule.getFitness();
          double dif = bestTotalKineticEnergy - moleculeTotalKineticEnergy;
          best.setKineticEnergy(best.getKineticEnergy() - dif);
          best.setNumCollisions(molecule.getNumCollisions());

          this.population.remove(moleculeIndex);
          this.population.add(best);
        }

        // keep it sorted. the algorithm does not need to have the population sorted, but if the
        // algorithms runs out of time, only the head of the population would be returned, and it
        // makes sense to be the best one. also, as elitism is enabled, the next elite of
        // individuals would be the first N individuals of the population, so better to keep sorted.
        this.sortPopulation();
      }

      // One of the fundamental assumptions of Chemical Reaction Optimization is conservation of
      // energy, which means that energy cannot be created or destroyed. The whole system refers to
      // all the defined molecules and the container, which is connected to buffer.

      double currentEnergy = this.getCurrentAmountOfEnergy();

      if (shouldApplyLocalSearch() || Properties.TEST_ARCHIVE) {
        // as a local-search approach and the use of a test archive could change or update the
        // fitness function value of any individual in the population, in here we have to make
        // sure the amount of energy in the system is exactly the same as initially defined

        if (currentEnergy > this.initialEnergy) {
          // if, for example, individuals in the population got worse (i.e., higher fitness
          // values) over time, in here we must to reduce the amount of memory that is free
          double delta = currentEnergy - this.initialEnergy;
          this.buffer = this.buffer - delta;
        } else if (currentEnergy < this.initialEnergy) {
          // if, for example, individuals in the population got better (i.e., lower fitness
          // values) over time, it means molecules have released some energy to the system
          double delta = this.initialEnergy - currentEnergy;
          this.buffer += delta;
        }

        if (this.buffer < 0.0) {
          throw new RuntimeException("Amount of energy in the buffer cannot be negative");
        }

        // sanity check: re-calculate current amount of energy
        currentEnergy = this.getCurrentAmountOfEnergy();
      }

      if (!this.hasEnergyBeenConserved(currentEnergy)) {
        throw new RuntimeException("Current amount of energy (" + currentEnergy
            + ") in the system is not equal to its initial amount of energy (" + this.initialEnergy
            + "). Conservation of energy has failed!");
      }
    }

    TimeController.execute(this::updateBestIndividualFromArchive, "Update from archive", 5_000);
    this.notifySearchFinished();
  }

  private boolean decompositionCheck(T molecule) {
    return molecule.getNumCollisions() > Properties.DECOMPOSITION_THRESHOLD;
  }

  private boolean synthesisCheck(T molecule) {
    return molecule.getKineticEnergy() <= Properties.SYNTHESIS_THRESHOLD;
  }

  /**
   * An on-wall ineffective collision represents the situation when a molecule collides with a wall
   * of the container and then bounces away remaining in one single unit.
   * 
   * @param molecule a {@link org.evosuite.ga.Chromosome} object
   * @return a {@link org.evosuite.ga.Chromosome} object if new solution is found, null otherwise
   */
  @SuppressWarnings("unchecked")
  private T onwallIneffectiveCollision(T molecule) {

    double potencialEnergy = molecule.getFitness();
    double kineticEnergy = molecule.getKineticEnergy();
    molecule.increaseNumCollisionsByOne();

    T moleculeClone = (T) molecule.clone();
 
    // mutate it

    this.notifyMutation(moleculeClone);
    moleculeClone.mutate();
    if (!moleculeClone.isChanged()) {
      logger.debug("Mutation failed to change the individual");
      return null;
    }
    if (isTooLong(moleculeClone) || moleculeClone.size() == 0) {
      logger.debug("Ignoring individual as it has zero or too many tests");
      return null;
    }

    // evaluate it

    for (FitnessFunction<T> fitnessFunction : this.fitnessFunctions) {
      fitnessFunction.getFitness(moleculeClone);
      this.notifyEvaluation(moleculeClone);
    }

    double potencialEnergyClone = moleculeClone.getFitness();
    if (potencialEnergy + kineticEnergy >= potencialEnergyClone) {
      double a = Randomness.nextDouble(Properties.KINETIC_ENERGY_LOSS_RATE, 1.0);
      moleculeClone.setKineticEnergy((potencialEnergy - potencialEnergyClone + kineticEnergy) * a);

      // the remaining energy is transferred to buffer
      this.buffer = this.buffer + (potencialEnergy - potencialEnergyClone + kineticEnergy) * (1.0 - a);

      logger.debug(
          "(" + potencialEnergy + "," + kineticEnergy + ")" + " vs " + "(" + potencialEnergyClone
              + "," + moleculeClone.getKineticEnergy() + ")\n" + "Buffer: " + this.buffer);

      return moleculeClone;
    }

    return null;
  }

  /**
   * Decomposition refers to the situation when a molecule hits a wall and then breaks into several
   * parts.
   * 
   * @param molecule a {@link org.evosuite.ga.Chromosome} object
   * @return a {@link java.util.List} object with two offspring if new solutions are found, null
   *         otherwise
   */
  @SuppressWarnings("unchecked")
  private List<T> decomposition(T molecule) {

    // The idea of decomposition is to allow the system to explore other regions of the solution
    // space after enough local search by the ineffective collisions, similar to what mutation does
    // in evolutionary algorithms.

    double potencialEnergy = molecule.getFitness();
    double kineticEnergy = molecule.getKineticEnergy();

    T offspring1 = (T) molecule.clone();
    T offspring2 = (T) molecule.clone();

    // mutate offspring

    this.notifyMutation(offspring1);
    offspring1.mutate();
    this.notifyMutation(offspring2);
    offspring2.mutate();

    if (!offspring1.isChanged() && !offspring2.isChanged()) {
      logger.debug("Mutation failed to change both individuals");
      return null;
    }
    if (isTooLong(offspring1) || offspring1.size() == 0 || isTooLong(offspring2)
        || offspring2.size() == 0) {
      logger.debug("Ignoring individuals as at least one has zero or too many tests");
      return null;
    }

    // evaluate offspring

    for (FitnessFunction<T> fitnessFunction : this.fitnessFunctions) {
      fitnessFunction.getFitness(offspring1);
      this.notifyEvaluation(offspring1);
      fitnessFunction.getFitness(offspring2);
      this.notifyEvaluation(offspring2);
    }

    double potencialEnergy1 = offspring1.getFitness();
    double potencialEnergy2 = offspring2.getFitness();

    boolean decomposed = false;

    if (potencialEnergy + kineticEnergy >= potencialEnergy1 + potencialEnergy2) {
      double eDec = potencialEnergy + kineticEnergy - (potencialEnergy1 + potencialEnergy2);
      this.updateMoleculesAfterDecomposition(offspring1, offspring2, eDec);

      decomposed = true;
    } else {
      double delta1 = Randomness.nextDouble();
      double delta2 = Randomness.nextDouble();

      double eDec = (potencialEnergy + kineticEnergy + delta1 * delta2 * this.buffer)
          - (potencialEnergy1 + potencialEnergy2);
      if (eDec >= 0) {
        this.buffer = this.buffer * (1.0 - delta1 * delta2);

        // update molecules
        this.updateMoleculesAfterDecomposition(offspring1, offspring2, eDec);

        // destroy 'molecule', i.e., 'molecule' must be replaced by the two newly generated
        // molecules ('offspring1', 'offspring2')
        decomposed = true;
      } else {
        molecule.increaseNumCollisionsByOne();

        // destroy 'offspring1' and 'offspring2'
        decomposed = false;
      }
    }

    if (decomposed) {
      List<T> offsprings = new ArrayList<T>(2);
      offsprings.add(offspring1);
      offsprings.add(offspring2);

      logger.debug("(" + potencialEnergy + "," + kineticEnergy + ")" + " vs " + "("
          + potencialEnergy1 + "," + offspring1.getKineticEnergy() + ")" + " --- " + "("
          + potencialEnergy2 + "," + offspring2.getKineticEnergy() + ")\n" + "Buffer: "
          + this.buffer + " of " + this.initialEnergy);

      return offsprings;
    }

    return null;
  }

  private void updateMoleculesAfterDecomposition(T moleculeClone1, T moleculeClone2, double eDec) {
    // distribute energy
    double delta3 = Randomness.nextDouble();
    moleculeClone1.setKineticEnergy(eDec * delta3);
    moleculeClone2.setKineticEnergy(eDec * (1.0 - delta3));
    // reset number of collisions
    moleculeClone1.resetNumCollisions();
    moleculeClone2.resetNumCollisions();
  }

  /**
   * Inter-molecular ineffective collision takes place when multiple molecules collide with each
   * other and then bounce away.
   * 
   * @param molecule1 a {@link org.evosuite.ga.Chromosome} object
   * @param molecule2 a {@link org.evosuite.ga.Chromosome} object
   * @return a pair of a {@link org.evosuite.ga.Chromosome} object if new solutions are found, null
   *         otherwise
   */
  @SuppressWarnings("unchecked")
  private Pair<T, T> intermolecularIneffectiveCollision(T molecule1, T molecule2) {

    double potencialEnergy1 = molecule1.getFitness();
    double kineticEnergy1 = molecule1.getKineticEnergy();
    molecule1.increaseNumCollisionsByOne();

    double potencialEnergy2 = molecule2.getFitness();
    double kineticEnergy2 = molecule2.getKineticEnergy();
    molecule2.increaseNumCollisionsByOne();

    T moleculeClone1 = (T) molecule1.clone();
    T moleculeClone2 = (T) molecule2.clone();

    // mutate clones

    this.notifyMutation(moleculeClone1);
    moleculeClone1.mutate();
    this.notifyMutation(moleculeClone2);
    moleculeClone2.mutate();

    if (!moleculeClone1.isChanged() && !moleculeClone2.isChanged()) {
      logger.debug("Mutation failed to change both individuals");
      return null;
    }
    if (isTooLong(moleculeClone1) || moleculeClone1.size() == 0 || isTooLong(moleculeClone2)
        || moleculeClone2.size() == 0) {
      logger.debug("Ignoring individuals as at least one has zero or too many tests");
      return null;
    }

    // evaluate clones

    for (FitnessFunction<T> fitnessFunction : this.fitnessFunctions) {
      fitnessFunction.getFitness(moleculeClone1);
      this.notifyEvaluation(moleculeClone1);
      fitnessFunction.getFitness(moleculeClone2);
      this.notifyEvaluation(moleculeClone2);
    }

    double potencialEnergyClone1 = moleculeClone1.getFitness();
    double potencialEnergyClone2 = moleculeClone2.getFitness();

    double eInter = (potencialEnergy1 + potencialEnergy2 + kineticEnergy1 + kineticEnergy2)
        - (potencialEnergyClone1 + potencialEnergyClone2);
    if (eInter >= 0) {
      // distribute energy
      double delta4 = Randomness.nextDouble();
      moleculeClone1.setKineticEnergy(eInter * delta4);
      moleculeClone2.setKineticEnergy(eInter * (1.0 - delta4));

      logger.debug("(" + potencialEnergy1 + "," + kineticEnergy1 + ")" + " vs " + "("
          + potencialEnergyClone1 + "," + moleculeClone1.getKineticEnergy() + ")\n" + "("
          + potencialEnergy2 + "," + kineticEnergy2 + ")" + " vs " + "(" + potencialEnergyClone2
          + "," + moleculeClone2.getKineticEnergy() + ")\n" + "Buffer: " + this.buffer);

      return new ImmutablePair<T, T>(moleculeClone1, moleculeClone2);
    }

    return null;
  }

  /**
   * Synthesis does the opposite of decomposition. A synthesis happens when multiple (assume two)
   * molecules hit against each other and fuse together.
   * 
   * @param molecule1 a {@link org.evosuite.ga.Chromosome} object
   * @param molecule2 a {@link org.evosuite.ga.Chromosome} object
   * @return a {@link org.evosuite.ga.Chromosome} object if a new solution is found, null otherwise
   */
  @SuppressWarnings("unchecked")
  private T synthesis(T molecule1, T molecule2) {

    // The idea behind synthesis is diversification of solutions, similar to what crossover does in
    // evolutionary algorithms.

    double potencialEnergy1 = molecule1.getFitness();
    double kineticEnergy1 = molecule1.getKineticEnergy();

    double potencialEnergy2 = molecule2.getFitness();
    double kineticEnergy2 = molecule2.getKineticEnergy();

    T offspring1 = (T) molecule1.clone();
    T offspring2 = (T) molecule2.clone();

    // crossover offspring

    try {
      this.crossoverFunction.crossOver(offspring1, offspring2);
    } catch (ConstructionFailedException e) {
      logger.debug("CrossOver failed");
      logger.debug(e.toString());
      return null;
    }

    if (!offspring1.isChanged() && !offspring2.isChanged()) {
      logger.debug("Crossover failed to change both individuals");
      return null;
    }

    // evaluate and choose one of the offspring

    for (FitnessFunction<T> fitnessFunction : this.fitnessFunctions) {
      fitnessFunction.getFitness(offspring1);
      this.notifyEvaluation(offspring1);
      fitnessFunction.getFitness(offspring2);
      this.notifyEvaluation(offspring2);
    }

    T offspring = offspring1.getFitness() < offspring2.getFitness() ? offspring1 : offspring2;

    double potencialEnergy = offspring.getFitness();
    if (potencialEnergy1 + potencialEnergy2 + kineticEnergy1 + kineticEnergy2 >= potencialEnergy) {
      offspring
          .setKineticEnergy((potencialEnergy1 + potencialEnergy2 + kineticEnergy1 + kineticEnergy2)
              - potencialEnergy);
      // reset number of collisions
      offspring.resetNumCollisions();

      logger.debug("(" + potencialEnergy1 + "," + kineticEnergy1 + ")" + " --- " + "("
          + potencialEnergy2 + "," + kineticEnergy2 + ")" + " vs " + "(" + potencialEnergy + ","
          + offspring.getKineticEnergy() + ")\n" + "Buffer: " + this.buffer);

      // destroy 'offspring1' and 'offspring2', i.e., 'molecule1' and 'molecule2' must be replaced
      // by the newly generated molecule ('offspring')
      return offspring;
    } else {
      molecule1.increaseNumCollisionsByOne();
      molecule2.increaseNumCollisionsByOne();

      // destroy 'offspring'
      return null;
    }
  }

  /**
   * Returns the current amount of energy in the system.
   * 
   * @return
   */
  private double getCurrentAmountOfEnergy() {
    double energy = this.buffer;
    for (T t : this.population) {
      energy += t.getFitness() + t.getKineticEnergy();
    }
    return energy;
  }

  /**
   * Given a certain amount of energy, it checks whether energy has been conserved in the system.
   * 
   * @param energy
   * @return true if energy has been conserved in the system, false otherwise
   */
  private boolean hasEnergyBeenConserved(double energy) {
    return Math.abs(this.initialEnergy - energy) < 0.000000001;
  }
}
