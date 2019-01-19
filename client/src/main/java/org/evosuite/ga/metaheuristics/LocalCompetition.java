package org.evosuite.ga.metaheuristics;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
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
     * This method forms the sub regions needed for local competition.
     * Uses KMeansPlusPlus clustering algorithm.
     *
     * @param population
     */
    void formSubRegions(Collection<T> population) {

        //1. Using KMeansPlusPlus clustering algorithm
        List<ChromosomeWrapper> clusterInput = new ArrayList<ChromosomeWrapper>(population.size());
        for (T individual : population)
            clusterInput.add(new ChromosomeWrapper(individual));

        KMeansPlusPlusClusterer<ChromosomeWrapper> clusterer = new KMeansPlusPlusClusterer<ChromosomeWrapper>(Properties.LOCAL_COMPETITION_GROUP_SIZE, 1000);
        List<CentroidCluster<ChromosomeWrapper>> clusterResults = clusterer.cluster(clusterInput);

        List<List<T>> groups = new ArrayList<>();
        for (int i=0; i<clusterResults.size(); i++) {
            System.out.println("Cluster " + i);
            List<T> subGroup = new ArrayList<>();
            for (ChromosomeWrapper chromosomeWrapper : clusterResults.get(i).getPoints()){
                subGroup.add((T)chromosomeWrapper.getIndividual());
                //System.out.println(chromosomeWrapper.getIndividual());
            }
            groups.add(subGroup);
            System.out.println();
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
