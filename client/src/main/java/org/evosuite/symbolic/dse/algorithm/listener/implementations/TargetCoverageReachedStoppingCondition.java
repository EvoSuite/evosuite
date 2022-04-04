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
package org.evosuite.symbolic.dse.algorithm.listener.implementations;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.symbolic.dse.algorithm.ExplorationAlgorithmBase;

import java.security.InvalidParameterException;

/**
 * Condition for stopping when a targeted coverage is reached.
 *
 * @author Ignacio Lebrero
 */
public class TargetCoverageReachedStoppingCondition extends StoppingConditionImpl {

    private static final long serialVersionUID = 7235280321530441520L;

    public static final String AND = " and ";
    public static final String ERROR_LIMIT_PARAMETER_MUST_BE_IN_BOUNDS = "ERROR | limit parameter must be in bounds ";

    /**
     * Bound values for setLimit input
     */
    private static final long MINIMUM_LIMIT_INPUT_VALUE = 0;
    private static final long MAXIMUM_LIMIT_INPUT_VALUE = 100;

    /**
     * Keep track of highest coverage seen so far
     */
    private int lastCoverage = Chromosome.MIN_REACHABLE_COVERAGE;

    /**
     * Keep track of the target coverage
     */
    private int targetCoverage = Properties.DSE_TARGET_COVERAGE;

    @Override
    public long getCurrentValue() {
        return lastCoverage;
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
        lastCoverage = Chromosome.MIN_REACHABLE_COVERAGE;
        targetCoverage = Chromosome.MAX_REACHABLE_COVERAGE;
    }

    /**
     * Sets the limit of the coverage.
     * <p>
     * IMPORTANT: As the values are normalized (between 0 and 1) and the limit
     * will arrive as a long value. we have to normalize it.
     *
     * @param limit a long.
     */
    @Override
    public void setLimit(long limit) throws InvalidParameterException {
        if (!isInputValid(limit)) {
            throw new InvalidParameterException(
                    new StringBuilder()
                            .append(ERROR_LIMIT_PARAMETER_MUST_BE_IN_BOUNDS)
                            .append(MINIMUM_LIMIT_INPUT_VALUE)
                            .append(AND)
                            .append(MAXIMUM_LIMIT_INPUT_VALUE).toString()
            );
        }
        targetCoverage = (int) limit;
    }

    @Override
    public void iteration(ExplorationAlgorithmBase algorithm) {
        lastCoverage = Math.max(lastCoverage, normalizeCoverage(algorithm.getGeneratedTestSuite().getCoverage()));
    }

    /**
     * Workaround: as coverage on the testSuite is represented as a double between [0.0, 1.0] and stopping conditions use
     * longs, we normalize the value for internal use.
     *
     * @param coverage
     * @return
     */
    private int normalizeCoverage(double coverage) {
        return (int) (coverage * Chromosome.MAX_REACHABLE_COVERAGE);
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
