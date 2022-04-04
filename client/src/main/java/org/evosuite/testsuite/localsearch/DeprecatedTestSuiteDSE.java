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
package org.evosuite.testsuite.localsearch;

import org.evosuite.Properties;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.localsearch.LocalSearchBudget;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.symbolic.BranchCondition;
import org.evosuite.symbolic.dse.ConcolicExecutorImpl;
import org.evosuite.symbolic.dse.DSEStatistics;
import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.symbolic.solver.SolverResult;
import org.evosuite.symbolic.solver.SolverUtils;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Deprecated
public class DeprecatedTestSuiteDSE {

    private static final Logger logger = LoggerFactory.getLogger(DeprecatedTestSuiteDSE.class);

    /**
     * Constant <code>nrConstraints=0</code>
     */
    private static int nrConstraints = 0;

    /**
     * Constant <code>nrSolvedConstraints=0</code>
     */
    private static final int nrSolvedConstraints = 0;
    private int nrCurrConstraints = 0;

    /**
     * Constant <code>success=0</code>
     */
    private static int success = 0;
    /**
     * Constant <code>failed=0</code>
     */
    private static int failed = 0;

    private LocalSearchObjective<TestSuiteChromosome> objective;

    private static class Branch {

        public Branch(int branchIndex, boolean isTrueBranch) {
            super();
            this.branchIndex = branchIndex;
            this.isTrueBranch = isTrueBranch;
        }

        private final int branchIndex;

        private final boolean isTrueBranch;

        public Branch negate() {
            return new Branch(branchIndex, !isTrueBranch);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + branchIndex;
            result = prime * result + (isTrueBranch ? 1231 : 1237);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Branch other = (Branch) obj;
            if (branchIndex != other.branchIndex)
                return false;
            return isTrueBranch == other.isTrueBranch;
        }

        @Override
        public String toString() {
            return "Branch [branchIndex=" + branchIndex + ", isTrueBranch=" + isTrueBranch + "]";
        }

    }

    // private final TestSuiteFitnessFunction fitness;

    /**
     * Stores the path condition from each test execution
     */
    private final Map<TestChromosome, List<BranchCondition>> pathConditions = new HashMap<>();

    private final Set<BranchCondition> unsolvableBranchConditions = new HashSet<>();

    private final Map<String, Integer> solutionAttempts = new HashMap<>();

    private final Collection<TestBranchPair> unsolvedBranchConditions = null;

    private static class TestBranchPair implements Comparable<TestBranchPair> {
        TestChromosome test;
        BranchCondition branch;
        List<BranchCondition> pathCondition;

        private final double ranking;

        TestBranchPair(TestChromosome test, List<BranchCondition> pathCondition, BranchCondition branchCondition) {
            this.test = test;
            this.branch = branchCondition;
            this.pathCondition = pathCondition;
            this.ranking = computeRanking(pathCondition, branchCondition);
        }

        private double computeRanking(List<BranchCondition> pathCondition, BranchCondition targetBranch) {
            List<Constraint<?>> reachingConstraints = new LinkedList<>();
            for (BranchCondition b : pathCondition) {
                reachingConstraints.addAll(b.getSupportingConstraints());
                reachingConstraints.add(b.getConstraint());
                if (b == targetBranch) {
                    break;
                }
            }

            int length = 1 + reachingConstraints.size();

            int totalSize = 0;
            for (Constraint<?> constraint : reachingConstraints) {
                totalSize += constraint.getSize();
            }
            double avg_size = (double) totalSize / (double) reachingConstraints.size();

            double ranking = length * avg_size;
            return ranking;
        }

        @Override
        public int compareTo(TestBranchPair arg0) {
            return Double.compare(this.ranking, arg0.ranking);
        }

    }

    /**
     * Iterate over path constraints to identify those which map to branches
     * that are only covered one way
     */
    private void calculateUncoveredBranches() {
        unsolvedBranchConditions.clear();

        if (Properties.DSE_NEGATE_ALL_CONDITIONS == true) {

            for (TestChromosome testChromosome : pathConditions.keySet()) {
                final List<BranchCondition> pathCondition = pathConditions.get(testChromosome);
                for (BranchCondition branch : pathCondition) {
                    if (!unsolvableBranchConditions.contains(branch)) {
                        unsolvedBranchConditions.add(new TestBranchPair(testChromosome, pathCondition, branch));
                    }
                }
            }
        } else {
            Map<String, Map<Comparator, Set<TestBranchPair>>> solvedConstraints = new HashMap<>();
            for (TestChromosome test : pathConditions.keySet()) {
                final List<BranchCondition> pathCondition = pathConditions.get(test);
                for (BranchCondition branch : pathCondition) {

                    if (unsolvableBranchConditions.contains(branch))
                        continue;

                    String index = getBranchIndex(branch);
                    if (!solvedConstraints.containsKey(index))
                        solvedConstraints.put(index, new HashMap<>());

                    Constraint<?> c = branch.getConstraint();

                    if (!solvedConstraints.get(index).containsKey(c.getComparator()))
                        solvedConstraints.get(index).put(c.getComparator(), new HashSet<>());

                    solvedConstraints.get(index).get(c.getComparator())
                            .add(new TestBranchPair(test, pathCondition, branch));
                }
            }

            for (String index : solvedConstraints.keySet()) {
                if (solvedConstraints.get(index).size() == 1) {
                    Set<TestBranchPair> branches = solvedConstraints.get(index).values().iterator().next();
                    unsolvedBranchConditions.addAll(branches);
                }
            }
            logger.info("Update set of unsolved branch conditions to " + unsolvedBranchConditions.size());

            if (Properties.DSE_RANK_BRANCH_CONDITIONS == false) {
                Randomness.shuffle((ArrayList<TestBranchPair>) unsolvedBranchConditions);
            }
        }
    }

    /**
     * Calculate and store path constraints for an individual
     *
     * @param test
     */
    private void updatePathConstraints(TestChromosome test) {
        List<BranchCondition> pathCondition = new ConcolicExecutorImpl().getSymbolicPath(test);
        pathConditions.put(test, pathCondition);
    }

    /**
     * Create path constraints for all tests in a test suite
     *
     * @param testSuite
     */
    private void createPathConstraints(TestSuiteChromosome testSuite) {

        for (TestChromosome test : testSuite.getTestChromosomes()) {
            updatePathConstraints(test);
        }
        calculateUncoveredBranches();
    }

    private String getBranchIndex(BranchCondition branch) {
        return branch.getFullName() + branch.getInstructionIndex();
    }

    /**
     * Get a new candidate for negation
     *
     * @return
     */
    private TestBranchPair getNextBranchCondition() {
        TestBranchPair pair;
        pair = getNextTestBranchPair();

        if (Properties.DSE_NEGATE_ALL_CONDITIONS == true) {
            return pair;
        }

        String index = getBranchIndex(pair.branch);
        if (!unsolvedBranchConditions.isEmpty()) {
            while (solutionAttempts.containsKey(index)
                    && solutionAttempts.get(index) >= Properties.CONSTRAINT_SOLUTION_ATTEMPTS
                    && !unsolvedBranchConditions.isEmpty()) {
                logger.info("Reached maximum number of attempts for branch " + index);
                pair = getNextTestBranchPair();
                index = getBranchIndex(pair.branch);
            }
        }

        if (!solutionAttempts.containsKey(index))
            solutionAttempts.put(index, 1);
        else
            solutionAttempts.put(index, solutionAttempts.get(index) + 1);

        return pair;
    }

    private TestBranchPair getNextTestBranchPair() {
        TestBranchPair pair;
        if (Properties.DSE_RANK_BRANCH_CONDITIONS) {
            pair = ((PriorityQueue<TestBranchPair>) unsolvedBranchConditions).poll();
        } else {
            pair = ((ArrayList<TestBranchPair>) unsolvedBranchConditions).remove(0);
        }
        return pair;
    }

    /**
     * Check if there are further candidates for negation
     *
     * @return
     */
    private boolean hasNextBranchCondition() {
        return !unsolvedBranchConditions.isEmpty();
    }

    /**
     * Generate new constraint and ask solver for solution
     *
     * @param reachingConstraints
     * @param localConstraint
     * @param test
     * @return
     */
    // @SuppressWarnings("rawtypes")
    // @SuppressWarnings("rawtypes")
    @SuppressWarnings({"unchecked", "rawtypes"})
    private TestCase negateCondition(Set<Constraint<?>> reachingConstraints, Constraint<?> localConstraint,
                                     TestCase test) {
        List<Constraint<?>> constraints = new LinkedList<>(reachingConstraints);

        Constraint<?> targetConstraint = localConstraint.negate();
        constraints.add(targetConstraint);
        if (!targetConstraint.isSolveable()) {
            logger.info("Found unsolvable constraint: " + targetConstraint);
            // TODO: This is usually the case when the same variable is used for
            // several parameters of a method
            // Could we treat this as a special case?
            return null;
        }

        int size = constraints.size();
        /*
         * int counter = 0; for (Constraint cnstr : constraints) { logger.debug(
         * "Cnstr " + (counter++) + " : " + cnstr + " dist: " +
         * DistanceEstimator.getDistance(constraints)); }
         */
        if (size > 0) {
            logger.debug("Calculating cone of influence for " + size + " constraints");
            constraints = reduce(constraints);
            logger.info("Reduced constraints from " + size + " to " + constraints.size());
            // for (Constraint<?> c : constraints) {
            // logger.info(c.toString());
            // }
        }

        nrCurrConstraints = constraints.size();
        nrConstraints += nrCurrConstraints;

        logger.info("Applying local search");
        DSEStatistics.getInstance().reportNewConstraints(constraints);

        long startSolvingTime = System.currentTimeMillis();
        SolverResult solverResult = SolverUtils.solveQuery(constraints);
        long estimatedSolvingTime = System.currentTimeMillis() - startSolvingTime;
        DSEStatistics.getInstance().reportNewSolvingTime(estimatedSolvingTime);

        if (solverResult == null) {
            logger.info("Found no solution");
            /* Timeout, parseException, error, trivialSolution, etc. */
            return null;

        } else if (solverResult.isUNSAT()) {

            logger.info("Found UNSAT solution");
            DSEStatistics.getInstance().reportNewUNSAT();
            return null;

        } else {

            Map<String, Object> model = solverResult.getModel();
            DSEStatistics.getInstance().reportNewSAT();

            TestCase newTest = test.clone();

            for (Object key : model.keySet()) {
                Object val = model.get(key);
                if (val != null) {
                    logger.info("New value: " + key + ": " + val);
                    if (val instanceof Long) {
                        Long value = (Long) val;
                        String name = ((String) key).replace("__SYM", "");
                        // logger.warn("New long value for " + name + " is " +
                        // value);
                        PrimitiveStatement p = getStatement(newTest, name);
                        if (p.getValue().getClass().equals(Character.class))
                            p.setValue((char) value.intValue());
                        else if (p.getValue().getClass().equals(Long.class))
                            p.setValue(value);
                        else if (p.getValue().getClass().equals(Integer.class))
                            p.setValue(value.intValue());
                        else if (p.getValue().getClass().equals(Short.class))
                            p.setValue(value.shortValue());
                        else if (p.getValue().getClass().equals(Boolean.class))
                            p.setValue(value.intValue() > 0);
                        else if (p.getValue().getClass().equals(Byte.class))
                            p.setValue(value.byteValue() > 0);
                        else
                            logger.warn("New value is of an unsupported type: " + p.getValue().getClass() + val);
                    } else if (val instanceof String) {
                        String name = ((String) key).replace("__SYM", "");
                        PrimitiveStatement p = getStatement(newTest, name);
                        // logger.warn("New string value for " + name + " is " +
                        // val);
                        assert (p != null) : "Could not find variable " + name + " in test: " + newTest.toCode()
                                + " / Orig test: " + test.toCode() + ", seed: " + Randomness.getSeed();
                        if (p.getValue().getClass().equals(Character.class))
                            p.setValue((char) Integer.parseInt(val.toString()));
                        else
                            p.setValue(val.toString());
                    } else if (val instanceof Double) {
                        Double value = (Double) val;
                        String name = ((String) key).replace("__SYM", "");
                        PrimitiveStatement p = getStatement(newTest, name);
                        // logger.warn("New double value for " + name + " is " +
                        // value);
                        assert (p != null) : "Could not find variable " + name + " in test: " + newTest.toCode()
                                + " / Orig test: " + test.toCode() + ", seed: " + Randomness.getSeed();

                        if (p.getValue().getClass().equals(Double.class))
                            p.setValue(value);
                        else if (p.getValue().getClass().equals(Float.class))
                            p.setValue(value.floatValue());
                        else
                            logger.warn("New value is of an unsupported type: " + val);
                    } else {
                        logger.debug("New value is of an unsupported type: " + val);
                    }
                } else {
                    logger.debug("New value is null");

                }
            }
            return newTest;
        }

    }

    /**
     * Get the statement that defines this variable
     *
     * @param test
     * @param name
     * @return
     */
    private PrimitiveStatement<?> getStatement(TestCase test, String name) {
        for (Statement statement : test) {

            if (statement instanceof PrimitiveStatement<?>) {
                if (statement.getReturnValue().getName().equals(name))
                    return (PrimitiveStatement<?>) statement;
            }
        }
        return null;
    }

    /**
     * Apply cone of influence reduction to constraints with respect to the last
     * constraint in the list
     *
     * @param constraints
     * @return
     */
    private List<Constraint<?>> reduce(List<Constraint<?>> constraints) {

        Constraint<?> target = constraints.get(constraints.size() - 1);
        Set<Variable<?>> dependencies = getVariables(target);

        LinkedList<Constraint<?>> coi = new LinkedList<>();
        if (dependencies.size() <= 0)
            return coi;

        coi.add(target);

        for (int i = constraints.size() - 2; i >= 0; i--) {
            Constraint<?> constraint = constraints.get(i);
            Set<Variable<?>> variables = getVariables(constraint);
            for (Variable<?> var : dependencies) {
                if (variables.contains(var)) {
                    dependencies.addAll(variables);
                    coi.addFirst(constraint);
                    break;
                }
            }
        }
        return coi;
    }

    /**
     * Determine the set of variable referenced by this constraint
     *
     * @param constraint
     * @return
     */
    private Set<Variable<?>> getVariables(Constraint<?> constraint) {
        Set<Variable<?>> variables = new HashSet<>();
        getVariables(constraint.getLeftOperand(), variables);
        getVariables(constraint.getRightOperand(), variables);
        return variables;
    }

    /**
     * Recursively determine constraints in expression
     *
     * @param expr      a {@link org.evosuite.symbolic.expr.Expression} object.
     * @param variables a {@link java.util.Set} object.
     */
    private static void getVariables(Expression<?> expr, Set<Variable<?>> variables) {
        variables.addAll(expr.getVariables());
    }

    private double getFitness(TestSuiteChromosome suite) {
        for (FitnessFunction<TestSuiteChromosome> ff : objective.getFitnessFunctions()) {
            ff.getFitness(suite);
        }
        return suite.getFitness();
    }

    private boolean reachesUncoveredBranch(TestChromosome test, Set<Branch> uncoveredBranches) {
        Set<Branch> testCoveredBranches = getCoveredBranches(test);
        for (Branch b : testCoveredBranches) {
            Branch negate = b.negate();
            if (uncoveredBranches.contains(negate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the set of all branches that are not covered both-wise
     *
     * @param coveredBranches
     * @return
     */
    private static Set<Branch> getUncoveredBranches(Set<Branch> coveredBranches) {
        Set<Branch> uncoveredBranches = new HashSet<>();
        for (Branch b : coveredBranches) {
            final Branch negate = b.negate();
            if (!coveredBranches.contains(negate)) {
                uncoveredBranches.add(negate);
            }
        }
        return uncoveredBranches;
    }

    /**
     * Returns the set of covered branches by this test
     *
     * @param test
     * @return
     */
    private static Set<Branch> getCoveredBranches(TestChromosome test) {
        final Set<Branch> testCoveredBranches = new HashSet<>();

        ExecutionTrace trace = test.getLastExecutionResult().getTrace();
        {
            Set<Integer> coveredTrueBranchIndexesInTrace = trace.getCoveredTrueBranches();
            for (Integer branchIndex : coveredTrueBranchIndexesInTrace) {
                Branch b = new Branch(branchIndex, true);
                testCoveredBranches.add(b);
            }
        }
        {

            Set<Integer> coveredFalseBranchIndexesInTrace = trace.getCoveredFalseBranches();
            for (Integer branchIndex : coveredFalseBranchIndexesInTrace) {
                Branch b = new Branch(branchIndex, false);
                testCoveredBranches.add(b);
            }
        }
        return testCoveredBranches;
    }

    /**
     * Attempt to negate individual branches until budget is used up, or there
     * are no further branches to negate
     *
     * @param individual
     */
    public boolean applyDSE0(TestSuiteChromosome individual) {
        logger.info("[DSE] Current test suite: " + individual.toString());

        boolean wasSuccess = false;
        // expansion already happens as part of LS
        // TestSuiteChromosome expandedTests = expandTestSuite(individual);
        TestSuiteChromosome expandedTests = individual.clone();
        createPathConstraints(expandedTests);
        // fitness.getFitness(expandedTests);

        double originalFitness = getFitness(individual);

        while (hasNextBranchCondition() && !LocalSearchBudget.getInstance().isFinished()) {
            logger.info("Branches remaining: " + unsolvedBranchConditions.size());

            TestBranchPair next = getNextBranchCondition();
            BranchCondition branch = next.branch;

            List<BranchCondition> pathCondition = next.pathCondition;

            List<Constraint<?>> reachingConstraints = new LinkedList<>();
            for (BranchCondition b : pathCondition) {
                reachingConstraints.addAll(b.getSupportingConstraints());
                if (b == branch) {
                    break;
                }
                reachingConstraints.add(b.getConstraint());
            }

            List<Constraint<?>> constraints = new LinkedList<>();

            TestCase newTest = negateCondition(new HashSet<>(reachingConstraints), branch.getConstraint(),
                    next.test.getTestCase());

            if (newTest != null) {
                logger.info("Found new test: " + newTest.toCode());
                TestChromosome newTestChromosome = new TestChromosome();
                newTestChromosome.setTestCase(newTest);
                expandedTests.addTest(newTestChromosome);

                if (Properties.DSE_KEEP_ALL_TESTS) {
                    updatePathConstraints(newTestChromosome);
                    calculateUncoveredBranches();
                    individual.addTest(newTest);
                    wasSuccess = true;
                } else {

                    if (getFitness(expandedTests) < originalFitness) {
                        logger.info("New test improves fitness to {}", getFitness(expandedTests));
                        DSEStatistics.getInstance().reportNewTestUseful();
                        wasSuccess = true;

                        // no need to clone so we can keep executionresult
                        updatePathConstraints(newTestChromosome);
                        calculateUncoveredBranches(newTestChromosome);
                        individual.addTest(newTest);
                        originalFitness = getFitness(expandedTests);
                        // TODO: Cancel on fitness 0 - would need to know if
                        // ZeroFitness is a stopping condition
                    } else {
                        logger.info("New test does not improve fitness");
                        DSEStatistics.getInstance().reportNewTestUnuseful();
                        expandedTests.deleteTest(newTest);
                    }
                }
                success++;
            } else {
                unsolvableBranchConditions.add(branch);
                failed++;
                logger.info("Failed to find new test.");
            }
        }
        logger.info("Finished DSE");
        getFitness(individual); // Ensure fitness values are up to date.
        LocalSearchBudget.getInstance().countLocalSearchOnTestSuite();

        return wasSuccess;
    }

    private void calculateUncoveredBranches(TestChromosome newTestChromosome) {

        if (Properties.DSE_NEGATE_ALL_CONDITIONS == true) {
            final List<BranchCondition> pathCondition = pathConditions.get(newTestChromosome);
            for (BranchCondition targetBranchCondition : pathCondition) {
                if (!unsolvableBranchConditions.contains(targetBranchCondition)) {
                    unsolvedBranchConditions
                            .add(new TestBranchPair(newTestChromosome, pathCondition, targetBranchCondition));
                }
            }
        } else {
            calculateUncoveredBranches();
        }
    }

}
