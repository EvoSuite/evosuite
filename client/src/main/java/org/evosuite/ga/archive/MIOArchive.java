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
package org.evosuite.ga.archive;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.evosuite.Properties;
import org.evosuite.testcase.TestCase;
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
public class MIOArchive<F extends TestFitnessFunction, T extends TestCase> extends Archive<F, T> {

  private static final long serialVersionUID = -6100903230303784634L;

  private static final Logger logger = LoggerFactory.getLogger(MIOArchive.class);

  /**
   * Map used to store all targets (keys of the map) and the corresponding covering solutions
   * (values of the map)
   **/
  protected final Map<F, Population> archive = new LinkedHashMap<F, Population>();

  public static final MIOArchive<TestFitnessFunction, TestCase> instance =
      new MIOArchive<TestFitnessFunction, TestCase>();

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
  public void updateArchive(F target, ExecutionResult executionResult, double fitnessValue) {
    // TODO
    assert target != null;
    assert this.archive.containsKey(target);

    // TODO

    ExecutionResult executionResultClone = executionResult.clone();
    T solutionClone = (T) executionResultClone.test.clone(); // in case executionResult.clone() has
                                                             // not cloned the test
    executionResultClone.setTest(solutionClone);

    this.archive.get(target).addSolution(1.0 - fitnessValue, solutionClone); // TODO should this return a boolean?!

    this.removeNonCoveredTargetOfAMethod(target);

    // TODO what about collateral coverage, i.e.,
    // this.handleCollateralCoverage(executionResultClone, solutionClone); ?
    // maybe only if fitnessValue = 0.0 -> covered ?
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isBetterThanCurrent(T currentSolution, T candidateSolution) {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void handleCollateralCoverage(ExecutionResult executionResult, T solution) {
    // TODO Auto-generated method stub
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isArchiveEmpty() {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfTargets() {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfCoveredTargets() {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<F> getCoveredTargets() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfSolutions() {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<T> getSolutions() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public T getSolution(F target) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasSolution(F target) {
    assert target != null;

    if (this.archive.containsKey(target)) {
      return this.archive.get(target).covered();
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  @Override
  public T getRandomSolution() {
    T randomSolution = null;

    // Choose one target at random that has not been covered but contains some solutions. In case
    // there is not any non-covered target with at least one solution, either because all targets
    // have been covered or for the non-covered targets there is not any solution yet, then choose
    // one of the covered targets at random. Thereafter, choose one solution randomly from the list
    // of solutions of the chosen target.

    List<F> targetsWithSolutions = this.archive.keySet().stream()
        .filter(target -> this.archive.get(target).numSolutions() > 0).collect(Collectors.toList());

    if (targetsWithSolutions.isEmpty()) {
      // TODO there are no solutions at all! should we randomly generate one?
    }

    List<F> potentialTargets = targetsWithSolutions.stream()
        .filter(target -> this.archive.get(target).covered() == false).collect(Collectors.toList());

    if (potentialTargets.isEmpty()) {
      potentialTargets =
          targetsWithSolutions.stream().filter(target -> this.archive.get(target).covered() == true)
              .collect(Collectors.toList());
    }
    assert !potentialTargets.isEmpty();

    // Instead of choosing a target at random, we choose the one with the lowest counter value.
    // (See Section 3.3 of the paper that describes this archive for more details)

    // F target = Randomness.choice(potentialTargets); randomSolution = (T)
    // this.archive.get(target).sampleSolution().clone();

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
    randomSolution = (T) this.archive.get(potentialTargets.get(0)).sampleSolution().clone();

    return randomSolution;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TestSuiteChromosome mergeArchiveAndSolution(TestSuiteChromosome solution) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    // TODO Auto-generated method stub
    return null;
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
    public Population(int populationSize) {
      this.capacity = populationSize;
      this.solutions = new ArrayList<Pair<Double, T>>(populationSize);
    }

    /**
     * 
     * @return
     */
    public int counter() {
      return this.counter;
    }

    /**
     * 
     * @return
     */
    public boolean covered() {
      return this.solutions.size() == 1 && this.capacity == 1
          && this.solutions.get(0).getLeft() == 1.0;
    }

    /**
     * 
     * @param h [0,1] value, where 1 means that the target is covered, and whereas 0 is the worst
     *        possible heuristics value
     * @param t
     */
    public void addSolution(Double h, T t) {
      assert h >= 0.0 && h <= 1.0;

      if (h == 0.0) {
        // from the paper that describes this type of archive: "if h=0, the test is not added
        // regardless of the following conditions"
        return;
      }

      if (h < 1.0 && this.covered()) {
        // candidate solution T does not cover the already fully covered target, therefore there is
        // no way it could be any better
        return;
      }

      Pair<Double, T> candidateSolution = new ImmutablePair<Double, T>(h, t);

      boolean added = false;

      // does the candidate solution fully cover the target?
      if (h == 1.0) {
        // yes. has the target been fully covered by a previous solution?
        if (this.covered()) {
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
          added = true;
          // TODO should we check whether candidateSolution is an existing solution?!
          this.solutions.add(candidateSolution);
        } else {
          // no, there is not. so, replace the worst one, if candidate is better.
          this.sortPairSolutions();
          Pair<Double, T> worstSolution = this.solutions.get(this.capacity - 1);

          if (isPairBetterThanCurrent(worstSolution, candidateSolution)) {
            added = true;
            this.solutions.set(this.capacity - 1, candidateSolution);
          }
        }
      }

      // a set of solutions larger that a maximum capacity would be considered illegal
      assert this.solutions.size() == this.capacity;

      if (added) {
        // reset counter if and only if a new/better solution has been found
        this.counter = 0;
      }
    }

    private boolean isPairBetterThanCurrent(Pair<Double, T> currentSolution,
        Pair<Double, T> candidateSolution) {
      // TODO is high better or not?!
      if (currentSolution.getLeft() < candidateSolution.getLeft()) {
        return true;
      } else if (currentSolution.getLeft() > candidateSolution.getLeft()) {
        return false;
      }
      assert currentSolution.getLeft() == candidateSolution.getLeft();

      return isBetterThanCurrent(currentSolution.getRight(), candidateSolution.getRight());
    }

    /**
     * 
     * @return
     */
    public T sampleSolution() {
      assert !this.solutions.isEmpty();

      this.counter++;
      return Randomness.choice(this.solutions).getRight();
    }

    /**
     * DESC sort, i.e., from the pair with the highest h to the pair with the lowest h
     */
    public void sortPairSolutions() {
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
    public int numSolutions() {
      return this.solutions.size();
    }

    /**
     * 
     * @return
     */
    public List<T> getSolutions() {
      List<T> allSolutions = new ArrayList<T>();
      this.solutions.stream().forEach(pair -> allSolutions.add(pair.getRight()));
      return allSolutions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
      return 31 * counter + this.solutions.hashCode();
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
      if (this.solutions.size() != p.solutions.size()) {
        return false;
      }

      return this.solutions.equals(p.solutions) && this.counter == p.counter;
    }
  }
}
