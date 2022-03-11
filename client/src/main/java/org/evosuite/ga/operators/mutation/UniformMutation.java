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
package org.evosuite.ga.operators.mutation;

import org.evosuite.utils.Randomness;

public class UniformMutation extends MutationDistribution {

    private static final long serialVersionUID = -2352083320831156232L;

    public UniformMutation(int sizeOfDistribution) {
        this.sizeOfDistribution = sizeOfDistribution;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean toMutate(int index) {
        return Randomness.nextDouble() < 1.0 / (double) this.sizeOfDistribution;
    }
}
