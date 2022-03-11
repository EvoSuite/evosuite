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

import org.evosuite.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public abstract class MutationDistribution implements Serializable {

    private static final long serialVersionUID = -5800252656232641574L;

    /**
     * Constant <code>logger</code>
     */
    protected static final Logger logger = LoggerFactory.getLogger(MutationDistribution.class);

    protected int sizeOfDistribution;

    /**
     * Check whether a particular chromosome is allowed to be mutated
     *
     * @param index
     * @return true if mutation is allowed, false otherwise
     */
    public abstract boolean toMutate(int index);

    /**
     * Get mutation distribution defined in
     * {@link org.evosuite.Properties.MutationProbabilityDistribution}
     *
     * @param sizeOfDistribution
     * @return
     */
    public static MutationDistribution getMutationDistribution(int sizeOfDistribution) {
        switch (Properties.MUTATION_PROBABILITY_DISTRIBUTION) {
            case UNIFORM:
            default:
                logger.debug("Using uniform mutation distribution");
                return new UniformMutation(sizeOfDistribution);
            case BINOMIAL:
                logger.debug("Using binomial mutation distribution");
                return new BinomialMutation(sizeOfDistribution);
        }
    }
}
