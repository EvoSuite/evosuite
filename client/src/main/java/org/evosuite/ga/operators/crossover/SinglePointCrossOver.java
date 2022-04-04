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
package org.evosuite.ga.operators.crossover;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.utils.Randomness;

/**
 * Select one random point in each individual and cross over (TPX)
 *
 * @author Gordon Fraser
 */
public class SinglePointCrossOver<T extends Chromosome<T>> extends CrossOverFunction<T> {

    private static final long serialVersionUID = 2881387570766261795L;

    /**
     * {@inheritDoc}
     * <p>
     * A different splitting point is selected for each individual
     *
     * @param parent1
     * @param parent2
     */
    @Override
    public void crossOver(T parent1, T parent2)
            throws ConstructionFailedException {

        if (parent1.size() < 2 || parent2.size() < 2) {
            return;
        }
        // Choose a position in the middle
        int point1 = Randomness.nextInt(parent1.size() - 1) + 1;
        int point2 = Randomness.nextInt(parent2.size() - 1) + 1;

        T t1 = parent1.clone();
        T t2 = parent2.clone();

        parent1.crossOver(t2, point1, point2);
        parent2.crossOver(t1, point2, point1);
    }

}
