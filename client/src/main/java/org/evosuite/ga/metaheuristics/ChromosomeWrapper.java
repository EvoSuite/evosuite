package org.evosuite.ga.metaheuristics;

import org.apache.commons.math3.ml.clustering.Clusterable;
import org.evosuite.coverage.dataflow.Feature;
import org.evosuite.coverage.dataflow.FeatureFactory;
import org.evosuite.ga.Chromosome;
import org.evosuite.novelty.FeatureNoveltyFunction;
import org.evosuite.testcase.TestChromosome;

import java.util.LinkedHashMap;
import java.util.Map;

public class ChromosomeWrapper<T extends Chromosome> implements Clusterable {

    private double[] points;
    private T individual;

    ChromosomeWrapper(T individual) {
        this.individual = individual;
        Map<Integer, Feature> featureMap = ((TestChromosome) individual).getLastExecutionResult().getTrace().getVisitedFeaturesMap();
        if (featureMap.isEmpty()) {
            // handle feature Map
            handleFeatureMap(FeatureFactory.getFeatures(), featureMap);
        }
        points = new double[FeatureFactory.getFeatures().size()];
        populatePoints(featureMap);
    }

    /**
     * This handles the case when there is no feature map generated.
     * When no featureMap is available for an individual then create
     * a dummy feature's map with the normalized value as mentioned below.
     * We need to do this as the clustering function would need all the
     * feature dimensions of equal size.
     *
     * @param featureMapRef
     * @param featureMap
     */
    private void handleFeatureMap(Map<Integer, Feature> featureMapRef, Map<Integer, Feature> featureMap) {
        Map<Integer, Feature> newFeatureMap = new LinkedHashMap<>();
        for(Map.Entry<Integer, Feature> refEntry :  featureMapRef.entrySet()){
            Feature feature = new Feature();
            feature.setVariableName(refEntry.getValue().getVariableName());
            feature.setNormalizedValue(0.5);// Setting it to a middle point as of now. TODO: Decide what would be better.
            newFeatureMap.put(refEntry.getKey(), feature);
        }
        featureMap.putAll(newFeatureMap);
    }

    /**
     * TODO: Handle the case when featureMap is null
     * TODO: Handle the case when the size of the MAP is variable
     *
     * @param featureMap
     */
    private void populatePoints(Map<Integer, Feature> featureMap) {
        int i = 0;
        for (Map.Entry<Integer, Feature> entry : featureMap.entrySet()) {
            points[i] = entry.getValue().getNormalizedValue();
            i++;
        }
    }

    public T getIndividual() {
        return individual;
    }

    @Override
    public double[] getPoint() {
        return points;
    }
}
