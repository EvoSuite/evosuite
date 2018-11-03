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
package org.evosuite.regression;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.evosuite.Properties;
import org.evosuite.assertion.Assertion;
import org.evosuite.ga.Chromosome;
import org.evosuite.junit.JUnitAnalyzer;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/*
 * Assertion generator for regression testing.
 * 
 * [Experimental]
 */
public class RegressionAssertionCounter {

  protected static final Logger logger = LoggerFactory.getLogger(RegressionAssertionCounter.class);

  // Map from a test case statements' hashcode to assertions for that test
  private static Map<Integer, List<String>> assertionComments = new HashMap<>();

  public static int getNumAssertions(RegressionTestChromosome indvidiual) {
    RegressionTestSuiteChromosome chromosome = new RegressionTestSuiteChromosome();
    chromosome.addTest(indvidiual);
    return getNumAssertions(chromosome);
  }

  /*
   * Gets and removes the number of assertions for the individual
   */
  public static int getNumAssertions(RegressionTestSuiteChromosome individual) {
    assertionComments.clear();
    int numAssertions = getNumAssertions(individual, true);

    if (numAssertions > 0) {
      logger.debug("num assertions bigger than 0");

      List<TestCase> testCases = new ArrayList<>();
      testCases.addAll(individual.getTests());

      logger.debug("tests are copied");

      // reset num assertions
      numAssertions = 0;

      JUnitAnalyzer.removeTestsThatDoNotCompile(testCases);
      logger.debug("... removeTestsThatDoNotCompile()");

      int numUnstable = JUnitAnalyzer.handleTestsThatAreUnstable(testCases);
      logger.debug("... handleTestsThatAreUnstable() = {}", numUnstable);

      // after removing non compiling & unstable tests, do we still have tests left?
      if (testCases.size() > 0) {
        logger
            .debug("{} out of {} tests remaining!", testCases.size(), individual.getTests().size());

        numAssertions = getNumStableAssertions(testCases);

        logger.debug("Keeping {} assertions.", numAssertions);

      } else {
        logger.debug("ignored assertions. tests were removed.");
      }
    }

    return numAssertions;
  }

  private static int getNumStableAssertions(List<TestCase> testCases) {
    RegressionTestSuiteChromosome clone = new RegressionTestSuiteChromosome();

    for (TestCase t : testCases) {
      // create a test suite clone of stable tests, and get the number of assertions again
      if (t.isUnstable()) {
        logger.debug("skipping unstable test...");
        continue;
      }
      RegressionTestChromosome rtc = new RegressionTestChromosome();
      TestChromosome tc = new TestChromosome();
      tc.setTestCase(t);
      rtc.setTest(tc);
      clone.addTest(rtc);
    }

    logger.debug("getting new num assertions ...");

    Map<Integer, List<String>> oldAssertionComments = new HashMap<>(assertionComments);
    assertionComments.clear();

    int numAssertions = getNumAssertions(clone, false);

    // for each test case, check if we had a different number of assertions before
    for (Entry<Integer, List<String>> entry : assertionComments.entrySet()) {
      List<String> newAssertions = entry.getValue();
      List<String> oldAssertions = oldAssertionComments.get(entry.getKey());

      if (oldAssertions == null || !newAssertions.equals(oldAssertions)) {
        numAssertions -= newAssertions.size();
      }
    }
    // if for some weird reason we remove more assertions than needed...
    if (numAssertions < 0) {
      numAssertions = 0;
      logger.error("We removed more assertions than expected");
    }
    return numAssertions;
  }

  public static int getNumAssertions(RegressionTestSuiteChromosome individual,
      Boolean removeAssertions) {
    return getNumAssertions(individual, removeAssertions, false);

  }

  public static int getNumAssertions(RegressionTestSuiteChromosome individual,
      Boolean removeAssertions, Boolean noExecution) {

    RegressionAssertionGenerator rgen = new RegressionAssertionGenerator();

    //(Hack) temporarily changing timeout to allow the assertions to run
    int oldTimeout = Properties.TIMEOUT;
    Properties.TIMEOUT *= 2;
    int totalCount = 0;

    logger.debug("Running assertion generator...");

    for (TestChromosome regressionTest : individual.getTestChromosomes()) {
      RegressionTestChromosome rtc = (RegressionTestChromosome) regressionTest;

      totalCount += checkForAssertions(removeAssertions, noExecution, rgen, rtc);
    }

    Properties.TIMEOUT = oldTimeout;
    if (totalCount > 0) {
      logger.warn("Assertions generated for the individual: " + totalCount);
    }

    return totalCount;
  }

  private static int checkForAssertions(Boolean removeAssertions, Boolean noExecution,
      RegressionAssertionGenerator assertionGenerator, RegressionTestChromosome regressionTest) {
    int totalCount = 0;

    if (!noExecution) {

      ExecutionResult result1 = assertionGenerator
          .runTest(regressionTest.getTheTest().getTestCase());
      ExecutionResult result2 = assertionGenerator
          .runTest(regressionTest.getTheSameTestForTheOtherClassLoader().getTestCase());

      if (result1.test == null || result2.test == null || result1.hasTimeout() || result2
          .hasTimeout()) {

        logger.warn("=============================== HAD TIMEOUT ===============================");
      } else {

        int exceptionDiffs = RegressionExceptionHelper
            .compareExceptionDiffs(result1.getCopyOfExceptionMapping(),
                result2.getCopyOfExceptionMapping());

        if (exceptionDiffs > 0) {
          logger.debug("Had {} different exceptions! ({})", exceptionDiffs, totalCount);
        }

        totalCount += exceptionDiffs;

        for (Class<?> observerClass : RegressionAssertionGenerator.observerClasses) {
          if (result1.getTrace(observerClass) != null) {
            result1.getTrace(observerClass).getAssertions(regressionTest.getTheTest().getTestCase(),
                result2.getTrace(observerClass));
          }
        }
      }
    }
    int assertionCount = regressionTest.getTheTest().getTestCase().getAssertions().size();
    totalCount += assertionCount;

    // Store assertion comments for later flakiness check
    if (assertionCount > 0) {
      List<Assertion> assertions = regressionTest.getTheTest().getTestCase().getAssertions();
      List<String> assertionComments = new ArrayList<>();
      for (Assertion assertion : assertions) {
        logger.warn("+++++ Assertion: {} {}", assertion.getCode(), assertion.getComment());
        assertionComments.add(assertion.getComment());
      }
      RegressionAssertionCounter.assertionComments
          .put(regressionTest.getTheTest().getTestCase().toCode().hashCode(), assertionComments);

      if (assertions.size() == 0) {
        logger.warn("=========> NO ASSERTIONS!!!");
      } else {
        logger.warn("Assertions ^^^^^^^^^");
      }
    }

    if (removeAssertions) {
      regressionTest.getTheTest().getTestCase().removeAssertions();
    }
    return totalCount;
  }

}
