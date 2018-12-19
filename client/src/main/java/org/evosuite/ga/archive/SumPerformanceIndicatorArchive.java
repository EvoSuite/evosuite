package org.evosuite.ga.archive;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;

/**
 * @author Giovanni Grano
 */
public class SumPerformanceIndicatorArchive<F extends TestFitnessFunction, T extends TestChromosome>
        extends CoverageArchive<F, T> {

    @Override
    public boolean isBetterThanCurrent(T currentSolution, T candidateSolution) {
        double bestValue = currentSolution.getIndicatorValues().values().stream().mapToDouble(Number::doubleValue).sum();
        double currentValue = candidateSolution.getIndicatorValues().values().stream().mapToDouble(Number::doubleValue).sum();
        if (currentValue < bestValue)
            return true;
        else
            return false;
    }
}
