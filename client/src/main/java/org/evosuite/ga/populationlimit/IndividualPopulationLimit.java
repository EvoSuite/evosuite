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
package org.evosuite.ga.populationlimit;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;

import java.util.List;


/**
 * <p>IndividualPopulationLimit class.</p>
 *
 * @author Gordon Fraser
 */
public class IndividualPopulationLimit<T extends Chromosome<T>> implements PopulationLimit<T> {

    private static final long serialVersionUID = -3985726226793280031L;

    public IndividualPopulationLimit() {
    }

    /**
     * Copy Constructor
     * <p>
     * This constructor is used by {@link org.evosuite.ga.metaheuristics.TestSuiteAdapter} to adapt the generic type
     * parameter.
     * <p>
     * This constructor shall preserve the current state of the IndividualPopulationLimit (if existing).
     *
     * @param other
     */
    public IndividualPopulationLimit(IndividualPopulationLimit<?> other) {
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.PopulationLimit#isPopulationFull(java.util.List)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPopulationFull(List<T> population) {
        return population.size() >= Properties.POPULATION;
    }

}
