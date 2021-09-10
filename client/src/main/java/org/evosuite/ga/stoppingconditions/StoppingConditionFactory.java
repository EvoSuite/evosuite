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

/**
 * Factory for StoppingConditions
 *
 * @author Ignacio Lebrero
 */
public class StoppingConditionFactory {

    /**
     * Convert property to actual stopping condition
     *
     * @return
     */
    public static <T extends Chromosome<T>> StoppingCondition<T> getStoppingCondition(Properties.StoppingCondition stoppingCondition) {
        switch (stoppingCondition) {
            case MAXGENERATIONS:
                return new MaxGenerationStoppingCondition<>();
            case MAXFITNESSEVALUATIONS:
                return new MaxFitnessEvaluationsStoppingCondition<>();
            case MAXTIME:
                return new MaxTimeStoppingCondition<>();
            case MAXTESTS:
                return new MaxTestsStoppingCondition<>();
            case MAXSTATEMENTS:
                return new MaxStatementsStoppingCondition<>();
            default:
                return new MaxGenerationStoppingCondition<>();
        }
    }


}
