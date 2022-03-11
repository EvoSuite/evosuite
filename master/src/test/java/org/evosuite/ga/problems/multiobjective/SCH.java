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
package org.evosuite.ga.problems.multiobjective;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.NSGAChromosome;
import org.evosuite.ga.problems.Problem;
import org.evosuite.ga.variables.DoubleVariable;

/**
 * SCH Problem
 * <p>
 * f1(x) = x^2
 * f2(x) = (x-2)^2
 * <p>
 * Optimal Solutions x E [0,2]
 *
 * @author José Campos
 */
@SuppressWarnings({"serial"})
public class SCH implements Problem<NSGAChromosome> {
    private final List<FitnessFunction<NSGAChromosome>> fitnessFunctions = new ArrayList<>();

    public SCH() {
        super();

        /**
         * First fitness function
         * f1(x) = x^2
         */
        class f1FitnessFunction extends FitnessFunction<NSGAChromosome> {
            @Override
            public double getFitness(NSGAChromosome c) {

                DoubleVariable dv = (DoubleVariable) c.getVariables().get(0);
                double x = dv.getValue();
                double fitness = x * x;

                updateIndividual(c, fitness);
                return fitness;
            }

            @Override
            public boolean isMaximizationFunction() {
                return false;
            }
        }

        /**
         * Second fitness function
         * f2(x) = (x-2)^2
         */
        class f2FitnessFunction extends FitnessFunction<NSGAChromosome> {
            @Override
            public double getFitness(NSGAChromosome c) {

                DoubleVariable dv = (DoubleVariable) c.getVariables().get(0);
                double x = dv.getValue();
                double fitness = (x - 2) * (x - 2);

                updateIndividual(c, fitness);
                return fitness;
            }

            @Override
            public boolean isMaximizationFunction() {
                return false;
            }
        }

        this.fitnessFunctions.add(new f1FitnessFunction());
        this.fitnessFunctions.add(new f2FitnessFunction());
    }

    @Override
    public List<FitnessFunction<NSGAChromosome>> getFitnessFunctions() {
        return this.fitnessFunctions;
    }
}
