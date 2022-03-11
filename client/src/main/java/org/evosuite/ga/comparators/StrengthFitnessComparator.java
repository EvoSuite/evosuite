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

import java.io.Serializable;
import java.util.Comparator;

/**
 * StrengthFitnessComparator class.
 *
 * @author José Campos
 */
public class StrengthFitnessComparator implements Comparator<Chromosome<?>>, Serializable {

    private static final long serialVersionUID = 1365198556267160032L;

    @Override
    public int compare(Chromosome<?> c1, Chromosome<?> c2) {
        if (c1 == null && c2 == null) {
            return 0;
        } else if (c1 == null) {
            return 1;
        } else if (c2 == null) {
            return -1;
        }

        double strengthC1 = c1.getDistance(); // TODO: should we change name of the function?
        double strengthC2 = c2.getDistance();

        return Double.compare(strengthC1, strengthC2);
    }
}
