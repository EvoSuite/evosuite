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
/**
 *
 */
package org.evosuite.regression;

import java.util.HashMap;
import java.util.Map;

import org.evosuite.TestGenerationContext;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.testcase.*;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * @author Gordon Fraser
 */
public class RegressionTestChromosome extends AbstractTestChromosome<RegressionTestChromosome> {

  private static final long serialVersionUID = -6345178117840330196L;

  private TestChromosome theTest;

  private TestChromosome theSameTestForTheOtherClassLoader;


  private transient ClassLoader theClassLoader = null;

  Map<String, Map<Integer, String>> diversityMap = new HashMap<>();

  //public int assertionCount = 0;
  //public int exAssertionCount = 0;

  public boolean exCommentsAdded = false;

  @Override
  public RegressionTestChromosome self() {
    return this;
  }

  public RegressionTestChromosome() {
    // TODO Auto-generated constructor stub
    theClassLoader = TestGenerationContext.getInstance().getRegressionClassLoaderForSUT();
  }


  /* (non-Javadoc)
   * @see org.evosuite.testcase.ExecutableChromosome#copyCachedResults(org.evosuite.testcase.ExecutableChromosome)
   */
  @Override
  public void copyCachedResults(RegressionTestChromosome other) {
    theTest.copyCachedResults(other.theTest);
    theSameTestForTheOtherClassLoader
        .copyCachedResults(other.theSameTestForTheOtherClassLoader);

  }

  /* (non-Javadoc)
   * @see org.evosuite.testcase.ExecutableChromosome#executeForFitnessFunction(org.evosuite.testsuite.TestSuiteFitnessFunction)
   */
  @Override
  public ExecutionResult executeForFitnessFunction(
      TestSuiteFitnessFunction testSuiteFitnessFunction) {
    // TODO Hmmmm...
    //assert false: "execute for fitness function";
    return null;
  }

  /* (non-Javadoc)
   * @see org.evosuite.ga.Chromosome#clone()
   */
  @Override
  public RegressionTestChromosome clone() {
    RegressionTestChromosome copy = new RegressionTestChromosome();
    copy.theClassLoader = TestGenerationContext.getInstance()
        .getRegressionClassLoaderForSUT(); // I don't think this should be a member of this class to be honest!
    copy.theTest = theTest.clone();
    copy.theSameTestForTheOtherClassLoader = (TestChromosome) theSameTestForTheOtherClassLoader
        .clone();
    copy.setFitnessValues(getFitnessValues());
    copy.diversityMap.putAll(diversityMap);
    copy.exCommentsAdded = exCommentsAdded;
    return copy;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((theClassLoader == null) ? 0 : theClassLoader.hashCode());
    result = prime * result + ((theTest == null) ? 0 : theTest.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    RegressionTestChromosome other = (RegressionTestChromosome) obj;
    if (theClassLoader == null) {
      if (other.theClassLoader != null) {
        return false;
      }
    } else if (!theClassLoader.equals(other.theClassLoader)) {
      return false;
    }
    if (theTest == null) {
      if (other.theTest != null) {
        return false;
      }
    } else if (!theTest.equals(other.theTest)) {
      return false;
    }
    return true;
  }

  /* (non-Javadoc)
   * @see org.evosuite.ga.Chromosome#compareSecondaryObjective(org.evosuite.ga.Chromosome)
   */
  @Override
  public int compareSecondaryObjective(RegressionTestChromosome o) {
    return theTest.compareSecondaryObjective(o.theTest);
  }

  /* (non-Javadoc)
   * @see org.evosuite.ga.Chromosome#mutate()
   */
  @Override
  public void mutate() {
    theTest.mutate();
    if (theTest.isChanged()) {
      this.increaseNumberOfMutations();
      try {
        updateClassloader();
      } catch (NoClassDefFoundError e) {
        String classname = e.getMessage();
        if (classname != null) {
          // TODO: blacklist class
        }
        TestCase t = new DefaultTestCase();
        theTest.setTestCase(t);
        updateClassloader();
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }
  }

  /* (non-Javadoc)
   * @see org.evosuite.ga.Chromosome#crossOver(org.evosuite.ga.Chromosome, int, int)
   */
  @Override
  public void crossOver(RegressionTestChromosome other, int position1, int position2)
      throws ConstructionFailedException {
    theTest.crossOver(other.theTest, position1, position2);
    updateClassloader();
  }

  /**
   *
   */
  protected void updateClassloader() {
    if (theTest.isChanged()) {
      theSameTestForTheOtherClassLoader = theTest.clone();
      ((DefaultTestCase) theSameTestForTheOtherClassLoader.getTestCase())
          .changeClassLoader(TestGenerationContext.getInstance().getRegressionClassLoaderForSUT());
    }
  }

  /* (non-Javadoc)
   * @see org.evosuite.ga.Chromosome#localSearch(org.evosuite.ga.LocalSearchObjective)
   */
  @Override
  public<F extends FitnessFunction<F,RegressionTestChromosome>> boolean localSearch(LocalSearchObjective<RegressionTestChromosome, F> objective) {
      // FIXME voglseb: Parameter needs to be a LocalSearchObjective for TestSuiteChromosome, but is a
      //                LocalSearchObjective for RegressionTestChromosome
    boolean result = theTest.localSearch(objective);
    updateClassloader();
    return result;
  }

  /* (non-Javadoc)
   * @see org.evosuite.ga.Chromosome#size()
   */
  @Override
  public int size() {
    return theTest.size();
  }

  /**
   * @param theTest the theTest to set
   */
  public void setTest(TestChromosome theTest) {
    this.theTest = theTest;
    updateClassloader();
  }

  /**
   * @return the theTest
   */
  public TestChromosome getTheTest() {
    return theTest;
  }

  /**
   * @return the theSameTestForTheOtherClassLoader
   */
  public TestChromosome getTheSameTestForTheOtherClassLoader() {
    return theSameTestForTheOtherClassLoader;
  }

  public void setLastExecutionResult(ExecutionResult lastExecutionResult) {
    theTest.setLastExecutionResult(lastExecutionResult);
  }

  /**
   * <p>Setter for the field <code>lastRegressionExecutionResult</code>.</p>
   *
   * @param lastExecutionResult a {@link org.evosuite.testcase.execution.ExecutionResult} object.
   */
  public void setLastRegressionExecutionResult(ExecutionResult lastExecutionResult) {
    theSameTestForTheOtherClassLoader.setLastExecutionResult(lastExecutionResult);
  }

  @Override
  public ExecutionResult getLastExecutionResult() {
    return theTest.getLastExecutionResult();
  }

  public ExecutionResult getLastRegressionExecutionResult() {
    return theSameTestForTheOtherClassLoader.getLastExecutionResult();
  }

}
