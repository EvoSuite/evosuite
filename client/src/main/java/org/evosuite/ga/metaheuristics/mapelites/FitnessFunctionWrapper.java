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
package org.evosuite.ga.metaheuristics.mapelites;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;

public class FitnessFunctionWrapper {
    /**
     * Counter for Feedback-Directed Sampling
     *
     * @see https://arxiv.org/pdf/1901.01541.pdf
     */
    private final Counter counter;
    private final TestFitnessFunction fitnessFunction;

    public FitnessFunctionWrapper(TestFitnessFunction fitnessFunction) {
        super();
        this.fitnessFunction = fitnessFunction;
        this.counter = new Counter();
    }

    public double getFitness(TestChromosome individual) {
        return this.fitnessFunction.getFitness(individual);
    }

    public boolean isCovered(TestChromosome individual) {
        return this.fitnessFunction.isCovered(individual);
    }

    public Counter getCounter() {
        return this.counter;
    }
}
