package org.evosuite.symbolic.dse.algorithm.listener.implementations;

import org.evosuite.Properties;
import org.evosuite.symbolic.dse.algorithm.ExplorationAlgorithmBase;

/**
 * Taken from {@link org.evosuite.ga.stoppingconditions.MaxTimeStoppingCondition} for using on the DSE module.
 *
 * @author Ignacio Lebrero
 */
public class MaxTimeStoppingCondition extends StoppingConditionImpl {

    private static final long serialVersionUID = 5262082660819074690L;

	/** Maximum number of seconds */
    private long maxSeconds = Properties.SEARCH_BUDGET;

    private long startTime;

    @Override
    public void generationStarted(ExplorationAlgorithmBase algorithm) {
        reset();
    }

    @Override
    public long getCurrentValue() {
        long currentTime = System.currentTimeMillis();
		return (currentTime - startTime) / 1000;
    }

    @Override
    public long getLimit() {
        return maxSeconds;
    }

    @Override
    public boolean isFinished() {
        long currentTime = System.currentTimeMillis();
		return (currentTime - startTime) / 1000 > maxSeconds;
    }

    @Override
    public void reset() {
        startTime = System.currentTimeMillis();
    }

    @Override
    public void setLimit(long limit) {
        maxSeconds = limit;
    }
}