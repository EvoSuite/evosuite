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
package org.evosuite.coverage.io.input;

import static org.evosuite.coverage.io.IOCoverageConstants.*;

import org.evosuite.Properties;
import org.evosuite.coverage.archive.TestsArchive;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.objectweb.asm.Type;

import java.util.*;

/**
 * @author Jose Miguel Rojas
 */
public class InputCoverageSuiteFitness extends TestSuiteFitnessFunction {



    public final int totalGoals;

    // Some stuff for debug output
    public int maxCoveredGoals = 0;
    public double bestFitness = Double.MAX_VALUE;

    private final Set<TestFitnessFunction> inputCoverageMap = new LinkedHashSet<>();

    private Set<TestFitnessFunction> toRemoveGoals = new LinkedHashSet<>();
    private Set<TestFitnessFunction> removedGoals  = new LinkedHashSet<>();

    
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
            inputCoverageMap.add(goal);
			if(Properties.TEST_ARCHIVE)
				TestsArchive.instance.addGoalToCover(this, goal);

        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Execute all tests and count covered input goals
     */
    @Override
    public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
        logger.trace("Calculating test suite fitness");
        double fitness = 0.0;

        List<ExecutionResult> results = runTestSuite(suite);

        HashSet<TestFitnessFunction> setOfCoveredGoals = new HashSet<>();

        boolean hasTimeoutOrTestException = false;

        for (ExecutionResult result : results) {
            if (result.hasTimeout() || result.hasTestException()) {
                hasTimeoutOrTestException = true;
            } else {
                for (Set<InputCoverageGoal> coveredGoals : result.getInputGoals().values()) {
                    for (InputCoverageGoal goal : coveredGoals) {
                        InputCoverageTestFitness testFitness = new InputCoverageTestFitness(goal);
                        // do nothing if it was already removed
                        if (removedGoals.contains(testFitness)) continue;
                        if (inputCoverageMap.contains(testFitness)) {
                            // update setOfCoveredGoals
                            setOfCoveredGoals.add(testFitness);
                            // add covered goal to test
                            result.test.addCoveredGoal(testFitness);
                            if (Properties.TEST_ARCHIVE) {
                                // add goal to archive
                                TestsArchive.instance.putTest(this, testFitness, result);
                                // mark goal to be removed for next generation
                                toRemoveGoals.add(testFitness);
                            }
                            suite.isToBeUpdated(true);
                        }

                    }
                }
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

    @Override
    public boolean updateCoveredGoals() {
        if(!Properties.TEST_ARCHIVE)
            return false;

        for (TestFitnessFunction goal : toRemoveGoals) {
            if (inputCoverageMap.remove(goal))
                removedGoals.add(goal);
            else
                throw new IllegalStateException("goal to remove not found");
        }
        toRemoveGoals.clear();
        logger.info("Current state of archive: "+TestsArchive.instance.toString());
        return true;
    }

    public double computeDistance(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite, 
                                  List<ExecutionResult> results, HashSet<TestFitnessFunction> setOfCoveredGoals) {
        Map<TestFitnessFunction, Double> mapDistances = new HashMap<>();
        for (ExecutionResult result : results) {
            if (result.hasTimeout() || result.hasTestException() || result.noThrownExceptions())
                continue;

            for (Set<InputCoverageGoal> coveredGoals : result.getInputGoals().values()) {
                for (InputCoverageGoal goal : coveredGoals) {
                    String className = goal.getClassName();
                    String methodName = goal.getMethodName();
                    Type argType = goal.getType();
                    Number argValue = goal.getNumericValue();
                    switch (argType.getSort()) {
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
                            updateDistances(suite, mapDistances, className, methodName, goal.getArgIndex(), argType, value);
                            break;
                        case Type.CHAR:
                            char charValue = (char)((Number) argValue).intValue();
                            updateCharDistances(suite, mapDistances, className, methodName, goal.getArgIndex(), argType, charValue);
                            break;
                        default:
                            break;
                    }

                }
            }
        }
        double distance = 0.0;
        for (TestFitnessFunction goal : inputCoverageMap) {
            if (!setOfCoveredGoals.contains(goal) && !removedGoals.contains(goal)) {
                if (mapDistances.containsKey(goal)) {
                    distance += normalize(mapDistances.get(goal));
                } else
                    distance += 1.0;
            }
        }
        return distance;
    }

    private void updateDistances(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite, Map<TestFitnessFunction, Double> mapDistances, String className, String methodName, int argIndex, Type argType, double value) {
        TestFitnessFunction goalNegative = InputCoverageFactory.createGoal(className, methodName, argIndex, argType, NUM_NEGATIVE);
        TestFitnessFunction goalZero = InputCoverageFactory.createGoal(className, methodName, argIndex, argType, NUM_ZERO);
        TestFitnessFunction goalPositive = InputCoverageFactory.createGoal(className, methodName, argIndex, argType, NUM_POSITIVE);
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

    private void updateCharDistances(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite, Map<TestFitnessFunction, Double> mapDistances, String className, String methodName, int argIndex, Type argType, char value) {
        TestFitnessFunction goalAlpha = InputCoverageFactory.createGoal(className, methodName, argIndex, argType, CHAR_ALPHA);
        TestFitnessFunction goalDigit = InputCoverageFactory.createGoal(className, methodName, argIndex, argType, CHAR_DIGIT);
        TestFitnessFunction goalOther = InputCoverageFactory.createGoal(className, methodName, argIndex, argType, CHAR_OTHER);

        double distanceToAlpha = 0.0;
        if(value < 'A')
            distanceToAlpha = 'A' - value;
        else if(value > 'z')
            distanceToAlpha = value - 'z';
        else if(value < 'a' && value > 'Z') {
            distanceToAlpha = Math.min('a' - value, value - 'Z');
        }

        double distanceToDigit = 0.0;
        if(value < '0')
            distanceToDigit = '0' - value;
        else if(value > '9')
            distanceToDigit = value - '9';

        double distanceToOther = 0.0;
        if(value > '0' && value < '9')
            distanceToAlpha = Math.min(value - '0', '9' - value);
        else if(value > 'A' && value < 'Z')
            distanceToAlpha = Math.min(value - 'A', 'Z' - value);
        else if(value > 'a' && value < 'z')
            distanceToAlpha = Math.min(value - 'A', 'Z' - value);

        if (mapDistances.containsKey(goalAlpha)) {
            if (distanceToAlpha < mapDistances.get(goalAlpha))
                mapDistances.put(goalAlpha, distanceToAlpha);
        } else
            mapDistances.put(goalAlpha, distanceToAlpha);

        if (mapDistances.containsKey(goalDigit)) {
            if (distanceToDigit < mapDistances.get(goalDigit))
                mapDistances.put(goalDigit, distanceToDigit);
        } else
            mapDistances.put(goalDigit, distanceToDigit);

        if (mapDistances.containsKey(goalOther)) {
            if (distanceToOther < mapDistances.get(goalOther))
                mapDistances.put(goalOther, distanceToOther);
        } else
            mapDistances.put(goalOther, distanceToOther);
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
