package org.evosuite.coverage.exception;

import org.evosuite.coverage.branch.BranchCoverageGoal;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;

/**
 * Created by gordon on 03/04/2016.
 */
public class TryCatchCoverageTestFitness extends BranchCoverageTestFitness {

    public TryCatchCoverageTestFitness(BranchCoverageGoal goal) throws IllegalArgumentException {
        super(goal);
    }
}
