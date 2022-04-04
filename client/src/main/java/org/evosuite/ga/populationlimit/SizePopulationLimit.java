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
 * <p>SizePopulationLimit class.</p>
 *
 * @author fraser
 */
public class SizePopulationLimit<T extends Chromosome<T>> implements PopulationLimit<T> {

    private static final long serialVersionUID = 7978512501601348014L;

    public SizePopulationLimit() {
    }

    /**
     * Copy constructor.
     * <p>
     * This constructor is used by {@link org.evosuite.ga.metaheuristics.TestSuiteAdapter} to adapt the generic type
     * parameter.
     * <p>
     * This constructor shall preserve the current state of the SizePopulationLimit (if existing).
     *
     * @param other
     */
    public SizePopulationLimit(SizePopulationLimit<?> other) {
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.PopulationLimit#isPopulationFull(java.util.List)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPopulationFull(List<T> population) {
        final int size = population.stream().mapToInt(Chromosome::size).sum();
        return size >= Properties.POPULATION;
    }

}
