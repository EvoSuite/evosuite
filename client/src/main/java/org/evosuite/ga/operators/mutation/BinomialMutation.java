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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.DoubleStream;

public class BinomialMutation extends MutationDistribution {

    private static final long serialVersionUID = 9013772318848850918L;

    private final Set<Integer> bitsToBeModified;

    public BinomialMutation(int sizeOfDistribution) {
        int numBits = howManyBits(sizeOfDistribution, 1.0 / (double) sizeOfDistribution);
        this.bitsToBeModified = new LinkedHashSet<>();
        while (this.bitsToBeModified.size() < numBits) {
            this.bitsToBeModified.add(Randomness.nextInt(0, sizeOfDistribution));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean toMutate(int index) {
        return this.bitsToBeModified.contains(index);
    }

    /**
     * Number of bits to be mutated (in our context, number of test cases to be mutated) according to
     * a binomial distribution.
     *
     * @param numTrials
     * @param probability
     * @return number of test cases to be mutated
     */
    private int howManyBits(int numTrials, double probability) {
        return (int) DoubleStream.generate(Randomness::nextDouble)
                .limit(numTrials)
                .filter(d -> d <= probability)
                .count();
    }
}
