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
import dk.brics.automaton.RegExp;

public class RegressionExceptionHelper {


  private static List<Class<?>> invalidExceptions = Arrays.asList(new Class<?>[]{
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
   * @param throwable
   * @param CUT the class to expect the exception to be thrown from
   * @return signature string
   */
  public static String getExceptionSignature(Throwable throwable, String CUT) {
    String signature = throwable.getClass().getSimpleName();

    StackTraceElement[] stackTrace = throwable.getStackTrace();
    for (StackTraceElement el : stackTrace) {
      String elClass = el.getClassName();
      if (elClass != CUT) {
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
  public static double compareExceptionDiffs(Map<Integer, Throwable> originalExceptionMapping,
                                             Map<Integer, Throwable> regressionExceptionMapping) {

    double exDiff = Math
        .abs((double) (originalExceptionMapping.size() - regressionExceptionMapping.size()));


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
          if (sourceClass != null && isValidSource(sourceClass) && isExceptionToAssertThrownBy(
              ex)) {
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
    for (Map.Entry<Integer, Throwable> origException : originalExceptionMapping.entrySet()) {
      if (!regressionExceptionMapping.containsKey(origException.getKey())) {

        if (regressionTest.getTheTest().getTestCase().hasStatement(origException.getKey())
            && !regressionTest.getTheTest().getTestCase().getStatement(origException.getKey())
            .getComment()
            .contains("modified version")) {
          regressionTest.getTheTest().getTestCase().getStatement(origException.getKey())
              .addComment(
                  "EXCEPTION DIFF:\nThe modified version did not exhibit this exception:\n    "
                      + origException.getValue().getClass().getName() + " : "
                      + origException.getValue().getMessage() + "\n");
          // regressionTest.getTheSameTestForTheOtherClassLoader().getTestCase().getStatement(origException.getKey()).addComment("EXCEPTION
          // DIFF:\nThe modified version did not exhibit this
          // exception:\n "
          // + origException.getValue().getMessage() + "\n");
        }
      } else {
        if (origException != null && origException.getValue() != null
            && origException.getValue().getMessage() != null) {
          // compare the exception messages
          if (!origException.getValue().getMessage()
              .equals(regressionExceptionMapping.get(origException.getKey()).getMessage())) {
            if (regressionTest.getTheTest().getTestCase().hasStatement(origException.getKey())
                && !regressionTest.getTheTest().getTestCase().getStatement(origException.getKey())
                .getComment().contains("EXCEPTION DIFF:")) {
              regressionTest.getTheTest().getTestCase().getStatement(origException.getKey())
                  .addComment(
                      "EXCEPTION DIFF:\nDifferent Exceptions were thrown:\nOriginal Version:\n    "
                          + origException.getValue().getClass().getName() + " : "
                          + origException.getValue().getMessage() + "\nModified Version:\n    "
                          + regressionExceptionMapping.get(origException.getKey()).getClass()
                          .getName()
                          + " : "
                          + regressionExceptionMapping.get(origException.getKey()).getMessage()
                          + "\n");
            }
          } else {
            // Compare the classes throwing the exception
            Throwable x = origException.getValue();
            Class<?> ex = getExceptionClassToUse(x);
            String sourceClass = getSourceClassName(x);
            if (sourceClass != null && isValidSource(sourceClass) && isExceptionToAssertThrownBy(ex)
                && regressionExceptionMapping.get(origException.getKey()) != null) {
              Throwable otherX = regressionExceptionMapping.get(origException.getKey());
              String otherSourceClass = getSourceClassName(otherX);
              if (!sourceClass.equals(otherSourceClass)) {
                if (regressionTest.getTheTest().getTestCase().hasStatement(origException.getKey())
                    && !regressionTest.getTheTest().getTestCase()
                    .getStatement(origException.getKey()).getComment()
                    .contains("EXCEPTION DIFF:")) {
                  regressionTest.getTheTest().getTestCase().getStatement(origException.getKey())
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
        regressionExceptionMapping.remove(origException.getKey());
      }
    }
    for (Map.Entry<Integer, Throwable> regException : regressionExceptionMapping.entrySet()) {
      if (regressionTest.getTheTest().getTestCase().hasStatement(regException.getKey())
          && !regressionTest.getTheTest().getTestCase().getStatement(regException.getKey())
          .getComment()
          .contains("original version")) {
                                /*
                                 * logger.warn(
				 * "Regression Test with exception \"{}\" was: \n{}\n---------\nException:\n{}"
				 * , regException.getValue().getMessage(),
				 * regressionTest.getTheTest().getTestCase(),
				 * regException.getValue().toString());
				 */
        regressionTest.getTheTest().getTestCase().getStatement(regException.getKey())
            .addComment(
                "EXCEPTION DIFF:\nThe original version did not exhibit this exception:\n    "
                    + regException.getValue().getClass().getName() + " : "
                    + regException.getValue().getMessage() + "\n\n");
        regressionTest.getTheSameTestForTheOtherClassLoader().getTestCase()
            .getStatement(regException.getKey())
            .addComment(
                "EXCEPTION DIFF:\nThe original version did not exhibit this exception:\n    "
                    + regException.getValue().getClass().getName() + " : "
                    + regException.getValue().getMessage() + "\n\n");
      }
    }
  }

  /*
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

  private static boolean isExceptionToAssertThrownBy(Class<?> exceptionClass) {
    return !invalidExceptions.contains(exceptionClass);
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
