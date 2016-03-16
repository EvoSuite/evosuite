/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.ga.problems.singleobjective;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.NSGAChromosome;
import org.evosuite.ga.problems.Problem;
import org.evosuite.ga.variables.DoubleVariable;

/**
 * Booth's Problem
 * 
 * @author José Campos
 */
@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
public class Booths<T extends NSGAChromosome> implements Problem
{
    private List<FitnessFunction<T>> fitnessFunctions = new ArrayList<FitnessFunction<T>>();

    public Booths()
    {
        super();

        /**
         * Fitness function
         */
        class fFitnessFunction extends FitnessFunction {
            @Override
            public double getFitness(Chromosome c) {
                NSGAChromosome individual = (NSGAChromosome)c;

                double x = ((DoubleVariable) individual.getVariables().get(0)).getValue();
                double y = ((DoubleVariable) individual.getVariables().get(1)).getValue();

                double fitness = Math.pow(x + 2.0 * y - 7.0, 2.0) + Math.pow(2.0 * x + y - 5.0, 2.0);
                updateIndividual(this, individual, fitness);
                return fitness;
            }
            @Override
            public boolean isMaximizationFunction() {
                return false;
            }
        }

        this.fitnessFunctions.add(new fFitnessFunction());
    }

    @Override
    public List getFitnessFunctions() {
        return this.fitnessFunctions;
    }
}
