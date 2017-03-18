/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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

  protected static final Logger logger = LoggerFactory
      .getLogger(RegressionAssertionCounter.class);

  private static List<List<String>> assertionComments = new ArrayList<>();

  /*
   * Gets and removes the number of assertions for the individual
   */
  public static int getNumAssertions(Chromosome individual) {
    assertionComments.clear();
    int numAssertions = getNumAssertions(individual, true);
    int oldNumAssertions = numAssertions;

    if (numAssertions > 0) {
      logger.debug("num assertions bigger than 0");
      RegressionTestSuiteChromosome clone = new RegressionTestSuiteChromosome();

      List<TestCase> testCases = new ArrayList<TestCase>();

      if (individual instanceof RegressionTestChromosome) {
        // clone.addTest((RegressionTestChromosome)individual);
        testCases.add(((RegressionTestChromosome) individual).getTheTest().getTestCase());
      } else {
        RegressionTestSuiteChromosome ind = (RegressionTestSuiteChromosome) individual;
        testCases.addAll(ind.getTests());
      }
      logger.debug("tests are copied");
      // List<TestCase> testCases = clone.getTests();
      numAssertions = 0;

      JUnitAnalyzer.removeTestsThatDoNotCompile(testCases);
      logger.debug("... removeTestsThatDoNotCompile()");

      int numUnstable = JUnitAnalyzer.handleTestsThatAreUnstable(testCases);
      logger.debug("... handleTestsThatAreUnstable() = {}", numUnstable);

      if (testCases.size() > 0) {
        logger.debug("{} out of {} tests remaining!", testCases.size(),
            ((RegressionTestSuiteChromosome) individual).getTests().size());
        clone = new RegressionTestSuiteChromosome();

        for (TestCase t : testCases) {
          // logger.warn("adding cloned test ...");
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
        List<List<String>> oldAssertionComments = new ArrayList<>(assertionComments);
        assertionComments.clear();
        numAssertions = getNumAssertions(clone, false);
        if (oldAssertionComments.size() != assertionComments.size()) {
          numAssertions = 0;
          logger.error("Assertion test size mismatch: {} VS {}", oldAssertionComments.size(),
              assertionComments.size());
        } else {
          for (int i = 0; i < oldAssertionComments.size(); i++) {
            List<String> testAssertionCommentsOld = oldAssertionComments.get(i);
            List<String> testAssertionCommentsNew = assertionComments.get(i);

            if (testAssertionCommentsNew.size() != testAssertionCommentsOld.size()) {
              numAssertions = 0;
              logger.error("Assertion comment size mismatch: {} VS {}",
                  testAssertionCommentsNew.size(), testAssertionCommentsOld.size());
              break;
            }

            for (int j = 0; j < testAssertionCommentsOld.size(); j++) {
              if (!testAssertionCommentsOld.get(j).equals(testAssertionCommentsNew.get(j))) {
                numAssertions = 0;
                logger.error("Assertion comment mismatch: [{}] VS [{}]",
                    testAssertionCommentsOld.get(j), testAssertionCommentsNew.get(j));
                break;
              }
            }
          }
        }

        logger.debug("Keeping {} assertions.", numAssertions);

      } else {
        logger.debug("ignored assertions. tests were removed.");
      }

    }

    return numAssertions;

  }

  public static int getNumAssertions(Chromosome individual, Boolean removeAssertions) {
    return getNumAssertions(individual, removeAssertions, false);

  }

  public static int getNumAssertions(Chromosome individual, Boolean removeAssertions,
                                     Boolean noExecution) {
    long startTime = System.nanoTime();
    RegressionAssertionGenerator rgen = new RegressionAssertionGenerator();

    //(Hack) temporarily changing timeout to allow the assertions to run
    int oldTimeout = Properties.TIMEOUT;
    Properties.TIMEOUT *= 2;
    int totalCount = 0;
    RegressionSearchListener.exceptionDiff = 0;

    boolean timedOut = false;

    logger.debug("Running assertion generator...");

    // RegressionTestSuiteChromosome ind = null;

    RegressionSearchListener.previousTestSuite = new ArrayList<TestCase>();
    RegressionSearchListener.previousTestSuite.addAll(RegressionSearchListener.currentTestSuite);
    RegressionSearchListener.currentTestSuite.clear();
    if (individual instanceof RegressionTestChromosome) {
      totalCount += checkForAssertions(removeAssertions, noExecution, rgen,
          (RegressionTestChromosome) individual);
    } else {
      // assert false;
      RegressionTestSuiteChromosome ind = (RegressionTestSuiteChromosome) individual;
      for (TestChromosome regressionTest : ind.getTestChromosomes()) {
        RegressionTestChromosome rtc = (RegressionTestChromosome) regressionTest;

        totalCount += checkForAssertions(removeAssertions, noExecution, rgen, rtc);
      }
    }

    // if(totalCount>0)
    Properties.TIMEOUT = oldTimeout;
    if (totalCount > 0) {
      logger.warn("Assertions generated for the individual: " + totalCount);
    }
    RegressionSearchListener.assertionTime += System.nanoTime() - startTime;
    return totalCount;
  }

  // public static boolean enable_a = false;

  private static int checkForAssertions(Boolean removeAssertions, Boolean noExecution,
                                        RegressionAssertionGenerator rgen,
                                        RegressionTestChromosome regressionTest) {
    long execStartTime = 0;
    long execEndTime = 0;
    int totalCount = 0;

    boolean timedOut;
    if (!noExecution) {
      execStartTime = System.currentTimeMillis();

      ExecutionResult result1 = rgen.runTest(regressionTest.getTheTest().getTestCase());
      // enable_a=true;
      // logger.warn("Fitness is: {}", regressionTest.getFitness());
      // regressionAssertionGenerator = new RegressionAssertionGenerator();
      ExecutionResult result2 = rgen
          .runTest(regressionTest.getTheSameTestForTheOtherClassLoader().getTestCase());
      // enable_a = false;
      execEndTime = System.currentTimeMillis();
      /*
       * if((execEndTime-execStartTime)>1500) assert false;
			 */

      if (result1.test == null || result2.test == null || result1.hasTimeout() || result2
          .hasTimeout()) {

        logger.warn(
            "================================== HAD TIMEOUT ==================================");
        timedOut = true;
        // assert false;
      } else {

        // logger.warn("getting exdiff..");
        double exDiff = RegressionExceptionHelper.compareExceptionDiffs(result1.getCopyOfExceptionMapping(),
            result2.getCopyOfExceptionMapping());

        if (exDiff > 0) {
          logger.debug("Had {} different exceptions! ({})", exDiff, totalCount);
          /*
           * logger.warn("mapping1: {} | mapping 2: {}",
					 * result1.getCopyOfExceptionMapping(),
					 * result2.getCopyOfExceptionMapping());
					 */
        }

        totalCount += exDiff;
        RegressionSearchListener.exceptionDiff += exDiff;

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

    if (assertionCount > 0) {
      List<Assertion> asses = regressionTest.getTheTest().getTestCase().getAssertions();
      List<String> assComments = new ArrayList<>();
      for (Assertion ass : asses) {
        logger.warn("+++++ Assertion: {} {}", ass.getCode(), ass.getComment());
        assComments.add(ass.getComment());
      }
      RegressionAssertionCounter.assertionComments.add(assComments);

      if (asses.size() == 0) {
        logger.warn("=========> NO ASSERTIONS!!!");
      } else {
        logger.warn("Assertions ^^^^^^^^^");
      }
    }

    RegressionSearchListener.currentTestSuite
        .add(regressionTest.getTheTest().getTestCase().clone());

    if (removeAssertions) {
      regressionTest.getTheTest().getTestCase().removeAssertions();
    }
    return totalCount;
  }

}
