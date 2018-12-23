package org.evosuite.ga.metaheuristics.mosa.structural.adaptive;

import org.evosuite.ga.Chromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;

/**
 * @author Giovanni Grano
 */
public class PerformanceUpdate<T extends Chromosome> implements ArchiveUpdate<T> {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceUpdate.class);

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
            logger.debug("Indicator = {}; Archive = {}; Current = {}", indicator, archiveInd, currentInd);

            sumArchive += archiveInd;
            sumNewIndividual += currentInd;
        }
        logger.debug("Sum Old = {}, Sum New {}", sumArchive, sumNewIndividual);
        if (sumNewIndividual < sumArchive)
            return true;
        else
            return false;
    }
}
