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

import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Gordon Fraser
 *
 */
public class RegressionTestSuiteChromosome
        extends AbstractTestSuiteChromosome<RegressionTestSuiteChromosome, RegressionTestChromosome>  {

  private static final long serialVersionUID = 2279207996777829420L;


  @Override
  public RegressionTestSuiteChromosome self() {
    return this;
  }

  public RegressionTestSuiteChromosome() {
    super();
  }

  public RegressionTestSuiteChromosome(ChromosomeFactory<RegressionTestChromosome> testChromosomeFactory) {
      super(testChromosomeFactory);
  }

  protected RegressionTestSuiteChromosome(RegressionTestSuiteChromosome source) {
    super(source);
  }

  @Override
  public void addTest(RegressionTestChromosome test) {
    if (test == null) {
      RegressionTestChromosome rtc = new RegressionTestChromosome();
      try {
        rtc.setTest(null);
      } catch (NoClassDefFoundError e) {
        String classname = e.getMessage();
        if (classname != null) {
          // TODO: blacklist class
        }
        return;
      } catch (Throwable e) {
        return;
      }
      tests.add(rtc);
    } else {
      tests.add(test);
    }
    this.setChanged(true);
  }

  @Override
  public void addTests(Collection<RegressionTestChromosome> tests) {
    for (RegressionTestChromosome test : tests) {
      test.setChanged(true);
      addTest(test);
    }
  }

  @Override
  public RegressionTestChromosome addTest(TestCase testCase) {
    RegressionTestChromosome regressionTestChromosome = new RegressionTestChromosome();
    TestChromosome theTest = new TestChromosome();
    theTest.setTestCase(testCase);
    regressionTestChromosome.setTest(theTest);
    addTest(regressionTestChromosome);
    return regressionTestChromosome;
  }


  @Override
  public void addTestChromosome(TestChromosome testChromosome) {
    RegressionTestChromosome regressionTestChromosome = new RegressionTestChromosome();
    regressionTestChromosome.setTest(testChromosome);
    addTest(regressionTestChromosome);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.evosuite.testsuite.AbstractTestSuiteChromosome#localSearch(org.evosuite
   * .ga.LocalSearchObjective)
   */
  public<F extends FitnessFunction<F,RegressionTestSuiteChromosome>> boolean localSearch(LocalSearchObjective<RegressionTestSuiteChromosome, F> objective) {
    // Ignore for now
    return false;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.evosuite.testsuite.AbstractTestSuiteChromosome#clone()
   */
  @Override
  public RegressionTestSuiteChromosome clone() {
    return new RegressionTestSuiteChromosome(this);
  }

  @Override
  public int compareSecondaryObjective(RegressionTestSuiteChromosome o) {
    return 0;
  }

  public List<TestCase> getTests() {
    return this.tests.stream().map(test -> test.getTheTest().getTestCase()).collect(Collectors.toList());
  }

  public RegressionTestSuiteChromosome getTestSuite() {
    RegressionTestSuiteChromosome suite = new RegressionTestSuiteChromosome();
    tests.stream().map(RegressionTestChromosome::getTheTest).forEach(suite::addTestChromosome);
    return suite;
  }

  public RegressionTestSuiteChromosome getTestSuiteForTheOtherClassLoader() {
    RegressionTestSuiteChromosome suite = new RegressionTestSuiteChromosome();
    tests.stream().map(RegressionTestChromosome::getTheSameTestForTheOtherClassLoader).forEach(suite::addTestChromosome);
    return suite;
  }

  @Override
  public String toString() {
    StringBuilder testSuiteString = new StringBuilder();
    for (RegressionTestChromosome test : tests) {
      testSuiteString.append(test.getTheTest()
              .getTestCase().toCode());
      testSuiteString.append("\n");
    }
    return testSuiteString.toString();
  }

  public List<RegressionTestChromosome> getTestChromosomes() {
    return tests;
  }

}
