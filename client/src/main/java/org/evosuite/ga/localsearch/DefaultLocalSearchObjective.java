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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Default local search objective only stores a list of fitness functions.
 * It cannot be used to check if an individual has changed, improved or if the objective
 * has been reached.
 *
 * @author Gordon Fraser
 */
public class DefaultLocalSearchObjective<T extends Chromosome<T>> implements LocalSearchObjective<T>,
        Serializable {

    private static final long serialVersionUID = -8640106627078837108L;

    private final List<FitnessFunction<T>> fitnessFunctions = new ArrayList<>();

    // TODO: This assumes we are not doing NSGA-II
    private boolean isMaximization = false;

    /**
     * This operation should not be invoked for this class
     */
    @Override
    public boolean isDone() {
        throw new UnsupportedOperationException("Not implemented for default objective");
    }

    /**
     * This operation should not be invoked for this class
     */
    @Override
    public boolean hasImproved(T chromosome) {
        throw new UnsupportedOperationException("Not implemented for default objective");
    }

    @Override
    public void addFitnessFunction(FitnessFunction<T> fitness) {
        for (FitnessFunction<T> ff : fitnessFunctions) {
            if (ff.isMaximizationFunction() != fitness.isMaximizationFunction()) {
                throw new RuntimeException("Local search only supports composition of multiple criteria");
            }
        }
        isMaximization = fitness.isMaximizationFunction();

        fitnessFunctions.add(fitness);
    }

    @Override
    public boolean isMaximizationObjective() {
        return isMaximization;
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.LocalSearchObjective#getFitnessFunction()
     */

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public List<FitnessFunction<T>> getFitnessFunctions() {
        return fitnessFunctions;
    }


    /**
     * This operation should not be invoked for this class
     */
    @Override
    public int hasChanged(T chromosome) {
        throw new UnsupportedOperationException("Not implemented for default objective");
    }

    /**
     * This operation should not be invoked for this class
     */
    @Override
    public boolean hasNotWorsened(T chromosome) {
        throw new UnsupportedOperationException("Not implemented for default objective");
    }


}
