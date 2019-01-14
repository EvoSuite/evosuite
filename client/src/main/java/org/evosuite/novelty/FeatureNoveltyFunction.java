package org.evosuite.novelty;

import org.evosuite.Properties;
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

    private static int evaluations = 0; // to store how many individuals in the current generation have novelty score greater than the novelty threshold


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
    public void calculateNovelty(Collection<T> population, Collection<T> noveltyArchive) {
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

        evaluations = 0;
        // calculating the normalized novelty
        for(T t : population){
            updateEuclideanDistance(t, population, noveltyArchive, featureValueRangeList);
        }

        // update the NOVELTY_THRESHOLD
        if(evaluations >= 25){
            Properties.NOVELTY_THRESHOLD += (0.20 * Properties.NOVELTY_THRESHOLD);
            if(Double.compare(Properties.NOVELTY_THRESHOLD,1.0) > 0){
                Properties.NOVELTY_THRESHOLD = 1.0;
            }
        }
        if(evaluations < 15){
            Properties.NOVELTY_THRESHOLD -= (0.05 * Properties.NOVELTY_THRESHOLD);
            if(Double.compare(Properties.NOVELTY_THRESHOLD,0.0) < 0){
                Properties.NOVELTY_THRESHOLD = 0.0;
            }
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
     * This calculates normalized novelty for 't' w.r.t the other population and the noveltyArchive
     * The closer the score to 1 more is the novelty or the distance and vice versa.
     * @param t
     * @param population
     * @param noveltyArchive
     * @param featureValueRangeList
     */
    public void updateEuclideanDistance(T t, Collection<T> population, Collection<T> noveltyArchive, Map<Integer, List<Double>> featureValueRangeList){
        double noveltyScore = 0;
        double sumDiff = 0;

        // fetch the features
        Map<Integer, Feature> featureMap1= ((TestChromosome)t).getLastExecutionResult().getTrace().getVisitedFeaturesMap();

        for(T other: population){
            if(t == other)
                continue;
            else{
                // fetch the features
                Map<Integer, Feature> featureMap2= ((TestChromosome)other).getLastExecutionResult().getTrace().getVisitedFeaturesMap();

                for (Map.Entry<Integer, Feature> entry : featureMap1.entrySet()) {
                    double squaredDiff =FeatureValueAnalyser.getFeatureDistance(entry.getValue(), featureMap2.get(entry.getKey()));
                    sumDiff +=squaredDiff;
                }
            }
        }
        // calculate the distance also w.r.t noveltyArchive
        for(T otherFromArchive: noveltyArchive){
            if(t == otherFromArchive){
                continue;
            }
            else{
                // fetch the features
                Map<Integer, Feature> featureMap2= ((TestChromosome)otherFromArchive).getLastExecutionResult().getTrace().getVisitedFeaturesMap();

                for (Map.Entry<Integer, Feature> entry : featureMap1.entrySet()) {
                    double squaredDiff =FeatureValueAnalyser.getFeatureDistance(entry.getValue(), featureMap2.get(entry.getKey()));
                    sumDiff +=squaredDiff;
                }
            }
        }



        // Number of features will remain constant throughout the iterations
        int numOfFeatures = featureValueRangeList.size()==0?1:featureValueRangeList.size();

        double distance = Math.sqrt(sumDiff);
        noveltyScore = distance / (Math.sqrt(((population.size()-1) + noveltyArchive.size()) * numOfFeatures)); // dividing by max. possible distance
        t.setNoveltyScore(noveltyScore);
        System.out.println("Novelty  : "+noveltyScore);
        updateNoveltyArchive(t, noveltyArchive);
    }

    public void updateNoveltyArchive(T t, Collection<T> archive){
        //1. read threshold from the Properties file
        double noveltyThreshold = Properties.NOVELTY_THRESHOLD;
        //2. read novelty score
        double currentNovelty = t.getNoveltyScore();
        //3. decide if it should be added to the archive or not
        if(currentNovelty > noveltyThreshold){
            evaluations++;
            //if the size of the archive grows bigger than certain threshold then
            if( archive.size() >= Properties.MAX_NOVELTY_ARCHIVE_SIZE){
                // Evict individual from the archive - Which one? maybe the oldest one
                ((Deque)archive).removeFirst();
            }
            archive.add(t);
        }
    }

    private double getDistance(Feature feature1, Feature feature2){
        return Math.abs((Integer)feature1.getValue() - (Integer)feature2.getValue());
    }




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
