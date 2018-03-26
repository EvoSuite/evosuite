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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.runtime.util.AtMostOnceLogger;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coverage Archive.
 * 
 * @author José Campos
 */
public class CoverageArchive<F extends TestFitnessFunction, T extends TestChromosome>
    extends Archive<F, T> {

  private static final long serialVersionUID = -4046845573050661961L;

  private static final Logger logger = LoggerFactory.getLogger(CoverageArchive.class);

  /**
   * Map used to store all covered targets (keys of the map) and the corresponding covering
   * solutions (values of the map)
   */
  private final Map<F, T> covered = new LinkedHashMap<F, T>();

  /**
   * Set used to store all targets that have not been covered yet
   */
  private final Set<F> uncovered = new LinkedHashSet<F>();

  public static final CoverageArchive<TestFitnessFunction, TestChromosome> instance =
      new CoverageArchive<TestFitnessFunction, TestChromosome>();

  /**
   * {@inheritDoc}
   */
  @Override
  public void addTarget(F target) {
    assert target != null;

    if (!this.uncovered.contains(target)) {
      logger.debug("Registering new target '" + target + "'");
      this.uncovered.add(target);
    }

    this.registerNonCoveredTargetOfAMethod(target);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void updateArchive(F target, T solution, double fitnessValue) {
    assert target != null;
    assert this.covered.containsKey(target) || this.uncovered.contains(target) : "Unknown goal: "+target;

    if (fitnessValue > 0.0) {
      // as this type of archive only cares about covered targets, it ignores all
      // targets with a fitness value greater than 0.0
      return;
    }

    boolean isNewCoveredTarget = false;
    boolean isNewSolutionBetterThanCurrent = false;

    T currentSolution = this.covered.get(target);

    if (currentSolution == null) {
      logger.debug("Solution for non-covered target '" + target + "'");
      isNewCoveredTarget = true;
    } else {
      isNewSolutionBetterThanCurrent = this.isBetterThanCurrent(currentSolution, solution);
    }

    if (isNewCoveredTarget || isNewSolutionBetterThanCurrent) {
      // update the archive if a new target has been covered, or if solution covers already existing
      // covered targets but it has been considered a better solution
      this.addToArchive(target, solution);
    }
  }

  private void addToArchive(F target, T solution) {
    this.uncovered.remove(target);
    this.covered.put(target, solution);
    this.removeNonCoveredTargetOfAMethod(target);
    this.hasBeenUpdated = true;

    ExecutionResult result = solution.getLastExecutionResult();
    if (result != null && (result.hasTimeout() || result.hasTestException())) {
      AtMostOnceLogger.warn(logger,
          "A solution with a timeout/exception result has been added to the archive. The covered goal was "
              + target.toString());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isArchiveEmpty() {
    return this.covered.isEmpty();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfTargets() {
    return this.covered.keySet().size() + this.uncovered.size();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfCoveredTargets() {
    return this.covered.size();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<F> getCoveredTargets() {
    return this.covered.keySet();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfUncoveredTargets() {
    return this.uncovered.size();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<F> getUncoveredTargets() {
    return this.uncovered;
  }

  private Set<F> getTargets() {
    Set<F> targets = new LinkedHashSet<F>();
    targets.addAll(this.getCoveredTargets());
    targets.addAll(this.getUncoveredTargets());
    return targets;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasTarget(F target) {
    assert target != null;
    return this.covered.containsKey(target) || this.uncovered.contains(target);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfSolutions() {
    return this.covered.size();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<T> getSolutions() {
    return new LinkedHashSet<T>(this.covered.values());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public T getSolution() {
    return this.getRandomSolution();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public T getSolution(F target) {
    assert target != null;
    assert this.covered.containsKey(target);
    return this.covered.get(target);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasSolution(F target) {
    assert target != null;
    return this.covered.containsKey(target);
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  @Override
  public T getRandomSolution() {
    // TODO this gives higher probability to tests that cover more targets. Maybe it is not the best
    // way, but likely the quickest to compute. A proper way to do it would be to first call
    // 'getSolutions' and only then select one at random.
    T randomChoice = Randomness.choice(this.getSolutions());
    if (randomChoice == null) {
      return null;
    }
    return (T) randomChoice.clone();
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

    // skip solutions that have been modified as those might not have been evaluated yet, or have
    // timeout or throw some exception and therefore they may slow down future analysis on the final
    // test suite
    mergedSolution.getTestChromosomes()
        .removeIf(t -> t.isChanged()
            || (t.getLastExecutionResult() != null && (t.getLastExecutionResult().hasTimeout()
                || t.getLastExecutionResult().hasTestException())));

    // to avoid adding the same solution to 'mergedSolution' suite
    Set<T> solutionsSampledFromArchive = new LinkedHashSet<T>();

    for (F target : this.getTargets()) {
      // has target been covered? to answer it, we perform a local check rather than calling method
      // {@link TestFitnessFunction.isCoveredBy} as it may perform a fitness evaluation to access
      // whether that 'target' is covered or not (and therefore, it could be more expensive)
      boolean isGoalCovered = false;
      for (TestChromosome test : mergedSolution.getTestChromosomes()) {
        if (test.getTestCase().isGoalCovered(target)) {
          isGoalCovered = true;
          break;
        }
      }

      if (!isGoalCovered) {
        T chromosome = this.covered.get(target);

        // is there any solution in the archive that covers it, and has that solution not been
        // considered yet?
        if (chromosome != null && !solutionsSampledFromArchive.contains(chromosome)) {
          solutionsSampledFromArchive.add(chromosome);
          mergedSolution.addTest(chromosome);
        }
      }
    }

    // re-evaluate merged solution
    for (FitnessFunction fitnessFunction : solution.getFitnessValues().keySet()) {
      fitnessFunction.getFitness(mergedSolution);
    }

    // re-active it
    Properties.TEST_ARCHIVE = true;

    return mergedSolution;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void shrinkSolutions(int size) {
    // NO-OP
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
    this.covered.clear();
    this.uncovered.clear();
  }
}
