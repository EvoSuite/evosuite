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
package org.evosuite.symbolic.dse.algorithm.listener;

import org.evosuite.Properties;
import org.evosuite.symbolic.dse.algorithm.listener.implementations.MaxTestsStoppingCondition;
import org.evosuite.symbolic.dse.algorithm.listener.implementations.MaxTimeStoppingCondition;
import org.evosuite.symbolic.dse.algorithm.listener.implementations.TargetCoverageReachedStoppingCondition;
import org.evosuite.symbolic.dse.algorithm.listener.implementations.ZeroFitnessStoppingCondition;

/**
 * Factory of stopping conditions.
 *
 * @author Ignacio Lebrero
 */
public class StoppingConditionFactory {

    /**
     * Convert property to actual stopping condition
     *
     * @return
     */
    public static StoppingCondition getStoppingCondition(Properties.DSEStoppingConditionCriterion stoppingCondition) {
        switch (stoppingCondition) {
            case MAXTIME:
                return new MaxTimeStoppingCondition();
            case TARGETCOVERAGE:
                return new TargetCoverageReachedStoppingCondition();
            case ZEROFITNESS:
                return new ZeroFitnessStoppingCondition();
            case MAXTESTS:
                return new MaxTestsStoppingCondition();
            default:
                return new MaxTimeStoppingCondition();
        }
    }

}
