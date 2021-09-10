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
package org.evosuite.ga.stoppingconditions;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;


/**
 * Stop search after a predefined amount of time
 *
 * @author Gordon Fraser
 */
public class MaxTimeStoppingCondition<T extends Chromosome<T>> extends StoppingConditionImpl<T> {

    private static final long serialVersionUID = -4524853279562896768L;

    /**
     * Maximum number of seconds
     */
    protected long maxSeconds;

    protected long startTime;

    public MaxTimeStoppingCondition() {
        maxSeconds = Properties.SEARCH_BUDGET;
    }

    public MaxTimeStoppingCondition(MaxTimeStoppingCondition<?> that) {
        this.startTime = that.startTime;
        this.maxSeconds = that.maxSeconds;
    }

    @Override
    public MaxTimeStoppingCondition<T> clone() {
        return new MaxTimeStoppingCondition<>(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void searchStarted(GeneticAlgorithm<T> algorithm) {
        startTime = System.currentTimeMillis();
    }

    /**
     * {@inheritDoc}
     * <p>
     * We are finished when the time is up
     */
    @Override
    public boolean isFinished() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - startTime) / 1000 > maxSeconds;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Reset
     */
    @Override
    public void reset() {
        startTime = System.currentTimeMillis();
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.StoppingCondition#setLimit(int)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLimit(long limit) {
        maxSeconds = limit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLimit() {
        return maxSeconds;
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.StoppingCondition#getCurrentValue()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCurrentValue() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - startTime) / 1000;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void forceCurrentValue(long value) {
        startTime = value;
    }

}
