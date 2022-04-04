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
package org.evosuite.ga.operators.crossover;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ConstructionFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Cross over two individuals
 *
 * @author Gordon Fraser
 */
public abstract class CrossOverFunction<T extends Chromosome<T>> implements Serializable {

    private static final long serialVersionUID = -4765602400132319324L;

    /**
     * Constant <code>logger</code>
     */
    protected static final Logger logger = LoggerFactory.getLogger(CrossOverFunction.class);

    /**
     * Replace parents with crossed over individuals
     *
     * @param parent1 a {@link Chromosome} object.
     * @param parent2 a {@link Chromosome} object.
     * @throws org.evosuite.ga.ConstructionFailedException if any.
     */
    public abstract void crossOver(T parent1, T parent2)
            throws ConstructionFailedException;

}
