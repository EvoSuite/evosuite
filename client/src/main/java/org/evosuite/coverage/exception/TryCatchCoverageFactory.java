package org.evosuite.coverage.exception;

import org.evosuite.coverage.branch.BranchCoverageFactory;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.testsuite.AbstractFitnessFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gordon on 03/04/2016.
 */
public class TryCatchCoverageFactory extends AbstractFitnessFactory<TryCatchCoverageTestFitness> {

    private static final Logger logger = LoggerFactory.getLogger(TryCatchCoverageFactory.class);

    private BranchCoverageFactory branchFactory = new BranchCoverageFactory();

    @Override
    public List<TryCatchCoverageTestFitness> getCoverageGoals() {
        return branchCoverageGoalsToTryCatchGoals(branchFactory.getCoverageGoals());
    }

    public List<TryCatchCoverageTestFitness> getCoverageGoalsForAllKnownClasses() {
        return branchCoverageGoalsToTryCatchGoals(branchFactory.getCoverageGoalsForAllKnownClasses());
    }

    private List<TryCatchCoverageTestFitness> branchCoverageGoalsToTryCatchGoals(List<BranchCoverageTestFitness> branchGoals) {
        List<TryCatchCoverageTestFitness> tryCatchGoals = new ArrayList<>();
        for(BranchCoverageTestFitness goal : branchGoals) {
            if(goal.getBranch() == null || !goal.getBranch().isInstrumented()) {
                if(goal.getBranch() == null)
                    logger.info("Not using root branch: "+goal);
                else
                    logger.info("Not using non-instrumented branch: "+goal);
                continue;
            }
            logger.info("Keeping instrumented branch: "+goal);
            TryCatchCoverageTestFitness tryCatchGoal = new TryCatchCoverageTestFitness(goal.getBranchGoal());
            tryCatchGoals.add(tryCatchGoal);
        }
        return tryCatchGoals;
    }

}
