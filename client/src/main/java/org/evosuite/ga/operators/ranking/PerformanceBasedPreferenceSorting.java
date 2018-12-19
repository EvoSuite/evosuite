package org.evosuite.ga.operators.ranking;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.comparators.PerformanceIndicatorComparator;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The class ranks the test cases according to the default MOSA's Preference Criterion except for the
 * first rank, when it uses the performance score to sort chromosomes when the fitness is the same
 *
 * @author Giovanni Grano
 */
public class PerformanceBasedPreferenceSorting<T extends Chromosome> extends RankBasedPreferenceSorting<T> implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(RankBasedPreferenceSorting.class);

    @Override
    public void computeRankingAssignment(List<T> solutions, Set<FitnessFunction<T>> uncovered_goals) {
        super.computeRankingAssignment(solutions, uncovered_goals);
    }

    private List<T> getZeroFront(List<T> solutionSet, Set<FitnessFunction<T>> uncovered_goals) {
        Set<T> zero_front = new LinkedHashSet<>(solutionSet.size());
        for (FitnessFunction<T> f : uncovered_goals){
            PerformanceIndicatorComparator<T> comparator = new PerformanceIndicatorComparator<>(f);

            T best = null;
            for (T test : solutionSet){
                int flag = comparator.compare(test, best);
                if (flag == -1 || (flag == 0  && Randomness.nextBoolean())){
                    best = test;
                }
            }
            zero_front.add(best);
        }
        List<T> list = new ArrayList<>(zero_front.size());
        list.addAll(zero_front);
        return list;
    }
}
