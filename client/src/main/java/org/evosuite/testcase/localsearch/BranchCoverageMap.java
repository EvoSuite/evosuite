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
package org.evosuite.testcase.localsearch;

import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.SearchListener;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.TestSuiteChromosome;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class BranchCoverageMap implements SearchListener<TestSuiteChromosome> {

    private static final long serialVersionUID = -3498997999289782541L;

    public static BranchCoverageMap instance = null;

    private Map<Integer, TestCase> coveredTrueBranches;

    private Map<Integer, TestCase> coveredFalseBranches;

    private BranchCoverageMap() {

    }

    public static BranchCoverageMap getInstance() {
        if (instance == null)
            instance = new BranchCoverageMap();

        return instance;
    }

    public boolean isCoveredTrue(int branchId) {
        return coveredTrueBranches.containsKey(branchId);
    }

    public boolean isCoveredFalse(int branchId) {
        return coveredFalseBranches.containsKey(branchId);
    }

    public TestCase getTestCoveringTrue(int branchId) {
        return coveredTrueBranches.get(branchId);
    }

    public TestCase getTestCoveringFalse(int branchId) {
        return coveredFalseBranches.get(branchId);
    }

    public Set<Integer> getCoveredTrueBranches() {
        return coveredTrueBranches.keySet();
    }

    public Set<Integer> getCoveredFalseBranches() {
        return coveredFalseBranches.keySet();
    }

    @Override
    public void searchStarted(GeneticAlgorithm<TestSuiteChromosome> algorithm) {
        coveredTrueBranches = new LinkedHashMap<>();
        coveredFalseBranches = new LinkedHashMap<>();
    }

    @Override
    public void iteration(GeneticAlgorithm<TestSuiteChromosome> algorithm) {

    }

    @Override
    public void searchFinished(GeneticAlgorithm<TestSuiteChromosome> algorithm) {
        coveredTrueBranches = null;
        coveredFalseBranches = null;
    }

    @Override
    public void fitnessEvaluation(TestSuiteChromosome suite) {
        if (suite == null) {
            return;
        }

        for (TestChromosome testChromosome : suite.getTestChromosomes()) {
            ExecutionResult lastResult = testChromosome.getLastExecutionResult();
            if (lastResult != null) {
                for (int branchId : lastResult.getTrace().getCoveredTrueBranches()) {
                    if (!coveredTrueBranches.containsKey(branchId)) {
                        coveredTrueBranches.put(branchId, testChromosome.getTestCase());
                    }
                }
                for (int branchId : lastResult.getTrace().getCoveredFalseBranches()) {
                    if (!coveredFalseBranches.containsKey(branchId)) {
                        coveredFalseBranches.put(branchId, testChromosome.getTestCase());
                    }
                }
            }
        }

    }

    @Override
    public void modification(TestSuiteChromosome individual) {
        // TODO Auto-generated method stub

    }


}
