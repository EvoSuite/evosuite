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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * GlobalTimeStoppingCondition class.
 * </p>
 *
 * @author Gordon Fraser
 */
public class GlobalTimeStoppingCondition<T extends Chromosome<T>> extends StoppingConditionImpl<T> {

    private static final Logger logger = LoggerFactory.getLogger(GlobalTimeStoppingCondition.class);

    private static final long serialVersionUID = -4880914182984895075L;

    /**
     * Assume the search has not started until start_time != 0
     */
    protected static long startTime = 0L;

    /**
     * Constant <code>pause_time=0L</code>
     */
    protected static long pauseTime = 0L;

    public GlobalTimeStoppingCondition() {
        // empty constructor
    }

    public GlobalTimeStoppingCondition(GlobalTimeStoppingCondition<?> that) {
        // empty copy constructor
    }

    @Override
    public GlobalTimeStoppingCondition<T> clone() {
        return new GlobalTimeStoppingCondition<>(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void searchStarted(GeneticAlgorithm<T> algorithm) {
        if (startTime == 0)
            reset();
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.StoppingCondition#getCurrentValue()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCurrentValue() {
        long current_time = System.currentTimeMillis();
        return (int) ((current_time - startTime) / 1000);
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.StoppingCondition#isFinished()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFinished() {
        long current_time = System.currentTimeMillis();
        if (Properties.GLOBAL_TIMEOUT != 0 && startTime != 0
                && (current_time - startTime) / 1000 > Properties.GLOBAL_TIMEOUT)
            logger.info("Timeout reached");

        return Properties.GLOBAL_TIMEOUT != 0 && startTime != 0
                && (current_time - startTime) / 1000 > Properties.GLOBAL_TIMEOUT;
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.StoppingCondition#reset()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        if (startTime == 0)
            startTime = System.currentTimeMillis();
    }

    /**
     * Fully resets the stopping condition. The start time is set to the current
     * time and thus "no time has elapsed so far". If you want a conditional
     * reset which only has an effect if the start time has never been changed
     * use <tt>reset()</tt>.
     */
    public void fullReset() {
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
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLimit() {
        // TODO Auto-generated method stub
        return Properties.GLOBAL_TIMEOUT;
    }

    /**
     * <p>
     * forceReset
     * </p>
     */
    public static void forceReset() {
        startTime = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void forceCurrentValue(long value) {
        startTime = value;
    }

    /**
     * Remember start pause time
     */
    public void pause() {
        pauseTime = System.currentTimeMillis();
    }

    /**
     * Change start time after MA
     */
    public void resume() {
        startTime += System.currentTimeMillis() - pauseTime;
    }

}
