/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.ga.operators.selection;

import org.evosuite.ga.Chromosome;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * {@inheritDoc}
 * <p>
 * Select individual by highest fitness
 */
public class BestKSelection<T extends Chromosome<T>> extends SelectionFunction<T> {

    private static final long serialVersionUID = -7106376944811871449L;

    public BestKSelection() {
    }

    public BestKSelection(BestKSelection<?> other) {
        // empty copy constructor
    }

    /**
     * {@inheritDoc}
     * <p>
     * Population has to be sorted!
     */
    @Override
    public List<T> select(List<T> population, int number) {
        return population.stream()
                .limit(number)
                .collect(toList());
    }

    /**
     * Selects index of best offspring.
     * <p>
     * Population has to be sorted!
     */
    @Override
    public int getIndex(List<T> population) {
        return 0;
    }
}
