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
 * Taken from {@link org.evosuite.ga.stoppingconditions.MaxTimeStoppingCondition} for using on the DSE module.
 *
 * @author Ignacio Lebrero
 */
public class MaxTimeStoppingCondition extends StoppingConditionImpl {

    private static final long serialVersionUID = 5262082660819074690L;

    /**
     * Maximum number of seconds
     */
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
