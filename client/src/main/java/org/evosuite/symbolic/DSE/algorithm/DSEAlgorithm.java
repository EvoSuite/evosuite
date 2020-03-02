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
import org.evosuite.symbolic.DSE.DSETestGenerator;
import org.evosuite.symbolic.DSE.algorithm.strategies.TestCaseBuildingStrategy;
import org.evosuite.symbolic.DSE.algorithm.strategies.TestCaseSelectionStrategy;
import org.evosuite.symbolic.DSE.algorithm.strategies.PathSelectionStrategy;
import org.evosuite.symbolic.DSE.algorithm.strategies.KeepSearchingCriteriaStrategy;
import org.evosuite.symbolic.DSE.algorithm.strategies.PathPruningStrategy;
import org.evosuite.symbolic.DSE.algorithm.strategies.implementations.KeepSearchingCriteriaStrategies.LastExecutionCreatedATestCaseStrategy;
import org.evosuite.symbolic.DSE.algorithm.strategies.implementations.PathPruningStrategies.AlreadySeenSkipStrategy;
import org.evosuite.symbolic.DSE.algorithm.strategies.implementations.PathSelectionStrategies.NegateLastConditionStrategy;
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
import org.evosuite.testsuite.TestSuiteChromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
            new ConcolicEngine(),
            SolverFactory.getInstance().buildNewSolver(),

            // Default Strategies
            new AlreadySeenSkipStrategy(),
            new LastExecutionCreatedATestCaseStrategy(),
            new NegateLastConditionStrategy(),
            new DefaultTestCaseBuildingStrategy(),
            new LastTestCaseSelectionStrategy()
        );
    }

    public DSEAlgorithm(
            ConcolicEngine engine,
            Solver solver,
            PathPruningStrategy pathPruningStrategy,
            KeepSearchingCriteriaStrategy keepSearchingCriteriaStrategy,
            PathSelectionStrategy pathSelectionStrategy,
            TestCaseBuildingStrategy testCaseBuildingStrategy,
            TestCaseSelectionStrategy testCaseSelectionStrategy
    ) {
        this.engine = engine;
        this.solver = solver;

        this.pathPruningStrategy = pathPruningStrategy;
        this.pathSelectionStrategy = pathSelectionStrategy;
        this.testCaseBuildingStrategy = testCaseBuildingStrategy;
        this.testCaseSelectionStrategy = testCaseSelectionStrategy;
        this.keepSearchingCriteriaStrategy = keepSearchingCriteriaStrategy;
    }

    // TODO: only static methods for, will extend later on
    public void algorithm(Method method) {
        List<TestCase> generatedTests = new ArrayList();

        // Build initial testCase input
        generatedTests.add(testCaseBuildingStrategy.buildInitialTestCase(method));

        HashSet<Set<Constraint<?>>> alreadyGeneratedPathConditions = new HashSet();
        while (keepSearchingCriteriaStrategy.ShouldKeepSearching(generatedTests)) {

            // Do a concolic execution on an arbitrary test case
            TestCase currentConcreteTest = testCaseSelectionStrategy.getCurrentIterationBasedTestCase(generatedTests).clone();
            final PathCondition currentPathCondition = engine.execute((DefaultTestCase) currentConcreteTest);

            // Next path condition generation
            List<Constraint<?>> query = pathSelectionStrategy.getNextPathConstraints(currentPathCondition);
            Set<Constraint<?>> normalizedQueryConstraints = normalize(query);
            alreadyGeneratedPathConditions.add(normalizedQueryConstraints);

            if (!pathPruningStrategy.shouldSkipCurrentPath(alreadyGeneratedPathConditions, normalizedQueryConstraints, queryCache)) {
                // TODO: check if this is really neded, probable the SMT language is asking for this.
                query.addAll(
                    SolverUtils.createBoundsForQueryVariables(query)
                );

                logger.debug(SOLVER_QUERY_STARTED_MESSAGE, query.size());
                SolverResult smtQueryResult = solveQuery(query);
                analizeResults(normalizedQueryConstraints, smtQueryResult, generatedTests, currentConcreteTest);
            }
        }
    }


    /**
     * Analyzes the results of an smtQuery and appends to the tests cases if needed
     *  @param query
     * @param smtQueryResult
     * @param generatedTests
     * @param currentConcreteTest
     */
    private void analizeResults(Set<Constraint<?>> query, SolverResult smtQueryResult, List<TestCase> generatedTests, TestCase currentConcreteTest) {
        if (smtQueryResult == null) {
            logger.debug("Solver outcome is null (probably failure/unknown/timeout)");
        } else {
            queryCache.put(query, smtQueryResult);

            if (smtQueryResult.isSAT()) {
                logger.debug("query is SAT (solution found)");
                Map<String, Object> solution = smtQueryResult.getModel();
                logger.debug("solver found solution " + solution.toString());

                TestCase newTest = DSETestGenerator.updateTest(currentConcreteTest, solution);
                logger.debug("Created new test case from SAT solution:" + newTest.toCode());
                generatedTests.add(newTest);

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

            } else {
                assert (smtQueryResult.isUNSAT());

                // Special value for unsat queries
                // queryCache.put(query, null);
                logger.debug("query is UNSAT (no solution found)");
            }
        }
    }

    /**
     * Generates a solution for a given class
     *
     * @return
     */
     public TestSuiteChromosome generateSolution() {
         // TODO: completar
         return null;
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
}
