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
package org.evosuite.ga;

import java.util.List;

/**
 * An interface that defines the four neighbourhood models used with the cGA
 *
 * @author Nasser Albunian
 */
public interface NeighborModels<T extends Chromosome<T>> {

    List<T> ringTopology(List<T> collection, int position);

    List<T> linearFive(List<T> collection, int position);

    List<T> compactNine(List<T> collection, int position);

    List<T> compactThirteen(List<T> collection, int position);

    /*
     * Neighbourhood positions
     */
    enum Positions {
        N,
        S,
        E,
        W,
        NW,
        SW,
        NE,
        SE
    }
}
