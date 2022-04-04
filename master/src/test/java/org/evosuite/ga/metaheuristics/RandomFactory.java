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
package org.evosuite.ga.metaheuristics;

import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.NSGAChromosome;

/**
 * @author José Campos
 */
public class RandomFactory implements ChromosomeFactory<NSGAChromosome> {
    private static final long serialVersionUID = -6984639266849566298L;

    private final double upperBound;
    private final double lowerBound;

    private final int number_of_variables;

    private boolean ZDT4 = false;

    /**
     * @param z  are you executing ZDT4 problem?
     * @param nv number of variables
     * @param lb lowerBound
     * @param ub upperBound
     */
    public RandomFactory(boolean z, int nv, double lb, double ub) {
        this.ZDT4 = z;
        this.number_of_variables = nv;
        this.lowerBound = lb;
        this.upperBound = ub;
    }

    @Override
    public NSGAChromosome getChromosome() {
        return new NSGAChromosome(this.ZDT4,
                this.number_of_variables,
                this.lowerBound, this.upperBound);
    }
}
