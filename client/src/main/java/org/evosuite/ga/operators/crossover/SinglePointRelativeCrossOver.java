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
 * Cross over individuals at relative position
 *
 * @author Gordon Fraser
 */
public class SinglePointRelativeCrossOver<T extends Chromosome<T>> extends CrossOverFunction<T> {

    private static final long serialVersionUID = -5320348525459502224L;

    /**
     * {@inheritDoc}
     * <p>
     * The splitting point is not an absolute value but a relative value (eg, at
     * position 70% of n). For example, if n1=10 and n2=20 and splitting point
     * is 70%, we would have position 7 in the first and 14 in the second.
     * Therefore, the offspring d have n<=max(n1,n2)
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

        T t1 = parent1.clone();
        T t2 = parent2.clone();
        // Choose a position in the middle
        float splitPoint = Randomness.nextFloat();

        int pos1 = ((int) Math.floor((t1.size() - 1) * splitPoint)) + 1;
        int pos2 = ((int) Math.floor((t2.size() - 1) * splitPoint)) + 1;

        parent1.crossOver(t2, pos1, pos2);
        parent2.crossOver(t1, pos2, pos1);
    }

}
