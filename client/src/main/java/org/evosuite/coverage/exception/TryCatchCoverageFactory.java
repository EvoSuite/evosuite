package org.evosuite.coverage.exception;

import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.MethodNameMatcher;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchCoverageGoal;
import org.evosuite.coverage.branch.BranchPool;
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

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.evosuite.coverage.TestCoverageFactory#getCoverageGoals()
	 */
    /** {@inheritDoc} */
    @Override
    public List<TryCatchCoverageTestFitness> getCoverageGoals() {
        List<TryCatchCoverageTestFitness> goals = new ArrayList<>();

        // logger.info("Getting branches");
        for (String className : BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).knownClasses()) {
            final MethodNameMatcher matcher = new MethodNameMatcher();

            // Branches
            for (String methodName : BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).knownMethods(className)) {
                if (!matcher.methodMatches(methodName)) {
                    logger.info("Method " + methodName + " does not match criteria. ");
                    continue;
                }

                for (Branch b : BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).retrieveBranchesInMethod(className,
                        methodName)) {
                    if(b.isInstrumented()) {
                        // Only keep true branch of instrumented branch
                        goals.add(new TryCatchCoverageTestFitness(new BranchCoverageGoal(b,
                                true, b.getClassName(), b.getMethodName())));
                    }
                }
            }
        }
        return goals;
    }

}
