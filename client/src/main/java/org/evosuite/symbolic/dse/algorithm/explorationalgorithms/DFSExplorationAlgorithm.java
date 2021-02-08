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
package org.evosuite.symbolic.dse.algorithm.explorationalgorithms;

import org.evosuite.symbolic.dse.DSEStatistics;
import org.evosuite.symbolic.dse.algorithm.ExplorationAlgorithm;
import org.evosuite.symbolic.dse.algorithm.strategies.implementations.KeepSearchingCriteriaStrategies.TestCasesPendingStrategy;
import org.evosuite.symbolic.dse.algorithm.strategies.implementations.PathExtensionStrategies.DFSStrategy;
import org.evosuite.symbolic.dse.algorithm.strategies.implementations.PathPruningStrategies.CounterExampleCache;
import org.evosuite.symbolic.dse.algorithm.strategies.implementations.TestCaseBuildingStrategies.DefaultTestCaseBuildingStrategy;
import org.evosuite.symbolic.dse.algorithm.strategies.implementations.TestCaseSelectionStrategies.TopTestCaseSelectionStrategy;
import org.evosuite.testcase.TestCase;

/**
 * Classic DFS exploration algorithm.
 * See Baldoni et.al., A Survey of Symbolic Execution Techniques for more info.
 *
 * We model it as a decremental score on each tests that is created so we maintain it's order in the
 * {@link java.util.PriorityQueue} in {@link org.evosuite.symbolic.dse.algorithm.ExplorationAlgorithm}. This strongly
 * depends on the order that {@link org.evosuite.symbolic.dse.algorithm.strategies.implementations.PathExtensionStrategies.DFSStrategy} creates them.
 *
 * In case a path condition diverges, it's score is automatically 0 so it's explored at the end of the process.
 *
 * @author Ignacio Lebrero
 */
public class DFSExplorationAlgorithm extends ExplorationAlgorithm {

    double currentScore = Double.MAX_VALUE;

    public DFSExplorationAlgorithm(DSEStatistics statistics, boolean showProgress) {
        super(statistics, showProgress);

        /** Strategies */
        setCachingStrategy(new CounterExampleCache());
        setPathsExpansionStrategy(new DFSStrategy());
        setTestCaseBuildingStrategy(new DefaultTestCaseBuildingStrategy());
        setTestCaseSelectionStrategy(new TopTestCaseSelectionStrategy());
        setKeepSearchingCriteriaStrategy(new TestCasesPendingStrategy());
    }

    /**
     * Returns the current position in the DFS exploration.
     *
     * @param newTestCase
     * @param hasPathConditionDiverged
     * @return
     */
    @Override
    protected double getTestScore(TestCase newTestCase, boolean hasPathConditionDiverged) {
        if (hasPathConditionDiverged) return 0;

        double resultScore = currentScore;
        currentScore--;

        return resultScore;

    }
}
