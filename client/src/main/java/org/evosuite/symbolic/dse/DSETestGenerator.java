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
import org.evosuite.ga.localsearch.LocalSearchBudget;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.symbolic.BranchCondition;
import org.evosuite.symbolic.PathCondition;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.symbolic.solver.SolverResult;
import org.evosuite.symbolic.solver.SolverUtils;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseUpdater;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Attempts to create a new test case by applying DSE. The algorithm
 * systematically negates all uncovered branches trying to satisfy the missing
 * branches.
 *
 * @author galeotti
 */
public class DSETestGenerator {

    private final TestSuiteChromosome suite;

    /**
     * Creates a new test generator with no suite. Only the test case will be used
     */
    public DSETestGenerator() {
        this(null);
    }

    /**
     * Creates a new test generator using a test suite. The test case will be added
     * to the test suite.
     *
     * @param suite
     */
    public DSETestGenerator(TestSuiteChromosome suite) {
        this.suite = suite;
    }

    private static final Logger logger = LoggerFactory.getLogger(DSETestGenerator.class);

    /**
     * Applies DSE to the passed test using as symbolic variables only those that
     * are declared in the set of statement indexes. The objective is used to detect
     * if the DSE has improved the fitness.
     *
     * @param test             the test case to be used as parameterised unit test
     * @param statementIndexes a set with statement indexes with primitive value declarations
     *                         that can be used as symbolic variables. This set must be
     *                         non-empty.
     * @param objective        the local search objective to measure fitness improvement.
     */
    public TestChromosome generateNewTest(final TestChromosome test, Set<Integer> statementIndexes,
                                          LocalSearchObjective<TestChromosome> objective) {

        logger.info("APPLYING DSE");
        logger.info(test.getTestCase().toCode());
        logger.info("Starting concolic execution");
        // Backup copy
        // test.getMutationHistory().clear();
        test.clone(); // I am not sure what is the purpose of this

        DefaultTestCase clone_test_case = (DefaultTestCase) test.getTestCase().clone();
        final PathCondition collectedPathCondition = new ConcolicExecutorImpl().execute(clone_test_case);

        logger.info("Done concolic execution");

        if (collectedPathCondition.isEmpty()) {
            return null;
        }

        for (BranchCondition c : collectedPathCondition.getBranchConditions()) {
            logger.info(" -> " + c.getConstraint());
        }

        Set<VariableReference> symbolicVariables = new HashSet<>();
        for (Integer position : statementIndexes) {
            final VariableReference variableReference = test.getTestCase().getStatement(position).getReturnValue();
            symbolicVariables.add(variableReference);
        }

        logger.info("Checking {} conditions", collectedPathCondition.size());

        List<Integer> conditionIndexesNotCoveredTwoWays = computeConditionIndexesNotCoveredTwoWays(test,
                collectedPathCondition);

        //
        for (int conditionIndex = 0; conditionIndex < collectedPathCondition.size(); conditionIndex++) {
            BranchCondition condition = collectedPathCondition.get(conditionIndex);

            if (LocalSearchBudget.getInstance().isFinished()) {
                logger.debug("Local search budget used up: " + Properties.LOCAL_SEARCH_BUDGET_TYPE);
                break;
            }
            logger.debug("Local search budget not yet used up");

            if (!conditionIndexesNotCoveredTwoWays.contains(conditionIndex)) {
                // skip branches covered two ways
                continue;
            }

            logger.info("Current condition: " + conditionIndex + "/" + collectedPathCondition.size() + ": "
                    + condition.getConstraint());
            // Determine if this a branch condition depending on the target
            // statement
            Constraint<?> currentConstraint = condition.getConstraint();

            if (!isRelevant(currentConstraint, symbolicVariables)) {
                // if(!isRelevant(currentConstraint, test.getTestCase(),
                // statement)) {
                logger.info("Is not relevant for " + symbolicVariables);
                continue;
            }
            logger.info("Is relevant for " + symbolicVariables);

            List<Constraint<?>> query = SolverUtils.buildQueryNegatingIthCondition(collectedPathCondition, conditionIndex);

            logger.info("Trying to solve: ");
            for (Constraint<?> c : query) {
                logger.info("  " + c);
            }

            DSEStatistics.getInstance().reportNewConstraints(query);

            long startSolvingTime = System.currentTimeMillis();

            // Get solution
            SolverResult solverResult = SolverUtils.solveQuery(query);

            long estimatedSolvingTime = System.currentTimeMillis() - startSolvingTime;
            DSEStatistics.getInstance().reportNewSolvingTime(estimatedSolvingTime);

            if (solverResult == null) {
                logger.info("Found no result");

            } else if (solverResult.isUNSAT()) {
                logger.info("Found UNSAT result");
                DSEStatistics.getInstance().reportNewUNSAT();
            } else {
                logger.info("Found SAT result");
                DSEStatistics.getInstance().reportNewSAT();
                Map<String, Object> model = solverResult.getModel();
                TestCase oldTest = test.getTestCase();
                ExecutionResult oldResult = test.getLastExecutionResult().clone();
                TestCase newTest = TestCaseUpdater.updateTest(oldTest, model);
                logger.info("New test: " + newTest.toCode());
                test.setTestCase(newTest);
                // test.clearCachedMutationResults(); // TODO Mutation
                test.clearCachedResults();

                if (objective.hasImproved(test)) {
                    DSEStatistics.getInstance().reportNewTestUseful();
                    logger.info("Solution improves fitness, finishing DSE");
                    /* new test was created */
                    return test;
                } else {
                    DSEStatistics.getInstance().reportNewTestUnuseful();
                    test.setTestCase(oldTest);
                    // FIXXME: How can this be null?
                    if (oldResult != null)
                        test.setLastExecutionResult(oldResult);
                    // TODO Mutation
                }
            }
        }
        /* no new test was created */
        return null;
    }

    /**
     * Compute the set of branch conditions in the path condition that are not
     * covered two ways. If the test case belongs to a whole test suite, the
     * coverage of the whole test suite is used, otherwise, only the coverage of the
     * single test case.
     *
     * @param test                   the original test case
     * @param collectedPathCondition a path condition obtained from concolic execution
     * @return
     */
    private List<Integer> computeConditionIndexesNotCoveredTwoWays(final TestChromosome test,
                                                                   final PathCondition collectedPathCondition) {
        List<Integer> conditionIndexesNotCoveredTwoWays = new LinkedList<>();
        for (int conditionIndex = 0; conditionIndex < collectedPathCondition.size(); conditionIndex++) {
            BranchCondition b = collectedPathCondition.get(conditionIndex);
            if (!isCoveredTwoWays(test, b.getInstructionIndex())) {
                conditionIndexesNotCoveredTwoWays.add(conditionIndex);
            }
        }
        return conditionIndexesNotCoveredTwoWays;
    }

    /**
     * Returns if the true and false branches for this were already covered. If the
     * test case belongs to a whole test suite, then the coverage of the test suite
     * is used, otherwise the single test case is used.
     *
     * @param test
     * @param branchIndex
     * @return
     */
    private boolean isCoveredTwoWays(TestChromosome test, int branchIndex) {

        Set<Integer> trueIndexes = new HashSet<>();
        Set<Integer> falseIndexes = new HashSet<>();

        if (suite != null) {
            for (ExecutionResult execResult : this.suite.getLastExecutionResults()) {
                Set<Integer> trueIndexesInTrace = execResult.getTrace().getCoveredTrueBranches();
                Set<Integer> falseIndexesInTrace = execResult.getTrace().getCoveredFalseBranches();

                trueIndexes.addAll(trueIndexesInTrace);
                falseIndexes.addAll(falseIndexesInTrace);
            }
        } else {
            ExecutionResult execResult = test.getLastExecutionResult();
            Set<Integer> trueIndexesInTest = execResult.getTrace().getCoveredTrueBranches();
            Set<Integer> falseIndexesInTest = execResult.getTrace().getCoveredFalseBranches();
            trueIndexes.addAll(trueIndexesInTest);
            falseIndexes.addAll(falseIndexesInTest);
        }

        final boolean trueIsCovered = trueIndexes.contains(branchIndex);
        final boolean falseIsCovered = falseIndexes.contains(branchIndex);

        return trueIsCovered && falseIsCovered;
    }

    /**
     * Returns true iff the constraint has at least one variable that is
     *
     * @param constraint
     * @param targets
     * @return
     */
    private boolean isRelevant(Constraint<?> constraint, Set<VariableReference> targets) {
        Set<Variable<?>> variables = constraint.getVariables();
        Set<String> targetNames = new HashSet<>();
        for (VariableReference v : targets) {
            targetNames.add(v.getName());
        }
        for (Variable<?> var : variables) {
            if (targetNames.contains(var.getName()))
                return true;
        }
        return false;
    }
}
