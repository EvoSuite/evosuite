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
import org.evosuite.symbolic.dse.algorithm.ExplorationAlgorithmBase;

/**
 * Mirror implementation of {@link org.evosuite.ga.stoppingconditions.MaxTestsStoppingCondition} for the DSE Module.
 *
 * @author Ignacio Lebrero
 */
public class MaxTestsStoppingCondition extends StoppingConditionImpl {

    /**
     * Current number of tests
     */
    protected static long numTests = 0;

    /**
     * Maximum number of evaluations
     */
    protected long maxTests;

    public MaxTestsStoppingCondition() {
        maxTests = Properties.SEARCH_BUDGET;
    }

    public MaxTestsStoppingCondition(MaxTestsStoppingCondition that) {
        this.maxTests = that.maxTests;
    }

    @Override
    public MaxTestsStoppingCondition clone() {
        return new MaxTestsStoppingCondition(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        numTests = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFinished() {
        return numTests >= maxTests;
    }

    @Override
    public void iteration(ExplorationAlgorithmBase algorithm) {
        numTests++;
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.StoppingCondition#getCurrentValue()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCurrentValue() {
        return numTests;
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.StoppingCondition#setLimit(int)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLimit(long limit) {
        maxTests = limit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLimit() {
        return maxTests;
    }
}
