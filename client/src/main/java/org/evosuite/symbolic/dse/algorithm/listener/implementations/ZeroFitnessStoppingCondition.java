package org.evosuite.symbolic.dse.algorithm.listener.implementations;

import org.evosuite.symbolic.dse.algorithm.ExplorationAlgorithmBase;

/**
 * Adaptation of {@link org.evosuite.ga.stoppingconditions.ZeroFitnessStoppingCondition} for the DSE module.
 *
 * @author Ignacio Lebrero
 */
public class ZeroFitnessStoppingCondition extends StoppingConditionImpl {

    private static final long serialVersionUID = 6593889710447350828L;

    /** Keep track of lowest fitness seen so far */
	private double lastFitness = Double.MAX_VALUE;

    @Override
    public long getCurrentValue() {
        return (long) lastFitness;
    }

    @Override
    public long getLimit() {
        return 0;
    }

    @Override
    public boolean isFinished() {
        return lastFitness <= 0.0;
    }

    @Override
    public void reset() {
        lastFitness = Double.MAX_VALUE;
    }

    @Override
    public void setLimit(long limit) {
        // Nothing
    }

    @Override
    public void generationStarted(ExplorationAlgorithmBase algorithm) {

    }

    @Override
    public void iteration(ExplorationAlgorithmBase algorithm) {
        // Why would the new iteration have worst fitness than the previous one????
        lastFitness = Math.min(lastFitness, algorithm.getGeneratedTestSuite().getFitness());
    }
}
