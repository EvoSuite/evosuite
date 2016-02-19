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
package org.evosuite.ga.problems.multiobjective;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.NSGAChromosome;
import org.evosuite.ga.problems.Problem;
import org.evosuite.ga.variables.DoubleVariable;

/**
 * POL Problem
 * 
 * @author José Campos
 */
@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
public class POL<T extends NSGAChromosome> implements Problem
{
	private List<FitnessFunction<T>> fitnessFunctions = new ArrayList<FitnessFunction<T>>();

	public POL() {
		super();

		/**
		 * First fitness function
		 */
		class f1FitnessFunction extends FitnessFunction {
			@Override
			public double getFitness(Chromosome c) {
				NSGAChromosome individual = (NSGAChromosome)c;

				double x1 = ((DoubleVariable)individual.getVariable(0)).getValue();
				double x2 = ((DoubleVariable)individual.getVariable(1)).getValue();

				double A1 = 0.5 * Math.sin(1.0) - 2.0 * Math.cos(1.0) + Math.sin(2.0) - 1.5 * Math.cos(2.0);
				double A2 = 1.5 * Math.sin(1.0) - Math.cos(1.0) + 2.0 * Math.sin(2.0) - 0.5 * Math.cos(2.0);
				double B1 = 0.5 * Math.sin(x1) - 2.0 * Math.cos(x1) + Math.sin(x2) - 1.5 * Math.cos(x2);
				double B2 = 1.5 * Math.sin(x1) - Math.cos(x1) + 2.0 * Math.sin(x2) - 0.5 * Math.cos(x2);

				double fitness = 1.0 + Math.pow(A1 - B1, 2.0) + Math.pow(A2 - B2, 2.0);
				updateIndividual(this, individual, fitness);
				return fitness;
			}
			@Override
			public boolean isMaximizationFunction() {
				return false;
			}
		}

		/**
		 * Second fitness function
		 */
		class f2FitnessFunction extends FitnessFunction {
			@Override
			public double getFitness(Chromosome c) {
				NSGAChromosome individual = (NSGAChromosome)c;

				double x1 = ((DoubleVariable)individual.getVariable(0)).getValue();
                double x2 = ((DoubleVariable)individual.getVariable(1)).getValue();

                double fitness = Math.pow(x1 + 3.0, 2.0) + Math.pow(x2 + 1.0, 2.0);
				updateIndividual(this, individual, fitness);
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
	public List<FitnessFunction<T>> getFitnessFunctions() {
		return this.fitnessFunctions;
	}
}
