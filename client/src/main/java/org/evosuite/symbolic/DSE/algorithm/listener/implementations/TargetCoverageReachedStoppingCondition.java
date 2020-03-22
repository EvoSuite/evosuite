package org.evosuite.symbolic.DSE.algorithm.listener.implementations;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.symbolic.DSE.algorithm.DSEBaseAlgorithm;

import java.security.InvalidParameterException;

public class TargetCoverageReachedStoppingCondition extends StoppingConditionImpl {

    private static final long serialVersionUID = 7235280321530441520L;

    /** Bound values for setLimit input */
    private static final long MINIMUM_LIMIT_INPUT_VALUE = 0;
    private static final long MAXIMUM_LIMIT_INPUT_VALUE = 100;

    /** Keep track of highest coverage seen so far */
	private int lastCoverage = Chromosome.MIN_COVERAGE_REACHABLE;

	/** Keep track of the target coverage */
	private int targetCoverage = Properties.DSE_TARGET_COVERAGE;

    @Override
    public long getCurrentValue() {
        return lastCoverage ;
    }

    @Override
    public long getLimit() {
        return targetCoverage;
    }

    @Override
    public boolean isFinished() {
        return lastCoverage >= targetCoverage;
    }

    @Override
    public void reset() {
        lastCoverage = Chromosome.MIN_COVERAGE_REACHABLE;
        targetCoverage = Chromosome.MAX_COVERAGE_REACHABLE;
    }

    /**
     * Sets the limit of the coverage.
     *
     * IMPORTANT: As the values are normalized (between 0 and 1) and the limit
     *            will arrive as a long value. we have to normalize it.
     *
     * @param limit a long.
     */
    @Override
    public void setLimit(long limit) throws InvalidParameterException {
        if (! isInputValid(limit)) {
            throw new InvalidParameterException(
                    new StringBuilder()
                            .append("ERROR | limit parameter must be in bounds ")
                            .append(MINIMUM_LIMIT_INPUT_VALUE)
                            .append(" and ")
                            .append(MAXIMUM_LIMIT_INPUT_VALUE).toString()
            );
        }
        targetCoverage = (int) limit;
    }

    @Override
    public void iteration(DSEBaseAlgorithm algorithm) {
        lastCoverage = Math.max(lastCoverage, normalizeCoverage(algorithm.getGeneratedTestSuite().getCoverage()));
    }

    /**
     * Workaround: as coverage on the testSuite is represented as a double between [0.0, 1.0] and stopping conditions use
     *             longs, we normalize the value for internal use.
     *
     * @param coverage
     * @return
     */
    private int normalizeCoverage(double coverage) {
        return (int) (coverage * Chromosome.MAX_COVERAGE_REACHABLE);
    }

    /**
     * Input validation for coverage.
     *
     * @param limit
     * @return
     */
    private boolean isInputValid(long limit) {
        return limit < MINIMUM_LIMIT_INPUT_VALUE
            || limit > MAXIMUM_LIMIT_INPUT_VALUE;
    }
}
