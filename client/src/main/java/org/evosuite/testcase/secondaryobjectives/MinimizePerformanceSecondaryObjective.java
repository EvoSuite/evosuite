package org.evosuite.testcase.secondaryobjectives;

import org.evosuite.ga.SecondaryObjective;
import org.evosuite.testcase.TestChromosome;

public class MinimizePerformanceSecondaryObjective extends SecondaryObjective<TestChromosome> {

    @Override
    public int compareChromosomes(TestChromosome chromosome1, TestChromosome chromosome2) {
        logger.debug("Comparing performance scores: " + chromosome1.getPerformanceScore() + " vs "
                + chromosome2.getPerformanceScore());
        double diff = chromosome1.getPerformanceScore() - chromosome2.getPerformanceScore();
        return diff <= 0 ? -1 : +1;
    }

    @Override
    public int compareGenerations(TestChromosome parent1, TestChromosome parent2,
                                  TestChromosome child1, TestChromosome child2) {
        logger.debug("Comparing performance scores: " + parent1.getPerformanceScore() + ", " + parent1.getPerformanceScore()
                + " vs " + child1.getPerformanceScore() + ", " + child2.getPerformanceScore());
        double diff = Math.min(parent1.getPerformanceScore(), parent2.getPerformanceScore())
                - Math.min(child1.getPerformanceScore(), child2.getPerformanceScore());
        return diff <= 0 ? -1 : +1;
    }
}
