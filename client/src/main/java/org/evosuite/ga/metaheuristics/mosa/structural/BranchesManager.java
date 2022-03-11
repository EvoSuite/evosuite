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
package org.evosuite.ga.metaheuristics.mosa.structural;

import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This Class manages the goals to consider during the search according to their structural
 * dependencies
 *
 * @author Annibale Panichella, Fitsum Meshesha Kifetew
 */
public class BranchesManager extends StructuralGoalManager {

    private static final Logger logger = LoggerFactory.getLogger(BranchesManager.class);
    private static final long serialVersionUID = 6453893627503159175L;

    protected BranchFitnessGraph graph;

    protected final Map<Integer, TestFitnessFunction> branchCoverageTrueMap = new HashMap<>();
    protected final Map<Integer, TestFitnessFunction> branchCoverageFalseMap = new HashMap<>();
    private final Map<String, TestFitnessFunction> branchlessMethodCoverageMap = new HashMap<>();

    /**
     * Constructor used to initialize the set of uncovered goals, and the initial set
     * of goals to consider as initial contrasting objectives
     *
     * @param fitnessFunctions List of all FitnessFunction<T>
     */
    public BranchesManager(List<TestFitnessFunction> fitnessFunctions) {
        super(fitnessFunctions);

        graph = new BranchFitnessGraph(new HashSet<>(fitnessFunctions));

        // initialize current goals
        this.currentGoals.addAll(graph.getRootBranches());

        // initialize the maps
        for (TestFitnessFunction ff : fitnessFunctions) {
            BranchCoverageTestFitness goal = (BranchCoverageTestFitness) ff;
            // Skip instrumented branches - we only want real branches
            if (goal.getBranch() != null && goal.getBranch().isInstrumented()) {
                continue;
            }

            if (goal.getBranch() == null) {
                branchlessMethodCoverageMap.put(goal.getClassName() + "." + goal.getMethod(), ff);
            } else if (goal.getBranchExpressionValue()) {
                branchCoverageTrueMap.put(goal.getBranch().getActualBranchId(), ff);
            } else {
                branchCoverageFalseMap.put(goal.getBranch().getActualBranchId(), ff);
            }
        }
    }

    public void calculateFitness(TestChromosome c, GeneticAlgorithm<TestChromosome> ga) {
        // run the test
        TestCase test = c.getTestCase();
        ExecutionResult result = TestCaseExecutor.runTest(test);
        c.setLastExecutionResult(result);
        c.setChanged(false);

        if (result.hasTimeout() || result.hasTestException()) {
            currentGoals.forEach(f -> c.setFitness(f, Double.MAX_VALUE));
            return;
        }

        // 1) we update the set of currents goals
        Set<TestFitnessFunction> visitedStatements = new HashSet<>(this.getUncoveredGoals().size() * 2);
        LinkedList<TestFitnessFunction> targets = new LinkedList<>(this.currentGoals);

        while (targets.size() > 0 && !ga.isFinished()) {
            TestFitnessFunction fitnessFunction = targets.poll();

            int pastSize = visitedStatements.size();
            visitedStatements.add(fitnessFunction);
            if (pastSize == visitedStatements.size())
                continue;

            double value = fitnessFunction.getFitness(c);
            if (value == 0.0) {
                updateCoveredGoals(fitnessFunction, c);
                for (TestFitnessFunction child : graph.getStructuralChildren(fitnessFunction)) {
                    targets.addLast(child);
                }
            } else {
                currentGoals.add(fitnessFunction);
            }
        }
        currentGoals.removeAll(this.getCoveredGoals());
        // 2) we update the archive
        for (Integer branchID : result.getTrace().getCoveredFalseBranches()) {
            TestFitnessFunction branch = this.branchCoverageFalseMap.get(branchID);
            if (branch == null)
                continue;
            updateCoveredGoals(branch, c);
        }
        for (Integer branchID : result.getTrace().getCoveredTrueBranches()) {
            TestFitnessFunction branch = this.branchCoverageTrueMap.get(branchID);
            if (branch == null)
                continue;
            updateCoveredGoals(branch, c);
        }
        for (String method : result.getTrace().getCoveredBranchlessMethods()) {
            TestFitnessFunction branch = this.branchlessMethodCoverageMap.get(method);
            if (branch == null)
                continue;
            updateCoveredGoals(branch, c);
        }
        //debugStructuralDependencies(c);
    }

    protected void debugStructuralDependencies(TestChromosome c) {
        for (TestFitnessFunction fitnessFunction : this.getUncoveredGoals()) {
            double value = fitnessFunction.getFitness(c);
            if (value < 1 && !currentGoals.contains(fitnessFunction) && !this.getCoveredGoals().contains(fitnessFunction)) {
                logger.error("Branch {} has fitness {} but is not in the current goals", fitnessFunction, value);
            }
        }
    }

    public BranchFitnessGraph getGraph() {
        return graph;
    }

}
