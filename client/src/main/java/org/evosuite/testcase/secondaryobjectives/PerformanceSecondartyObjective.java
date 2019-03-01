package org.evosuite.testcase.secondaryobjectives;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.SecondaryObjective;

public class PerformanceSecondartyObjective extends SecondaryObjective {
    @Override
    public int compareChromosomes(Chromosome chromosome1, Chromosome chromosome2) {
        logger.debug("Comparing performance scores: " + chromosome1.size() + " vs "
                + chromosome2.size());
        double diff = chromosome1.getPerformanceScore() - chromosome2.getPerformanceScore();
        return diff <= 0 ? -1 : +1;
    }

    @Override
    public int compareGenerations(Chromosome parent1, Chromosome parent2, Chromosome child1, Chromosome child2) {
        logger.debug("Comparing performance scores: " + parent1.size() + ", " + parent1.size()
                + " vs " + child1.size() + ", " + child2.size());
        double diff = Math.min(parent1.size(), parent2.size())
                - Math.min(child1.size(), child2.size());
        return diff <= 0 ? -1 : +1;
    }
}
