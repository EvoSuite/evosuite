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
public class KUR<T extends NSGAChromosome> implements Problem
{
	private List<FitnessFunction<T>> fitnessFunctions = new ArrayList<FitnessFunction<T>>();

	public KUR() {
		super();

		/**
		 * First fitness function
		 */
		class f1FitnessFunction extends FitnessFunction {
			@Override
			public double getFitness(Chromosome c) {
				NSGAChromosome individual = (NSGAChromosome)c;

				double fitness = 0.0;
                for (int i = 0; i < individual.getNumberOfVariables() - 1; i++) {
                    DoubleVariable dv = (DoubleVariable) individual.getVariables().get(i);
                    DoubleVariable nextdv = (DoubleVariable) individual.getVariables().get(i+1);

                    fitness += -10 * Math.exp(-0.2 * Math.sqrt( Math.pow(dv.getValue(), 2) + Math.pow(nextdv.getValue(), 2) ));
                }

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

				double fitness = 0.0;
                for (int i = 0; i < individual.getNumberOfVariables(); i++) {
                    DoubleVariable dv = (DoubleVariable) individual.getVariables().get(i);

                    fitness += Math.pow(Math.abs(dv.getValue()), 0.8) + 5 * Math.sin(Math.pow(dv.getValue(), 3));
                }

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
