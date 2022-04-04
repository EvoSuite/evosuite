/*
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
package org.evosuite.symbolic.dse;

import org.evosuite.Properties;
import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.symbolic.ConstraintTypeCounter;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.constraint.IntegerConstraint;
import org.evosuite.symbolic.expr.constraint.RealConstraint;
import org.evosuite.symbolic.expr.constraint.StringConstraint;
import org.evosuite.symbolic.solver.SolverCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is used to store statistics on DSE.
 *
 * @author galeotti
 */
public class DSEStatistics {

    static Logger logger = LoggerFactory.getLogger(DSEStatistics.class);

    /**
     * Messages Constants
     **/
    public static final String NO_QUERY_CACHE_CALLS_WERE_MADE = "No query cache calls were made";

    /**
     * Tracking of runtime variables used in DSE,
     * please add them here when adding a new one so they are saved in the backend
     **/
    public static List<String> dseRuntimeVariables = Arrays.asList(
            // Solver
            RuntimeVariable.NumberOfSATQueries.name(),
            RuntimeVariable.NumberOfUNSATQueries.name(),
            RuntimeVariable.NumberOfTimeoutQueries.name(),
            RuntimeVariable.NumberOfUsefulNewTests.name(),
            RuntimeVariable.NumberOfUnusefulNewTests.name(),

            // Query Cache
            RuntimeVariable.QueryCacheSize.name(),
            RuntimeVariable.QueryCacheCalls.name(),
            RuntimeVariable.QueryCacheHitRate.name(),

            // Execution Times
            RuntimeVariable.TotalTimeSpentSolvingConstraints.name(),
            RuntimeVariable.TotalTimeSpentExecutingTestCases.name(),
            RuntimeVariable.TotalTimeSpentExecutingConcolicaly.name(),
            RuntimeVariable.TotalTimeSpentExecutingNonConcolicTestCases.name(),

            // Path Conditions
            RuntimeVariable.MaxPathConditionLength.name(),
            RuntimeVariable.MinPathConditionLength.name(),
            RuntimeVariable.AvgPathConditionLength.name(),
            RuntimeVariable.NumberOfPathsExplored.name(),
            RuntimeVariable.NumberOfPathsDiverged.name()
    );

    private static DSEStatistics instance = null;

    public static DSEStatistics getInstance() {
        if (instance == null) {
            instance = new DSEStatistics();
        }
        return instance;
    }

    /**
     * This method initializes all counters to 0. It should be called only if
     * the user wants to clean all statistics.
     */
    public static void clear() {
        instance = null;
    }

    /**
     * This class cannot be built directly
     */
    private DSEStatistics() {

    }

    // Solver metrics
    private long nrOfUNSATs = 0;
    private long nrOfSATs = 0;
    private long nrOfTimeouts = 0;
    private long totalSolvingTimeMillis = 0;
    private long totalConcolicExecutionTimeMillis = 0;
    private long totalTestExecutionTime = 0;

    // Solver cahe
    private long queryCacheHits = 0;
    private long querycacheSize = 0;
    private long queryCacheCalls = 0;

    // New solutions found metrics
    private long nrOfSolutionWithNoImprovement = 0;
    private long nrOfNewTestFound = 0;

    // Path condition metrics
    private int pathConditionCount = 0;
    private int pathsExploredCounter = 0;
    private int pathDivergencesCounter = 0;
    private int maxPathConditionLength;
    private int minPathConditionLength;
    private double avgPathConditionLength;

    // Constraint metrics
    private int constraintCount = 0;
    private int maxConstraintSize = 0;
    private int minConstraintSize = 0;
    private int constraintTooLongCounter = 0;
    private double avgConstraintSize = 0;

    private final List<Boolean> changes = new LinkedList<>();
    private final ConstraintTypeCounter constraintTypeCounter = new ConstraintTypeCounter();

    public void reportNewUNSAT() {
        nrOfUNSATs++;
    }

    /**
     * Invoke this method when a SAT instance was found by a Constraint Solver
     */
    public void reportNewSAT() {
        nrOfSATs++;
    }

    /**
     * Call this method to report a new test found by DSE did not lead to a
     * fitness improvement.
     */
    public void reportNewTestUnuseful() {
        nrOfSolutionWithNoImprovement++;
    }

    /**
     * Invoke this method when no instance was found by a Constraint Solver
     */
    private long getUNSAT() {
        return nrOfUNSATs;
    }

    /**
     * Returns the number of SAT instances found. This instance may lead to a
     * new Test Case or not
     *
     * @return
     */
    private long getSAT() {
        return nrOfSATs;
    }

    /**
     * Returns the total number of SAT instances that did not lead to a fitness
     * improvement.
     *
     * @return
     */
    private long getUnusefulTests() {
        return nrOfSolutionWithNoImprovement;
    }

    /**
     * Invoke this method when a new test found by DSE is added to the test
     * suite.
     */
    public void reportNewTestUseful() {
        nrOfNewTestFound++;
    }

    /**
     * Returns the total number of new tests found by DSE added to a test suite.
     *
     * @return
     */
    private long getUsefulTests() {
        return nrOfNewTestFound;
    }

    /**
     * Invoke this method when a new test found by DSE turned out to diverged.
     */
    public void reportNewPathDivergence() {
        pathDivergencesCounter++;
    }

    /**
     * Invoke this method when a new path condition is found.
     */
    public void reportNewPathExplored() {
        pathsExploredCounter++;
    }


    public void logStatistics() {

        logger.info("");
        logger.info("#### DSE Statistics");

        logger.info("");
        logSolverStatistics();

        logger.info("");
        logSolverQueryCacheStatistics();

        logger.info("");
        logConstraintSizeStatistics();

        logger.info("");
        logPathConditionLengthStatistics();

        logger.info("");
        logPathsExploredStatistics();

        logger.info("");
        logTimeStatistics();

        logger.info("");
        logCacheStatistics();
        logger.info("");

        logger.info("");
        logAdaptationStatistics();
        logger.info("");

        logger.info("");
        logConstraintTypeStatistics();
        logger.info("");

        logger.info("");
        logger.info("###################");
        logger.info("");
    }

    private void logSolverQueryCacheStatistics() {
        logger.info("* Solver Query Cache:");
        logger.info(String.format("  - Query Cache size: %s", querycacheSize));
        logger.info(String.format("  - Query Cache calls: %s", queryCacheCalls));
        logger.info(String.format("  - Query Cache hitRare: %s", getQueryCacheHitRate(queryCacheHits, queryCacheCalls)));
    }

    private void logPathsExploredStatistics() {
        logger.info("* Paths exploration:");
        logger.info(String.format("  - paths explored: %s", pathsExploredCounter));
        logger.info(String.format("  - diverged paths: %s", pathDivergencesCounter));
    }

    private void logAdaptationStatistics() {
        StringBuffer buff = new StringBuffer();
        buff.append("[");
        for (Boolean change : changes) {
            if (change) {
                buff.append("+");
            } else {
                buff.append("-");
            }
        }
        buff.append("]");

        logger.info("* LS) Local Search Adaptation statistics");
        logger.info("* LS)   Adaptations: " + buff);
    }

    private void logCacheStatistics() {
        logger.info("* Constraint Cache Statistics");
        final int numberOfSATs = SolverCache.getInstance().getNumberOfSATs();
        final int numberOfUNSATs = SolverCache.getInstance().getNumberOfUNSATs();

        if (numberOfSATs == 0 || numberOfUNSATs == 0) {
            logger.info("  - Constraint Cache was not used.");

        } else {

            logger.info(String.format("  - Stored SAT constraints: %s", numberOfSATs));

            logger.info(String.format("  - Stored UNSAT constraints: %s", numberOfUNSATs));

            NumberFormat percentFormat = NumberFormat.getPercentInstance();
            percentFormat.setMaximumFractionDigits(1);
            String hit_rate_str = percentFormat.format(SolverCache.getInstance().getHitRate());
            logger.info(String.format("  - Cache hit rate: %s", hit_rate_str));
        }
    }

    private void logTimeStatistics() {
        logger.info("* Time Statistics");
        logger.info(String.format("  - Time spent executing test cases: %sms", totalTestExecutionTime));
        logger.info(String.format("  - Time spent executing test concolically: %sms",
                totalConcolicExecutionTimeMillis));
        logger.info(String.format("  - Time spent executing non concolic test cases: %sms",
                totalTestExecutionTime - totalConcolicExecutionTimeMillis));
        logger.info(String.format("  - Time spent solving constraints: %sms", totalSolvingTimeMillis));
    }

    private void logSolverStatistics() {
        long total_constraint_solvings = getSAT()
                + getUNSAT() + getTimeouts();

        String SAT_ratio_str = "Nan";
        String UNSAT_ratio_str = "Nan";
        String useful_tests_ratio_str = "Nan";
        String unuseful_tests_ratio_str = "Nan";
        String timeout_ratio_str = "Nan";

        if (total_constraint_solvings > 0) {
            double SAT_ratio = (double) getSAT()
                    / (double) total_constraint_solvings;
            double UNSAT_ratio = (double) getUNSAT()
                    / (double) total_constraint_solvings;
            double useful_tests_ratio = (double) getUsefulTests()
                    / (double) total_constraint_solvings;
            double unuseful_tests_ratio = (double) getUnusefulTests()
                    / (double) total_constraint_solvings;
            double timeout_ratio = (double) getTimeouts()
                    / (double) total_constraint_solvings;

            NumberFormat percentFormat = NumberFormat.getPercentInstance();
            percentFormat.setMaximumFractionDigits(1);

            SAT_ratio_str = percentFormat.format(SAT_ratio);
            UNSAT_ratio_str = percentFormat.format(UNSAT_ratio);
            useful_tests_ratio_str = percentFormat.format(useful_tests_ratio);
            unuseful_tests_ratio_str = percentFormat
                    .format(unuseful_tests_ratio);
            timeout_ratio_str = percentFormat.format(timeout_ratio);
        }

        logger.info("* Solving statistics");
        logger.info(String.format("  - SAT: %s (%s)", getSAT(),
                SAT_ratio_str));
        logger.info(String.format("  - Useful Tests: %s (%s)",
                getUsefulTests(), useful_tests_ratio_str));
        logger.info(String.format("  - Unuseful Tests:  %s (%s)",
                getUnusefulTests(), unuseful_tests_ratio_str));
        logger.info(String.format("  - UNSAT: %s (%s)",
                getUNSAT(), UNSAT_ratio_str));
        logger.info(String.format("  - Timeouts: %s (%s)",
                timeout_ratio_str, getTimeouts()));

        logger.info(String.format("  - # Constraint solvings: %s (%s+%s)",
                total_constraint_solvings, getSAT(),
                getUNSAT()));

    }

    private void logConstraintTypeStatistics() {
        int total = constraintTypeCounter.getTotalNumberOfConstraints();

        int integerOnly = constraintTypeCounter.getIntegerOnlyConstraints();
        int realOnly = constraintTypeCounter.getRealOnlyConstraints();
        int stringOnly = constraintTypeCounter.getStringOnlyConstraints();

        int integerRealOnly = constraintTypeCounter.getIntegerAndRealConstraints();
        int integerStringOnly = constraintTypeCounter.getIntegerAndStringConstraints();
        int realStringOnly = constraintTypeCounter.getRealAndStringConstraints();

        int integerRealStringConstraints = constraintTypeCounter.getIntegerRealAndStringConstraints();

        if (total == 0) {
            logger.info(String.format("  - no constraints {}", avgConstraintSize));
        } else {
            String line1 = String.format("  - Number of integer only constraints : %s / %s ", integerOnly, total);
            String line2 = String.format("  - Number of real only constraints : %s", realOnly, total);
            String line3 = String.format("  - Number of string only constraints : %s", stringOnly, total);
            String line4 = String.format("  - Number of integer+real constraints : %s / %s ", integerRealOnly,
                    total);
            String line5 = String.format("  - Number of integer+string constraints : %s / %s ", integerStringOnly,
                    total);
            String line6 = String.format("  - Number of real+string constraints : %s / %s ", realStringOnly,
                    total);
            String line7 = String.format("  - Number of integer+real+string constraints : %s / %s ",
                    integerRealStringConstraints, total);

            logger.info(line1);
            logger.info(line2);
            logger.info(line3);
            logger.info(line4);
            logger.info(line5);
            logger.info(line6);
            logger.info(line7);

        }
    }

    private void logConstraintSizeStatistics() {
        logger.info("* Constraint size:");
        logger.info(String.format("  - max constraint size: %s", maxConstraintSize));
        logger.info(String.format("  - min constraint size: %s", minConstraintSize));
        logger.info(String.format("  - avg constraint size: %s", avgConstraintSize));
        logger.info(String.format("  - Too big constraints: %s (max size %s)",
                getConstraintTooLongCounter(), Properties.DSE_CONSTRAINT_LENGTH));
    }

    private void logPathConditionLengthStatistics() {
        logger.info("* Path condition length:");
        logger.info(String.format("  - max path condition length: %s", maxPathConditionLength));
        logger.info(String.format("  - min path condition length: %s", minPathConditionLength));
        logger.info(String.format("  - avg path condition length: %s", avgPathConditionLength));
    }

    private int getConstraintTooLongCounter() {
        return constraintTooLongCounter;
    }

    private int getPathDivergencesCounter() {
        return pathDivergencesCounter;
    }

    public void reportNewConstraints(Collection<Constraint<?>> constraints) {

        if (pathConditionCount == 0) {
            minPathConditionLength = constraints.size();
            maxPathConditionLength = constraints.size();
            avgPathConditionLength = constraints.size();
        } else {
            // update average size
            double new_avg_size = avgPathConditionLength
                    + ((((double) constraints.size() - avgPathConditionLength))
                    / ((double) pathConditionCount + 1));
            avgPathConditionLength = new_avg_size;

            // update max length
            if (constraints.size() > maxPathConditionLength) {
                maxPathConditionLength = constraints.size();
            }

            // update min length
            if (constraints.size() < minPathConditionLength) {
                minPathConditionLength = constraints.size();
            }
        }

        pathConditionCount++;

        for (Constraint<?> c : constraints) {
            if (constraintCount == 0) {
                minConstraintSize = c.getSize();
                maxConstraintSize = c.getSize();
                avgConstraintSize = c.getSize();
            } else {
                // update average size
                double new_avg_size = avgConstraintSize
                        + ((((double) c.getSize() - avgConstraintSize)) / ((double) constraintCount + 1));
                avgConstraintSize = new_avg_size;

                // update max size
                if (c.getSize() > maxConstraintSize) {
                    maxConstraintSize = c.getSize();
                }
                // update min size
                if (c.getSize() < minConstraintSize) {
                    minConstraintSize = c.getSize();
                }

            }

            constraintCount++;
        }

        countTypesOfConstraints(constraints);

    }

    private void countTypesOfConstraints(Collection<Constraint<?>> constraints) {
        boolean hasIntegerConstraint = false;
        boolean hasRealConstraint = false;
        boolean hasStringConstraint = false;
        for (Constraint<?> constraint : constraints) {
            if (constraint instanceof StringConstraint) {
                hasStringConstraint = true;
            } else if (constraint instanceof IntegerConstraint) {
                hasIntegerConstraint = true;
            } else if (constraint instanceof RealConstraint) {
                hasRealConstraint = true;
            } else {
                throw new IllegalArgumentException(
                        "The constraint type " + constraint.getClass().getCanonicalName() + " is not considered!");
            }
        }
        constraintTypeCounter.addNewConstraint(hasIntegerConstraint, hasRealConstraint, hasStringConstraint);
    }

    /**
     * Reports a new solving time (use of a constraint solver)
     *
     * @param solvingTimeMillis
     */
    public void reportNewSolvingTime(long solvingTimeMillis) {
        totalSolvingTimeMillis += solvingTimeMillis;
    }

    /**
     * Reports a new test execution time (use of testCaseExecutor)
     *
     * @param testExecutionTimeMillis
     */
    public void reportNewTestExecutionTime(long testExecutionTimeMillis) {
        totalTestExecutionTime += testExecutionTimeMillis;
    }

    /**
     * Reports total test execution time (use of testCaseExecutor)
     * TestCaseExecutor is already incrementally saving execution time
     *
     * @param testExecutionTimeMillis
     */
    public void reportTotalTestExecutionTime(long testExecutionTimeMillis) {
        totalTestExecutionTime = testExecutionTimeMillis;
    }

    /**
     * Reports a new concolic execution time (use of instrumentation and path
     * constraint collection)
     *
     * @param concolicExecutionTimeMillis
     */
    public void reportNewConcolicExecutionTime(long concolicExecutionTimeMillis) {
        totalConcolicExecutionTimeMillis += concolicExecutionTimeMillis;
    }

    public void reportConstraintTooLong(int size) {
        constraintTooLongCounter++;
    }

    public void reportSolverError() {
        nrOfTimeouts++;
    }

    private long getTimeouts() {
        return nrOfTimeouts;
    }

    public void reportNewIncrease() {
        changes.add(true);
    }

    public void reportNewDecrease() {
        changes.add(false);
    }

    /**
     * Solver Query Cache related reports
     */
    public void reportNewQueryCacheHit() {
        queryCacheHits++;
    }

    public void reportNewQueryCacheCall() {
        queryCacheCalls++;
    }

    public void reportNewQueryCachedValue() {
        querycacheSize++;
    }

    /**
     * Entry point for statistics tracking on output variables
     */
    public void trackStatistics() {
        trackConstraintTypes();
        trackSolverStatistics();
        trackQueryCacheStatistics();
        trackExplorationStatistics();
        trackExecutionTimeStatistics();
    }

    /**
     * Sets the constraint types related output variables to be saved.
     */
    private void trackConstraintTypes() {
        int total = constraintTypeCounter.getTotalNumberOfConstraints();

        int integerOnly = constraintTypeCounter.getIntegerOnlyConstraints();
        int realOnly = constraintTypeCounter.getRealOnlyConstraints();
        int stringOnly = constraintTypeCounter.getStringOnlyConstraints();

        int integerRealOnly = constraintTypeCounter.getIntegerAndRealConstraints();
        int integerStringOnly = constraintTypeCounter.getIntegerAndStringConstraints();
        int realStringOnly = constraintTypeCounter.getRealAndStringConstraints();

        int integerRealStringConstraints = constraintTypeCounter.getIntegerRealAndStringConstraints();

        /** Specific tracking */
        trackOutputVariable(RuntimeVariable.IntegerOnlyConstraints, integerOnly);
        trackOutputVariable(RuntimeVariable.RealOnlyConstraints, realOnly);
        trackOutputVariable(RuntimeVariable.StringOnlyConstraints, stringOnly);
        trackOutputVariable(RuntimeVariable.IntegerAndRealConstraints, integerRealOnly);
        trackOutputVariable(RuntimeVariable.IntegerAndStringConstraints, integerStringOnly);
        trackOutputVariable(RuntimeVariable.RealAndStringConstraints, realStringOnly);
        trackOutputVariable(RuntimeVariable.IntegerRealAndStringConstraints, integerRealStringConstraints);
        trackOutputVariable(RuntimeVariable.TotalNumberOfConstraints, total);
    }

    /**
     * Sets the path exploration related output variables to be saved.
     */
    private void trackQueryCacheStatistics() {
        trackOutputVariable(RuntimeVariable.QueryCacheSize, querycacheSize);
        trackOutputVariable(RuntimeVariable.QueryCacheCalls, queryCacheCalls);
        trackOutputVariable(RuntimeVariable.QueryCacheHitRate, getQueryCacheHitRate(queryCacheHits, queryCacheCalls));
    }

    /**
     * Sets the path exploration related output variables to be saved.
     */
    private void trackExplorationStatistics() {
        /** Path conditions data */
        trackOutputVariable(RuntimeVariable.MaxPathConditionLength, maxPathConditionLength);
        trackOutputVariable(RuntimeVariable.MinPathConditionLength, minPathConditionLength);
        trackOutputVariable(RuntimeVariable.AvgPathConditionLength, avgPathConditionLength);

        /** Path exploration specific data */
        trackOutputVariable(RuntimeVariable.NumberOfPathsExplored, pathsExploredCounter);
        trackOutputVariable(RuntimeVariable.NumberOfPathsDiverged, pathDivergencesCounter);
    }

    /**
     * Sets the execution time related output variables to be saved.
     */
    private void trackExecutionTimeStatistics() {
        trackOutputVariable(RuntimeVariable.TotalTimeSpentSolvingConstraints, totalSolvingTimeMillis);
        trackOutputVariable(RuntimeVariable.TotalTimeSpentExecutingTestCases, totalTestExecutionTime);
        trackOutputVariable(RuntimeVariable.TotalTimeSpentExecutingConcolicaly, totalConcolicExecutionTimeMillis);
        trackOutputVariable(
                RuntimeVariable.TotalTimeSpentExecutingNonConcolicTestCases,
                totalTestExecutionTime - totalConcolicExecutionTimeMillis
        );
    }

    /**
     * Sets the smt solver related output variables to be saved.
     */
    private void trackSolverStatistics() {
        trackOutputVariable(RuntimeVariable.NumberOfSATQueries, getSAT());
        trackOutputVariable(RuntimeVariable.NumberOfUNSATQueries, getUNSAT());
        trackOutputVariable(RuntimeVariable.NumberOfTimeoutQueries, getTimeouts());
        trackOutputVariable(RuntimeVariable.NumberOfUsefulNewTests, getUsefulTests());
        trackOutputVariable(RuntimeVariable.NumberOfUnusefulNewTests, getUnusefulTests());

    }

    private void trackOutputVariable(RuntimeVariable var, Object value) {
        ClientServices.getInstance().getClientNode().trackOutputVariable(var, value);
    }


    /**
     * Calculates the cache hit rate.
     * If theres no calls to the cache, we simply return 0.
     *
     * @param cacheHits
     * @param cacheCalls
     * @return
     */
    private double getQueryCacheHitRate(long cacheHits, long cacheCalls) {
        if (cacheCalls == 0) {
            logger.info(NO_QUERY_CACHE_CALLS_WERE_MADE);
            return 0;
        }

        return (double) cacheHits / (double) cacheCalls;
    }

}
