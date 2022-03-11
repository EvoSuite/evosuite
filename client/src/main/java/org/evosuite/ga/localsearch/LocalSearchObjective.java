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
package org.evosuite.ga.localsearch;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;

import java.util.List;

/**
 * Represents a local search objective that will be used during local search to
 * assess the success (or failure) of a local search to a given chromosome
 * (it could be TestSuiteChromosome or TestChromosome).
 * <p>
 * The local search objective contains a list of fitness functions that are used
 * to compute the fitness of an individual.
 *
 * @author Gordon Fraser
 */
public interface LocalSearchObjective<T extends Chromosome<T>> {

    /**
     * true if the objective was achieved
     *
     * @return
     */
    boolean isDone();

    /**
     * Returns true if all the fitness functions are maximising functions
     * (Observe that it is not possible to store simoustaneously maximising and
     * minimising fitness functions)
     *
     * @return
     */
    boolean isMaximizationObjective();

    /**
     * Returns true if the individual has improved due to the applied local
     * search
     *
     * @param chromosome a {@link org.evosuite.ga.Chromosome} object.
     * @return a boolean.
     */
    boolean hasImproved(T chromosome);

    /**
     * Returns true if the individual has not worsened due to the applied local
     * search
     *
     * @param chromosome a {@link org.evosuite.ga.Chromosome} object.
     * @return a boolean.
     */
    boolean hasNotWorsened(T chromosome);

    /**
     * Returns true if the individual has changed since local search started
     *
     * @param chromosome a {@link org.evosuite.ga.Chromosome} object.
     * @return a int.
     */
    int hasChanged(T chromosome);

    void addFitnessFunction(FitnessFunction<T> fitness);

    /**
     * Returns a list with all the fitness functions stored in this local search
     * objective
     *
     * @return a {@link org.evosuite.ga.FitnessFunction} object.
     */
    List<FitnessFunction<T>> getFitnessFunctions();

}
