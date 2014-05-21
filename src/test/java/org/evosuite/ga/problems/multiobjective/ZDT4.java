package org.evosuite.ga.problems.multiobjective;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.NSGAChromosome;
import org.evosuite.ga.problems.Problem;
import org.evosuite.ga.variables.DoubleVariable;

/**
 * ZDT4 Problem
 * 
 * @author José Campos
 */
@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
public class ZDT4<T extends NSGAChromosome> implements Problem
{
	private List<FitnessFunction<T>> fitnessFunctions = new ArrayList<FitnessFunction<T>>();

	public ZDT4() {
		super();

		/**
		 * First fitness function
		 */
		class f1FitnessFunction extends FitnessFunction {
			@Override
			public double getFitness(Chromosome c) {
				NSGAChromosome individual = (NSGAChromosome)c;

				double fitness = ((DoubleVariable)individual.getVariable(0)).getValue();
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

				double g = 0.0;
				for (int i = 1; i < individual.getNumberOfVariables(); i++) {
					DoubleVariable dv = (DoubleVariable) individual.getVariable(i);
					g += Math.pow(dv.getValue(), 2.0) - 10.0 * Math.cos(4.0 * Math.PI * dv.getValue() / 180.0);
				}
				g += 1.0 + 10.0 * (individual.getNumberOfVariables() - 1);

				double h = 1.0 - Math.sqrt(((DoubleVariable)individual.getVariable(0)).getValue() / g);

				double fitness = g * h;
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
