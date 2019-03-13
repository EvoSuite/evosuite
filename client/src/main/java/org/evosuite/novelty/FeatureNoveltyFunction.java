package org.evosuite.novelty;

import org.evosuite.Properties;
import org.evosuite.coverage.dataflow.Feature;
import org.evosuite.coverage.dataflow.FeatureFactory;
import org.evosuite.coverage.dataflow.FeatureKey;
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
    private static int count = 0;

    /**
     * This method analyses features which are basically the serialized form of some complex objects.
     * If the input is such a feature then this method analyses it and creates two features 'XXX_Struct'
     * and 'XXX_Value' which replaces the original feature in the featureMap.
     *
     * @param individual
     */
    public void executeAndAnalyseFeature(T individual, boolean processForNovelty) {
        getExecutionResult((TestChromosome) individual);
        if (processForNovelty) {
            Map<String, List<Double>> map = null;
            List<Map<Integer, Feature>> featureMapList = ((TestChromosome) individual).getLastExecutionResult().getTrace().getListOfFeatureMap();

            for (Map<Integer, Feature> map1 : featureMapList) {
                Map<Integer, Feature> newFeatures = new LinkedHashMap<>();
                int featuresSize = map1.size();
                for (Map.Entry<Integer, Feature> entry : map1.entrySet()) {

                    boolean isCreated = false;
                    if (entry.getValue().getValue() instanceof String) {
                        map = FeatureValueAnalyser.getAnalysisFromStringRepresentationUsingDomParser((String) entry.getValue().getValue());
                        // the derived features may grow rapidly increasing the number of features
                        // limit the number of features
                        Map<String, List<Double>> tempMap = new HashMap<>();
                        if (map.size() > 5) {
                            int countTemp = 0;
                            for (Map.Entry<String, List<Double>> entryTemp : map.entrySet()) {
                                tempMap.put(entryTemp.getKey(), entryTemp.getValue());
                                countTemp++;
                                if (countTemp == 20)
                                    break;
                            }
                            map.clear();
                            map.putAll(tempMap);
                        }
                        isCreated = true;
                    }
                    if (isCreated) {
                        boolean isFirst = true;
                        // add new features which we derived from the Complex Object
                        for (Map.Entry<String, List<Double>> newFeaturesMapEntry : map.entrySet()) {
                            Feature structureFeature = new Feature();
                            structureFeature.setVariableName(newFeaturesMapEntry.getKey() + "_Struct");
                            structureFeature.setValue(newFeaturesMapEntry.getValue().get(0));
                            structureFeature.setMethodName(entry.getValue().getMethodName());
                            if (isFirst) {
                                newFeatures.put(entry.getKey(), structureFeature);
                                FeatureFactory.updateFeatureMap(entry.getKey(), structureFeature);
                            } else {
                                featuresSize++;
                                newFeatures.put(featuresSize, structureFeature);
                                FeatureFactory.updateFeatureMap(featuresSize, structureFeature);
                            }

                            Feature valueFeature = new Feature();
                            valueFeature.setVariableName(newFeaturesMapEntry.getKey() + "_Value");
                            valueFeature.setValue(newFeaturesMapEntry.getValue().get(1));
                            valueFeature.setMethodName(entry.getValue().getMethodName());
                            featuresSize++;
                            newFeatures.put(featuresSize, valueFeature);
                            FeatureFactory.updateFeatureMap(featuresSize, valueFeature);
                            isFirst = false;
                        }

                    }
                }
                if (!newFeatures.isEmpty()) {
                    map1.putAll(newFeatures);
                }
            }
        }
    }

    @Override
    public void calculateNovelty(Collection<T> population, Collection<T> noveltyArchive, List<String> uncoveredMethodList, boolean processForNovelty) {
        // Step 1. Run all the tests
        for(T t : population){
            executeAndAnalyseFeature(t, processForNovelty);
        }
        //For Experimental Purpose

        // Treat the 'distance' calculated in the process of MOSA as novelty score
        if(!processForNovelty && Properties.DISTANCE_FOR_NOVELTY) {
            // since the distance is already calculated we just need to check if it needs to
            // be added in the archive or not
            for (T t : population) {
                //t.setNoveltyScore(t.getDistance());
                updateNoveltyArchive(t, noveltyArchive);
            }
        }
        //End
        if (processForNovelty) {
            // Step 2. Normalize each of the feature values. For this we need to
            // find the min, max range for each Feature
            Iterator<T> iterator = (Iterator<T>) population.iterator();

            // stores the min and max value for each of the Feature
            Map<FeatureKey, List<Double>> featureValueRangeList = new HashMap<>();
            // Making it static
            while (iterator.hasNext()) {
                T individual = iterator.next();
                // expect all the feature vectors to be populated
                List<Map<Integer, Feature>> featureMapList = ((TestChromosome) individual).getLastExecutionResult().getTrace().getListOfFeatureMap();
                for (Map<Integer, Feature> map : featureMapList) {
                    for (Map.Entry<Integer, Feature> entry : map.entrySet())
                        updateFeatureValueRange(featureValueRangeList, entry);
                }
            }
            // Also do this for the Archive
            for (T individual : noveltyArchive) {
                List<Map<Integer, Feature>> featureMapList = ((TestChromosome) individual).getLastExecutionResult().getTrace().getListOfFeatureMap();
                for (Map<Integer, Feature> map : featureMapList) {
                    for (Map.Entry<Integer, Feature> entry : map.entrySet())
                        updateFeatureValueRange(featureValueRangeList, entry);
                }
            }

            // better to normalize all the feature values to (0-1) according to their value ranges
            // calculated above. Otherwise the calculation and the values may go in 'long' range
            // calculating the normalized novelty
            for (T t : population) {
                FeatureValueAnalyser.updateNormalizedFeatureValues((TestChromosome) t, featureValueRangeList);
            }
            for (T t : noveltyArchive) {
                FeatureValueAnalyser.updateNormalizedFeatureValues((TestChromosome) t, featureValueRangeList);
            }

            evaluations = 0;
            // calculating the normalized novelty
            for (T t : population) {
                updateEuclideanDistance(t, population, noveltyArchive, featureValueRangeList, uncoveredMethodList);
            }

            // update the NOVELTY_THRESHOLD

            if (Properties.DYNAMIC_NOVELTY_THRESHOLD && evaluations >= 25) {
                Properties.NOVELTY_THRESHOLD += (Properties.NOVELTY_THRESHOLD_PERCENTAGE * Properties.NOVELTY_THRESHOLD);
                if (Double.compare(Properties.NOVELTY_THRESHOLD, 1.0) > 0) {
                    Properties.NOVELTY_THRESHOLD = 1.0;
                }
            }
            if (Properties.DYNAMIC_NOVELTY_THRESHOLD && evaluations < 15) {
                Properties.NOVELTY_THRESHOLD -= (Properties.NOVELTY_THRESHOLD_PERCENTAGE * Properties.NOVELTY_THRESHOLD);
                if (Double.compare(Properties.NOVELTY_THRESHOLD, 0.0) < 0) {
                    Properties.NOVELTY_THRESHOLD = 0.0;
                }
            }
            FeatureFactory.setFeatureValueRangeList(featureValueRangeList);
        }
    }

    /**
     * list(0) -> min , list(1) -> max
     * @param featureValueRangeList
     * @param entry
     */
    private void updateFeatureValueRange(Map<FeatureKey, List<Double>> featureValueRangeList, Map.Entry<Integer, Feature> entry) {
        FeatureKey featureKey = new FeatureKey(entry.getValue().getVariableName(), entry.getValue().getMethodName());
        if (null == featureValueRangeList.get(featureKey)) {
            List<Double> rangeList = new ArrayList<>();
            double featureMin = FeatureValueAnalyser.readDoubleValue(entry.getValue().getValue());
            double featureMax = FeatureValueAnalyser.readDoubleValue(entry.getValue().getValue());
            rangeList.add(0, featureMin);
            rangeList.add(1, featureMax);
            featureValueRangeList.put(featureKey, rangeList);
            return;
        } else {
            // do the comparision
            double min = featureValueRangeList.get(featureKey).get(0);
            double max = featureValueRangeList.get(featureKey).get(1);

            if (FeatureValueAnalyser.readDoubleValue(entry.getValue().getValue()) < min) {
                double featureMin = FeatureValueAnalyser.readDoubleValue(entry.getValue().getValue());
                featureValueRangeList.get(featureKey).remove(0);
                featureValueRangeList.get(featureKey).add(0, featureMin);
            }

            if (FeatureValueAnalyser.readDoubleValue(entry.getValue().getValue()) > max) {
                double featureMax = FeatureValueAnalyser.readDoubleValue(entry.getValue().getValue());
                featureValueRangeList.get(featureKey).remove(1);
                featureValueRangeList.get(featureKey).add(1, featureMax);
            }
        }
    }
    /**
     * This calculates normalized novelty for 't' w.r.t the other population and the noveltyArchive
     * The closer the score to 1 more is the novelty or the distance and vice versa.
     * Comparision between two individuals testing a 'foo()' and a 'boo()' respectively are considered to be the most different.
     * @param t
     * @param population
     * @param noveltyArchive
     * @param uncoveredMethodList
     */
    public void updateEuclideanDistance(T t, Collection<T> population, Collection<T> noveltyArchive, Map<FeatureKey, List<Double>> featureValueRangeList, List<String> uncoveredMethodList){
        double noveltyScore = 0;
        double sumDiff = 0;
        // fetch the features
        List<Map<Integer, Feature>> featureMapList1= ((TestChromosome)t).getLastExecutionResult().getTrace().getListOfFeatureMap();

        //Setting to lowest novelty if there is no featureMap
        /*if(!(((TestChromosome) t).getLastExecutionResult().getAllThrownExceptions().isEmpty())){
            t.setNoveltyScore(noveltyScore);
            // no need to update the novelty archive
            count++;
            System.out.println("No. of Individuals setting score to min novelty : "+count);
            return;
        }*/

        for(T other: population){
            boolean testDiffMethodsTemp = false;
            if(t == other)
                continue;
            else{
                // fetch the features
                List<Map<Integer, Feature>> featureMapList2= ((TestChromosome)other).getLastExecutionResult().getTrace().getListOfFeatureMap();
                // comparision between two individuals testing a 'foo()' and a 'boo()' respectively are considered to be the most different
                for(Map.Entry<FeatureKey, List<Double>> entry : featureValueRangeList.entrySet()){
                    // for each matching feature find the max normalized distance.
                    // To handle cases when individuals tests different methods
                    // check if a particular feature in present in both the featureMapList1 and featureMapList2
                    // because it doesn't make sense to calculate feature difference if two individuals affect two different features.
                    // We cannot derive any conclusive information about how far they are from each other. We can do that only on same features
                    // from two different invocations.
                    if(!uncoveredMethodList.contains(entry.getKey().getMethodName()))
                        continue;
                    String result = isFeaturePresentInBothLists(entry.getKey(), featureMapList1, featureMapList2);
                    if("NONE".equals(result))
                        continue;
                    else if("BOTH".equals(result)){
                        double squaredDiff = getMaxFeatureDistance(entry.getKey(), featureMapList1, featureMapList2);
                        sumDiff +=squaredDiff;
                    }
                    /*else{
                        sumDiff++; // '1' -> maximum distance between two individuals testing two different methods
                    }*/
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
                List<Map<Integer, Feature>> featureMapList2 = ((TestChromosome)otherFromArchive).getLastExecutionResult().getTrace().getListOfFeatureMap();
                for(Map.Entry<FeatureKey, List<Double>> entry : featureValueRangeList.entrySet()){
                    // for each matching feature find the max normalized distance.
                    if(!uncoveredMethodList.contains(entry.getKey().getMethodName()))
                        continue;
                    String result = isFeaturePresentInBothLists(entry.getKey(), featureMapList1, featureMapList2);
                    if("NONE".equals(result))
                        continue;
                    else if("BOTH".equals(result)){
                        double squaredDiff = getMaxFeatureDistance(entry.getKey(), featureMapList1, featureMapList2);
                        sumDiff +=squaredDiff;
                    }
                    /*else{
                        sumDiff++; // '1' -> maximum distance between two individuals testing two different methods
                    }*/
                }
            }
        }
        // Number of features will remain constant throughout the iterations
        // FeatureFactory.getFeatures().size() and featureValueRangeList.size() should always be equal.
        int numOfFeatures = FeatureFactory.getFeatures().size()==0?1:FeatureFactory.getFeatures().size();
        double distance = Math.sqrt(sumDiff);
        noveltyScore = distance / (Math.sqrt(((population.size()-1) + noveltyArchive.size()) * numOfFeatures)); // dividing by max. possible distance
        noveltyScore = Double.parseDouble(String.format("%.3f", noveltyScore));
        if(Double.compare(noveltyScore, 1) >0){
            noveltyScore = 1;
        }
        t.setNoveltyScore(noveltyScore);
        updateNoveltyArchive(t, noveltyArchive);
    }

    public Feature matchFeatureWithKey(Map<Integer, Feature> featureMap, FeatureKey featureKey) {
        if (featureMap != null && !featureMap.isEmpty()) {
            Optional<Feature> featureOptional = featureMap.values().stream().filter((feature) -> feature.getVariableName().equals(featureKey.getVariableName()) && feature.getMethodName().equals(featureKey.getMethodName()))
                    .findFirst();
            if (featureOptional.isPresent())
                return featureOptional.get();
            else
                return null;
        } else
            return null;
    }

    private String isFeaturePresentInBothLists(FeatureKey featureKey, List<Map<Integer, Feature>> featureMapList1, List<Map<Integer, Feature>> featureMapList2){
        boolean isPresentInList1 = false;
        boolean isPresentInList2 = false;
        for (Map<Integer, Feature> map1 : featureMapList1) {
            if (null != matchFeatureWithKey(map1, featureKey)) {
                isPresentInList1 = true;
                break;
            }
        }
        for (Map<Integer, Feature> map2 : featureMapList2) {
            if (null != matchFeatureWithKey(map2, featureKey)) {
                isPresentInList2 = true;
                break;
            }
        }
        if(isPresentInList1 && isPresentInList2)
            return "BOTH";
        else if(isPresentInList1 || isPresentInList2)
            return "ONE";
        else
            return "NONE";
    }

    private double getMaxFeatureDistance(FeatureKey featureKey, List<Map<Integer, Feature>> featureMapList1, List<Map<Integer, Feature>> featureMapList2){

        double distance =0;// default distance
        double maxDistance =1;// maximum distance
        boolean flag = false;
        boolean flag2 = false;
        if(featureMapList2.isEmpty())
            return 1; // return max distance
        for (Map<Integer, Feature> map1 : featureMapList1) {
            Feature feature1 = matchFeatureWithKey(map1, featureKey);
            if(feature1 == null)
                continue;
            flag = true;
            for (Map<Integer, Feature> map2 : featureMapList2) {
                Feature feature2 = matchFeatureWithKey(map2, featureKey);
                if(feature2 == null)
                    continue;
                flag2 = true;
                double squaredDiff = FeatureValueAnalyser.getFeatureDistance(feature1, feature2);

                if(Properties.MAX_FEATURE_DISTANCE){
                    if (Double.compare(distance, squaredDiff) < 0) {
                        distance = squaredDiff;
                    }
                }else{
                    // finding the min distance between the feature vectors
                    if (Double.compare(maxDistance, squaredDiff) > 0) {
                        maxDistance = squaredDiff;
                    }
                }

            }
        }
        if (!flag) {
            // this means the feature we are looking for in the featureMapList1 is not present in featureMapList1
            for (Map<Integer, Feature> map2 : featureMapList2) {
                Feature feature2 = matchFeatureWithKey(map2, featureKey);
                if (feature2 != null){
                    // this means in featureMapList2 there exists a feature which we were looking for, and since its not there in featureMapList1 we set the distance to max
                    return 1;
                }

            }
        }
        if(!flag2){
            // this means the feature we are looking for in the featureMapList2 is not present in featureMapList2 but it was present in featureMapList1
            // so we set the distance to max
            return 1;
        }
        if(Properties.MAX_FEATURE_DISTANCE)
            return distance;
        else
            return maxDistance;// the variable name is maxDistance but is actually the min value. See line 339
    }

    public void updateNoveltyArchive(T t, Collection<T> archive){
        //1. read threshold from the Properties file
        double noveltyThreshold = Properties.NOVELTY_THRESHOLD;
        //2. read novelty score
        double currentNovelty = Properties.DISTANCE_FOR_NOVELTY? t.getDistance():t.getNoveltyScore();
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
