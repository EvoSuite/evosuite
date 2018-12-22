package org.evosuite.ga.metaheuristics.mosa.structural.adaptive;

import org.evosuite.ga.Chromosome;

import java.util.LinkedHashMap;

/**
 * This classes is used by the archive for the update based on the min-max value of the indicators
 * Please, recall that the update for the archive is done locally i.e., not considering the score in respect to the
 * entire population, but only looking at the scores of the best and of the candidate solution.
 *
 * @author Giovanni Grano
 */
public class MinMaxArchiveUpdater<T extends Chromosome> implements ArchiveUpdate<T> {

    @Override
    @SuppressWarnings("Duplicates")
    public boolean isBetterSolution(T currentSolution, T candidateSolution) {
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
