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
package org.evosuite.ga.archive;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the archive described in the 'Many Independent Objective (MIO) Algorithm for
 * Test Suite Generation' paper.
 * 
 * @author José Campos
 */
public class MIOArchive<F extends TestFitnessFunction, T extends TestChromosome> extends Archive<F, T> {

  private static final long serialVersionUID = -6100903230303784634L;

  private static final Logger logger = LoggerFactory.getLogger(MIOArchive.class);

  /**
   * Map used to store all targets (keys of the map) and the corresponding covering solutions
   * (values of the map)
   **/
  protected final Map<F, Population> archive = new LinkedHashMap<F, Population>();

  public static final MIOArchive<TestFitnessFunction, TestChromosome> instance =
      new MIOArchive<TestFitnessFunction, TestChromosome>();

  /**
   * {@inheritDoc}
   */
  @Override
  public void addTarget(F target) {
    assert target != null;

    if (!this.archive.containsKey(target)) {
      logger.debug("Registering new target '" + target + "'");
      this.archive.put(target, new Population(Properties.NUMBER_OF_TESTS_PER_TARGET));
    }

    this.registerNonCoveredTargetOfAMethod(target);
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  @Override
  public void updateArchive(F target, T solution, double fitnessValue) {
    assert target != null;
    assert this.archive.containsKey(target);
    assert fitnessValue >= 0.0;

    T solutionClone = (T) solution.clone();

    ExecutionResult executionResult = solutionClone.getLastExecutionResult();
    // remove all statements after an exception
    if (!executionResult.noThrownExceptions()) {
      solutionClone.getTestCase().chop(executionResult.getFirstPositionOfThrownException() + 1);
    }

    boolean isNewCoveredTarget = this.archive.get(target)
        .addSolution(1.0 - FitnessFunction.normalize(fitnessValue), solutionClone);
    if (isNewCoveredTarget) {
      this.removeNonCoveredTargetOfAMethod(target);
      this.hasBeenUpdated = true;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isArchiveEmpty() {
    return this.getNumberOfSolutions() == 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfTargets() {
    return this.archive.keySet().size();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfCoveredTargets() {
    return this.getCoveredTargets().size();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<F> getCoveredTargets() {
    return this.archive.keySet().stream().filter(target -> this.archive.get(target).isCovered())
        .collect(Collectors.toSet());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfUncoveredTargets() {
    return this.getUncoveredTargets().size();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<F> getUncoveredTargets() {
    return this.archive.keySet().stream().filter(target -> !this.archive.get(target).isCovered())
        .collect(Collectors.toSet());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasTarget(F target) {
    assert target != null;
    return this.archive.containsKey(target);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfSolutions() {
    return this.getSolutions().size();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<T> getSolutions() {
    Set<T> solutions = new LinkedHashSet<T>();
    for (Population population : this.archive.values()) {
      T solution = population.getBestSolutionIfAny();
      if (solution != null) {
        solutions.add(solution);
      }
    }
    return solutions;
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  @Override
  public T getSolution() {

    // Choose one target at random that has not been covered but contains some solutions. In case
    // there is not any non-covered target with at least one solution, either because all targets
    // have been covered or for the non-covered targets there is not any solution yet, then choose
    // one of the covered targets at random. Thereafter, choose one solution randomly from the list
    // of solutions of the chosen target.

    List<F> targetsWithSolutions = this.archive.keySet().stream()
        .filter(target -> this.archive.get(target).numSolutions() > 0).collect(Collectors.toList());

    if (targetsWithSolutions.isEmpty()) {
      // there is not at least one target with at least one solution
      return null;
    }

    List<F> potentialTargets = targetsWithSolutions.stream()
        .filter(target -> this.archive.get(target).isCovered() == false).collect(Collectors.toList());

    if (potentialTargets.isEmpty()) {
      potentialTargets =
          targetsWithSolutions.stream().filter(target -> this.archive.get(target).isCovered() == true)
              .collect(Collectors.toList());
    }
    assert !potentialTargets.isEmpty();

    // Instead of choosing a target at random, we choose the one with the lowest counter value.
    // (See Section 3.3 of the paper that describes this archive for more details)

    // F target = Randomness.choice(potentialTargets);
    // T randomSolution = (T) this.archive.get(target).sampleSolution();

    // ASC sort, i.e., from the population with the lowest counter to the population with the
    // highest counter
    potentialTargets.sort(new Comparator<F>() {
      @Override
      public int compare(F f0, F f1) {
        if (archive.get(f0).counter() < archive.get(f1).counter()) {
          return -1;
        } else if (archive.get(f0).counter() > archive.get(f1).counter()) {
          return 1;
        }
        return 0;
      }
    });

    T randomSolution = this.archive.get(potentialTargets.get(0)).sampleSolution();
    return randomSolution == null ? null : (T) randomSolution.clone();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public T getSolution(F target) {
    assert target != null;
    assert this.archive.containsKey(target);
    return this.archive.get(target).getBestSolutionIfAny();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasSolution(F target) {
    assert target != null;
    assert this.archive.containsKey(target);
    return this.archive.get(target).isCovered();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public T getRandomSolution() {
    return Randomness.choice(this.getSolutions());
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public TestSuiteChromosome mergeArchiveAndSolution(Chromosome solution) {
    // Deactivate in case a test is executed and would access the archive as this might cause a
    // concurrent access
    Properties.TEST_ARCHIVE = false;

    TestSuiteChromosome mergedSolution = (TestSuiteChromosome) solution.clone();

    // to avoid adding the same solution to 'mergedSolution' suite
    Set<T> solutionsSampledFromArchive = new LinkedHashSet<T>();

    for (F target : this.archive.keySet()) {
      // does solution cover target?
      if (!target.isCoveredBy(mergedSolution)) {
        Population population = this.archive.get(target);

        // is there any solution in the archive that covers it?
        T t = population.getBestSolutionIfAny();
        if (t != null) {
          // has t been considered?
          if (!solutionsSampledFromArchive.contains(t)) {
            solutionsSampledFromArchive.add(t);

            T tClone = (T) t.clone();
            mergedSolution.addTest(tClone);
          }
        }
      }
    }

    // re-evaluate merged solution
    for (FitnessFunction fitnessFunction : solution.getFitnessValues().keySet()) {
      fitnessFunction.getFitness(mergedSolution);
    }

    // re-active it
    Properties.TEST_ARCHIVE = true;

    logger.info("Final test suite size from archive: " + mergedSolution);
    return mergedSolution;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void shrinkSolutions(int newPopulationSize) {
    assert newPopulationSize > 0;
    for (F target : this.archive.keySet()) {
      this.archive.get(target).shrinkPopulation(newPopulationSize);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "NumTargets: " + this.getNumberOfTargets() + ", NumCoveredTargets: "
        + this.getNumberOfCoveredTargets() + ", NumSolutions: " + this.getNumberOfSolutions();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void reset() {
    super.reset();
    this.archive.clear();
  }

  private class Population implements Serializable {

    private static final long serialVersionUID = 1671692598239736237L;

    private int counter = 0;

    private int capacity;

    private List<Pair<Double, T>> solutions = null;

    /**
     * 
     * @param populationSize
     */
    private Population(int populationSize) {
      this.capacity = populationSize;
      this.solutions = new ArrayList<Pair<Double, T>>(populationSize);
    }

    /**
     * 
     * @return
     */
    private int counter() {
      return this.counter;
    }

    /**
     * 
     * @return
     */
    private boolean isCovered() {
      return this.solutions.size() == 1 && this.capacity == 1
          && this.solutions.get(0).getLeft() == 1.0;
    }

    /**
     * 
     * @param h [0,1] value, where 1 means that the target is covered, and whereas 0 is the worst
     *        possible heuristics value
     * @param t
     */
    private boolean addSolution(Double h, T t) {
      assert h >= 0.0 && h <= 1.0;

      if (h == 0.0) {
        // from the paper that describes this type of archive: "if h=0, the test is not added
        // regardless of the following conditions"
        return false;
      }

      if (h < 1.0 && this.isCovered()) {
        // candidate solution T does not cover the already fully covered target, therefore there is
        // no way it could be any better
        return false;
      }

      Pair<Double, T> candidateSolution = new ImmutablePair<Double, T>(h, t);

      boolean added = false;

      // does the candidate solution fully cover the target?
      if (h == 1.0) {
        // yes. has the target been fully covered by a previous solution?
        if (this.isCovered()) {
          Pair<Double, T> currentSolution = this.solutions.get(0);

          if (isPairBetterThanCurrent(currentSolution, candidateSolution)) {
            added = true;
            this.solutions.set(0, candidateSolution);
          }
        } else {
          // as the target is now fully covered by the candidate solution T, from now on there is no
          // need to keep more than one solution, only the single best one. therefore, we can get
          // rid of all solutions (if any) and shrink the number of solutions to only one.
          added = true;
          this.capacity = 1;
          this.solutions.clear();
          this.solutions.add(candidateSolution);
        }
      } else {
        // no, candidate solution T does not fully cover the target.

        // is there enough room for yet another solution?
        if (this.solutions.size() < this.capacity) {
          // yes, there is.

          // as an optimisation, in here we could check whether candidateSolution is an existing
          // solution, however it could be quite expensive to do it and most likely not worth it
          this.solutions.add(candidateSolution);
          this.sortPairSolutions(); // keep solutions sorted from the best to the worse
        } else {
          // no, there is not. so, replace the worst one, if candidate is better.
          this.sortPairSolutions();
          Pair<Double, T> worstSolution = this.solutions.get(this.capacity - 1);

          if (isPairBetterThanCurrent(worstSolution, candidateSolution)) {
            this.solutions.set(this.capacity - 1, candidateSolution);
          }
        }
      }

      // a set of solutions larger that a maximum capacity would be considered illegal
      assert this.solutions.size() <= this.capacity;

      if (added) {
        // reset counter if and only if a new/better solution has been found
        this.counter = 0;
      }

      return added;
    }

    /**
     * 
     * @param currentSolution
     * @param candidateSolution
     * @return
     */
    private boolean isPairBetterThanCurrent(Pair<Double, T> currentSolution,
        Pair<Double, T> candidateSolution) {
      int cmp = Double.compare(currentSolution.getLeft(), candidateSolution.getLeft());
      if (cmp < 0) {
        return true;
      } else if (cmp > 0) {
        return false;
      }
      assert cmp == 0;

      return isBetterThanCurrent(currentSolution.getRight(), candidateSolution.getRight());
    }

    /**
     * 
     * @return
     */
    private T sampleSolution() {
      if (this.numSolutions() == 0) {
        return null;
      }
      this.counter++;
      return Randomness.choice(this.solutions).getRight();
    }

    /**
     * DESC sort, i.e., from the pair with the highest h to the pair with the lowest h
     */
    private void sortPairSolutions() {
      this.solutions.sort(new Comparator<Pair<Double, T>>() {
        @Override
        public int compare(Pair<Double, T> solution0, Pair<Double, T> solution1) {
          if (solution0.getLeft() < solution1.getLeft()) {
            return 1;
          } else if (solution0.getLeft() > solution1.getLeft()) {
            return -1;
          }
          return 0;
        }
      });
    }

    /**
     * 
     * @return
     */
    private int numSolutions() {
      return this.solutions.size();
    }

    /**
     * 
     * @return
     */
    private T getBestSolutionIfAny() {
      if (this.numSolutions() == 0 || !this.isCovered()) {
        return null;
      }
      return this.solutions.get(0).getRight();
    }

    /**
     * 
     * @param newPopulationSize
     */
    private void shrinkPopulation(int newPopulationSize) {
      assert newPopulationSize > 0;

      if (this.isCovered()) {
        return;
      }

      this.capacity = newPopulationSize;

      if (this.numSolutions() < newPopulationSize) {
        // no need to shrink it
        return;
      }

      List<Pair<Double, T>> shrinkSolutions = new ArrayList<Pair<Double, T>>(newPopulationSize);
      for (int i = 0; i < newPopulationSize; i++) {
        shrinkSolutions.add(this.solutions.get(i));
      }
      this.solutions.clear();
      this.solutions.addAll(shrinkSolutions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
      return 31 * counter + capacity + this.solutions.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }

      Population p = (Population) obj;
      if (this.counter != p.counter) {
        return false;
      }
      if (this.capacity != p.capacity) {
        return false;
      }
      if (this.solutions.size() != p.solutions.size()) {
        return false;
      }

      return this.solutions.equals(p.solutions);
    }
  }
}
