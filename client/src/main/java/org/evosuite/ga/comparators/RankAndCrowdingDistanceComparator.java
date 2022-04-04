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
 * This class implements a <code>Comparator</code> (a method for comparing <code>Chromosomes</code>
 * objects) based on the dominance test, as in NSGA-II.
 *
 * @author José Campos
 */
public class RankAndCrowdingDistanceComparator<T extends Chromosome<T>> implements Comparator<T>,
        Serializable {

    private static final long serialVersionUID = -1663917547588039444L;

    private boolean isToMaximize;

    public RankAndCrowdingDistanceComparator() {
        this.isToMaximize = false;
    }

    public RankAndCrowdingDistanceComparator(boolean maximize) {
        this.isToMaximize = maximize;
    }

    public RankAndCrowdingDistanceComparator(RankAndCrowdingDistanceComparator<?> other) {
        this.isToMaximize = other.isToMaximize;
    }

    /**
     * Compares two solutions.
     *
     * @param c1 Object representing the first <code>Solution</code>.
     * @param c2 Object representing the second <code>Solution</code>.
     * @return -1, or 0, or 1 according to the non-dominated ranks
     */
    @Override
    public int compare(T c1, T c2) {

        if (c1 == null) {
            return 1;
        }
        if (c2 == null) {
            return -1;
        }

        if (c1.getRank() == c2.getRank() && c1.getDistance() == c2.getDistance()) {
            return 0;
        }

        if (this.isToMaximize) {
            if (c1.getRank() < c2.getRank()) {
                return 1;
            } else if (c1.getRank() > c2.getRank()) {
                return -1;
            } else if (c1.getRank() == c2.getRank()) {
                return (c1.getDistance() > c2.getDistance()) ? -1 : 1;
            }
        } else {
            if (c1.getRank() < c2.getRank()) {
                return -1;
            } else if (c1.getRank() > c2.getRank()) {
                return 1;
            } else if (c1.getRank() == c2.getRank()) {
                return (c1.getDistance() > c2.getDistance()) ? -1 : 1;
            }
        }

        return 0;
    }

    /**
     * Are we maximizing or minimizing fitness?
     *
     * @param max a boolean.
     */
    public void setMaximize(boolean max) {
        this.isToMaximize = max;
    }
}
