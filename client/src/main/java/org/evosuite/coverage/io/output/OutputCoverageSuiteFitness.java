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
package org.evosuite.coverage.io.output;

import static org.evosuite.coverage.io.IOCoverageConstants.*;

import org.evosuite.Properties;
import org.evosuite.coverage.archive.TestsArchive;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.hibernate.result.Output;
import org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Jose Miguel Rojas
 */
public class OutputCoverageSuiteFitness extends TestSuiteFitnessFunction {

    private static final long serialVersionUID = -8345906214972153096L;

    //public final int numBranchlessMethods;
    public final int totalGoals;

    // Some stuff for debug output
    public int maxCoveredGoals = 0;
    public double bestFitness = Double.MAX_VALUE;

    private final Set<TestFitnessFunction> outputCoverageGoals = new LinkedHashSet<>();

    private Set<TestFitnessFunction> toRemoveGoals = new LinkedHashSet<>();
    private Set<TestFitnessFunction> removedGoals  = new LinkedHashSet<>();

    
    public OutputCoverageSuiteFitness() {    	
        // Add observer
        TestCaseExecutor executor = TestCaseExecutor.getInstance();
        OutputObserver observer = new OutputObserver();
        executor.addObserver(observer);
        //TODO: where to remove observer?: executor.removeObserver(observer);

        determineCoverageGoals();

        totalGoals = outputCoverageGoals.size();
    }

    /**
     * Initialize the set of known coverage goals
     */
    private void determineCoverageGoals() {
        List<OutputCoverageTestFitness> goals = new OutputCoverageFactory().getCoverageGoals();
        for (OutputCoverageTestFitness goal : goals) {
            outputCoverageGoals.add(goal);
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

        HashSet<TestFitnessFunction> setOfCoveredGoals = new HashSet<>();

        boolean hasTimeoutOrTestException = false;

        for (ExecutionResult result : results) {
            if (result.hasTimeout() || result.hasTestException()) {
                hasTimeoutOrTestException = true;
            } else {
                for(Set<OutputCoverageGoal> coveredGoals : result.getOutputGoals().values()) {
                    for (OutputCoverageGoal goal : coveredGoals) {
                        OutputCoverageTestFitness testFitness = new OutputCoverageTestFitness(goal);
                        // do nothing if it was already removed
                        if (removedGoals.contains(testFitness)) continue;
                        if (outputCoverageGoals.contains(testFitness)) {
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
            if (outputCoverageGoals.remove(goal))
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

            for (Set<OutputCoverageGoal> coveredGoals : result.getOutputGoals().values()) {
                for(OutputCoverageGoal goal : coveredGoals) {
                    String className  = goal.getClassName();
                    String methodName = goal.getMethodName();
                    Type returnType = goal.getType();

                    Number returnValue = goal.getNumericValue();
                    switch (returnType.getSort()) {
                        case Type.BYTE:
                        case Type.SHORT:
                        case Type.INT:
                        case Type.FLOAT:
                        case Type.LONG:
                        case Type.DOUBLE:
                            assert (returnValue != null);
                            assert (returnValue instanceof Number);
                            // TODO: ideally we should be able to tell between Number as an object, and primitive numeric types
                            double value = ((Number) returnValue).doubleValue();
                            if (Double.isNaN(value)) // EvoSuite generates Double.NaN
                                continue;
                            updateDistances(suite, mapDistances, className, methodName, returnType, value);
                            break;
                        default:
                            break;
                    }

                }
            }
        }
        double distance = 0.0;
        for (TestFitnessFunction goal : outputCoverageGoals) {
            if (!setOfCoveredGoals.contains(goal) && !removedGoals.contains(goal)) {
                if (mapDistances.containsKey(goal)) {
                    distance += normalize(mapDistances.get(goal));
                } else
                    distance += 1.0;
            }
        }
        return distance;
    }

    private void updateDistances(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite, Map<TestFitnessFunction, Double> mapDistances, String className, String methodName, Type returnType, double value) {
        TestFitnessFunction goalNegative = OutputCoverageFactory.createGoal(className, methodName, returnType, NUM_NEGATIVE);
        TestFitnessFunction goalZero = OutputCoverageFactory.createGoal(className, methodName, returnType, NUM_ZERO);
        TestFitnessFunction goalPositive = OutputCoverageFactory.createGoal(className, methodName, returnType, NUM_POSITIVE);
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
            logger.info("(Output Goals) Best individual covers " + coveredGoals + "/"
                    + totalGoals + " output goals");
            maxCoveredGoals = coveredGoals;
            logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
                    + suite.totalLengthOfTestCases());

        }
        if (fitness < bestFitness) {
            logger.info("(Fitness) Best individual covers " + coveredGoals + "/"
                    + totalGoals + " output goals");
            bestFitness = fitness;
            logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
                    + suite.totalLengthOfTestCases());

        }
    }

}
