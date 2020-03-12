/**
 * Copyright (C) 2010-2020 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.symbolic.DSE.algorithm;

import org.evosuite.symbolic.DSE.ConcolicEngine;
import org.evosuite.symbolic.DSE.DSEStatistics;
import org.evosuite.symbolic.DSE.DSETestCase;
import org.evosuite.symbolic.DSE.DSETestGenerator;
import org.evosuite.symbolic.DSE.algorithm.strategies.KeepSearchingCriteriaStrategy;
import org.evosuite.symbolic.DSE.algorithm.strategies.PathPruningStrategy;
import org.evosuite.symbolic.DSE.algorithm.strategies.PathSelectionStrategy;
import org.evosuite.symbolic.DSE.algorithm.strategies.TestCaseBuildingStrategy;
import org.evosuite.symbolic.DSE.algorithm.strategies.TestCaseSelectionStrategy;
import org.evosuite.symbolic.DSE.algorithm.strategies.implementations.KeepSearchingCriteriaStrategies.LastExecutionCreatedATestCaseStrategy;
import org.evosuite.symbolic.DSE.algorithm.strategies.implementations.PathPruningStrategies.AlreadySeenSkipStrategy;
import org.evosuite.symbolic.DSE.algorithm.strategies.implementations.PathSelectionStrategies.generationalGenerationStrategy;
import org.evosuite.symbolic.DSE.algorithm.strategies.implementations.TestCaseBuildingStrategies.DefaultTestCaseBuildingStrategy;
import org.evosuite.symbolic.DSE.algorithm.strategies.implementations.TestCaseSelectionStrategies.LastTestCaseSelectionStrategy;
import org.evosuite.symbolic.PathCondition;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.solver.Solver;
import org.evosuite.symbolic.solver.SolverEmptyQueryException;
import org.evosuite.symbolic.solver.SolverErrorException;
import org.evosuite.symbolic.solver.SolverFactory;
import org.evosuite.symbolic.solver.SolverParseException;
import org.evosuite.symbolic.solver.SolverResult;
import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.evosuite.symbolic.solver.SolverUtils;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
   * General Structure of a DSE Algorithm
   *
   * @author ilebrero
   */
public class DSEAlgorithm extends DSEBaseAlgorithm {

    private static final Logger logger = LoggerFactory.getLogger(DSEAlgorithm.class);

    private static final String SOLVER_ERROR_DEBUG_MESSAGE = "Solver threw an exception when running: {}";
    private static final String SOLVER_QUERY_STARTED_MESSAGE = "Solving query with {} constraints";

    /**
     * A cache of previous results from the constraint solver
     * */
    protected final Map<Set<Constraint<?>>, SolverResult> queryCache
            = new HashMap<Set<Constraint<?>>, SolverResult>();

    /**
     * DSE Algorithm strategies
     * */
    private final PathPruningStrategy pathPruningStrategy;
    private final PathSelectionStrategy pathSelectionStrategy;
    private final TestCaseBuildingStrategy testCaseBuildingStrategy;
    private final TestCaseSelectionStrategy testCaseSelectionStrategy;
    private final KeepSearchingCriteriaStrategy keepSearchingCriteriaStrategy;

    /**
     * Internal Symbolic engine and Solver
     * */
    private final ConcolicEngine engine;
    private final Solver solver;

    public DSEAlgorithm() {
        this(
            DSEStatistics.getInstance(), //TODO: move this to a dependency injection schema
            new ConcolicEngine(),
            SolverFactory.getInstance().buildNewSolver(),

            // Default Strategies
            new AlreadySeenSkipStrategy(),
            new LastExecutionCreatedATestCaseStrategy(),
            new generationalGenerationStrategy(),
            new DefaultTestCaseBuildingStrategy(),
            new LastTestCaseSelectionStrategy()
        );
    }

    public DSEAlgorithm(
            PathPruningStrategy pathPruningStrategy,
            KeepSearchingCriteriaStrategy keepSearchingCriteriaStrategy,
            PathSelectionStrategy pathSelectionStrategy,
            TestCaseBuildingStrategy testCaseBuildingStrategy,
            TestCaseSelectionStrategy testCaseSelectionStrategy
    ) {
        this(
                DSEStatistics.getInstance(),
                new ConcolicEngine(),
                SolverFactory.getInstance().buildNewSolver(),
                pathPruningStrategy,
                keepSearchingCriteriaStrategy,
                pathSelectionStrategy,
                testCaseBuildingStrategy,
                testCaseSelectionStrategy
        );
    }

    public DSEAlgorithm(
            DSEStatistics dseStatistics,
            ConcolicEngine engine,
            Solver solver,
            PathPruningStrategy pathPruningStrategy,
            KeepSearchingCriteriaStrategy keepSearchingCriteriaStrategy,
            PathSelectionStrategy pathSelectionStrategy,
            TestCaseBuildingStrategy testCaseBuildingStrategy,
            TestCaseSelectionStrategy testCaseSelectionStrategy
    ) {
        super(dseStatistics);

        this.engine = engine;
        this.solver = solver;

        this.pathPruningStrategy = pathPruningStrategy;
        this.pathSelectionStrategy = pathSelectionStrategy;
        this.testCaseBuildingStrategy = testCaseBuildingStrategy;
        this.testCaseSelectionStrategy = testCaseSelectionStrategy;
        this.keepSearchingCriteriaStrategy = keepSearchingCriteriaStrategy;
    }

    /**
     * Current implementation represents the high level algorithm of SAGE without the running&checking section
     * since our goal is just to generate the test suite.
     *
     * For more details, please take a look at:
     *     Godefroid P., Levin Y. M. & Molnar D. (2008) Automated Whitebox Fuzz Testing
     *
     * @param method
     */
    protected void DSEalgorithm(Method method) {
        // Children cache
        HashSet<Set<Constraint<?>>> seenChildren = new HashSet();

        // WorkList
        PriorityQueue<DSETestCase> testCasesWorkList = new PriorityQueue();

        // Initial element
        DSETestCase initialTestCase = testCaseBuildingStrategy.buildInitialTestCase(method);

        // Run & check
        testCasesWorkList.add(initialTestCase);

        while (keepSearchingCriteriaStrategy.ShouldKeepSearching(testCasesWorkList)) {
            // This gets wrapped into the building and fitness strategy selected due to the PriorityQueue sorting nature
            DSETestCase currentTestCase = testCaseSelectionStrategy.getCurrentIterationBasedTestCase(testCasesWorkList).clone();

            addNewTestCaseToTestSuite(currentTestCase);
            if (isFinished()) return;

            // Runs the current test case
            DSEPathCondition currentExecutedPathCondition = executeConcolicEngine(currentTestCase);

            // Checks for a divergence
            checkPathConditionDivergence(
                    currentExecutedPathCondition.getPathCondition(),
                    currentTestCase.getOriginalPathCondition().getPathCondition()
            );

            // Generates the children
            List<DSEPathCondition> children = pathSelectionStrategy.generateChildren(currentExecutedPathCondition);

            processChildren(seenChildren, testCasesWorkList, currentTestCase, children);

            notifyIteration();
        }
    }

    private void processChildren(HashSet<Set<Constraint<?>>> seenChildren, PriorityQueue<DSETestCase> testCasesWorkList, DSETestCase currentTestCase, List<DSEPathCondition> children) {
        // We look at all the children
        for (DSEPathCondition child : children) {
            List<Constraint<?>> childQuery = SolverUtils.buildQuery(child.getPathCondition());
            Set<Constraint<?>> normalizedChildQuery = normalize(childQuery);
            seenChildren.add(normalizedChildQuery);

            if (!pathPruningStrategy.shouldSkipCurrentPath(seenChildren, normalizedChildQuery, queryCache)) {

                // Post-processing stuff
                childQuery.addAll(
                    SolverUtils.createBoundsForQueryVariables(childQuery)
                );

                // Solves the SMT query
                logger.debug(SOLVER_QUERY_STARTED_MESSAGE, childQuery.size());
                SolverResult smtQueryResult = solveQuery(childQuery);
                Map<String, Object> smtSolution = getQuerySolution(
                    normalizedChildQuery,
                    smtQueryResult
                );

                if (smtSolution != null) {
                    // Generates the new tests based on the current solution
                    DSETestCase newTestCase = generateNewTestCase(currentTestCase, child, smtSolution);
                    testCasesWorkList.add(newTestCase);
                }
            }
        }
    }

    private DSETestCase generateNewTestCase(DSETestCase currentConcreteTest, DSEPathCondition currentPathCondition, Map<String, Object> smtSolution) {
        TestCase newTestCase = DSETestGenerator.updateTest(currentConcreteTest.getTestCase(), smtSolution);

        DSETestCase newDSETestCase =  new DSETestCase(
            newTestCase,
            currentPathCondition,
            getTestScore(newTestCase)
        );

        logger.debug("Created new test case from SAT solution: {}", newDSETestCase.getTestCase().toCode());
        logger.debug("New test case score: {}", newDSETestCase.getScore());

        return newDSETestCase;
    }

    /**
     * Analyzes the results of an smtQuery
     *
     * @param query
     * @param smtQueryResult
     * @return
     */
    private Map<String, Object> getQuerySolution(Set<Constraint<?>> query, SolverResult smtQueryResult) {
         Map<String, Object> solution = null;

        if (smtQueryResult == null) {
            logger.debug("Solver outcome is null (probably failure/unknown/timeout)");
        } else {
            queryCache.put(query, smtQueryResult);

            if (smtQueryResult.isSAT()) {
                logger.debug("query is SAT (solution found)");
                solution = smtQueryResult.getModel();
                logger.debug("solver found solution {}", solution.toString());


            } else {
                assert (smtQueryResult.isUNSAT());
                logger.debug("query is UNSAT (no solution found)");
            }
        }

        return solution;
    }

    /**
     * Generates a solution for a given class
     *
     * @return
     */
     public TestSuiteChromosome generateSolution() {
         notifyGenerationStarted();
//       Method a  = new Method();
//       testSuite.addTests(transformResultToChromosome(runDSEAlgorithm(a)));

         // Post-process work
         // TODO: complete, here we can have optimizations like testSuite redundancy reduction.

         // Run this before finish
         notifyGenerationFinished();
         statisticsLogger.logStatistics();
         return testSuite;
     }

    /**
     * Solves an SMT query
     *
     * @param SMTQuery
     * @return
     */
    private SolverResult solveQuery(List<Constraint<?>> SMTQuery) {
        SolverResult smtQueryResult = null;

        logger.debug("* Solving current SMT query");
        try {
            smtQueryResult = solver.solve(SMTQuery);
        } catch (SolverTimeoutException
                | SolverParseException
                | SolverEmptyQueryException
                | SolverErrorException
                | IOException e) {
            logger.debug(SOLVER_ERROR_DEBUG_MESSAGE, e.getMessage());
        }

        logger.debug("* Finished solving current SMT query with result isSat: {}", smtQueryResult.isSAT());
        return smtQueryResult;
    }

    /**
     * Normalizes the query
     *
     * @param query
     * @return
     */
     private Set<Constraint<?>> normalize(List<Constraint<?>> query) {
        return new HashSet<Constraint<?>>(query);
     }

    /**
     * Executes concolicaly the current TestCase
     *
     * @param currentTestCase
     * @return
     */
    private DSEPathCondition executeConcolicEngine(DSETestCase currentTestCase) {
        logger.debug("* Executing concolically the current test case");

        PathCondition result = engine.execute((DefaultTestCase) currentTestCase.getTestCase());
        int currentGeneratedFromIndex = currentTestCase.getOriginalPathCondition().getGeneratedFromIndex();

        logger.debug("* Finished concolic execution.");

        return new DSEPathCondition(result, currentGeneratedFromIndex);
    }
}
