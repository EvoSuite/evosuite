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

import static org.evosuite.Properties.REGRESSION_ANALYSIS_OBJECTDISTANCE;
import static org.evosuite.regression.RegressionFitnessHelper.useMeasure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.testcase.execution.MethodCall;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;


public class RegressionSuiteFitness extends TestSuiteFitnessFunction {

  /**
   *
   */
  private static final long serialVersionUID = -1979463801167353053L;

  private Map<String, Map<Integer, String>> diversityMap = new HashMap<>();
  private int maxBranchFitnessValueO = 0;
  private int maxBranchFitnessValueR = 0;
  private Map<Integer, Integer> branchIdMap = new HashMap<>();
  private transient RegressionExecutionObserver observer;
  private BranchCoverageSuiteFitness bcFitness;
  private BranchCoverageSuiteFitness bcFitnessRegression;
  private Map<Integer, Double> branchDistanceMap;
  private double bestFitness = Double.MAX_VALUE;
  private HashMap<Integer, Double> tempBranchDistanceMap;
  private int uniqueCalls;

  public RegressionSuiteFitness() {
    super();
    logger.warn("### initialising Regression-GA... ###");

    initBranchMap();

    try {
      TestGenerationContext.getInstance().getRegressionClassLoaderForSUT()
          .loadClass(Properties.TARGET_CLASS);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    // init branch coverage fitness
    bcFitness = new BranchCoverageSuiteFitness();
    bcFitnessRegression = new BranchCoverageSuiteFitness(
        TestGenerationContext.getInstance().getRegressionClassLoaderForSUT());

    // set branch coverage max values
    maxBranchFitnessValueO = bcFitness.getMaxValue();
    maxBranchFitnessValueR = bcFitnessRegression.getMaxValue();

    observer = new RegressionExecutionObserver();

    ExecutionTracer.enableTraceCalls();

  }

  private void initBranchMap() {
    // populate a temp branch distance map with initial data for all
    // branches(if they are not covered, 4 will be considered).
    tempBranchDistanceMap = new HashMap<>();
    double maxBranchValue = 4.0;

    for (Branch b : BranchPool
        .getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getAllBranches()) {
      tempBranchDistanceMap.put(b.getActualBranchId(), maxBranchValue);
    }
  }

  private void executeChangedTestsAndUpdateResults(
      AbstractTestSuiteChromosome<? extends ExecutableChromosome> changedSuite) {

    observer.clearPools();
    diversityMap.clear();

    RegressionTestSuiteChromosome suite = (RegressionTestSuiteChromosome) changedSuite;
    for (TestChromosome chromosome : suite.getTestChromosomes()) {
      RegressionTestChromosome c = (RegressionTestChromosome) chromosome;

      observer.enable();
      observer.resetObjPool();
      observer.setRegressionFlag(false);

      TestChromosome testChromosome = c.getTheTest();
      TestChromosome otherChromosome = c.getTheSameTestForTheOtherClassLoader();

      // Only execute test if it hasn't been changed
      if (testChromosome.isChanged() || testChromosome.getLastExecutionResult() == null) {

        // record diversity
        if (Properties.REGRESSION_DIVERSITY) {
          RegressionFitnessHelper.trackDiversity(c, testChromosome);
        }

        ExecutionResult result = TestCaseExecutor.runTest(testChromosome.getTestCase());

        observer.setRegressionFlag(true);
        ExecutionResult otherResult = TestCaseExecutor.runTest(otherChromosome.getTestCase());
        observer.setRegressionFlag(false);

        observer.disable();

        double objectDistance = getTestObjectDistance(
            observer.currentObjectMapPool,
            observer.currentRegressionObjectMapPool);

        result.regressionObjectDistance = objectDistance;
        otherResult.regressionObjectDistance = objectDistance;

        testChromosome.setLastExecutionResult(result);
        testChromosome.setChanged(false);

        otherChromosome.setLastExecutionResult(otherResult);
        otherChromosome.setChanged(false);
      }

      if (Properties.REGRESSION_DIVERSITY) {
        measureDiversity(c);
      }
    }


  }

  private void measureDiversity(RegressionTestChromosome c) {
    for (Entry<String, Map<Integer, String>> dEntry : c.diversityMap.entrySet()) {
      Map<Integer, String> divInstance = diversityMap.get(dEntry.getKey());
      if (divInstance == null) {
        diversityMap.put(dEntry.getKey(), dEntry.getValue());
      } else {
        Map<Integer, String> testMethodCalls = dEntry.getValue();
        for (Entry<Integer, String> mc : testMethodCalls.entrySet()) {
          String calls = divInstance.get(mc.getKey());
          if (calls == null || calls.length() < mc.getValue().length()) {
            calls = mc.getValue();
          }
          divInstance.put(mc.getKey(), calls);
        }
      }
    }
  }


  /*
   * Get fitness value for individual
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

    int numDifferentExceptions = 0;
    int totalExceptions = 0;

    executeChangedTestsAndUpdateResults(individual);

    RegressionTestSuiteChromosome suite = (RegressionTestSuiteChromosome) individual;

    List<Double> objectDistances = new ArrayList<>();

    for (TestChromosome regressionTest : suite.getTestChromosomes()) {

      RegressionTestChromosome rtc = (RegressionTestChromosome) regressionTest;

      ExecutionResult result1 = rtc.getTheTest().getLastExecutionResult();

      ExecutionResult result2 = rtc.getTheSameTestForTheOtherClassLoader().getLastExecutionResult();

      // calculating exception difference
      int numExceptionOrig = result1.getNumberOfThrownExceptions();
      int numExceptionReg = result2.getNumberOfThrownExceptions();

      double exDiff = Math.abs((double) (numExceptionOrig - numExceptionReg));

      totalExceptions += numExceptionOrig + numExceptionReg;

      numDifferentExceptions += exDiff;

      // branch distance
      if (useMeasure(RegressionMeasure.BRANCH_DISTANCE)) {
        this.getBranchDistance(
            result1.getTrace().getMethodCalls(),
            result2.getTrace().getMethodCalls());
      }

      // object distance
      objectDistances.add(result1.regressionObjectDistance);

    }

    double objectDistanceFitness = 0;
    if (useMeasure(RegressionMeasure.STATE_DIFFERENCE)) {
      if (!objectDistances.isEmpty()) {
        distance = Collections.max(objectDistances);
      }
      objectDistanceFitness =
          (1.0 / (1.0 + distance)) * (maxBranchFitnessValueO + maxBranchFitnessValueR);
    }

    AbstractTestSuiteChromosome<TestChromosome> testSuiteChromosome = suite.getTestSuite();

    AbstractTestSuiteChromosome<TestChromosome> testRegressionSuiteChromosome = null;
    if (useMeasure(RegressionMeasure.COVERAGE_NEW)) {
      testRegressionSuiteChromosome = suite.getTestSuiteForTheOtherClassLoader();
    }

    double coverageOld = 0, coverageNew = 0;
    if (useMeasure(RegressionMeasure.COVERAGE_OLD)) {
      coverageOld = bcFitness.getFitness(testSuiteChromosome);
    }
    if (useMeasure(RegressionMeasure.COVERAGE_NEW)) {
      coverageNew = bcFitnessRegression.getFitness(testRegressionSuiteChromosome);
    }
    double coverage = coverageOld + coverageNew;

    double branchDistanceFitness = 0;

    double totalBranchDistanceFitness = 0.0;
    if (useMeasure(RegressionMeasure.BRANCH_DISTANCE)) {
      for (Map.Entry<Integer, Double> branch : branchDistanceMap.entrySet()) {
        totalBranchDistanceFitness += branch.getValue();
      }

      branchDistanceFitness = totalBranchDistanceFitness;
    }

    switch (Properties.REGRESSION_FITNESS) {
      case COVERAGE_OLD:
        fitness += coverageOld;
        break;
      case COVERAGE_NEW:
        fitness += coverageNew;
        break;
      case BRANCH_DISTANCE:
        fitness += branchDistanceFitness;
        break;
      case STATE_DIFFERENCE:
        fitness += objectDistanceFitness;
        break;
      case COVERAGE:
        fitness += coverage;
        break;
      case ALL_MEASURES:
      default:
        fitness += coverage;
        fitness += branchDistanceFitness;
        fitness += objectDistanceFitness;
        break;
    }

    double exceptionDistance = (1.0 / (1.0 + numDifferentExceptions));
    fitness += exceptionDistance;

    if (Properties.REGRESSION_DIVERSITY) {
      calculateDiversity();

      double diversityFitness = (1.0 / (1.0 + uniqueCalls));
      fitness += diversityFitness;
    }

    individual.setCoverage(this, (bcFitness.totalCovered + bcFitnessRegression.totalCovered) / 2.0);
    updateIndividual(this, individual, fitness);

    if (fitness < bestFitness) {
      bestFitness = fitness;

      logger.warn("OBJ distance: " + distance + " - fitness:" + fitness + " - branchDistance:"
          + totalBranchDistanceFitness + " - coverage:" + coverage + " - ex: "
          + numDifferentExceptions + " - tex: " + totalExceptions);

      logger.warn("Best Fitness " + fitness + ", number of tests: " + testSuiteChromosome.size()
          + ", total length: " + testSuiteChromosome.totalLengthOfTestCases());
    }

    return fitness;
  }

  /**
   * Calculate diversity among objects
   */
  private void calculateDiversity() {
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
          uniqueCalls++;
        }
      }
    }
  }

  /*
   * Get the distance of two branches given two method calls
   * 
   * @deprecated This function isn't in use anymore...
   */
  private void getBranchDistance(List<MethodCall> methodCallsOrig,
      List<MethodCall> methodCallsReg) {
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

    for (int i = 0, j = 0; i < methodCallsOrig.size() && j < methodCallsReg.size(); ) {
      MethodCall mO = methodCallsOrig.get(i);
      MethodCall mR = methodCallsReg.get(j);

      if (mO.methodName.equals(mR.methodName)) {
        // logger.warn("mO is mR: " + mO.methodName);

        List<Integer> branchesO = mO.branchTrace;
        List<Integer> branchesR = mR.branchTrace;

        for (int k = 0, l = 0; k < branchesO.size() && l < branchesR.size(); ) {
          Integer branchO = branchesO.get(k);
          Integer branchR = branchesR.get(l);

          if (Properties.REGRESSION_DIFFERENT_BRANCHES) {

            logger.error(
                "Regression_differrent_branches has been deprecated and removed from Evosuite. Please disable the property and try again");

            if (branchIdMap.containsKey(branchO)) {
              branchR = branchIdMap.get(branchO);
            } else {

              if ((Objects.equals(branchO, branchR))) {
                k++;
              }
              l++;
              continue;
            }
          }

          if (Objects.equals(branchO, branchR)) {

            double trueDisO = normalize(mO.trueDistanceTrace.get(k));
            double trueDisR = normalize(mR.trueDistanceTrace.get(l));

            double falseDisO = normalize(mO.falseDistanceTrace.get(k));
            double falseDisR = normalize(mR.falseDistanceTrace.get(l));

            double tempBranchDistance =
                2.0 * (1 - (Math.abs(trueDisO - trueDisR) + Math.abs(falseDisO - falseDisR)));
            tempBranchDistance += (Math.abs(trueDisR) + Math.abs(falseDisR)) / 2.0;

            tempBranchDistance += (Math.abs(trueDisO) + Math.abs(falseDisO)) / 2.0;

            if ((trueDisO == 0 && falseDisR == 0) || (falseDisO == 0 && trueDisR == 0)) {
              tempBranchDistance = 0;
            }

            if (!branchDistanceMap.containsKey(branchO)
                || branchDistanceMap.get(branchO) > tempBranchDistance) {
              branchDistanceMap.put(branchO, tempBranchDistance);
            }

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

    ObjectDistanceCalculator distanceCalculator = new ObjectDistanceCalculator();

    double distance = 0.0;

    // logger.warn("" + topmap1 + topmap2);

    Map<String, Double> maxClassDistance = new HashMap<String, Double>();

    for (int j = 0; j < originalMap.size(); j++) {
      Map<Integer, Map<String, Map<String, Object>>> map1 = originalMap.get(j);

      if (regressionMap.size() <= j) {
        continue;
      }
      Map<Integer, Map<String, Map<String, Object>>> map2 = regressionMap.get(j);

      for (Map.Entry<Integer, Map<String, Map<String, Object>>> map1_entry : map1.entrySet()) {

        Map<String, Map<String, Object>> map1_values = map1_entry.getValue();
        Map<String, Map<String, Object>> map2_values = map2.get(map1_entry.getKey());

        if (map1_values == null || map2_values == null) {
          continue;
        }
        for (Map.Entry<String, Map<String, Object>> internal_map1_entries : map1_values
            .entrySet()) {

          Map<String, Object> map1_value = internal_map1_entries.getValue();
          Map<String, Object> map2_value = map2_values.get(internal_map1_entries.getKey());
          if (map1_value == null || map2_value == null) {
            continue;
          }

          double objectDistance = distanceCalculator.getObjectMapDistance(map1_value, map2_value);

          if (!maxClassDistance.containsKey(internal_map1_entries.getKey())
              || (maxClassDistance.get(internal_map1_entries.getKey()) < objectDistance)) {
            maxClassDistance.put(internal_map1_entries.getKey(), objectDistance);
          }
        }
      }
    }

    double tmpDistance = 0.0;

    switch (REGRESSION_ANALYSIS_OBJECTDISTANCE) {
      // MAX
      case 4:
        tmpDistance = Collections.max(maxClassDistance.values());
        break;
      // AVG
      case 5:
        if (maxClassDistance.size() > 0) {
          tmpDistance = tmpDistance / (maxClassDistance.size());
        }
        break;
      // MIN
      case 6:
        tmpDistance = Collections.min(maxClassDistance.values());
        break;
      // SUM
      default:
        for (Map.Entry<String, Double> maxEntry : maxClassDistance.entrySet()) {
          tmpDistance += maxEntry.getValue();
        }
    }

    distance += tmpDistance;

    distance += distanceCalculator.getNumDifferentVariables();

    return distance;

  }


  /*
   * This is a minimizing fitness function
   * 
   * @see org.evosuite.ga.FitnessFunction#isMaximizationFunction()
   */
  @Override
  public boolean isMaximizationFunction() {
    return false;
  }

}
