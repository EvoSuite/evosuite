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

import org.apache.commons.lang3.StringUtils;
import org.evosuite.PackageInfo;
import org.evosuite.runtime.mock.EvoSuiteMock;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;

import java.lang.reflect.Modifier;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import dk.brics.automaton.RegExp;

public class RegressionExceptionHelper {


  private static final List<Class<?>> INVALID_EXCEPTIONS = Arrays.asList(new Class<?>[]{
      StackOverflowError.class, // Might be thrown at different places
      AssertionError.class}     // Depends whether assertions are enabled or not
  );

  /**
   * Get a simple (and unique looking) exception name (exType or exThrowingMethodCall:exType)
   */
  public static String simpleExceptionName(RegressionTestChromosome test, Integer statementPos,
                                           Throwable ex) {
    if (ex == null) {
      return "";
    }
    String exception = ex.getClass().getSimpleName();
    if (test.getTheTest().getTestCase().hasStatement(statementPos)) {
      Statement exThrowingStatement = test.getTheTest().getTestCase().getStatement(statementPos);
      if (exThrowingStatement instanceof MethodStatement) {
        String exMethodcall = ((MethodStatement) exThrowingStatement).getMethod().getName();
        exception = exMethodcall + ":" + exception;
      }
    }
    return exception;
  }

  /**
   * Get signature based on the root cause from the stack trace
   * (uses: location of the error triggering line from CUT)
   * @param CUT the class to expect the exception to be thrown from
   * @return signature string
   */
  public static String getExceptionSignature(Throwable throwable, String CUT) {
    String signature = throwable.getClass().getSimpleName();

    StackTraceElement[] stackTrace = throwable.getStackTrace();
    for (StackTraceElement el : stackTrace) {
      String elClass = el.getClassName();
      if (!Objects.equals(elClass, CUT)) {
        continue;
      }

      String method = el.getMethodName();
      int line = el.getLineNumber();
      signature += ":" + method + "-" + line;
      break;
    }
    return signature;
  }

  /**
   * Calculate the number of different exceptions, given two sets of exceptions.
   */
  public static int compareExceptionDiffs(Map<Integer, Throwable> originalExceptionMapping,
                                          Map<Integer, Throwable> regressionExceptionMapping) {

    int exDiff = (int) Math
        .abs((originalExceptionMapping.size() - regressionExceptionMapping.size()));


    /*
     * If the number of exceptions is different, clearly the executions are
     * propagating to different results. Otherwise, we need to compare the
     * same assertions to make sure they're actually the same.
     */

    if (exDiff == 0) {
      // For all exceptions thrown original class
      for (Map.Entry<Integer, Throwable> origException : originalExceptionMapping.entrySet()) {
        boolean skip = false;

        // Skip if the exception or the message are null
        // Sometimes the getMesage may call the CUT's exception handler which may crash
        try {
          if (origException.getValue() == null || origException.getValue().getMessage() == null) {
            originalExceptionMapping.remove(origException.getKey());
            skip = true;
          }
        } catch (Throwable t) {
          continue;
        }

        // See if exception throwing classes differed
        try {
          Throwable x = origException.getValue();
          Class<?> ex = getExceptionClassToUse(x);
          String sourceClass = getSourceClassName(x);
          if (sourceClass != null && isValidSource(sourceClass) && isExceptionToAssertValid(ex)) {
            // Get other exception throwing class and compare them
            Throwable otherX = regressionExceptionMapping.get(origException.getKey());
            String otherSourceClass = getSourceClassName(otherX);
            if (!sourceClass.equals(otherSourceClass)) {
              exDiff++;
              //logger.warn("Exception throwing classes differed: {} {}", sourceClass, otherSourceClass);
            }

          }
        } catch (Throwable t) {
          // ignore
        }

        // Skip if the exceptions are not comparable
        try {
          if (regressionExceptionMapping.containsKey(origException.getKey())
              && (regressionExceptionMapping.get(origException.getKey()) == null
              || regressionExceptionMapping.get(origException.getKey()).getMessage() == null)) {
            regressionExceptionMapping.remove(origException.getKey());
            skip = true;
          }
        } catch (Throwable t) {
          continue;
        }

        // If they start differing from @objectID skip this one
        if (!skip && regressionExceptionMapping.get(origException.getKey()) != null) {
          String origExceptionMessage = origException.getValue().getMessage();
          String regExceptionMessage = regressionExceptionMapping.get(origException.getKey())
              .getMessage();
          int diffIndex = StringUtils.indexOfDifference(origExceptionMessage, regExceptionMessage);
          if (diffIndex > 0) {
            if (origExceptionMessage.charAt(diffIndex - 1) == '@') {
              originalExceptionMapping.remove(origException.getKey());
              regressionExceptionMapping.remove(origException.getKey());
              skip = true;
            } else {
              // If @ is in the last 10 characters, it's likely an object pointer comparison issue
              int howFarBack = 10;
              if (diffIndex > howFarBack) {
                String last10 = origExceptionMessage.substring(diffIndex - howFarBack, diffIndex);
                if (last10.contains("@")) {
                  originalExceptionMapping.remove(origException.getKey());
                  regressionExceptionMapping.remove(origException.getKey());
                  skip = true;
                }
              }
            }

          }
        }

        // ignore security manager exceptions
        if (!skip && origException.getValue().getMessage().contains("Security manager blocks")) {
          originalExceptionMapping.remove(origException.getKey());
          regressionExceptionMapping.remove(origException.getKey());
          skip = true;
        }

        if (skip) {
          continue;
        }

        // do the comparison
        if (!regressionExceptionMapping.containsKey(origException.getKey())
            || (!regressionExceptionMapping
            .get(origException.getKey()).getMessage()
            .equals(origException.getValue().getMessage()))) {
          exDiff++;
        }
      }
      // For all exceptions in the regression class.
      // Any bad exceptions were removed from this object earlier
      for (Map.Entry<Integer, Throwable> regException : regressionExceptionMapping.entrySet()) {
        if (!originalExceptionMapping.containsKey(regException.getKey())) {
          exDiff++;
        }
      }
    }

    return exDiff;
  }

  /**
   * Add regression-diff comments for exception messages
   */
  public static void addExceptionAssertionComments(RegressionTestChromosome regressionTest,
                                                   Map<Integer, Throwable> originalExceptionMapping,
                                                   Map<Integer, Throwable> regressionExceptionMapping) {
    for (Map.Entry<Integer, Throwable> original : originalExceptionMapping.entrySet()) {
      int originalStatementPos = original.getKey();
      Throwable originalException = original.getValue();
      if (!regressionExceptionMapping.containsKey(originalStatementPos)) {

        if (testStatementCommentNotContains(regressionTest, originalStatementPos,
            "modified version")) {
          addExceptionDifferenceComment(regressionTest, originalException, originalStatementPos,
              "The modified version did not exhibit this exception", false);
          //FIXME: for some reason we're not adding this comment to the other version!
        }
      } else {
        if (originalException != null && originalException.getMessage() != null) {
          // compare the exception messages
          if (!originalException.getMessage()
              .equals(regressionExceptionMapping.get(originalStatementPos).getMessage())) {
            if (testStatementCommentNotContains(regressionTest, originalStatementPos,
                "EXCEPTION DIFF:")) {
              regressionTest.getTheTest().getTestCase().getStatement(originalStatementPos)
                  .addComment(
                      "EXCEPTION DIFF:\nDifferent Exceptions were thrown:\nOriginal Version:\n    "
                          + originalException.getClass().getName() + " : "
                          + originalException.getMessage() + "\nModified Version:\n    "
                          + regressionExceptionMapping.get(originalStatementPos).getClass()
                          .getName()
                          + " : "
                          + regressionExceptionMapping.get(originalStatementPos).getMessage()
                          + "\n");
            }
          } else {
            // Compare the classes throwing the exception
            Class<?> ex = getExceptionClassToUse(originalException);
            String sourceClass = getSourceClassName(originalException);
            if (sourceClass != null && isValidSource(sourceClass) && isExceptionToAssertValid(ex)
                && regressionExceptionMapping.get(originalStatementPos) != null) {
              Throwable otherX = regressionExceptionMapping.get(originalStatementPos);
              String otherSourceClass = getSourceClassName(otherX);
              if (!sourceClass.equals(otherSourceClass)) {
                if (testStatementCommentNotContains(regressionTest, originalStatementPos,
                    "EXCEPTION DIFF:")) {
                  regressionTest.getTheTest().getTestCase().getStatement(originalStatementPos)
                      .addComment(
                          "EXCEPTION DIFF:\nExceptions thrown by different classes:\nOriginal Version:\n    "
                              + sourceClass + "\nModified Version:\n    "
                              + otherSourceClass + "\n");
                }
              }
            }
          }
        }

        // If both show the same error, pop the error from the
        // regression exception map, to get to a diff.
        regressionExceptionMapping.remove(originalStatementPos);
      }
    }
    for (Map.Entry<Integer, Throwable> regression : regressionExceptionMapping.entrySet()) {
      Throwable regressionException = regression.getValue();
      int regressionStatementPos = regression.getKey();
      if (testStatementCommentNotContains(regressionTest, regressionStatementPos,
          "original version")) {

        addExceptionDifferenceComment(regressionTest, regressionException, regressionStatementPos,
            "The original version did not exhibit this exception", true);
      }
    }
  }

  private static boolean testStatementCommentNotContains(RegressionTestChromosome test,
                                                         int statementPos, String compareComment) {
    return (test.getTheTest().getTestCase().hasStatement(statementPos)
        && !test.getTheTest().getTestCase().getStatement(statementPos)
        .getComment().contains(compareComment));
  }

  /**
   * Add exception difference comment to test case, at given position
   * @param test the test case to add the comment to
   * @param t throwable (Exception) which has occurred
   * @param statementPos the position to add the comment on
   * @param comment the comment string
   * @param addToTestForOtherClassLoader whether to add the same comment on the test on reg. CL
   */
  private static void addExceptionDifferenceComment(RegressionTestChromosome test, Throwable t,
                                                    int statementPos, String comment,
                                                    boolean addToTestForOtherClassLoader) {
    String exceptionDiffComment = "EXCEPTION DIFF:\n" + comment + ":\n    "
        + t.getClass().getName() + " : " + t.getMessage() + "\n\n";

    test.getTheTest().getTestCase().getStatement(statementPos)
        .addComment(exceptionDiffComment);
    if (addToTestForOtherClassLoader) {
      test.getTheSameTestForTheOtherClassLoader().getTestCase().getStatement(statementPos)
          .addComment(exceptionDiffComment);
    }
  }

  /**
   * This part is "temporarily" copied over from TestCodeVisitor.
   * Until they are made statically available to use in this class.
   */
  private static String getSourceClassName(Throwable exception) {
    if (exception.getStackTrace().length == 0) {
      return null;
    }
    return exception.getStackTrace()[0].getClassName();
  }

  private static boolean isValidSource(String sourceClass) {
    return (!sourceClass.startsWith(PackageInfo.getEvoSuitePackage() + ".") ||
        sourceClass.startsWith(PackageInfo.getEvoSuitePackage() + ".runtime.")) &&
        !sourceClass.equals(URLClassLoader.class.getName()) &&
        // Classloaders may differ, e.g. when running with ant
        !sourceClass.startsWith(RegExp.class.getPackage().getName()) &&
        !sourceClass.startsWith("java.lang.System") &&
        !sourceClass.startsWith("java.lang.String") &&
        !sourceClass.startsWith("sun.") &&
        !sourceClass.startsWith("com.sun.") &&
        !sourceClass.startsWith("jdk.internal.");
  }

  private static boolean isExceptionToAssertValid(Class<?> exceptionClass) {
    return !INVALID_EXCEPTIONS.contains(exceptionClass);
  }

  private static Class<?> getExceptionClassToUse(Throwable exception) {
        /*
            we can only catch a public class.
            for "readability" of tests, it shouldn't be a mock one either
          */
    Class<?> ex = exception.getClass();
    while (!Modifier.isPublic(ex.getModifiers()) || EvoSuiteMock.class.isAssignableFrom(ex) ||
        ex.getCanonicalName().startsWith("com.sun.")) {
      ex = ex.getSuperclass();
    }
    return ex;
  }
}
