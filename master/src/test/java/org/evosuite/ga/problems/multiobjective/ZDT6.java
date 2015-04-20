package org.evosuite.ga.problems.multiobjective;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.NSGAChromosome;
import org.evosuite.ga.problems.Problem;
import org.evosuite.ga.variables.DoubleVariable;

/**
 * ZDT6 Problem
 * 
 * @author José Campos
 */
@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
public class ZDT6<T extends NSGAChromosome> implements Problem
{
	private List<FitnessFunction<T>> fitnessFunctions = new ArrayList<FitnessFunction<T>>();

	public ZDT6() {
		super();

		/**
		 * First fitness function
		 */
		class f1FitnessFunction extends FitnessFunction {
			@Override
			public double getFitness(Chromosome c) {
				NSGAChromosome individual = (NSGAChromosome)c;

				double x = ((DoubleVariable)individual.getVariable(0)).getValue();
				double fitness = 1.0 - Math.exp(-4.0 * x) * Math.pow(Math.sin(6.0 * Math.PI * x), 6.0);
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

				// f1
				double x = ((DoubleVariable)individual.getVariable(0)).getValue();
                double f1 = 1.0 - Math.exp(-4.0 * x) * Math.pow(Math.sin(6.0 * Math.PI * x), 6.0);

                // f2
				double sum = 0.0;
				for (int i = 1; i < individual.getNumberOfVariables(); i++) {
					double dv = ((DoubleVariable) individual.getVariable(i)).getValue();
					sum += dv;
				}

				double g = 1.0 + 9.0 * Math.pow(sum / (individual.getNumberOfVariables() - 1.0), 0.25);
				double h = 1.0 - Math.pow(f1 / g, 2.0);

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
