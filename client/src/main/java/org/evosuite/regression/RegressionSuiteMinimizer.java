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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.evosuite.Properties;
import org.evosuite.TimeController;
import org.evosuite.assertion.Assertion;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.assertion.OutputTrace;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.TestCaseMinimizer;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegressionSuiteMinimizer {

  private transient final static Logger logger = LoggerFactory
      .getLogger(RegressionSuiteMinimizer.class);

  private RegressionAssertionGenerator
      regressionAssertionGenerator = new RegressionAssertionGenerator();

  public void minimize(TestSuiteChromosome suite) {
    track(RuntimeVariable.Result_Size, suite.size());
    track(RuntimeVariable.Result_Length, suite.totalLengthOfTestCases());
    track(RuntimeVariable.RSM_OverMinimized, 0);

    logger.warn("Going to minimize test suite. Length: {} ", suite.totalLengthOfTestCases());
    logger.debug("suite: \n{}", suite);

    RegressionTestSuiteChromosome regressionSuite = new RegressionTestSuiteChromosome();
    regressionSuite.addTests(suite.clone().getTestChromosomes());

    // Seems to be broken:
    // removeUnusedVariables(regressionSuite);

    executeSuite(regressionSuite);

    removeDuplicateAssertions(regressionSuite);

    removeDuplicateExceptions(regressionSuite);

    removePassingTests(regressionSuite);

    int testCount = regressionSuite.size();

    minimizeSuite(regressionSuite);

    executeSuite(regressionSuite);

    removePassingTests(regressionSuite);

    sendStats(regressionSuite);

    // Sanity check
    if (regressionSuite.size() == 0 && testCount > 0) {
      track(RuntimeVariable.RSM_OverMinimized, 1);
      logger.error("Test suite over-minimized. Returning non-minimized suite.");
    } else {
      // Adding tests back to the original test suite (if minimization didn't remove all tests)
      suite.clearTests();
      for (TestChromosome t : regressionSuite.getTestChromosomes()) {
        RegressionTestChromosome rtc = (RegressionTestChromosome) t;
        suite.addTest(rtc.getTheTest());
      }
    }

    logger.warn("Minimized Length: {} ", suite.totalLengthOfTestCases());
    logger.debug("suite: \n{}", suite);
  }

  private void sendStats(RegressionTestSuiteChromosome regressionSuite) {
    int assCount = 0;

    assCount = numFailingAssertions(regressionSuite);
    track(RuntimeVariable.Generated_Assertions, assCount);

    track(RuntimeVariable.Minimized_Size, regressionSuite.size());
    track(RuntimeVariable.Minimized_Length, regressionSuite.totalLengthOfTestCases());
  }

  private void executeSuite(RegressionTestSuiteChromosome regressionSuite) {
    for (TestChromosome chromosome : regressionSuite.getTestChromosomes()) {
      RegressionTestChromosome c = (RegressionTestChromosome) chromosome;
      try {
        executeTest(c);
      } catch (Throwable t) {
        logger.error("Test execution failed. See stack trace.");
        t.printStackTrace();
      }
    }
  }

  /**
   * Execute regression test case on both versions
   *
   * @param regressionTest regression test chromosome to be executed on both versions
   */
  private void executeTest(RegressionTestChromosome regressionTest) {
    TestChromosome testChromosome = regressionTest.getTheTest();
    TestChromosome otherChromosome = regressionTest.getTheSameTestForTheOtherClassLoader();

    ExecutionResult result = regressionAssertionGenerator
        .runTest(testChromosome.getTestCase());
    ExecutionResult otherResult = regressionAssertionGenerator
        .runTest(otherChromosome.getTestCase());

    regressionTest.setLastExecutionResult(result);
    regressionTest.setLastRegressionExecutionResult(otherResult);

    testChromosome.setLastExecutionResult(result);
    otherChromosome.setLastExecutionResult(otherResult);
  }

  private void removeDuplicateAssertions(RegressionTestSuiteChromosome suite) {
    Iterator<TestChromosome> it = suite.getTestChromosomes().iterator();
    Map<String, List<String>> uniqueAssertions = new HashMap<String, List<String>>();
    // int i = -1;
    while (it.hasNext()) {
      // i++;
      RegressionTestChromosome test = (RegressionTestChromosome) it.next();
      boolean changed = false;
      boolean hadAssertion = false;
      // keep track of new unique assertions, and if not unique, remove the assertion
      for (Assertion a : test.getTheTest().getTestCase().getAssertions()) {
        String aClass = a.getClass().getSimpleName();
        List<String> aTypes = uniqueAssertions.get(aClass);
        if (aTypes == null) {
          aTypes = new ArrayList<String>();
        }
        String aType = "";
        if (a instanceof InspectorAssertion) {
          InspectorAssertion ia = (InspectorAssertion) a;
          try {
            aType = ia.getInspector().getMethod().getName();
          } catch (NullPointerException e) {
            // technically this should not happen
            Statement s = ia.getStatement();
            if (s instanceof MethodStatement) {
              aType = ((MethodStatement) s).getMethod().getName();
            }
          }
        }
        if (aTypes.contains(aType)) {
          // logger.warn("removing non-unique assertion: {}-{}", aClass, aType);
          changed = true;
          a.getStatement().getPosition();
          test.getTheTest().getTestCase().removeAssertion(a);
          continue;
        }
        aTypes.add(aType);
        uniqueAssertions.put(aClass, aTypes);
        hadAssertion = true;
      }

      if (changed) {
        test.updateClassloader();
      }
    }
    if (uniqueAssertions.size() > 0) {
      logger.warn("unique assertions: {}", uniqueAssertions);
    }
  }

  private void removeDuplicateExceptions(RegressionTestSuiteChromosome suite) {

    Set<String> uniqueExceptions = new HashSet<>();
    Map<String, String> exceptionStatementMapping = new HashMap<>();
    List<TestChromosome> chromosomes = suite.getTestChromosomes();

    for (int i = 0; i < chromosomes.size(); i++) {

      RegressionTestChromosome test = (RegressionTestChromosome) chromosomes.get(i);

      boolean changed = false;
      boolean hadAssertion = test.getTheTest().getTestCase().getAssertions().size() > 0;

      ExecutionResult resultA = test.getLastExecutionResult();
      ExecutionResult resultB = test.getLastRegressionExecutionResult();

      // If for some reason we haven't executed the test suite yet, or previous execution failed
      // re-execute the tests and check
      if (resultA == null || resultB == null) {
        executeTest(test);
        resultA = test.getLastExecutionResult();
        resultB = test.getLastRegressionExecutionResult();
        if (resultA == null || resultB == null) {
          continue;
        }
      }

      Map<Integer, Throwable> exceptionMapA = resultA.getCopyOfExceptionMapping();
      Map<Integer, Throwable> exceptionMapB = resultB.getCopyOfExceptionMapping();

      // logger.warn("Test{} - had exceptions? {} {} - {} {}",i,
      // resultA.noThrownExceptions(), resultB.noThrownExceptions(),
      // exceptionMapA.size(), exceptionMapB.size());
      if (!resultA.noThrownExceptions() || !resultB.noThrownExceptions()) {
        double exDiff = RegressionExceptionHelper
            .compareExceptionDiffs(exceptionMapA, exceptionMapB);
        logger.warn("Test{} - Difference in number of exceptions: {}", i, exDiff);
        if (exDiff > 0) {
          /*
           * Three scenarios:
           * 1. Same exception, different messages
           * 2. Different exception in A
           * 3. Different exception in B
           */
          for (Entry<Integer, Throwable> ex : exceptionMapA.entrySet()) {
            Throwable exA = ex.getValue();
            // unique statement key over all tests (to avoid removing the same exception twice)
            String exKey = i + ":" + ex.getKey();
            // exceptionA signatures
            String exception =
                RegressionExceptionHelper.simpleExceptionName(test, ex.getKey(), exA);
            String exSignatureA =
                RegressionExceptionHelper.getExceptionSignature(exA, Properties.TARGET_CLASS);
            String signaturePair = exSignatureA + ",";

            Throwable exB = exceptionMapB.get(ex.getKey());
            if (exB != null) { // if the same statement in B also throws an exception
              // exceptionB signatures
              String exceptionB = RegressionExceptionHelper
                  .simpleExceptionName(test, ex.getKey(), exB);
              String exSignatureB =
                  RegressionExceptionHelper.getExceptionSignature(exA, Properties.TARGET_CLASS);
              signaturePair += exSignatureB;

              if (exception.equals(exceptionB) || exSignatureA.equals(exSignatureB)) {
                // We will be taking care of removing this exception when checking from A to B
                // so there isn't a need to check again from B to A
                exceptionMapB.remove(ex.getKey());
              }
            }

            logger.warn("Test{}, uniqueExceptions: {}", i, uniqueExceptions);
            logger.warn("checking exception: {} at {}", exception, ex.getKey());

            List<String> signatures = Arrays.asList(exception, signaturePair);
            for (String sig : signatures) {
              // Compare exceptions on the merit of their message or signature
              if (uniqueExceptions.contains(sig)
                  && exceptionStatementMapping.get(sig) != exKey
                  && !hadAssertion) {
                TestChromosome originalTestChromosome = (TestChromosome) test
                    .getTheTest().clone();
                try {
                  TestFactory testFactory = TestFactory.getInstance();
                  testFactory
                      .deleteStatementGracefully(test.getTheTest().getTestCase(), ex.getKey());
                  test.getTheTest().setChanged(true);
                  logger.warn("Removed exceptionA throwing line {}", ex.getKey());
                } catch (ConstructionFailedException e) {
                  test.getTheTest().setChanged(false);
                  test.getTheTest().setTestCase(originalTestChromosome.getTestCase());
                  logger.error("ExceptionA deletion failed");
                  continue;
                }
                changed = true;
                break;
              } else {
                uniqueExceptions.add(sig);
                exceptionStatementMapping.put(sig, exKey);
              }
            }
          }

          // Check from the other way around (B to A)
          for (Entry<Integer, Throwable> ex : exceptionMapB.entrySet()) {
            String exception =
                RegressionExceptionHelper.simpleExceptionName(test, ex.getKey(), ex.getValue());

            String exKey = i + ":" + ex.getKey();

            logger.warn("Test{}, uniqueExceptions: {}", i, uniqueExceptions);
            logger.warn("checking exceptionB: {} at {}", exception, ex.getKey());
            if (uniqueExceptions.contains(exception)
                && exceptionStatementMapping.get(exception) != exKey
                && !hadAssertion
                && test.getTheTest().getTestCase().hasStatement(ex.getKey())) {
              TestChromosome originalTestChromosome = (TestChromosome) test.getTheTest().clone();
              try {
                TestFactory testFactory = TestFactory.getInstance();
                logger.warn("removing statementB: {}",
                    test.getTheTest().getTestCase().getStatement(ex.getKey()));
                testFactory
                    .deleteStatementGracefully(test.getTheTest().getTestCase(), ex.getKey());
                test.getTheTest().setChanged(true);
                logger.warn("removed exceptionB throwing line {}", ex.getKey());
              } catch (ConstructionFailedException e) {
                test.getTheTest().setChanged(false);
                test.getTheTest().setTestCase(originalTestChromosome.getTestCase());
                logger.error("ExceptionB deletion failed");
                continue;
              }
              changed = true;
            } else {
              uniqueExceptions.add(exception);
              exceptionStatementMapping.put(exception, exKey);
            }
          }

        }
      }

      if (changed) {
        test.updateClassloader();
        executeTest(test);
        i--;
      }
    }

    if (uniqueExceptions.size() > 0) {
      logger.warn("unique exceptions: {}", uniqueExceptions);
    }
  }

  private int numFailingAssertions(RegressionTestSuiteChromosome suite) {

    int count = 0;

    for (TestChromosome testChromosome : suite.getTestChromosomes()) {
      RegressionTestChromosome test = (RegressionTestChromosome) testChromosome;

      count += numFailingAssertions(test);
    }
    return count;
  }

  private int numFailingAssertions(RegressionTestChromosome test) {

    int count = 0;
    Set<Assertion> invalidAssertions = new HashSet<>();

    ExecutionResult resultA = test.getLastExecutionResult();
    ExecutionResult resultB = test.getLastRegressionExecutionResult();

    for (Assertion assertion : test.getTheSameTestForTheOtherClassLoader().getTestCase()
        .getAssertions()) {
      for (OutputTrace<?> outputTrace : resultA.getTraces()) {
        if (outputTrace.isDetectedBy(assertion)) {
          logger.error("shouldn't be happening: assertion was failing on original version");
          invalidAssertions.add(assertion);
          break;
        }
      }
      for (OutputTrace<?> outputTrace : resultB.getTraces()) {
        if (outputTrace.isDetectedBy(assertion) && !invalidAssertions.contains(assertion)) {
          count++;
          break;
        }
      }
    }

    if (invalidAssertions.size() != 0) {
      logger.warn("{} invalid assertion(s) to be removed", invalidAssertions);
      for (Assertion assertion : invalidAssertions) {
        test.getTheTest().getTestCase().removeAssertion(assertion);
        test.getTheTest().setChanged(true);
        test.updateClassloader();
      }
    }

    int exDiffCount = 0;

    if (resultA != null && resultB != null) {
      exDiffCount = (int) RegressionExceptionHelper
          .compareExceptionDiffs(resultA.getCopyOfExceptionMapping(),
              resultB.getCopyOfExceptionMapping());
      // logger.warn("exDiffCount: {}: \nmap1:{}\nmap2:{}\n",exDiffCount,resultA.getCopyOfExceptionMapping(),
      // resultB.getCopyOfExceptionMapping());
      count += exDiffCount;
      // logger.warn("adding exception comment for test:\n{}",test.getTheTest());
      // logger.wran("test{}")

      if (exDiffCount > 0 && !test.exCommentsAdded) {
        // logger.warn("Adding Exception Comments for test: \nmap1:{}\nmap2:{}\n",resultA.getCopyOfExceptionMapping(),
        // resultB.getCopyOfExceptionMapping());
        RegressionExceptionHelper.addExceptionAssertionComments(test,
            resultA.getCopyOfExceptionMapping(),
            resultB.getCopyOfExceptionMapping());
        test.exCommentsAdded = true;
      }
    } else {
      logger.error("resultA: {} | resultB: {}", resultA, resultB);
    }
    // logger.warn("{} assertions", count);

    // test.assertionCount = count;
    // test.exAssertionCount = exDiffCount;

    return count;
  }

  private void removePassingTests(RegressionTestSuiteChromosome suite) {
    Iterator<TestChromosome> it = suite.getTestChromosomes().iterator();
    int i = 0;
    while (it.hasNext()) {
      i++;
      RegressionTestChromosome test = (RegressionTestChromosome) it.next();

      if (numFailingAssertions(test) == 0) {
        logger.warn("Removing test {}: no assertions", (i - 1));
        it.remove();
      }
    }
  }

  private void minimizeSuite(RegressionTestSuiteChromosome suite) {
    // logger.warn("minimizeSuite:\n{}", suite);
    Iterator<TestChromosome> it = suite.getTestChromosomes().iterator();
    int testCount = 0;
    while (it.hasNext()) {
      if (isTimeoutReached()) {
        logger.warn("minimization timeout reached. skipping minimization");
        break;
      }
      // logger.warn("##########################   TEST{}   %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%",
      // testCount);
      RegressionTestChromosome test = (RegressionTestChromosome) it.next();

      // iterate over test statements (from end to beginning), and remove them one by one,
      // see if the assertion is still failing after removing the statement
      for (int i = test.getTheTest().size() - 1; i >= 0; i--) {
        if (isTimeoutReached()) {
          logger.warn("minimization timeout reached. skipping minimization");
          break;
        }

        logger.debug("Current size: " + suite.size() + "/" + suite.totalLengthOfTestCases());
        logger.debug(
            "Deleting statement {} " + test.getTheTest().getTestCase().getStatement(i).getCode()
                + " from test", i);
        TestChromosome originalTestChromosome = (TestChromosome) test.getTheTest().clone();

        executeTest(test);
        /*
         * if(test.getLastExecutionResult()==null ||
				 * test.getLastRegressionExecutionResult()==null){
				 * logger.error("test execution result was null"); //continue; }
				 */
        // originalTestChromosome.setLastExecutionResult(test.getLastExecutionResult());

        int preRemovalAssertions = numFailingAssertions(test);

        try {
          TestFactory testFactory = TestFactory.getInstance();
          testFactory.deleteStatementGracefully(test.getTheTest().getTestCase(), i);
          test.getTheTest().setChanged(true);
        } catch (ConstructionFailedException e) {
          test.getTheTest().setChanged(false);
          test.getTheTest().setTestCase(originalTestChromosome.getTestCase());
          logger.error("Deleting failed");
          continue;
        }

        RegressionTestChromosome rtc = new RegressionTestChromosome();
        rtc.setTest((TestChromosome) test.getTheTest().clone());
        // rtc.updateClassloader();

        executeTest(rtc);

        int postRemovalAssertions = numFailingAssertions(rtc);
        // logger.warn("Pre-Removal Assertions: {} | Post-Removal Assertions: {}",
        // preRemovalAssertions, postRemovalAssertions);
        if (postRemovalAssertions == preRemovalAssertions) {
          test.updateClassloader();
          continue; // the change had no effect
        } else {
          // Restore previous state
          logger.debug(
              "Can't remove statement " + originalTestChromosome.getTestCase().getStatement(i)
                  .getCode());
          test.getTheTest().setTestCase(originalTestChromosome.getTestCase());
          test.getTheTest().setLastExecutionResult(originalTestChromosome.getLastExecutionResult());
          test.getTheTest().setChanged(false);
        }
      }

      test.updateClassloader();
      if (test.getTheTest().isChanged()) {
        executeTest(test);
      }
      testCount++;
    }
  }

  /*
   * "borrowed" from TestCaseMinimizer
   */
  private void removeUnusedVariables(RegressionTestSuiteChromosome suite) {
    for (TestChromosome testChromosome : suite.getTestChromosomes()) {
      RegressionTestChromosome test = (RegressionTestChromosome) testChromosome;
      boolean changed = TestCaseMinimizer.removeUnusedVariables(test.getTheTest().getTestCase());
      if (changed) {
        test.updateClassloader();
        executeSuite(suite);
      }
    }
  }

  /*
   * "borrowed" from TestSuiteMinimizer
   */
  private boolean isTimeoutReached() {
    return !TimeController.getInstance().isThereStillTimeInThisPhase();
  }

  /**
   * Helper for tracking output values
   */
  private void track(RuntimeVariable variable, Object value) {
    ClientServices.getInstance().getClientNode().trackOutputVariable(variable, value);
  }

}
