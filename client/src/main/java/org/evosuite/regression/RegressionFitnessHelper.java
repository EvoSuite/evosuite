package org.evosuite.regression;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.evosuite.Properties;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;

/**
 * Created by sina on 08/05/2017.
 */
public class RegressionFitnessHelper {

  /**
   * Diversity is based on the test case statements, and doesn't used with execution results
   */
  static void trackDiversity(RegressionTestChromosome c, TestChromosome testChromosome) {
    Map<String, Map<Integer, String>> testDiversityMap = new HashMap<>();
    for (int i = 0; i < testChromosome.getTestCase().size(); i++) {
      Statement x = testChromosome.getTestCase().getStatement(i);
      if (x instanceof MethodStatement) {
        MethodStatement methodCall = (MethodStatement) x;
        VariableReference callee = methodCall.getCallee();
        if (callee == null) {
          continue;
        }
        int calleePosition = callee.getStPosition();
        String calleeClass = callee.getClassName();
        String methodCallName = methodCall.getMethod().getName();

        Map<Integer, String> calleeMap = testDiversityMap.get(calleeClass);
        if (calleeMap == null) {
          calleeMap = new HashMap<Integer, String>();
        }

        String calledMethods = calleeMap.get(calleePosition);
        if (calledMethods == null) {
          calledMethods = "";
        }

        calledMethods += methodCallName;

        calleeMap.put(calleePosition, calledMethods);
        testDiversityMap.put(calleeClass, calleeMap);
      }
    }

    c.diversityMap = testDiversityMap;
  }

  static boolean useMeasure(RegressionMeasure m) {
    boolean flag = false;
    if (m == Properties.REGRESSION_FITNESS) {
      return true;
    }

    // for more complicated measurements (that combine stuff)
    switch (Properties.REGRESSION_FITNESS) {
      case COVERAGE_OLD:
        if (m == RegressionMeasure.COVERAGE || m == RegressionMeasure.COVERAGE_OLD) {
          return true;
        }
        break;
      case COVERAGE_NEW:
        if (m == RegressionMeasure.COVERAGE || m == RegressionMeasure.COVERAGE_NEW) {
          return true;
        }
        break;
      case STATE_DIFFERENCE:
        if (m == RegressionMeasure.STATE_DIFFERENCE) {
          flag = true;
        }
        break;
      case BRANCH_DISTANCE:
        if (m == RegressionMeasure.BRANCH_DISTANCE && Properties.REGRESSION_BRANCH_DISTANCE) {
          flag = true;
        }
        break;
      case COVERAGE:
        if (m == RegressionMeasure.COVERAGE || m == RegressionMeasure.COVERAGE_OLD
            || m == RegressionMeasure.COVERAGE_NEW) {
          flag = true;
        }
        break;
      case ALL_MEASURES:
      default:
        if (m == RegressionMeasure.COVERAGE || m == RegressionMeasure.STATE_DIFFERENCE
            || (m == RegressionMeasure.BRANCH_DISTANCE && Properties.REGRESSION_BRANCH_DISTANCE)
            || m == RegressionMeasure.COVERAGE_OLD || m == RegressionMeasure.COVERAGE_NEW) {
          flag = true;
        }
        break;

    }
    return flag;
  }

  /*
     * Longest Repeated Substring. Uses suffix sorting, but not very efficient
     *
     * Partially based on http://introcs.cs.princeton.edu/java/42sort/LRS.java.html
     */
  private static class LRS {

    // return the longest common prefix of s and t
    public String lcp(String s, String t) {
      int n = Math.min(s.length(), t.length());
      for (int i = 0; i < n; i++) {
        if (s.charAt(i) != t.charAt(i)) {
          return s.substring(0, i);
        }
      }
      return s.substring(0, n);
    }


    // return the longest repeated string in s
    public String lrs(String s) {

      // form the N suffixes
      int N = s.length();
      String[] suffixes = new String[N];
      for (int i = 0; i < N; i++) {
        suffixes[i] = s.substring(i, N);
      }

      // sort them
      Arrays.sort(suffixes);

      // find longest repeated substring by comparing adjacent sorted suffixes
      String lrs = "";
      for (int i = 0; i < N - 1; i++) {
        String x = lcp(suffixes[i], suffixes[i + 1]);
        if (x.length() > lrs.length()) {
          lrs = x;
          // SINA: Uncommenting the "optimization" `break` below will cause the function to return as soon as a substring is found
          // break;
        }
      }
      return lrs;
    }
  }
}
