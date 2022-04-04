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
/*
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

import java.io.Serializable;
import java.util.Comparator;

/**
 * This class implements a <code>Comparator</code> (a method for comparing <code>Chromosomes</code>
 * objects) based on the crowd distance of two chromosome objects.
 *
 * @author Annibale Panichella, Fitsum M. Kifetew
 */
public class OnlyCrowdingComparator<T extends Chromosome<T>> implements Comparator<T>,
        Serializable {

    private static final long serialVersionUID = -6576898111709166470L;

    /**
     * Compare the crowd distance value of two chromosome objects.
     *
     * @param c1 a {@link Chromosome} object
     * @param c2 a {@link Chromosome} object
     * @return -1 if crowd distance value of c1 is higher than the crowd distance of c2, 0 if the crowd
     * distance of both objects is equal, or 1 if crowd distance value of c1 is lower than the
     * crowd distance of c2.
     */
    @Override
    public int compare(T c1, T c2) {
        return Double.compare(c2.getDistance(), c1.getDistance());
    }
}
