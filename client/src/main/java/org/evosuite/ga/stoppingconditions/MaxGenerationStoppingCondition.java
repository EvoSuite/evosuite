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
 * Stop search after a predefined number of iterations
 *
 * @author Gordon Fraser
 */
public class MaxGenerationStoppingCondition<T extends Chromosome<T>> extends StoppingConditionImpl<T> {

    private static final long serialVersionUID = 251196904115160351L;

    /**
     * Maximum number of iterations
     */
    protected long maxIterations;

    /**
     * Maximum number of iterations
     */
    protected long currentIteration;

    public MaxGenerationStoppingCondition() {
        maxIterations = Properties.SEARCH_BUDGET;
        currentIteration = 0;
    }

    public MaxGenerationStoppingCondition(MaxGenerationStoppingCondition<?> that) {
        this.maxIterations = that.maxIterations;
        this.currentIteration = that.currentIteration;
    }

    @Override
    public MaxGenerationStoppingCondition<T> clone() {
        return new MaxGenerationStoppingCondition<>(this);
    }

    /**
     * <p>
     * setMaxIterations
     * </p>
     *
     * @param max a int.
     */
    public void setMaxIterations(int max) {
        maxIterations = max;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Increase iteration counter
     */
    @Override
    public void iteration(GeneticAlgorithm<T> algorithm) {
        currentIteration++;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Stop search after a number of iterations
     */
    @Override
    public boolean isFinished() {
        return currentIteration >= maxIterations;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void searchFinished(GeneticAlgorithm<T> algorithm) {
        currentIteration = 0;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Reset counter
     */
    @Override
    public void reset() {
        currentIteration = 0;
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.StoppingCondition#setLimit(int)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLimit(long limit) {
        maxIterations = limit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLimit() {
        return maxIterations;
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.StoppingCondition#getCurrentValue()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCurrentValue() {
        return currentIteration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void forceCurrentValue(long value) {
        currentIteration = value;
    }
}
