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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.setup.TestCluster;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.statements.FunctionalMockStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.statements.reflection.PrivateFieldStatement;
import org.evosuite.testcase.statements.reflection.PrivateMethodStatement;
import org.evosuite.utils.generic.GenericAccessibleObject;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericMethod;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Archive.
 * 
 * @author José Campos
 */
public abstract class Archive<F extends TestFitnessFunction, T extends TestChromosome>
    implements Serializable {

  private static final long serialVersionUID = 2604119519478973245L;

  private static final Logger logger = LoggerFactory.getLogger(Archive.class);

  /**
   * Map used to store all targets (values of the map) of each method (here represented by its name,
   * keys of the map)
   */
  protected final Map<String, Set<F>> nonCoveredTargetsOfEachMethod =
      new LinkedHashMap<String, Set<F>>();

  /**
   * Has this archive been updated with new candidate solutions?
   */
  protected boolean hasBeenUpdated = false;

  /**
   * Register a target.
   * 
   * @param target
   */
  public abstract void addTarget(F target);

  /**
   * Register a collection of targets.
   * 
   * @param target
   */
  public void addTargets(Collection<F> targets) {
    for (F target : targets) {
      this.addTarget(target);
    }
  }

  /**
   * Register a non-covered target of a method.
   * 
   * @param target
   */
  protected void registerNonCoveredTargetOfAMethod(F target) {
    String targetMethod = this.getMethodFullName(target);
    if (!this.nonCoveredTargetsOfEachMethod.containsKey(targetMethod)) {
      this.nonCoveredTargetsOfEachMethod.put(targetMethod, new LinkedHashSet<F>());
    }
    this.nonCoveredTargetsOfEachMethod.get(targetMethod).add(target);
  }

  /**
   * Removes a specific covered target from the list of non-covered targets of a method.
   * 
   * @param target
   */
  protected void removeNonCoveredTargetOfAMethod(F target) {
    String targetMethod = this.getMethodFullName(target);
    if (this.nonCoveredTargetsOfEachMethod.containsKey(targetMethod)) {
      if (this.nonCoveredTargetsOfEachMethod.get(targetMethod).contains(target)) {
        // target has been covered, therefore we can remove it from the list of non-covered
        this.nonCoveredTargetsOfEachMethod.get(targetMethod).remove(target);
      }

      if (this.nonCoveredTargetsOfEachMethod.get(targetMethod).isEmpty()) {
        // method is fully covered, therefore we do not need to keep track of it
        this.nonCoveredTargetsOfEachMethod.remove(targetMethod);

        // inform TestCluster that method 'targetMethod' is fully covered
        this.ignoreMethodCall(this.getClassName(target), this.getMethodName(target));
      }
    }
  }

  /**
   * Updates the archive by adding a chromosome solution that covers a target, or by replacing an
   * existing solution if the new one is better.
   * 
   * @param target
   * @param solution
   * @param fitnessValue
   */
  public abstract void updateArchive(F target, T solution, double fitnessValue);

  /**
   * Checks whether a candidate solution is better than an existing one.
   * 
   * @param currentSolution
   * @param candidateSolution
   * @return true if a candidate solution is better than an existing one, false otherwise
   */
  public boolean isBetterThanCurrent(T currentSolution, T candidateSolution) {
    int penaltyCurrentSolution = this.calculatePenalty(currentSolution.getTestCase());
    int penaltyCandidateSolution = this.calculatePenalty(candidateSolution.getTestCase());

    // Check if solutions are using any functional mock or private access. A solution is considered
    // better than any other solution if does not use functional mock / private access at all, or if
    // it uses less of those functionalities.

    if (penaltyCandidateSolution < penaltyCurrentSolution) {
      return true;
    } else if (penaltyCandidateSolution > penaltyCurrentSolution) {
      return false;
    }

    // only look at other properties (e.g., length) if penalty scores are the same
    assert penaltyCandidateSolution == penaltyCurrentSolution;

    // If we try to add a test for a target we've already covered
    // and the new test is shorter, keep the shorter one
    // TODO should not this be based on the SECONDARY_CRITERIA?
    if (candidateSolution.size() < currentSolution.size()) {
      return true;
    }

    return false;
  }

  /**
   * Returns false if there is not any solution in the archive, true otherwise.
   * 
   * @return
   */
  public abstract boolean isArchiveEmpty();

  /**
   * Return the total number of targets (either covered by any solution or not).
   * 
   * @return
   */
  public abstract int getNumberOfTargets();

  /**
   * Returns the total number of targets covered by all solutions in the archive.
   * 
   * @return
   */
  public abstract int getNumberOfCoveredTargets();

  /**
   * Returns the union of all targets covered by all solutions in the archive.
   * 
   * @return
   */
  public abstract Set<F> getCoveredTargets();

  /**
   * Returns the total number of targets that have not been covered by any solution.
   * 
   * @return
   */
  public abstract int getNumberOfUncoveredTargets();

  /**
   * Returns a set of all targets that have not been covered by any solution.
   * 
   * @return
   */
  public abstract Set<F> getUncoveredTargets();

  /**
   * Returns true if the archive contains the specific target, false otherwise
   * 
   * @param target
   * @return
   */
  public abstract boolean hasTarget(F target);

  /**
   * Returns the number of unique solutions in the archive.
   * 
   * @return
   */
  public abstract int getNumberOfSolutions();

  /**
   * Returns the union of all solutions in the archive.
   * 
   * @return
   */
  public abstract Set<T> getSolutions();

  /**
   * Returns a particular solution in the archive. The underline algorithm to select a solution
   * depends on the type of archive.
   * 
   * @return
   */
  public abstract T getSolution();

  /**
   * Returns the solution that covers a particular target.
   * 
   * @param target
   * @return
   */
  public abstract T getSolution(F target);

  /**
   * Returns true if the archive has a solution for the specific target, false otherwise.
   * 
   * 
   * @param target
   * @return
   */
  public abstract boolean hasSolution(F target);

  /**
   * Returns the clone of a solution selected at random.
   * 
   * @return
   */
  public abstract T getRandomSolution();

  /**
   * Creates a solution based on the best solutions in the archive and the parameter solution.
   * 
   * @param solution a {@link org.evosuite.testsuite.TestSuiteChromosome} object.
   * @return a {@link org.evosuite.testsuite.TestSuiteChromosome} object.
   */
  public abstract <C extends Chromosome> C mergeArchiveAndSolution(C solution);

  /**
   * 
   * @param size
   */
  public abstract void shrinkSolutions(int size);

  /**
   * Informs {@link org.evosuite.setup.TestCluster} that a particular method of a particular class
   * has been fully covered, and therefore no need to generate any solution to cover any of its
   * targets.
   * 
   * @param className name of the class which contains the method that has been fully covered and
   *        can be ignored
   * @param methodName name of the method that has been fully covered and can be ignored
   */
  protected void ignoreMethodCall(String className, String methodName) {
    TestCluster cluster = TestCluster.getInstance();
    List<GenericAccessibleObject<?>> calls = cluster.getTestCalls();
    for (GenericAccessibleObject<?> call : calls) {
      if (!call.getDeclaringClass().getName().equals(className)) {
        continue;
      }
      if (call instanceof GenericMethod) {
        GenericMethod genericMethod = (GenericMethod) call;
        if (!methodName.startsWith(genericMethod.getName())) {
          continue;
        }
        String desc = Type.getMethodDescriptor(genericMethod.getMethod());
        if ((genericMethod.getName() + desc).equals(methodName)) {
          logger.info("Removing method " + methodName + " from cluster");
          cluster.removeTestCall(call);
          logger.info("Testcalls left: " + cluster.getNumTestCalls());
        }
      } else if (call instanceof GenericConstructor) {
        GenericConstructor genericConstructor = (GenericConstructor) call;
        if (!methodName.startsWith("<init>")) {
          continue;
        }
        String desc = Type.getConstructorDescriptor(genericConstructor.getConstructor());
        if (("<init>" + desc).equals(methodName)) {
          logger.info("Removing constructor " + methodName + " from cluster");
          cluster.removeTestCall(call);
          logger.info("Testcalls left: " + cluster.getNumTestCalls());
        }
      }
    }
  }

  /**
   * Calculate the penalty of a {@link org.evosuite.testcase.TestCase}. A
   * {@link org.evosuite.testcase.TestCase} is penalised if it has functional mocks, or/and if it
   * accesses private fields/methods of the class under test.
   * 
   * @param a {@link org.evosuite.testcase.TestCase} object.
   * @return number of penalty points
   */
  protected int calculatePenalty(TestCase testCase) {
    int penalty = 0;

    if (hasFunctionalMocks(testCase)) {
      penalty++;
    }
    if (hasFunctionalMocksForGenerableTypes(testCase)) {
      penalty++;
    }
    if (hasPrivateAccess(testCase)) {
      penalty++;
    }

    return penalty;
  }

  private boolean hasFunctionalMocks(TestCase testCase) {
    for (Statement statement : testCase) {
      if (statement instanceof FunctionalMockStatement) {
        return true;
      }
    }
    return false;
  }

  private boolean hasFunctionalMocksForGenerableTypes(TestCase testCase) {
    for (Statement statement : testCase) {
      if (statement instanceof FunctionalMockStatement) {
        FunctionalMockStatement fm = (FunctionalMockStatement) statement;
        Class<?> target = fm.getTargetClass();
        GenericClass gc = new GenericClass(target);
        if (TestCluster.getInstance().hasGenerator(gc)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean hasPrivateAccess(TestCase testCase) {
    for (Statement statement : testCase) {
      if (statement instanceof PrivateFieldStatement
          || statement instanceof PrivateMethodStatement) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the concatenation of the name of the class and the name of the method to which a target
   * belongs.
   * 
   * @param target
   * @return
   */
  protected String getMethodFullName(F target) {
    return this.getClassName(target) + this.getMethodName(target);
  }

  private String getClassName(F target) {
    return target.getTargetClass();
  }

  private String getMethodName(F target) {
    return target.getTargetMethod();
  }

  /**
   * Reports whether a method has or has not been fully covered.
   * 
   * @param methodFullName
   * @return true if a method has been fully covered, false otherwise
   */
  protected boolean isMethodFullyCovered(String methodFullName) {
    if (!this.nonCoveredTargetsOfEachMethod.containsKey(methodFullName)) {
      return true;
    }
    return this.nonCoveredTargetsOfEachMethod.get(methodFullName).isEmpty();
  }

  /**
   * Returns the number of targets of a method remaining to be covered.
   * 
   * @param methodFullName
   * @return
   */
  public int getNumOfRemainingTargets(String methodFullName) {
    if (!this.nonCoveredTargetsOfEachMethod.containsKey(methodFullName)) {
      return 0;
    }
    return this.nonCoveredTargetsOfEachMethod.get(methodFullName).size();
  }

  /**
   * {@inheritDoc}
   */
  public abstract String toString();

  /**
   * Reset any field.
   */
  public void reset() {
    this.nonCoveredTargetsOfEachMethod.clear();
  }

  /**
   * Returns true if the archive has been updated with new instances, false otherwise
   * 
   * @return
   */
  public boolean hasBeenUpdated() {
    return this.hasBeenUpdated;
  }

  /**
   * Sets the status (update or not update) of the archive
   * 
   * @param b
   */
  public void setHasBeenUpdated(boolean b) {
    this.hasBeenUpdated = b;
  }

  /**
   * 
   * @return
   */
  public static final Archive<TestFitnessFunction, TestChromosome> getArchiveInstance() {
    switch (Properties.ARCHIVE_TYPE) {
      case COVERAGE:
      default:
        return CoverageArchive.instance;
      case MIO:
        return MIOArchive.instance;
    }
  }
}
