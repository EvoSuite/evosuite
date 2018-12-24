package org.evosuite.ga.metaheuristics.mosa.structural.adaptive;

import org.evosuite.ga.Chromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;

/**
 * Handles the update of the archive for Performance DynaMOSA.
 * Gets the values of the indicators for the solution in the archive and the candidate ones;
 * Each indicator is normalized and the sum is computed for the two solutions.
 * The solution with the lower sum is preferred
 *
 * @author Giovanni Grano
 */
public class PerformanceUpdate<T extends Chromosome> extends ArchiveUpdate<T> {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceUpdate.class);

    @Override
    public boolean isBetterSolution(T currentSolution, T candidateSolution) {
        double sumArchive = 0;
        double sumNewIndividual = 0;

        LinkedHashMap<String, Double> archiveIndicators = currentSolution.getIndicatorValues();
        LinkedHashMap<String, Double> currentIndicators = candidateSolution.getIndicatorValues();

        for (String indicator : archiveIndicators.keySet()) {
            double archiveInd = archiveIndicators.get(indicator);
            double currentInd = currentIndicators.get(indicator);
            logger.debug("Indicator = {}; Archive = {}; Current = {}", indicator, archiveInd, currentInd);

            // normalize with the formula x/x+1 and sum up
            sumArchive += normalize(archiveInd);
            sumNewIndividual += normalize(currentInd);
        }
        logger.debug("Sum Old = {}, Sum New {}", sumArchive, sumNewIndividual);
        if (sumNewIndividual < sumArchive)
            return true;
        else
            return false;
    }
}
