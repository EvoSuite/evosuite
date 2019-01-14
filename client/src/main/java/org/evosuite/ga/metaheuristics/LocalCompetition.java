package org.evosuite.ga.metaheuristics;

import org.evosuite.Properties;
import org.evosuite.coverage.dataflow.Feature;
import org.evosuite.ga.Chromosome;
import org.evosuite.testcase.TestChromosome;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class LocalCompetition<T extends Chromosome> implements Serializable {

    /**
     * This method forms the sub regions needed for local competition
     *
     * @param population
     */
    void formSubRegions(Collection<T> population) {
        // 1. store distance w.r.t a fixed point
        updateDistance(population);

        List<T> sortedPopulation = new ArrayList<>(population);
        sortedPopulation.sort((o1, o2) -> Double.compare(o1.getDistance(), o2.getDistance()));

        List<List<T>> groups = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < sortedPopulation.size() / Properties.LOCAL_COMPETITION_GROUP_SIZE; i++) {

            List<T> subGroup = new ArrayList<>();
            subGroup.addAll(sortedPopulation.subList(index, index + Properties.LOCAL_COMPETITION_GROUP_SIZE));
            groups.add(subGroup);
            index = index + Properties.LOCAL_COMPETITION_GROUP_SIZE;
        }
    }

    /**
     * Updates distance in each individual w.r.t a fixed point in Euclidean space.
     *
     * @param population
     */
    void updateDistance(Collection<T> population) {
        Feature fixedPoint = new Feature();
        fixedPoint.setNormalizedValue(1);

        for (T individual : population) {
            double sumDiff = 0;
            Map<Integer, Feature> featureMap = ((TestChromosome) individual).getLastExecutionResult().getTrace().getVisitedFeaturesMap();
            for (Map.Entry<Integer, Feature> entry : featureMap.entrySet()) {
                double squaredDiff = FeatureValueAnalyser.getFeatureDistance(entry.getValue(), fixedPoint);
                sumDiff += squaredDiff;
            }
            double euclideanDistance = Math.sqrt(sumDiff);
            individual.setDistance(euclideanDistance);
        }

    }
}
