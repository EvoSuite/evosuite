package org.evosuite.performance;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.performance.comparator.MinMaxCalculator;

import java.util.List;

/**
 * This class sets the value of the performance score according to the combination strategy chosen
 *
 * @author Giovanni Grano
 */
public class PerformanceScore<T extends Chromosome> {

    @SuppressWarnings("Duplicates")
    public void assignPerformanceScore(List<T> population) {
        switch (Properties.P_COMBINATION_STRATEGY) {
            case SUM:
                for (T test : population){
                    double sum = 0.0;
                    for (Double indicator : test.getIndicatorValues().values()){
                        sum += indicator / (indicator+1);
                    }
                    test.setPerformanceScore(sum);
                }
                break;
            case MIN_MAX:
                MinMaxCalculator<T> indicator = new MinMaxCalculator<>();
                // computes the min-max scores stored in an ad-hoc variable in the chromosome
                indicator.computeIndicatorMinMaxSum(population);
                // move the computed min-max scores in the performance score variable
                population.stream().forEach(ch -> ch.setPerformanceScore(ch.getMinMaxSum()));
                break;
            case DOMINANCE:
                // It needs to run the PerformanceDominanceSorted before (to compute the performance_rank)
                population.stream().forEach(ch -> ch.setPerformanceScore(ch.getPerformance_rank()));
                break;
        }
    }
}
