package org.evosuite.performance.strategies;

import org.evosuite.ga.Chromosome;
import org.evosuite.performance.comparator.DominanceComparator;

import java.util.*;

/**
 * @author Sebastiano Panichella, Giovanni Grano
 * @param <T>
 */
public class PerformanceDominanceSorter<T extends Chromosome> implements IDominanceSorter {

    private static Comparator comparator = new DominanceComparator();

    /**
     * Returns the population with the information about the performance rank computed
     * @return
     *      a population with the performance rank computed
     */
    @Override
    public List getSortedWithIndicatorsDominance(List population) {
        List<T> newPopulation = new ArrayList<>();

        List<T> oldPopulation = new ArrayList<>(population);
        Set<T> front0 = new HashSet<>();

        for (String indicator : oldPopulation.get(0).getIndicatorValues().keySet()) {
            double min = Double.MAX_VALUE;
            T minIndividual = null;

            for (Iterator<T> iter = oldPopulation.iterator(); iter.hasNext(); ) {
                T individual = iter.next();
                if (individual.getIndicatorValue(indicator) < min) {
                    min = individual.getIndicatorValue(indicator);
                    minIndividual = individual;
                }
            } // end population

            // add the minimum for the indicator to the new population
            // set the performance rank to 0
            // delete it from the initial population
            if (minIndividual != null) {
                minIndividual.setPerformance_rank(0);
                front0.add(minIndividual);
            }
        } // end indicators

        newPopulation.addAll(front0);
        oldPopulation.removeAll(front0);

        if (!oldPopulation.isEmpty()) {
            List<T>[] fronts = getNextNonDominatedFronts(oldPopulation);
            for (int i = 0; i < fronts.length; i++) {
                for (T welcome : fronts[i])
                    newPopulation.add(welcome);
            }
        }

        return newPopulation;
    }

    @SuppressWarnings("Duplicates")
    private List<T>[] getNextNonDominatedFronts(List<T> solutionSet) {

        List<T> solutionSet_ = solutionSet;

        int[] dominateMe = new int[solutionSet_.size()];

        List<Integer>[] iDominate = new List[solutionSet_.size()];

        List<Integer>[] front = new List[solutionSet_.size() + 1];

        int flagDominate;

        // Initialize the fronts
        for (int i = 0; i < front.length; i++)
            front[i] = new LinkedList<>();

        // -> Fast non dominated sorting algorithm
        for (int p = 0; p < solutionSet_.size(); p++) {
            iDominate[p] = new LinkedList<>();
            dominateMe[p] = 0;
        }

        for (int p = 0; p < (solutionSet_.size() - 1); p++) {
            for (int q = p + 1; q < solutionSet_.size(); q++) {
                flagDominate = comparator.compare(solutionSet.get(p), solutionSet.get(q));
                if (flagDominate == -1) {
                    iDominate[p].add(q);
                    dominateMe[q]++;
                } else if (flagDominate == 1) {
                    iDominate[q].add(p);
                    dominateMe[p]++;
                }
            }
        }

        for (int p = 0; p < solutionSet_.size(); p++) {
            if (dominateMe[p] == 0) {
                front[0].add(p);
                solutionSet.get(p).setPerformance_rank(1);
            }
        }

        // Obtain the rest of fronts
        int i = 0;
        Iterator<Integer> it1, it2;
        while (front[i].size() != 0) {
            i++;
            it1 = front[i - 1].iterator();
            while (it1.hasNext()) {
                it2 = iDominate[it1.next()].iterator();
                while (it2.hasNext()) {
                    int index = it2.next();
                    dominateMe[index]--;
                    if (dominateMe[index] == 0) {
                        front[i].add(index);
                        solutionSet_.get(index).setPerformance_rank(i + 1);
                    }
                }
            }
        }
        List<T>[] fronts = new ArrayList[i];
        // 0,1,2,....,i-1 are front, then i fronts
        for (int j = 0; j < i; j++) {
            fronts[j] = new ArrayList<>();
            it1 = front[j].iterator();
            while (it1.hasNext()) {
                fronts[j].add(solutionSet.get(it1.next()));
            }
        }
        return fronts;
    }

    @Override
    public String nameAlgo() {
        return "STANDARD DOMINANCE";
    }
}