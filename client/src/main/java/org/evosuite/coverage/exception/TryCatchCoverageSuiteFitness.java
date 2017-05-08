/**
 * Copyright (C) 2010-2017 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.coverage.exception;

import org.evosuite.Properties;
import org.evosuite.coverage.archive.TestsArchive;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;

import java.util.List;
import java.util.Map;

/**
 * Created by gordon on 03/04/2016.
 */
public class TryCatchCoverageSuiteFitness extends BranchCoverageSuiteFitness {

    /**
     * Make sure we only include artificial branches
     */
    protected void determineCoverageGoals() {
        List<TryCatchCoverageTestFitness> goals = new TryCatchCoverageFactory().getCoverageGoals();
        for (TryCatchCoverageTestFitness goal : goals) {

            if(Properties.TEST_ARCHIVE)
                TestsArchive.instance.addGoalToCover(this, goal);

            branchesId.add(goal.getBranch().getActualBranchId());
            if (goal.getBranchExpressionValue())
                branchCoverageTrueMap.put(goal.getBranch().getActualBranchId(), goal);
            else
                branchCoverageFalseMap.put(goal.getBranch().getActualBranchId(), goal);
        }
        totalGoals = goals.size();
    }

    @Override
    protected void handleBranchlessMethods(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite, ExecutionResult result, Map<String, Integer> callCount) {
        // no-op
    }

    @Override
    protected void handleFalseDistances(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite, ExecutionResult result, Map<Integer, Double> falseDistance) {
        // We only aim to cover true branches
    }
}
