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
package org.evosuite.ga.comparators;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Sort a Collection of Chromosomes by their fitness value
 *
 * @author José Campos
 */
public class SortByFitness<T extends Chromosome<T>> implements Comparator<T>, Serializable {

    private static final long serialVersionUID = 4982933698286500461L;

    private final FitnessFunction<T> ff;

    private final boolean order;

    /**
     * @param ff
     * @param desc descending order
     */
    public SortByFitness(FitnessFunction<T> ff, boolean desc) {
        this.ff = ff;
        this.order = desc;
    }

    @Override
    public int compare(T c1, T c2) {
        if (c1 == null)
            return 1;
        else if (c2 == null)
            return -1;

        double objetive1 = c1.getFitness(this.ff);
        double objetive2 = c2.getFitness(this.ff);

        return this.order
                ? Double.compare(objetive2, objetive1)
                : Double.compare(objetive1, objetive2);
    }
}
