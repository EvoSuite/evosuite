package org.evosuite.ga.problems;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.NSGAChromosome;
import org.evosuite.ga.variables.DoubleVariable;

/**
 * One variable Problem
 * 
 * f(x) = x^2
 * 
 * Optimal Solutions x = 0
 * 
 * @author José Campos
 */
@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
public class OneVariableProblem<T extends NSGAChromosome> implements Problem
{
    private List<FitnessFunction<T>> fitnessFunctions = new ArrayList<FitnessFunction<T>>();

    public OneVariableProblem()
    {
        super();

        /**
         * Fitness function
         * f(x) = x^2
         */
        class fFitnessFunction extends FitnessFunction {
            @Override
            public double getFitness(Chromosome c) {
                NSGAChromosome individual = (NSGAChromosome)c;

                DoubleVariable dv = (DoubleVariable) individual.getVariables().get(0);
                double x = dv.getValue();
                double fitness = x * x;

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
