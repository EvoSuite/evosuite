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
package org.evosuite.ga.operators.ranking;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Interface for ranking algorithms.
 *
 * @param <T> the type of chromosomes this ranking function works with
 * @author Annibale Panichella, Fitsum M. Kifetew
 */
public interface RankingFunction<T extends Chromosome<T>> extends Serializable {

    /**
     * Computes the ranking assignment for the given population of solutions w.r.t. the given set
     * of coverage goals. More precisely, every individual in the population is assigned to a
     * specific dominance front, which can afterwards be retrieved by calling
     * {@link RankingFunction#getSubfront(int)}. The concrete dominance comparator
     * used for computing the ranking is defined by subclasses implementing this interface.
     *
     * @param solutions       the population to rank
     * @param uncovered_goals the set of coverage goals to consider for the ranking assignment
     */
    void computeRankingAssignment(List<T> solutions,
                                  Set<? extends FitnessFunction<T>> uncovered_goals);

    /**
     * Returns the sub-front of {@link org.evosuite.ga.Chromosome} objects of the given rank. Sub-
     * fronts are ordered starting from 0 in ascending order, i.e., the first non-dominated front
     * has rank 0, the next sub-front rank 1 etc.
     *
     * @param rank the sub-front to retrieve
     * @return a list of solutions of a given rank.
     */
    List<T> getSubfront(int rank);

    /**
     * Returns the total number of sub-fronts found.
     */
    int getNumberOfSubfronts();

}