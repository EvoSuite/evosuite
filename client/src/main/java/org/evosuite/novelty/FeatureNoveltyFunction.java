package org.evosuite.novelty;

import org.evosuite.coverage.dataflow.Feature;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.NoveltyFunction;
import org.evosuite.ga.metaheuristics.FeatureValueAnalyser;
import org.evosuite.testcase.TestChromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

public class FeatureNoveltyFunction<T extends Chromosome> extends NoveltyFunction<T> implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(FeatureNoveltyFunction.class);

    /*public NoveltyMetric getNoveltyMetric() {
        return noveltyMetric;
    }

    public void setNoveltyMetric(NoveltyMetric noveltyMetric) {
        this.noveltyMetric = noveltyMetric;
    }*/


    public void executeAndAnalyseFeature(T individual) {
        getExecutionResult((TestChromosome) individual);
        Map<String, Double> map= null;
        Map<Integer, Feature> featureMap = ((TestChromosome)individual).getLastExecutionResult().getTrace().getVisitedFeaturesMap();
        Map<Integer, Feature> newFeatures = new LinkedHashMap<>();
        int featuresSize = featureMap.size();

        for(Map.Entry<Integer, Feature> entry: featureMap.entrySet()){

            boolean isCreated = false;
            if(entry.getValue().getValue() instanceof  String){
                map = FeatureValueAnalyser.getAnalysisFromStringRepresentation((String)entry.getValue().getValue());
                isCreated = true;
            }
            if(isCreated){
                Feature structureFeature = new Feature();
                structureFeature.setVariableName(entry.getValue().getVariableName()+"_Struct");
                structureFeature.setValue(map.get(FeatureValueAnalyser.STRUCT_DIFF));
                Feature valueFeature = new Feature();
                valueFeature.setVariableName(entry.getValue().getVariableName()+"_Value");
                valueFeature.setValue(map.get(FeatureValueAnalyser.VALUE_DIFF));
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
    public void calculateNovelty(Collection<T> population) {
        // Step 1. Run all the tests
        for(T t : population){
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
        for(T t : population){
            FeatureValueAnalyser.updateNormalizedFeatureValues((TestChromosome) t, featureValueRangeList);
        }

        // calculating the normalized novelty
        for(T t : population){
            updateEuclideanDistance((TestChromosome)t, population, featureValueRangeList);
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
            double featureMin = FeatureValueAnalyser.readDoubleValue(entry.getValue().getValue());
            double featureMax = FeatureValueAnalyser.readDoubleValue(entry.getValue().getValue());
            rangeList.add(0, featureMin);
            rangeList.add(1, featureMax);
            featureValueRangeList.put(entry.getKey(), rangeList);
            return;
        } else {
            // do the comparision
            double min = featureValueRangeList.get(entry.getKey()).get(0);
            double max = featureValueRangeList.get(entry.getKey()).get(1);

            if (FeatureValueAnalyser.readDoubleValue(entry.getValue().getValue()) < min) {
                double featureMin = FeatureValueAnalyser.readDoubleValue(entry.getValue().getValue());
                featureValueRangeList.get(entry.getKey()).remove(0);
                featureValueRangeList.get(entry.getKey()).add(0, featureMin);
            }

            if (FeatureValueAnalyser.readDoubleValue(entry.getValue().getValue()) > max) {
                double featureMax = FeatureValueAnalyser.readDoubleValue(entry.getValue().getValue());
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
    public void updateEuclideanDistance(TestChromosome t, Collection<T> population, Map<Integer, List<Double>> featureValueRangeList){
        double noveltyScore = 0;
        double sumDiff = 0;
        // debug
        /*if(t.getTestCase().getID() == 82){
           System.out.println("got");
        }*/
        for(T other: population){
            if(t == other)
                continue;
            else{
                // fetch the features
                Map<Integer, Feature> featureMap1= ((TestChromosome)t).getLastExecutionResult().getTrace().getVisitedFeaturesMap();
                Map<Integer, Feature> featureMap2= ((TestChromosome)other).getLastExecutionResult().getTrace().getVisitedFeaturesMap();

                long maxSumDiff = 0;
                for (Map.Entry<Integer, Feature> entry : featureMap1.entrySet()) {
                    double squaredDiff =FeatureValueAnalyser.getFeatureDistance(entry.getValue(), featureMap2.get(entry.getKey()));
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

    /*@Override
    public double getDistance(T individual1, T individual2) {
        return 0;
    }*/


    @Override
    public void sortPopulation(List<T> population) {
        Collections.sort(population, Collections.reverseOrder(new Comparator<T>() {
            @Override
            public int compare(Chromosome c1, Chromosome c2) {
                return Double.compare(c1.getNoveltyScore(), c2.getNoveltyScore());
            }
        }));
    }
}
