package org.evosuite.novelty;

import org.apache.commons.collections.map.LinkedMap;
import org.evosuite.coverage.dataflow.Feature;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.NoveltyFunction;
import org.evosuite.ga.metaheuristics.FeatureDiffCalculator;
import org.evosuite.ga.metaheuristics.FeatureNovelty;
import org.evosuite.ga.metaheuristics.NoveltyMetric;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class BranchNoveltyFunction<T extends Chromosome> extends NoveltyFunction<TestChromosome> implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(BranchNoveltyFunction.class);

    private NoveltyMetric noveltyMetric;

    public NoveltyMetric getNoveltyMetric() {
        return noveltyMetric;
    }

    public void setNoveltyMetric(NoveltyMetric noveltyMetric) {
        this.noveltyMetric = noveltyMetric;
    }

    private ExecutionResult runTest(TestCase test) {
        return TestCaseExecutor.runTest(test);
    }

    private ExecutionResult getExecutionResult(TestChromosome individual) {
       ExecutionResult origResult = individual.getLastExecutionResult();
       if(origResult == null||individual.isChanged()) {
            origResult = runTest(individual.getTestCase());
            individual.setLastExecutionResult(origResult);
            individual.setChanged(false);
       }
       return individual.getLastExecutionResult();
    }

    /**
     *
     * This method calculates Euclidean distance between the features
     *
     * @param individual1
     * @param individual2
     * @return the feature-wise distance for all the features
     */
    @Override
    public double getDistance(TestChromosome individual1, TestChromosome individual2) {
        return this.noveltyMetric.calculateDistance(individual1, individual2);
    }

    public void executeAndAnalyseFeature(TestChromosome individual) {
        getExecutionResult(individual);
        Map<String, Double> map= null;
        Map<Integer, Feature> featureMap = individual.getLastExecutionResult().getTrace().getVisitedFeaturesMap();
        Map<Integer, Feature> newFeatures = new LinkedHashMap<>();
        int featuresSize = featureMap.size();

        for(Map.Entry<Integer, Feature> entry: featureMap.entrySet()){

            boolean isCreated = false;
            if(entry.getValue().getValue() instanceof  String){
                map = FeatureDiffCalculator.getDifferenceMapOfStringRepresentation((String)entry.getValue().getValue());
                isCreated = true;
            }
            if(isCreated){
                Feature structureFeature = new Feature();
                structureFeature.setVariableName(entry.getValue().getVariableName()+"_Struct");
                structureFeature.setValue(map.get(FeatureDiffCalculator.STRUCT_DIFF));
                Feature valueFeature = new Feature();
                valueFeature.setVariableName(entry.getValue().getVariableName()+"_Value");
                valueFeature.setValue(map.get(FeatureDiffCalculator.VALUE_DIFF));
                // replace the existing entry of String representation
                newFeatures.put(entry.getKey(), structureFeature);
                featuresSize++;
                newFeatures.put(featuresSize, valueFeature);
            }
        }
        if(!newFeatures.isEmpty()){
            featureMap.putAll(newFeatures);
        }

    }

    @Override
    public void calculateNovelty(Collection<TestChromosome> population) {
        // Step 1. Run all the tests
        for(TestChromosome t : population){
            executeAndAnalyseFeature(t);
        }
        // Step 2. Normalize each of the feature values. For this we need to
        // find the min, max range for each Feature
        Iterator<T> iterator = (Iterator<T>) population.iterator();

        // stores the min and max value for each of the Feature
        Map<Integer, List<Double>> featureValueRangeList = new HashMap<>();

        while (iterator.hasNext()) {
            T c = iterator.next();
            // expect all the feature vectors to be populated
            Map<Integer, Feature> featureMap =((TestChromosome)c).getLastExecutionResult().getTrace().getVisitedFeaturesMap();
            for(Map.Entry<Integer, Feature> entry: featureMap.entrySet()){
                updateFeatureValueRange(featureValueRangeList, entry);
            }
        }

        // better to normalize all the feature values to (0-1) according to their value ranges
        // calculated above. Otherwise the calculation and the values may go in 'long' range
        // calculating the normalized novelty
        for(TestChromosome t : population){
            FeatureDiffCalculator.updateNormalizedFeatureValues(t, featureValueRangeList);
        }

        // calculating the normalized novelty
        for(TestChromosome t : population){
            updateEuclideanDistance(t, population, featureValueRangeList);
        }
    }



    /**
     * list(0) -> min , list(1) -> max
     * @param featureValueRangeList
     * @param entry
     */
    private void updateFeatureValueRange(Map<Integer, List<Double>> featureValueRangeList, Map.Entry<Integer, Feature> entry) {
        if (null == featureValueRangeList.get(entry.getKey())) {
            List<Double> rangeList = new ArrayList<>();
            double featureMin = FeatureDiffCalculator.readDoubleValue(entry.getValue().getValue());
            double featureMax = FeatureDiffCalculator.readDoubleValue(entry.getValue().getValue());
            rangeList.add(0, featureMin);
            rangeList.add(1, featureMax);
            featureValueRangeList.put(entry.getKey(), rangeList);
            return;
        } else {
            // do the comparision
            double min = featureValueRangeList.get(entry.getKey()).get(0);
            double max = featureValueRangeList.get(entry.getKey()).get(1);

            if (FeatureDiffCalculator.readDoubleValue(entry.getValue().getValue()) < min) {
                double featureMin = FeatureDiffCalculator.readDoubleValue(entry.getValue().getValue());
                featureValueRangeList.get(entry.getKey()).remove(0);
                featureValueRangeList.get(entry.getKey()).add(0, featureMin);
            }

            if (FeatureDiffCalculator.readDoubleValue(entry.getValue().getValue()) > max) {
                double featureMax = FeatureDiffCalculator.readDoubleValue(entry.getValue().getValue());
                featureValueRangeList.get(entry.getKey()).remove(1);
                featureValueRangeList.get(entry.getKey()).add(1, featureMax);
            }
        }
    }

    /**
     * This calculates normalized novelty for 't' w.r.t the other population
     * The closer the score to 1 more is the novelty or the distance and vice versa.
     * @param t
     * @param population
     * @param featureValueRangeList
     */
    public void updateEuclideanDistance(TestChromosome t, Collection<TestChromosome> population, Map<Integer, List<Double>> featureValueRangeList){
        double noveltyScore = 0;
        double sumDiff = 0;
        // debug
        /*if(t.getTestCase().getID() == 82){
           System.out.println("got");
        }*/
        for(TestChromosome other: population){
            if(t == other)
                continue;
            else{
                // fetch the features
                Map<Integer, Feature> featureMap1= ((TestChromosome)t).getLastExecutionResult().getTrace().getVisitedFeaturesMap();
                Map<Integer, Feature> featureMap2= ((TestChromosome)other).getLastExecutionResult().getTrace().getVisitedFeaturesMap();

                long maxSumDiff = 0;
                for (Map.Entry<Integer, Feature> entry : featureMap1.entrySet()) {
                    double squaredDiff =FeatureNovelty.getDistance1(entry.getValue(), featureMap2.get(entry.getKey()));
                    sumDiff +=squaredDiff;

                    /*maxSumDiff += (featureValueRangeList.get(entry.getKey()).get(0) - featureValueRangeList.get(entry.getKey()).get(1)) *
                            (featureValueRangeList.get(entry.getKey()).get(0) - featureValueRangeList.get(entry.getKey()).get(1));*/

                }
            }
        }
        int numOfFeatures = featureValueRangeList.size()==0?1:featureValueRangeList.size();

        double distance = Math.sqrt(sumDiff);
        noveltyScore = distance / (Math.sqrt((population.size()-1) * numOfFeatures)); // dividing by max. possible distance
        t.setNoveltyScore(noveltyScore);
        System.out.println("Novelty  : "+noveltyScore);
    }

    private double getDistance(Feature feature1, Feature feature2){
        return Math.abs((Integer)feature1.getValue() - (Integer)feature2.getValue());
    }

}
