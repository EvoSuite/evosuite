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
