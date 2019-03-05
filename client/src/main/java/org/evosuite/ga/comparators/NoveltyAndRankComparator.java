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
package org.evosuite.ga.comparators;

import java.io.Serializable;
import java.util.Comparator;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;

/**
 * This class implements a <code>Comparator</code> (a method for comparing <code>Chromosomes</code>
 * objects) based on the dominance test, as in NSGA-II.
 *
 * @author José Campos
 */
public class NoveltyAndRankComparator<T extends Chromosome> implements Comparator<T>, Serializable {

    private static final long serialVersionUID = -1663917547588039444L;

    private boolean isToMaximize;

    private boolean isRankBasedCompetition = false;

    public NoveltyAndRankComparator() {
        this.isToMaximize = false;
    }

    public NoveltyAndRankComparator(boolean maximize) {
        this.isToMaximize = maximize;
    }

    public boolean isRankBasedCompetition() {
        return this.isRankBasedCompetition;
    }

    public void setRankBasedCompetition(boolean rankBasedCompetition) {
        this.isRankBasedCompetition = rankBasedCompetition;
    }

    /**
     * Compares two solutions.
     *
     * @param c1 Object representing the first <code>Solution</code>.
     * @param c2 Object representing the second <code>Solution</code>.
     * @return -1, or 0, or 1 according to the non-dominated ranks
     */
    @Override
    public int compare(Chromosome c1, Chromosome c2) {

        if (c1 == null) {
            return 1;
        }
        if (c2 == null) {
            return -1;
        }

        if (c1.getNoveltyScore() == c2.getNoveltyScore() && c1.getRank() == c2.getRank()) {
            return 0;
        }

        if (this.isRankBasedCompetition) {
            if (c1.getRank() < c2.getRank()) {
                return -1;
            } else if (c1.getRank() > c2.getRank()) {
                return 1;
            } else if (c1.getRank() == c2.getRank()) {
                return (c2.getNoveltyScore() < c1.getNoveltyScore())? -1 : 1;
            }
        }

        if (c2.getNoveltyScore() < c1.getNoveltyScore()) {
            return -1;
        } else if (c2.getNoveltyScore() > c1.getNoveltyScore()) {
            return 1;
        } else if (c1.getNoveltyScore() == c2.getNoveltyScore()) {
            return 0;
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

