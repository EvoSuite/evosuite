package org.evosuite.ga.metaheuristics.paes.Grid;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;

public class DummyFitnessFunction extends FitnessFunction {
    @Override
    public double getFitness(Chromosome individual) {
        return 0;
    }

    @Override
    public boolean isMaximizationFunction() {
        return false;
    }
}
