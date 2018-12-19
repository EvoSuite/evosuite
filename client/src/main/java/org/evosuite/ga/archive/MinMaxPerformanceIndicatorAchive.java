package org.evosuite.ga.archive;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;

import java.util.LinkedHashMap;

/**
 * @author Giovanni Grano
 */
public class MinMaxPerformanceIndicatorAchive<F extends TestFitnessFunction, T extends TestChromosome>
        extends CoverageArchive<F, T> {

    @Override
    public boolean isBetterThanCurrent(T currentSolution, T candidateSolution) {
        double sumArchive = 0;
        double sumNewIndividual = 0;

        LinkedHashMap<String, Double> archiveIndicators = currentSolution.getIndicatorValues();
        LinkedHashMap<String, Double> currentIndicators = candidateSolution.getIndicatorValues();

        for (String indicator : archiveIndicators.keySet()) {
            double archiveInd = archiveIndicators.get(indicator);
            double currentInd = currentIndicators.get(indicator);

            double min = 0;
            double max = 0;

            if (archiveInd > currentInd) {
                max = archiveInd;
                min = currentInd;
            } else if (archiveInd < currentInd) {
                min = archiveInd;
                max = currentInd;
            }

            if (min != max) {
                sumArchive += (archiveInd - min)/(max - min);
                sumNewIndividual += (currentInd - min)/(max - min);
            }

        }

        if (sumNewIndividual < sumArchive)
            return true;
        else
            return false;
    }
}
