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
     * Symbolic algorithm general schema.
     *
     * Current implementation represents the high level algorithm of SAGE without the running&checking section
     * since our goal is just to generate the test suite.
     *
     * For more details, please take a look at:
     *     Godefroid P., Levin Y. M. & Molnar D. (2008) Automated Whitebox Fuzz Testing
     *
     * @param method
     */
    private List<DSETestCase> runDSEAlgorithm(Method method) {
        // Result tests
        List<DSETestCase> resultTestCases = new ArrayList();

        // Children cache
        HashSet<Set<Constraint<?>>> alreadyGeneratedChildren = new HashSet();

        // WorkList
        PriorityQueue<DSETestCase> generatedTests = new PriorityQueue();

        // Initial element
        DSETestCase initialTestCase = testCaseBuildingStrategy.buildInitialTestCase(method);

        // Run & check
        generatedTests.add(initialTestCase);
        resultTestCases.add(initialTestCase);

        while (keepSearchingCriteriaStrategy.ShouldKeepSearching(generatedTests)) {
            // This gets wrapped into the building and fitness strategy selected due to the PriorityQueue sorting nature
            DSETestCase currentTestCase = testCaseSelectionStrategy.getCurrentIterationBasedTestCase(generatedTests).clone();

            // Runs the current test case
            DSEPathCondition currentExecutedPathCondition = executeConcolicEngine(currentTestCase);

            // Checks for a divergence
            checkPathConditionDivergence(
                    currentExecutedPathCondition.getPathCondition(),
                    currentTestCase.getOriginalPathCondition().getPathCondition()
            );

            // Generates the children
            List<DSEPathCondition> children = pathSelectionStrategy.generateChildren(currentExecutedPathCondition);

            // We look at all the children
            for (DSEPathCondition child : children) {
                List<Constraint<?>> childQuery = SolverUtils.buildQuery(child.getPathCondition());
                Set<Constraint<?>> normalizedChildQuery = normalize(childQuery);
                alreadyGeneratedChildren.add(normalizedChildQuery);

                // Almost equivalent to a < 0 score, except we are not running these ones on the queue
                if (!pathPruningStrategy.shouldSkipCurrentPath(alreadyGeneratedChildren, normalizedChildQuery, queryCache)) {

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

                        generatedTests.add(newTestCase);
                        resultTestCases.add(newTestCase);
                    }
                }
            }
        }

        return resultTestCases;
    }

    private DSETestCase generateNewTestCase(DSETestCase currentConcreteTest, DSEPathCondition currentPathCondition, Map<String, Object> smtSolution) {
        DSETestCase newTestCase =  new DSETestCase(
            DSETestGenerator.updateTest(currentConcreteTest.getTestCase(), smtSolution),
            currentPathCondition,
            0 // TODO: implement the score section
        );

        logger.debug("Created new test case from SAT solution: {}", newTestCase.getTestCase().toCode());
        logger.debug("New test case score: {}", newTestCase.getScore());

        //          TODO: re implement this part
        //          double fitnessBeforeAddingNewTest = this.getBestIndividual().getFitness();
        //          logger.debug("Fitness before adding new test" + fitnessBeforeAddingNewTest);
        //          getBestIndividual().addTest(newTest);
        //          calculateFitness(getBestIndividual());
        //
        //          double fitnessAfterAddingNewTest = this.getBestIndividual().getFitness();
        //          logger.debug("Fitness after adding new test " + fitnessAfterAddingNewTest);
        //          this.notifyIteration();
        //
        //          if (fitnessAfterAddingNewTest == 0) {
        //            logger.debug("No more DSE test generation since fitness is 0");
        //            return;
        //          }

        return newTestCase;
    }

    /**
     * Analyzes the results of an smtQuery and appends to the tests cases if needed
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

                // Special value for unsat queries
                // queryCache.put(query, null);
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
         TestSuiteChromosome testSuite = new TestSuiteChromosome();

//       Method a  = new Method();
//       testSuite.addTests(transformResultToChromosome(runDSEAlgorithm(a)));

         // Post-process work
         // TODO: complete, here we can have optimizations like testSuite redundancy reduction.

         // Run this before finish
         statisticsLogger.logStatistics();
         return testSuite;
     }

    /**
     * wrapping method till migrate all the GA parts of the algorithm.
     *
     * @param testCaseResults
     * @return
     */
    private Collection<TestChromosome> transformResultToChromosome(List<DSETestCase> testCaseResults) {
        List<TestChromosome> res = new ArrayList<>();


        for (DSETestCase t : testCaseResults) {
            TestChromosome c = new TestChromosome();
            c.setTestCase(t.getTestCase());
            res.add(c);
        }

        return res;
    }

    /**
     * Solves an SMT query
     *
     * @param SMTQuery
     * @return
     */
    private SolverResult solveQuery(List<Constraint<?>> SMTQuery) {
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
     * Executes concolicaly the current TestCase
     *
     * @param currentTestCase
     * @return
     */
    private DSEPathCondition executeConcolicEngine(DSETestCase currentTestCase) {
        PathCondition result = engine.execute((DefaultTestCase) currentTestCase.getTestCase());
        int currentGeneratedFromIndex = currentTestCase.getOriginalPathCondition().getGeneratedFromIndex();

        return new DSEPathCondition(result, currentGeneratedFromIndex);
    }
}
