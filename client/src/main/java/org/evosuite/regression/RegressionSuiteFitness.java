/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with EvoSuite. If
 * not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.regression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.testcase.execution.MethodCall;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;


public class RegressionSuiteFitness extends TestSuiteFitnessFunction {

  /**
   * 
   */
  private static final long serialVersionUID = -1979463801167353053L;

  private double bestFitness = Double.MAX_VALUE;

  Map<Integer, Integer> branchIdMap = new HashMap<Integer, Integer>();

  private HashMap<Integer, Double> tempBranchDistanceMap;

  public int max_branch_fitness_valueO = 0;
  public int max_branch_fitness_valueR = 0;

  transient RegressionExecutionObserver observer;


  BranchCoverageSuiteFitness bcFitness;
  BranchCoverageSuiteFitness bcFitnessRegression;

  private int numDifferentExceptions;
  private long diffTime;

  private int totalExceptions;

  Map<Integer, Double> branchDistanceMap;
  boolean firstF = false;

  public RegressionSuiteFitness(boolean underTest) {
    branchDistanceMap = new HashMap<Integer, Double>();
  }

  public RegressionSuiteFitness() {
    super();
    logger.warn(
        "initialising regression Suite Fitness... #################################################");
    if (Properties.REGRESSION_ANALYZE) {
      Properties.REGRESSION_FITNESS = RegressionMeasure.COVERAGE;
    }

    // populate a temp branch distance map with initial data for all
    // branches(if they are not covered, 4 will be considered).
    tempBranchDistanceMap = new HashMap<Integer, Double>();
    double max_branch_value = 4.0;

    for (Branch b : BranchPool
        .getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getAllBranches()) {
      tempBranchDistanceMap.put(b.getActualBranchId(), max_branch_value);
    }

    try {
      TestGenerationContext.getInstance().getRegressionClassLoaderForSUT()
          .loadClass(Properties.TARGET_CLASS);
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    bcFitness = new BranchCoverageSuiteFitness();
    max_branch_fitness_valueO = bcFitness.getMaxValue();
    bcFitnessRegression = new BranchCoverageSuiteFitness(
        TestGenerationContext.getInstance().getRegressionClassLoaderForSUT());
    max_branch_fitness_valueR = bcFitnessRegression.getMaxValue();

    observer = new RegressionExecutionObserver();

    ExecutionTracer.enableTraceCalls();

  }

  Map<String, Map<Integer, String>> diversityMap = new HashMap<String, Map<Integer, String>>();

  private int uniqueCalls;

  protected void executeChangedTestsAndUpdateResults(
      AbstractTestSuiteChromosome<? extends ExecutableChromosome> s) {
    observer.clearPools();
    diversityMap.clear();
    RegressionTestSuiteChromosome suite = (RegressionTestSuiteChromosome) s;
    for (TestChromosome chromosome : suite.getTestChromosomes()) {
      RegressionTestChromosome c = (RegressionTestChromosome) chromosome;
      observer.off = false;
      observer.requestNewPools();
      observer.regressionFlag(false);

      TestChromosome testChromosome = c.getTheTest();
      TestChromosome otherChromosome = c.getTheSameTestForTheOtherClassLoader();

      // Only execute test if it hasn't been changed
      if (testChromosome.isChanged() || testChromosome.getLastExecutionResult() == null) {

        // record diversity
        // diversity is based on the test case statements, and doesn't used with execution results
        if (Properties.REGRESSION_DIVERSITY) {
          Map<String, Map<Integer, String>> testDiversityMap =
              new HashMap<String, Map<Integer, String>>();
          for (int i = 0; i < testChromosome.getTestCase().size(); i++) {
            Statement x = testChromosome.getTestCase().getStatement(i);
            if (x instanceof MethodStatement) {
              MethodStatement methodCall = (MethodStatement) x;
              VariableReference callee = methodCall.getCallee();
              if (callee == null)
                continue;
              int calleePosition = callee.getStPosition();
              String calleeClass = callee.getClassName();
              String methodCallName = methodCall.getMethod().getName();

              Map<Integer, String> calleeMap = testDiversityMap.get(calleeClass);
              if (calleeMap == null)
                calleeMap = new HashMap<Integer, String>();

              String calledMethods = calleeMap.get(calleePosition);
              if (calledMethods == null)
                calledMethods = "";

              calledMethods += methodCallName;

              calleeMap.put(calleePosition, calledMethods);
              testDiversityMap.put(calleeClass, calleeMap);
            }
          }

          c.diversityMap = testDiversityMap;
        }

        ExecutionResult result = TestCaseExecutor.runTest(testChromosome.getTestCase());
        // result.objectPool.addAll(observer.currentObjectMapPool);
        observer.regressionFlag(true);

        ExecutionResult otherResult = TestCaseExecutor.runTest(otherChromosome.getTestCase());
        /*
         * otherResult.objectPool .addAll(observer.currentRegressionObjectMapPool);
         */
        observer.regressionFlag(false);
        observer.off = true;

        double objectDistance = getTestObjectDistance(observer.currentObjectMapPool,
            observer.currentRegressionObjectMapPool);

        result.regressionObjectDistance = objectDistance;
        otherResult.regressionObjectDistance = objectDistance;

        if (result != null && otherResult != null) {

          testChromosome.setLastExecutionResult(result);
          testChromosome.setChanged(false);

          otherChromosome.setLastExecutionResult(otherResult);
          otherChromosome.setChanged(false);
        }
      }


      if (Properties.REGRESSION_DIVERSITY) {
        long startTime = System.nanoTime();
        for (Entry<String, Map<Integer, String>> dEntry : c.diversityMap.entrySet()) {
          Map<Integer, String> divInstance = diversityMap.get(dEntry.getKey());
          if (divInstance == null)
            diversityMap.put(dEntry.getKey(), dEntry.getValue());
          else {
            Map<Integer, String> testMethodCalls = dEntry.getValue();
            for (Entry<Integer, String> mc : testMethodCalls.entrySet()) {
              String calls = divInstance.get(mc.getKey());
              if (calls == null || calls.length() < mc.getValue().length())
                calls = mc.getValue();
              divInstance.put(mc.getKey(), calls);
            }
          }
        }
        RegressionSearchListener.diversityCalculationTime += System.nanoTime() - startTime;
      }
    }


  }


  /*
   * (non-Javadoc)
   * 
   * @see org.evosuite.ga.FitnessFunction#getFitness(org.evosuite.ga.Chromosome)
   */
  @Override
  public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> individual) {

    if (useMeasure(RegressionMeasure.STATE_DIFFERENCE)) {
      TestCaseExecutor.getInstance().addObserver(observer);
      observer.clearPools();
    }

    double distance = 0.0;
    double fitness = 0.0;

    // populate branches with a value of 2 (branch not covered yet)
    // branchDistanceMap = new HashMap<Integer, Double>();
    branchDistanceMap = (Map<Integer, Double>) tempBranchDistanceMap.clone();

    numDifferentExceptions = 0;
    totalExceptions = 0;
    diffTime = 0;

    long startTime = System.nanoTime();
    executeChangedTestsAndUpdateResults(individual);
    RegressionSearchListener.testExecutionTime += System.nanoTime() - startTime;

    RegressionTestSuiteChromosome suite = (RegressionTestSuiteChromosome) individual;

    List<Double> objectDistances = new ArrayList<Double>();

    for (TestChromosome regressionTest : suite.getTestChromosomes()) {

      RegressionTestChromosome rtc = (RegressionTestChromosome) regressionTest;

      ExecutionResult result1 = rtc.getTheTest().getLastExecutionResult();

      ExecutionResult result2 = rtc.getTheSameTestForTheOtherClassLoader().getLastExecutionResult();


      // calculating exception difference
      int numExceptionOrig = result1.getNumberOfThrownExceptions();
      int numExceptionReg = result2.getNumberOfThrownExceptions();


      double execTimeDiff = Math.abs((double) (numExceptionOrig - numExceptionReg));
      if (execTimeDiff > 0.003)
        diffTime += execTimeDiff;

      double exDiff = Math.abs((double) (numExceptionOrig - numExceptionReg));

      totalExceptions += numExceptionOrig + numExceptionReg;

      numDifferentExceptions += exDiff;

      startTime = System.nanoTime();

      // branch distance
      if (useMeasure(RegressionMeasure.BRANCH_DISTANCE)) {
        this.getBranchDistance(result1.getTrace().getMethodCalls(),
            result2.getTrace().getMethodCalls());
      }
      RegressionSearchListener.branchDistanceTime += System.nanoTime() - startTime;

      // object distance
      objectDistances.add(result1.regressionObjectDistance);

    }
    firstF = true;



    double objectDfitness = 0;
    if (useMeasure(RegressionMeasure.STATE_DIFFERENCE)) {
      if (!objectDistances.isEmpty())
        distance = Collections.max(objectDistances);
      objectDfitness =
          (1.0 / (1.0 + distance)) * (max_branch_fitness_valueO + max_branch_fitness_valueR);
    }

    startTime = System.nanoTime();
    AbstractTestSuiteChromosome<TestChromosome> testSuiteChromosome = suite.getTestSuite();

    AbstractTestSuiteChromosome<TestChromosome> testRegressionSuiteChromosome = null;
    if (useMeasure(RegressionMeasure.COVERAGE_NEW)) {
      testRegressionSuiteChromosome = suite.getTestSuiteForTheOtherClassLoader();
    }


    double coverage_old = 0, coverage_new = 0;
    if (useMeasure(RegressionMeasure.COVERAGE_OLD)) {
      coverage_old = bcFitness.getFitness(testSuiteChromosome);
    }
    if (useMeasure(RegressionMeasure.COVERAGE_NEW)) {
      coverage_new = bcFitnessRegression.getFitness(testRegressionSuiteChromosome);
    }
    double coverage = coverage_old + coverage_new;

    RegressionSearchListener.coverageTime += System.nanoTime() - startTime;


    double branchDfitness = 0;

    double totalBranchDistanceFitness = 0.0;
    if (useMeasure(RegressionMeasure.BRANCH_DISTANCE)) {
      for (Map.Entry<Integer, Double> br : branchDistanceMap.entrySet()) {

        totalBranchDistanceFitness += br.getValue();
      }

      branchDfitness = totalBranchDistanceFitness;
    }


    switch (Properties.REGRESSION_FITNESS) {
      case COVERAGE_OLD:
        fitness += coverage_old;
        break;
      case COVERAGE_NEW:
        fitness += coverage_new;
        break;
      case BRANCH_DISTANCE:
        fitness += branchDfitness;
        break;
      case STATE_DIFFERENCE:
        fitness += objectDfitness;
        break;
      case COVERAGE:
        fitness += coverage;
        break;
      case ALL_MEASURES:
      default:
        fitness += coverage;
        fitness += branchDfitness;
        fitness += objectDfitness;
        break;
    }

    // * (max_branch_fitness_valueO + max_branch_fitness_valueR);
    double exceptionDistance = (1.0 / (1.0 + numDifferentExceptions));

    fitness += exceptionDistance;

    if (Properties.REGRESSION_DIVERSITY) {
      calculateDiversity();

      double diversityFitness = (1.0 / (1.0 + uniqueCalls));
      fitness += diversityFitness;
    }


    String covered_old = String.format("%.2f", bcFitness.totalCovered * 100);
    String covered_new = String.format("%.2f", bcFitnessRegression.totalCovered * 100);

    suite.diffExceptions = numDifferentExceptions;

    if (RegressionSearchListener.killTheSearch) {
      // updateIndividual(individual, 0);
      // return 0;
    }

    if (Properties.REGRESSION_ANALYZE) {
      if (bcFitness.totalCovered >= 0.5 && bcFitnessRegression.totalCovered >= 0.5) {

        RegressionSearchListener.analysisReport =
            "Coverage: Successful | Orig: " + covered_old + "% | New: " + covered_new + "%";
        // RegressionSearchListener.killTheSearch = true;
      } else {
        RegressionSearchListener.analysisReport =
            "Coverage: Failed | Orig: " + covered_old + "% | New: " + covered_new + "%";
      }
    }



    suite.fitnessData = fitness + "," + testSuiteChromosome.size() + ","
        + testSuiteChromosome.totalLengthOfTestCases() + "," + branchDfitness + "," + objectDfitness
        + "," + coverage + ",numDifferentExceptions," + totalExceptions + "," + covered_old + ","
        + covered_new + "," + MaxStatementsStoppingCondition.getNumExecutedStatements();

    suite.objDistance = objectDfitness;

    /*
     * logger.warn("OBJ distance: " + distance + " - fitness:" + fitness + " - branchDistance:" +
     * totalBranchDistanceFitness + " - coverage:" + coverage + " - ex: " + exceptionDistance +
     * " - tex: " + totalExceptions);
     */
    individual.setCoverage(this, (bcFitness.totalCovered + bcFitnessRegression.totalCovered) / 2.0);
    updateIndividual(this, individual, fitness);

    if (fitness < bestFitness) {
      bestFitness = fitness;


      logger.warn("OBJ distance: " + distance + " - fitness:" + fitness + " - branchDistance:"
          + totalBranchDistanceFitness + " - coverage:" + coverage + " - ex: "
          + numDifferentExceptions + " - tex: " + totalExceptions);
      logger.debug("Timings so far: Test Execution - "
          + (RegressionSearchListener.testExecutionTime + 1) / 1000000 + " | Assertion - "
          + (RegressionSearchListener.assertionTime + 1) / 1000000 + " | Coverage - "
          + (RegressionSearchListener.coverageTime + 1) / 1000000 + " | Obj Distance - "
          + (RegressionSearchListener.ObjectDistanceTime + 1) / 1000000 + " | Branch Distance - "
          + (RegressionSearchListener.branchDistanceTime + 1) / 1000000 + " | Obj Collection - "
          + (RegressionSearchListener.odCollectionTime + 1) / 1000000
          + " | Diversity Calculation - "
          + (RegressionSearchListener.diversityCalculationTime + 1) / 1000000);
      logger.warn("Best Fitness " + fitness + ", number of tests: " + testSuiteChromosome.size()
          + ", total length: " + testSuiteChromosome
              .totalLengthOfTestCases() /*
                                         * + ", max adds: " +maxAdds + ", total adds: " + totalAdds
                                         */);
    }


    return fitness;
  }

  /**
   * Calculate diversity among objects
   * 
   */
  private void calculateDiversity() {
    long divStartTime = System.nanoTime();

    // LRS lrs = new LRS();

    uniqueCalls = 0;
    for (Entry<String, Map<Integer, String>> dEntry : diversityMap.entrySet()) {
      Map<Integer, String> calleeObjects = diversityMap.get(dEntry.getKey());
      for (Entry<Integer, String> mCall : calleeObjects.entrySet()) {
        boolean alreadyPresent = false;
        for (int position : calleeObjects.keySet()) {
          if (position != mCall.getKey()
              && calleeObjects.get(position).contains(mCall.getValue())) {
            alreadyPresent = true;
            break;
          }
        }
        if (!alreadyPresent) {
          //
          // String longestRepetition = lrs.lrs(mCall.getValue());
          // if(longestRepetition.length()==0)
          uniqueCalls++;
        }

      }
    }
    /*
     * if(uniqueCalls>0) System.out.println(uniqueCalls);
     */
    RegressionSearchListener.diversityCalculationTime += System.nanoTime() - divStartTime;
  }

  /*
   * Get the distance of two branches given two method calls
   * 
   * @deprecated This function isn't in use anymore...
   */
  public void getBranchDistance(List<MethodCall> methodCallsOrig, List<MethodCall> methodCallsReg) {
    /*
     * Here's how this method works:
     * 
     * It takes two pointers i and j, and two lists of method calls
     * 
     * Then for each of the equal methods: - it takes two sets of branch traces and two pointers k
     * and l - it then compares the branch distances
     * 
     * The pointers skip forward if one side is not equal to the other (possibly to skip new code
     * that was added)
     */

    for (int i = 0, j = 0; i < methodCallsOrig.size() && j < methodCallsReg.size();) {
      MethodCall mO = methodCallsOrig.get(i);
      MethodCall mR = methodCallsReg.get(j);

      if (mO.methodName.equals(mR.methodName)) {
        // logger.warn("mO is mR: " + mO.methodName);

        List<Integer> branchesO = mO.branchTrace;
        List<Integer> branchesR = mR.branchTrace;

        for (int k = 0, l = 0; k < branchesO.size() && l < branchesR.size();) {
          Integer branchO = branchesO.get(k);
          Integer branchR = branchesR.get(l);

          if (Properties.REGRESSION_DIFFERENT_BRANCHES) {

            logger.error(
                "Regression_differrent_branches has been deprecated and removed from Evosuite. Please disable the property and try again");

            if (branchIdMap.containsKey(branchO)) {
              branchR = branchIdMap.get(branchO);
            } else {

              if ((branchO == branchR))
                k++;
              l++;
              continue;
            }
          }


          if (branchO == branchR) {

            double trueDisO = normalize(mO.trueDistanceTrace.get(k));
            double trueDisR = normalize(mR.trueDistanceTrace.get(l));

            double falseDisO = normalize(mO.falseDistanceTrace.get(k));
            double falseDisR = normalize(mR.falseDistanceTrace.get(l));

            double tempBranchDistance =
                2.0 * (1 - (Math.abs(trueDisO - trueDisR) + Math.abs(falseDisO - falseDisR)));
            tempBranchDistance += (Math.abs(trueDisR) + Math.abs(falseDisR)) / 2.0;

            tempBranchDistance += (Math.abs(trueDisO) + Math.abs(falseDisO)) / 2.0;

            if ((trueDisO == 0 && falseDisR == 0) || (falseDisO == 0 && trueDisR == 0))
              tempBranchDistance = 0;

            if (!branchDistanceMap.containsKey(branchO)
                || branchDistanceMap.get(branchO) > tempBranchDistance)
              branchDistanceMap.put(branchO, tempBranchDistance);

            k++;
            l++;
            continue;
          } else {
            break;
          }
        }

        i++;
        j++;
        continue;
      } else if (mO.callDepth == 1 && mR.callDepth > 1) {
        j++;
        continue;
      } else if (mR.callDepth == 1 && mO.callDepth > 1) {
        i++;
        continue;
      } else {
        i++;
        j++;
      }
    }


  }



  private double getTestObjectDistance(
      List<Map<Integer, Map<String, Map<String, Object>>>> originalMap,
      List<Map<Integer, Map<String, Map<String, Object>>>> regressionMap) {

    ObjectDistanceCalculator.different_variables = 0;

    double distance = 0.0;

    // logger.warn("" + topmap1 + topmap2);

    Map<String, Double> maxClassDistance = new HashMap<String, Double>();

    for (int j = 0; j < originalMap.size(); j++) {
      Map<Integer, Map<String, Map<String, Object>>> map1 = originalMap.get(j);

      if (regressionMap.size() <= j)
        continue;
      Map<Integer, Map<String, Map<String, Object>>> map2 = regressionMap.get(j);

      for (Map.Entry<Integer, Map<String, Map<String, Object>>> map1_entry : map1.entrySet()) {
        // Map.Entry<Integer, Map<String, Object>> map2_entry =
        // (Entry<Integer, Map<String, Object>>) map2.get(
        // map1_entry.getKey());

        // logger.warn("key: " + map1_entry.getKey());

        Map<String, Map<String, Object>> map1_values = map1_entry.getValue();
        Map<String, Map<String, Object>> map2_values = map2.get(map1_entry.getKey());
        // logger.warn("" + map1_values + map2_values);
        if (map1_values == null || map2_values == null)
          continue;
        for (Map.Entry<String, Map<String, Object>> internal_map1_entries : map1_values
            .entrySet()) {

          Map<String, Object> map1_value = internal_map1_entries.getValue();
          Map<String, Object> map2_value = map2_values.get(internal_map1_entries.getKey());
          if (map1_value == null || map2_value == null)
            continue;

          double objectDistance =
              ObjectDistanceCalculator.getObjectMapDistance(map1_value, map2_value);
          /*
           * logger.warn("oDistance: " + objectDistance); logger.warn("var1: " +map1_value +
           * " | var2: " + map2_value);
           */
          /*
           * if(map1_value.containsKey("fake_var_java_lang_Double") && (
           * Double)map1_value.get("fake_var_java_lang_Double")==0.5){
           * logger.warn("Map1: {} | Map2: {} " , map1_value,map2_value);
           * 
           * }
           */
          if (!maxClassDistance.containsKey(internal_map1_entries.getKey())
              || (maxClassDistance.get(internal_map1_entries.getKey()) < objectDistance))

            maxClassDistance.put(internal_map1_entries.getKey(), Double.valueOf(objectDistance));
          /*
           * logger.warn(internal_map1_entries.getKey() + ": " + map1_value + " --VS-- "+
           * map2_value);
           */

        }
      }

      // logger.warn("maxClassDistance size:" +
      // maxClassDistance.size() + " > " + entries);

    }
    // String entries = "";
    double temp_dis = 0.0;
    for (Map.Entry<String, Double> maxEntry : maxClassDistance.entrySet()) {
      temp_dis += maxEntry.getValue();

      /*
       * entries += maxEntry.getKey().toString() + " : " + maxEntry.getValue().toString() + " | ";
       */
    }

    if (Properties.REGRESSION_ANALYSIS_OBJECTDISTANCE == 4) {
      temp_dis = Collections.max(maxClassDistance.values());
    }

    if (Properties.REGRESSION_ANALYSIS_OBJECTDISTANCE == 5) {
      if (maxClassDistance.size() > 0)
        temp_dis = temp_dis / (maxClassDistance.size());
    }

    if (Properties.REGRESSION_ANALYSIS_OBJECTDISTANCE == 6) {
      temp_dis = Collections.min(maxClassDistance.values());
    }

    distance += temp_dis;
    // logger.warn(entries + " < " + observer.objectMapPool.size());

    distance += ObjectDistanceCalculator.different_variables;
    /*
     * if(distance>0) logger.warn("distance was {}",distance);
     */
    return distance;

  }

  /*
   * Table for name:
   * 
   * @ a: all measures
   * 
   * @ s: state difference
   * 
   * @ b: branch distance
   * 
   * @ c: coverage
   * 
   * @ o: coverage old
   * 
   * @ n: coverage new
   */
  public boolean useMeasure(RegressionMeasure m) {
    boolean flag = false;
    if (m == Properties.REGRESSION_FITNESS)
      return true;

    // for more complicated measurements (that combine stuff)
    switch (Properties.REGRESSION_FITNESS) {
      case COVERAGE_OLD:
        if (m == RegressionMeasure.COVERAGE || m == RegressionMeasure.COVERAGE_OLD)
          return true;
        break;
      case COVERAGE_NEW:
        if (m == RegressionMeasure.COVERAGE || m == RegressionMeasure.COVERAGE_NEW)
          return true;
        break;
      case STATE_DIFFERENCE:
        if (m == RegressionMeasure.STATE_DIFFERENCE)
          flag = true;
        break;
      case BRANCH_DISTANCE:
        if (m == RegressionMeasure.BRANCH_DISTANCE && Properties.REGRESSION_BRANCH_DISTANCE)
          flag = true;
        break;
      case COVERAGE:
        if (m == RegressionMeasure.COVERAGE || m == RegressionMeasure.COVERAGE_OLD
            || m == RegressionMeasure.COVERAGE_NEW)
          flag = true;
        break;
      case ALL_MEASURES:
      default:
        if (m == RegressionMeasure.COVERAGE || m == RegressionMeasure.STATE_DIFFERENCE
            || (m == RegressionMeasure.BRANCH_DISTANCE && Properties.REGRESSION_BRANCH_DISTANCE)
            || m == RegressionMeasure.COVERAGE_OLD || m == RegressionMeasure.COVERAGE_NEW)
          flag = true;
        break;

    }
    return flag;
  }


  /*
   * This is a minimizing fitness function
   * 
   * @see org.evosuite.ga.FitnessFunction#isMaximizationFunction()
   */
  @Override
  public boolean isMaximizationFunction() {
    // TODO Auto-generated method stub
    return false;
  }

  /*
   * Longest Repeated Substring. Uses suffix sorting, but not very efficient
   * 
   * Partially based on http://introcs.cs.princeton.edu/java/42sort/LRS.java.html
   */
  private class LRS {

    // return the longest common prefix of s and t
    public String lcp(String s, String t) {
      int n = Math.min(s.length(), t.length());
      for (int i = 0; i < n; i++) {
        if (s.charAt(i) != t.charAt(i))
          return s.substring(0, i);
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
          // SINA: Uncommenting the "optimization" `break` below will cause the function to return
          // as
          // soon as a substring is found
          // break;
        }
      }
      return lrs;
    }


  }

}
