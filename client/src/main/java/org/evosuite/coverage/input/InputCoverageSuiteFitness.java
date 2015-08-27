/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.coverage.input;

import org.evosuite.Properties;
import org.evosuite.coverage.archive.TestsArchive;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.objectweb.asm.Type;

import java.util.*;

/**
 * @author Jose Miguel Rojas
 */
public class InputCoverageSuiteFitness extends TestSuiteFitnessFunction {

    private static final long serialVersionUID = -8345906214972153096L;

    public final int totalGoals;

    // Some stuff for debug output
    public int maxCoveredGoals = 0;
    public double bestFitness = Double.MAX_VALUE;

    // Each test gets a set of distinct covered goals, these are mapped by goal string
    private final Map<String, TestFitnessFunction> inputCoverageMap = new HashMap<String, TestFitnessFunction>();

    private Set<String> toRemoveGoals = new LinkedHashSet<>();
    private Set<String> removedGoals  = new LinkedHashSet<>();

    
    public InputCoverageSuiteFitness() {
        // Add observer
        TestCaseExecutor executor = TestCaseExecutor.getInstance();
        InputObserver observer = new InputObserver();
        executor.addObserver(observer);
        //TODO: where to remove observer?: executor.removeObserver(observer);

        determineCoverageGoals();

        totalGoals = inputCoverageMap.size();
    }

    /**
     * Initialize the set of known coverage goals
     */
    private void determineCoverageGoals() {
        List<InputCoverageTestFitness> goals = new InputCoverageFactory().getCoverageGoals();
        for (InputCoverageTestFitness goal : goals) {
            inputCoverageMap.put(goal.toString(), goal);
			if(Properties.TEST_ARCHIVE)
				TestsArchive.instance.addGoalToCover(this, goal);

        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Execute all tests and count covered output goals
     */
    @Override
    public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
        logger.trace("Calculating test suite fitness");
        double fitness = 0.0;

        List<ExecutionResult> results = runTestSuite(suite);

        HashSet<String> setOfCoveredGoals = new HashSet<String>();

        boolean hasTimeoutOrTestException = false;

        for (ExecutionResult result : results) {
            if (result.hasTimeout() || result.hasTestException()) {
                hasTimeoutOrTestException = true;
            } else {
                updateCoveredGoals(suite, result, setOfCoveredGoals);
            }
        }

        int coveredGoals = setOfCoveredGoals.size() + removedGoals.size();

        if (hasTimeoutOrTestException) {
            logger.info("Test suite has timed out, setting fitness to max value " + totalGoals);
            fitness = totalGoals;
        } else
            fitness = computeDistance(suite, results, setOfCoveredGoals);

        if (totalGoals > 0)
            suite.setCoverage(this, (double) coveredGoals / (double) totalGoals);
        else
            suite.setCoverage(this, 1.0);

        suite.setNumOfCoveredGoals(this, coveredGoals);

        printStatusMessages(suite, coveredGoals, fitness);
        updateIndividual(this, suite, fitness);

        assert (coveredGoals <= totalGoals) : "Covered " + coveredGoals + " vs total goals " + totalGoals;
        assert (fitness >= 0.0);
        assert (fitness != 0.0 || coveredGoals == totalGoals) : "Fitness: " + fitness + ", "
                + "coverage: " + coveredGoals + "/" + totalGoals;
        assert (suite.getCoverage(this) <= 1.0) && (suite.getCoverage(this) >= 0.0) : "Wrong coverage value "
                + suite.getCoverage(this);

        return fitness;
    }

    private void updateCoveredGoals(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite, ExecutionResult result, HashSet<String> setOfCoveredGoals) {
        HashSet<String> strGoals = InputCoverageTestFitness.listCoveredGoals(result.getArgumentsValues());
        for (String strGoal : strGoals) {
        	if(removedGoals.contains(strGoal)) continue;
            if (inputCoverageMap.containsKey(strGoal)) {
                setOfCoveredGoals.add(strGoal);
                result.test.addCoveredGoal(inputCoverageMap.get(strGoal));
                if(Properties.TEST_ARCHIVE) {
					TestsArchive.instance.putTest(this, inputCoverageMap.get(strGoal), result);
					toRemoveGoals.add(strGoal);
					suite.isToBeUpdated(true);
                }
            }
        }
    }

    public double computeDistance(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite, List<ExecutionResult> results, HashSet<String> setOfCoveredGoals) {
        Map<String, Double> mapDistances = new HashMap<String, Double>();
        for (ExecutionResult result : results) {
            if (result.hasTimeout() || result.hasTestException() || result.noThrownExceptions())
                continue;

            Map<MethodStatement, List<Object>> argumentsValues = result.getArgumentsValues();

            for (Map.Entry<MethodStatement, List<Object>> entry : argumentsValues.entrySet()) {
                String className = entry.getKey().getMethod().getMethod().getDeclaringClass().getName();
                String methodDesc = Type.getMethodDescriptor(entry.getKey().getMethod().getMethod());
                String methodName = entry.getKey().getMethod().getName() + methodDesc;

                Type[] argumentTypes = Type.getArgumentTypes(methodDesc);

                for (int i=0; i<argumentTypes.length; i++) {
                    Type argType = argumentTypes[i];
                    Object argValue = entry.getValue().get(i);
                    String goalSuffix = "";
                    switch (argType.getSort()) {
                        case Type.BOOLEAN:
                            if (((boolean) argValue))
                                goalSuffix = InputCoverageFactory.BOOL_TRUE;
                            else
                                goalSuffix = InputCoverageFactory.BOOL_FALSE;
                            break;
                        case Type.CHAR:
                            char c = (char) argValue;
                            if (Character.isAlphabetic(c))
                                goalSuffix = InputCoverageFactory.CHAR_ALPHA;
                            else if (Character.isDigit(c))
                                goalSuffix = InputCoverageFactory.CHAR_DIGIT;
                            else
                                goalSuffix = InputCoverageFactory.CHAR_OTHER;
                            break;
                        case Type.BYTE:
                        case Type.SHORT:
                        case Type.INT:
                        case Type.FLOAT:
                        case Type.LONG:
                        case Type.DOUBLE:
                            assert (argValue != null);
                            assert (argValue instanceof Number);
                            // TODO: ideally we should be able to tell between Number as an object, and primitive numeric types
                            double value = ((Number) argValue).doubleValue();
                            if (Double.isNaN(value)) // EvoSuite generates Double.NaN
                                continue;

                            if (value < 0) {
                                goalSuffix = InputCoverageFactory.NUM_NEGATIVE;
                            } else if (value == 0) {
                                goalSuffix = InputCoverageFactory.NUM_ZERO;
                            } else {
                                goalSuffix = InputCoverageFactory.NUM_POSITIVE;
                            }
                            updateDistances(suite, mapDistances, className, methodName, i, value);
                            break;
                        case Type.ARRAY:
                        case Type.OBJECT:
                            if (argValue == null)
                                goalSuffix = InputCoverageFactory.REF_NULL;
                            else
                                goalSuffix = InputCoverageFactory.REF_NONNULL;
                            break;
                        default:
                            break;
                    }

                    if (!goalSuffix.isEmpty()) {
                        String strGoal = InputCoverageFactory.goalString(className, methodName, i, goalSuffix);
                        if (removedGoals.contains(strGoal))
                            continue;
                        if (inputCoverageMap.containsKey(strGoal)) {
                            setOfCoveredGoals.add(strGoal);
                            result.test.addCoveredGoal(inputCoverageMap.get(strGoal));
                            if (Properties.TEST_ARCHIVE) {
                                TestsArchive.instance.putTest(this, inputCoverageMap.get(strGoal), result);
                                toRemoveGoals.add(strGoal);
                                suite.isToBeUpdated(true);
                            }
                        }
                    }
                }
            }
        }
        double distance = 0.0;
        for (String strG : inputCoverageMap.keySet()) {
            if (!setOfCoveredGoals.contains(strG) && !removedGoals.contains(strG)) {
                if (mapDistances.containsKey(strG)) {
                    distance += normalize(mapDistances.get(strG));
                } else
                    distance += 1.0;
            }
        }
        return distance;
    }

    private void updateDistances(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite, Map<String, Double> mapDistances, String className, String methodName, int argIndex, double value) {
        String goalNegative = InputCoverageFactory.goalString(className, methodName, argIndex, InputCoverageFactory.NUM_NEGATIVE);
        String goalZero = InputCoverageFactory.goalString(className, methodName, argIndex, InputCoverageFactory.NUM_ZERO);
        String goalPositive = InputCoverageFactory.goalString(className, methodName, argIndex, InputCoverageFactory.NUM_POSITIVE);
        double distanceToNegative = 0.0;
        double distanceToZero = 0.0;
        double distanceToPositive = 0.0;
        if (value < 0) {
            distanceToNegative = 0;
            distanceToZero = Math.abs(value);
            distanceToPositive = Math.abs(value) + 1;
        } else if (value == 0) {
            distanceToNegative = 1;
            distanceToZero = 0;
            distanceToPositive = 1;
        } else {
            distanceToNegative = value + 1;
            distanceToZero = value;
            distanceToPositive = 0;
        }
        if (mapDistances.containsKey(goalNegative)) {
            if (distanceToNegative < mapDistances.get(goalNegative))
                mapDistances.put(goalNegative, distanceToNegative);
        } else
            mapDistances.put(goalNegative, distanceToNegative);
        if (mapDistances.containsKey(goalZero)) {
            if (distanceToZero < mapDistances.get(goalZero))
                mapDistances.put(goalZero, distanceToZero);
        } else
            mapDistances.put(goalZero, distanceToZero);
        if (mapDistances.containsKey(goalPositive)) {
            if (distanceToPositive < mapDistances.get(goalPositive))
                mapDistances.put(goalPositive, distanceToPositive);
        } else
            mapDistances.put(goalPositive, distanceToPositive);
    }

    /**
     * Some useful debug information
     *
     * @param coveredGoals
     * @param fitness
     */
    private void printStatusMessages(
            AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite,
            int coveredGoals, double fitness) {
        if (coveredGoals > maxCoveredGoals) {
            logger.info("(Input Goals) Best individual covers " + coveredGoals + "/"
                    + totalGoals + " input goals");
            maxCoveredGoals = coveredGoals;
            logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
                    + suite.totalLengthOfTestCases());

        }
        if (fitness < bestFitness) {
            logger.info("(Fitness) Best individual covers " + coveredGoals + "/"
                    + totalGoals + " input goals");
            bestFitness = fitness;
            logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
                    + suite.totalLengthOfTestCases());

        }
    }

}
