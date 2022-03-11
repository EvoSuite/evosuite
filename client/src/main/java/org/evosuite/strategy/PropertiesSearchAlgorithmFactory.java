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
package org.evosuite.strategy;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.populationlimit.IndividualPopulationLimit;
import org.evosuite.ga.populationlimit.PopulationLimit;
import org.evosuite.ga.populationlimit.SizePopulationLimit;
import org.evosuite.ga.stoppingconditions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for GAs
 *
 * @param <T> the {@code Chromosome} type used by the genetic algorithm
 * @author gordon
 */
public abstract class PropertiesSearchAlgorithmFactory<T extends Chromosome<T>> {

    protected static final Logger logger = LoggerFactory.getLogger(PropertiesSearchAlgorithmFactory.class);

    protected PopulationLimit<T> getPopulationLimit() {
        switch (Properties.POPULATION_LIMIT) {
            case INDIVIDUALS:
                return new IndividualPopulationLimit<>();
            case TESTS:
                return new SizePopulationLimit<>();
            default:
                throw new RuntimeException("Unsupported population limit");
        }
    }

    protected StoppingCondition<T> getStoppingCondition() {
        logger.info("Setting stopping condition: " + Properties.STOPPING_CONDITION);
        switch (Properties.STOPPING_CONDITION) {
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
            case TIMEDELTA:
                return new TimeDeltaStoppingCondition<>();
            default:
                logger.warn("Unknown stopping condition: " + Properties.STOPPING_CONDITION);
                return new MaxGenerationStoppingCondition<>();
        }
    }

    public abstract GeneticAlgorithm<T> getSearchAlgorithm();
}
