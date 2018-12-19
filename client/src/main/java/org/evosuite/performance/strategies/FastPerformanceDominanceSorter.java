package org.evosuite.performance.strategies;

import org.evosuite.ga.Chromosome;
import org.evosuite.performance.comparator.DominanceComparator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Sebastiano Panichella
 * @param <T>
 */
public class FastPerformanceDominanceSorter<T extends Chromosome> implements IDominanceSorter<T> {

    private static Comparator comparator = new DominanceComparator();

    /**
     * Returns the population with the information about the performance rank computed
     * @return
     *      a population with the performance rank computed
     */
    @Override
    public List<T> getSortedWithIndicatorsDominance(List<T> population) {
        List<T> newPopulation = new ArrayList<>();
        newPopulation.addAll(population);
        //Collections.sort(newPopulation, comparator);
        for (int i=0; i<newPopulation.size()-1; i++){
            for (int j=i+1; j<newPopulation.size(); j++){
                if (comparator.compare(newPopulation.get(i),newPopulation.get(j))==-1){
                    T temp = newPopulation.get(i);
                    newPopulation.set(i, newPopulation.get(j));
                    newPopulation.set(j, temp);
                }
            }
        }

        int rank = 0;
        newPopulation.get(0).setPerformance_rank(rank);
        for (int i=1; i<newPopulation.size()-1; i++){
            if (comparator.compare(newPopulation.get(i-1),newPopulation.get(i))==+1){
                rank++;
                newPopulation.get(i).setPerformance_rank(rank);
            }
        }
        newPopulation.get(newPopulation.size()-1).setPerformance_rank(rank);

        return newPopulation;
    }

    @Override
    public String nameAlgo() {
        return "FAST DOMINANCE";
    }

}