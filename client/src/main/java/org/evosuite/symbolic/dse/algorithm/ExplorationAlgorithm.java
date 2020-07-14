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
package org.evosuite.symbolic.dse.algorithm;

import org.evosuite.Properties;
import org.evosuite.symbolic.dse.ConcolicExecutor;
import org.evosuite.symbolic.dse.DSEStatistics;
import org.evosuite.symbolic.dse.DSETestCase;
import org.evosuite.symbolic.dse.algorithm.strategies.KeepSearchingCriteriaStrategy;
import org.evosuite.symbolic.dse.algorithm.strategies.PathPruningStrategy;
import org.evosuite.symbolic.dse.algorithm.strategies.PathSelectionStrategy;
import org.evosuite.symbolic.dse.algorithm.strategies.TestCaseBuildingStrategy;
import org.evosuite.symbolic.dse.algorithm.strategies.TestCaseSelectionStrategy;
import org.evosuite.symbolic.dse.algorithm.strategies.implementations.KeepSearchingCriteriaStrategies.LastExecutionCreatedATestCaseStrategy;
import org.evosuite.symbolic.dse.algorithm.strategies.implementations.PathPruningStrategies.AlreadySeenSkipStrategy;
import org.evosuite.symbolic.dse.algorithm.strategies.implementations.PathSelectionStrategies.generationalGenerationStrategy;
import org.evosuite.symbolic.dse.algorithm.strategies.implementations.TestCaseBuildingStrategies.DefaultTestCaseBuildingStrategy;
import org.evosuite.symbolic.dse.algorithm.strategies.implementations.TestCaseSelectionStrategies.LastTestCaseSelectionStrategy;
import org.evosuite.symbolic.MethodComparator;
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
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.TestCaseUpdater;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.ClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

  /**
   * Structure of a DSE Exploration algorithm,
   *
   * Current implementation represents an abstracted version of SAGE's generational algorithm.
   *
   * For more details, please take a look at:
   *     Godefroid P., Levin Y. M. & Molnar D. (2008) Automated Whitebox Fuzz Testing
   *
   * @author Ignacio Lebrero
   */
public class ExplorationAlgorithm extends ExplorationAlgorithmBase {

    private static final transient Logger logger = LoggerFactory.getLogger(ExplorationAlgorithm.class);

    /**
     * Logger Messages
     **/

    // Solver
    public static final String SOLVER_ERROR_DEBUG_MESSAGE = "Solver threw an exception when running: {}";
    public static final String SOLVER_QUERY_STARTED_MESSAGE = "Solving query with {} constraints";
    public static final String SOLVER_SOLUTION_DEBUG_MESSAGE = "solver found solution {}";
    public static final String SOLVER_OUTCOTE_NULL_DEBUG_MESSAGE = "Solver outcome is null (probably failure/unknown/timeout)";
    public static final String SOLVER_OUTCOME_IS_SAT_DEBUG_MESSAGE = "query is SAT (solution found)";
    public static final String SOLVER_OUTCOME_IS_UNSAT_DEBUG_MESSAGE = "query is UNSAT (no solution found)";
    public static final String SOLVING_CURRENT_SMT_QUERY_DEBUG_MESSAGE = "* Solving current SMT query";

    // Concolic Engine
    public static final String FINISHED_CONCOLIC_EXECUTION_DEBUG_MESSAGE = "* Finished concolic execution.";
    public static final String EXECUTING_CONCOLICALLY_THE_CURRENT_TEST_CASE_DEBUG_MESSAGE = "* Executing concolically the current test case";

    // TestCase generation
    public static final String NEW_TEST_CASE_SCORE_DEBUG_MESSAGE = "New test case score: {}";
    public static final String NEW_TEST_CASE_CREATED_DEBUG_MESSAGE = "Created new test case from SAT solution: {}";

    // Algorithm
    public static final String PROGRESS_MSG_INFO = "Total progress: {}";
    public static final String ENTRY_POINTS_FOUND_DEBUG_MESSAGE = "Found {} as entry points for DSE";
    public static final String STOPPING_CONDITION_MET_DEBUG_MESSAGE = "A stoping condition was met. No more tests can be generated using DSE.";
    public static final String GENERATING_TESTS_FOR_ENTRY_DEBUG_MESSAGE = "Generating tests for entry method {}";
    public static final String TESTS_WERE_GENERATED_FOR_ENTRY_METHOD_DEBUG_MESSAGE = "{} tests were generated for entry method {}";

    /**
     * A cache of previous results from the constraint solver
     **/
    protected final transient Map<Set<Constraint<?>>, SolverResult> queryCache
            = new HashMap<Set<Constraint<?>>, SolverResult>();

    /**
     * Exploration strategies
     **/
    private final transient PathPruningStrategy pathPruningStrategy;
    private final transient PathSelectionStrategy pathSelectionStrategy;
    private final transient TestCaseBuildingStrategy testCaseBuildingStrategy;
    private final transient TestCaseSelectionStrategy testCaseSelectionStrategy;
    private final transient KeepSearchingCriteriaStrategy keepSearchingCriteriaStrategy;

    /**
     * Internal executor and solver
     **/
    private final transient ConcolicExecutor engine;
    private final transient Solver solver;

    public ExplorationAlgorithm() {
        this(
            SHOW_PROGRESS_DEFAULT_VALUE,
            DSEStatistics.getInstance(), //TODO: move this to a dependency injection schema
            new ConcolicExecutor(),
            SolverFactory.getInstance().buildNewSolver(),

            // Default Strategies
            new AlreadySeenSkipStrategy(),
            new LastExecutionCreatedATestCaseStrategy(),
            new generationalGenerationStrategy(),
            new DefaultTestCaseBuildingStrategy(),
            new LastTestCaseSelectionStrategy()
        );
    }

    public ExplorationAlgorithm(
        DSEStatistics statisticsLogger,
        boolean showProgress,
        PathPruningStrategy pathPruningStrategy,
        KeepSearchingCriteriaStrategy keepSearchingCriteriaStrategy,
        PathSelectionStrategy pathSelectionStrategy,
        TestCaseBuildingStrategy testCaseBuildingStrategy,
        TestCaseSelectionStrategy testCaseSelectionStrategy
    ) {
        this(
            showProgress,
            statisticsLogger,
            new ConcolicExecutor(),
            SolverFactory.getInstance().buildNewSolver(),
            pathPruningStrategy,
            keepSearchingCriteriaStrategy,
            pathSelectionStrategy,
            testCaseBuildingStrategy,
            testCaseSelectionStrategy
        );
    }

    public ExplorationAlgorithm(
            boolean showProgress,
            DSEStatistics dseStatistics,
            ConcolicExecutor engine,
            Solver solver,
            PathPruningStrategy pathPruningStrategy,
            KeepSearchingCriteriaStrategy keepSearchingCriteriaStrategy,
            PathSelectionStrategy pathSelectionStrategy,
            TestCaseBuildingStrategy testCaseBuildingStrategy,
            TestCaseSelectionStrategy testCaseSelectionStrategy
    ) {
        super(dseStatistics, showProgress);

        this.engine = engine;
        this.solver = solver;

        this.pathPruningStrategy = pathPruningStrategy;
        this.pathSelectionStrategy = pathSelectionStrategy;
        this.testCaseBuildingStrategy = testCaseBuildingStrategy;
        this.testCaseSelectionStrategy = testCaseSelectionStrategy;
        this.keepSearchingCriteriaStrategy = keepSearchingCriteriaStrategy;
    }

    /**
     * DSE algorithm
     *
     * @param method
     */
    @Override
    protected void runAlgorithm(Method method) {
        // Path divergence check
        boolean hasPathConditionDiverged;

        // Children cache
        HashSet<Set<Constraint<?>>> seenChildren = new HashSet();

        // WorkList
        PriorityQueue<DSETestCase> testCasesWorkList = new PriorityQueue<DSETestCase>();

        // Initial element
        DSETestCase initialTestCase = testCaseBuildingStrategy.buildInitialTestCase(method);

        // Run & check
        testCasesWorkList.add(initialTestCase);

        while (keepSearchingCriteriaStrategy.ShouldKeepSearching(testCasesWorkList)) {
            // This gets wrapped into the building and fitness strategy selected due to the PriorityQueue sorting nature
            DSETestCase currentTestCase = testCaseSelectionStrategy.getCurrentIterationBasedTestCase(testCasesWorkList);

            // NOTE: We consider Adding a testCase an iteration
            addNewTestCaseToTestSuite(currentTestCase);
            notifyIteration();

            // After iteration checks and logs
            if (showProgress) logger.info(PROGRESS_MSG_INFO, getProgress());
            if (isFinished()) return;

            // Runs the current test case
            DSEPathCondition currentExecutedPathCondition = executeTestCaseConcolically(currentTestCase);
            seenChildren.add(
                 normalize(
                     currentExecutedPathCondition.getPathCondition().getConstraints()));

            // Checks for a divergence
            hasPathConditionDiverged = checkPathConditionDivergence(
                currentExecutedPathCondition.getPathCondition(),
                currentTestCase.getOriginalPathCondition().getPathCondition()
            );

            // Generates the children
            List<DSEPathCondition> children = pathSelectionStrategy.generateChildren(currentExecutedPathCondition);

            processChildren(seenChildren, testCasesWorkList, currentTestCase, children, hasPathConditionDiverged);
        }
    }

    private void processChildren(HashSet<Set<Constraint<?>>> seenChildren, PriorityQueue<DSETestCase> testCasesWorkList, DSETestCase currentTestCase, List<DSEPathCondition> children, boolean hasPathConditionDiverged) {
        // We look at all the children
        for (DSEPathCondition child : children) {
            List<Constraint<?>> childQuery = SolverUtils.buildQuery(child.getPathCondition());
            Set<Constraint<?>> normalizedChildQuery = normalize(childQuery);

            if (!pathPruningStrategy.shouldSkipCurrentPath(seenChildren, normalizedChildQuery, queryCache)) {

                // Logs query data
                statisticsLogger.reportNewConstraints(childQuery);

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
                    DSETestCase newTestCase = generateNewTestCase(
                        currentTestCase,
                        child,
                        smtSolution,
                        hasPathConditionDiverged);

                    testCasesWorkList.offer(newTestCase);
                }
            }
        }
    }

    /**
     * Generates a new test case from the concolic execution data.
     *
     * @param currentConcreteTest
     * @param currentPathCondition
     * @param smtSolution
     * @param hasPathConditionDiverged
     * @return
     */
    private DSETestCase generateNewTestCase(DSETestCase currentConcreteTest, DSEPathCondition currentPathCondition, Map<String, Object> smtSolution, boolean hasPathConditionDiverged) {
        TestCase newTestCase = TestCaseUpdater.updateTest(currentConcreteTest.getTestCase(), smtSolution);

        DSETestCase newDSETestCase = new DSETestCase(
            newTestCase,
            currentPathCondition,
            getTestScore(newTestCase, hasPathConditionDiverged)
        );

        logger.debug(NEW_TEST_CASE_CREATED_DEBUG_MESSAGE, newDSETestCase.getTestCase().toCode());
        logger.debug(NEW_TEST_CASE_SCORE_DEBUG_MESSAGE, newDSETestCase.getScore());

        return newDSETestCase;
    }

    /**
     * Analyzes the results of an smtQuery.
     *
     * @param query
     * @param smtQueryResult
     * @return
     */
    private Map<String, Object> getQuerySolution(Set<Constraint<?>> query, SolverResult smtQueryResult) {
         Map<String, Object> solution = null;

        if (smtQueryResult == null) {
            logger.debug(SOLVER_OUTCOTE_NULL_DEBUG_MESSAGE);

            // This doesn't necessarily is a timeout, but we model it this way
            statisticsLogger.reportSolverError();
        } else {
            queryCache.put(query, smtQueryResult);
            statisticsLogger.reportNewQueryCachedValue();

            if (smtQueryResult.isSAT()) {
                logger.debug(SOLVER_OUTCOME_IS_SAT_DEBUG_MESSAGE);
                statisticsLogger.reportNewSAT();

                solution = smtQueryResult.getModel();
                logger.debug(SOLVER_SOLUTION_DEBUG_MESSAGE, solution.toString());
            } else {
                assert (smtQueryResult.isUNSAT());
                statisticsLogger.reportNewUNSAT();
                logger.debug(SOLVER_OUTCOME_IS_UNSAT_DEBUG_MESSAGE);
            }
        }

        return solution;
    }

    /**
     * Generates a solution for a given class
     *
     * @return
     */
     public TestSuiteChromosome explore() {
         notifyGenerationStarted();
         final Class<?> targetClass = Properties.getTargetClassAndDontInitialise();

         List<Method> targetStaticMethods = ClassUtil.getTargetClassStaticMethods(targetClass);
         Collections.sort(targetStaticMethods, new MethodComparator());
         logger.debug(ENTRY_POINTS_FOUND_DEBUG_MESSAGE, targetStaticMethods.size());

         for (Method entryMethod : targetStaticMethods) {
             if (this.isFinished()) {
                  logger.debug(STOPPING_CONDITION_MET_DEBUG_MESSAGE);
                  break;
             }

             logger.debug(GENERATING_TESTS_FOR_ENTRY_DEBUG_MESSAGE, entryMethod.getName());
             int testCaseCount = testSuite.getTests().size();
             runAlgorithm(entryMethod);
             int numOfGeneratedTestCases = testSuite.getTests().size() - testCaseCount;
             logger.debug(TESTS_WERE_GENERATED_FOR_ENTRY_METHOD_DEBUG_MESSAGE, numOfGeneratedTestCases, entryMethod.getName());
         }

         // Run this before finish
         notifyGenerationFinished();
         statisticsLogger.reportTotalTestExecutionTime(TestCaseExecutor.timeExecuted);
         statisticsLogger.logStatistics();
         return testSuite;
     }

    /**
     * Solves an SMT query
     *
     * TODO: check if moving the time estimation to a lower level layer improves precision.
     *
     * @param SMTQuery
     * @return
     */
    private SolverResult solveQuery(List<Constraint<?>> SMTQuery) {
        long startSolvingTime;
        long estimatedSolvingTime;
        SolverResult smtQueryResult;

			  logger.debug(SOLVING_CURRENT_SMT_QUERY_DEBUG_MESSAGE);

			  /** Track solving time and solve the query **/
        startSolvingTime = System.currentTimeMillis();
        smtQueryResult = doSolveQuery(SMTQuery);
        estimatedSolvingTime = System.currentTimeMillis() - startSolvingTime;
			  DSEStatistics.getInstance().reportNewSolvingTime(estimatedSolvingTime);

        return smtQueryResult;
    }

    private SolverResult doSolveQuery(List<Constraint<?>> SMTQuery) {
        SolverResult smtQueryResult = null;

        try {
            smtQueryResult = solver.solve(SMTQuery);
        } catch (SolverTimeoutException
                | SolverParseException
                | SolverEmptyQueryException
                | SolverErrorException
                | IOException e) {
            logger.debug(SOLVER_ERROR_DEBUG_MESSAGE, e.getMessage());
        }
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
     * Executes concolically the current TestCase
     *
     * @param currentTestCase
     * @return
     */
    private DSEPathCondition executeTestCaseConcolically(DSETestCase currentTestCase) {
        logger.debug(EXECUTING_CONCOLICALLY_THE_CURRENT_TEST_CASE_DEBUG_MESSAGE);

        TestCase clonedCurrentTestCase = currentTestCase.getTestCase().clone();
        PathCondition result = engine.execute((DefaultTestCase) clonedCurrentTestCase);

        // In case of a divergence, we need to keep the lowest value
        int currentGeneratedFromIndex = Math.min(
          currentTestCase.getOriginalPathCondition().getGeneratedFromIndex(),
          result.getPathConditionNodes().size()
        );

        logger.debug(FINISHED_CONCOLIC_EXECUTION_DEBUG_MESSAGE);

        return new DSEPathCondition(result, currentGeneratedFromIndex);
    }
}