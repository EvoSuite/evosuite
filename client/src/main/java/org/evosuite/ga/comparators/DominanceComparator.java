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
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class implements a <code>Comparator</code> (a method for comparing <code>Chromosomes</code>
 * objects) based on the dominance test.
 *
 * @author José Campos, Annibale Panichella
 */
public class DominanceComparator<T extends Chromosome<T>> implements Comparator<T>, Serializable {

    private static final long serialVersionUID = -2154238776555768364L;

    private Set<FitnessFunction<T>> objectives;


    public DominanceComparator() {
        this.objectives = null;
    }

    /**
     * @param goals set of target goals to consider when computing the dominance relationship
     */
    public DominanceComparator(Set<? extends FitnessFunction<T>> goals) {
        this.objectives = new LinkedHashSet<>(goals);
    }

    /**
     * @param goal to consider when computing the dominance relationship
     */
    public DominanceComparator(FitnessFunction<T> goal) {
        this.objectives = new LinkedHashSet<>();
        this.objectives.add(goal);
    }

    /**
     * Compares two chromosome objects in terms of dominance.
     * <p>
     * http://en.wikipedia.org/wiki/Multi-objective_optimization#Introduction
     *
     * @param c1 a {@link org.evosuite.ga.Chromosome} object
     * @param c2 a {@link org.evosuite.ga.Chromosome} object
     * @return -1 if c1 dominates c2, +1 if c2 dominates c1, 0 if both are non-dominated
     */
    @Override
    public int compare(T c1, T c2) {

        if (c1 == null) {
            return 1;
        } else if (c2 == null) {
            return -1;
        }

        boolean dominate1 = false;
        boolean dominate2 = false;

        if (this.objectives == null) {
            this.objectives = new LinkedHashSet<>(c1.getFitnessValues().keySet());
        }

        for (FitnessFunction<T> ff : this.objectives) {
            int flag = Double.compare(c1.getFitness(ff), c2.getFitness(ff));

            if (flag < 0) {
                dominate1 = true;

                if (dominate2) {
                    return 0;
                }
            } else if (flag > 0) {
                dominate2 = true;

                if (dominate1) {
                    return 0;
                }
            }
        }

        if (dominate1 == dominate2) {
            return 0; // no one dominate the other
        } else if (dominate1) {
            return -1; // c1 dominates
        } else {
            return 1; // c2 dominates
        }
    }
}
