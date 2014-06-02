package org.evosuite.ga.problems.singleobjective;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.NSGAChromosome;
import org.evosuite.ga.problems.Problem;
import org.evosuite.ga.variables.DoubleVariable;

/**
 * Three-Hump Problem
 * 
 * @author José Campos
 */
@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
public class ThreeHump<T extends NSGAChromosome> implements Problem
{
    private List<FitnessFunction<T>> fitnessFunctions = new ArrayList<FitnessFunction<T>>();

    public ThreeHump()
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

                double fitness = (2.0 * Math.pow(x, 2.0))
                                - (1.05 * Math.pow(x, 4.0))
                                + (Math.pow(x, 6.0) / 6.0)
                                + (x * y)
                                + (Math.pow(y, 2.0));
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
