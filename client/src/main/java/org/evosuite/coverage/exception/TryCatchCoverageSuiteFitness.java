package org.evosuite.coverage.exception;

import org.evosuite.Properties;
import org.evosuite.coverage.archive.TestsArchive;
import org.evosuite.coverage.branch.BranchCoverageFactory;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;

import java.util.List;

/**
 * Created by gordon on 03/04/2016.
 */
public class TryCatchCoverageSuiteFitness extends BranchCoverageSuiteFitness {

    public TryCatchCoverageSuiteFitness() {
        super();
    }

    public TryCatchCoverageSuiteFitness(ClassLoader loader) {
        super(loader);
    }

    /**
     * Make sure we only include artificial branches
     */
    protected void determineCoverageGoals() {
        List<BranchCoverageTestFitness> goals = new BranchCoverageFactory().getCoverageGoals();
        for (BranchCoverageTestFitness goal : goals) {

            // Only instrumented branches
            if(goal.getBranch() == null || !goal.getBranch().isInstrumented()) {
                continue;
            }
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
}
