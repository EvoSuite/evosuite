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
package org.evosuite.ga;

import java.util.Objects;

/**
 * A wrapper class to treat fitness functions for chromosomes of type {@code T} as fitness functions
 * for chromosomes of type {@code U}. This is mainly useful to satisfy the type checker; other than
 * wrapping and retrieving a given fitness functions, nothing useful can actually be done with this
 * mocking class. One possible use case is to bridge the gap between {@code TestSuiteChromosome}s
 * and {@code TestChromosome}s, as is the case for {@code MOSA} in
 * {@link org.evosuite.strategy.MOSuiteStrategy}.
 *
 * @param <T> the chromosome type of the wrapped fitness function
 * @param <U> the chromosome type of the fitness function this mock should masquerade as
 * @author Sebastian Schweikl
 */
public class FitnessFunctionMock<T extends Chromosome<T>, U extends Chromosome<U>>
        extends FitnessFunction<U> {
    private static final long serialVersionUID = -2764090795456211662L;
    /**
     * The wrapped chromosome factory.
     */
    private final FitnessFunction<T> wrapped;

    /**
     * Creates a new mock of the given factory.
     *
     * @param wrapped the chromosome factory to mock; must not be {@code null}
     */
    public FitnessFunctionMock(final FitnessFunction<T> wrapped) {
        this.wrapped = Objects.requireNonNull(wrapped);
    }

    /**
     * Returns the wrapped chromosome factory.
     *
     * @return the wrapped chromosome factory
     */
    public FitnessFunction<T> getWrapped() {
        return wrapped;
    }

    /**
     * Throws an {@code UnsupportedOperationException} when called.
     *
     * @return never completes, always throws {@code UnsupportedOperationException}
     * @throws UnsupportedOperationException always fails, never succeeds
     */
    @Override
    public double getFitness(final U individual) {
        throw new UnsupportedOperationException("getFitness() called on mock");
    }

    /**
     * Throws an {@code UnsupportedOperationException} when called.
     *
     * @return never completes, always throws {@code UnsupportedOperationException}
     * @throws UnsupportedOperationException always fails, never succeeds
     */
    @Override
    public boolean isMaximizationFunction() {
        throw new UnsupportedOperationException("isMaximizationFunction() called on mock");

    }
}
