package org.evosuite.coverage.dataflow;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureFactory implements Serializable {

    private static Map<Integer, Feature> features = new HashMap<Integer, Feature>();
    private static Map<Integer, Feature> tempMap = new HashMap<>();
    private static Map<FeatureKey, List<Double>> featureValueRangeList = new HashMap<>();
    private static int defCounter = 0;


    /**
     * Registers the feature using the combination of variable name and the method name.
     * @param varName
     * @param methodName
     * @return
     */
    public static boolean registerAsFeature(String varName, String methodName) {

        defCounter++;
        String var = varName;
        Feature feature = new Feature();
        feature.setVariableName(methodName + '_' + var);
        feature.setMethodName(methodName);
        features.put(defCounter, feature);

        return true;
    }

    /**
     * Just to keep track of total number of features. As a part of evaluation features are added to the
     * featureList (One such place is FeatureNoveltyFunction.executeAndAnalyseFeature()). The newly added
     * feature will not contain any specific value or normalizedValue.
     * This is just to keep a track of total number of features.
     *
     * @param feature
     */
    public static void updateFeatureMap(Integer key, Feature feature) {
        tempMap.put(key, feature);
    }

    public static Map<Integer, Feature> getFeatures() {
        Map<Integer, Feature> tempMap1 = new HashMap<>(tempMap);
        tempMap.clear();
        tempMap.putAll(features);
        tempMap.putAll(tempMap1);
        return tempMap;
    }

    public static Feature getFeatureByVarName(String name) {
        Map.Entry<Integer, Feature> entry = getEntryByName(name);
        if (null != entry)
            return entry.getValue();
        else
            return null;
    }

    public static Integer getFeatureIdByVarName(String name) {
        Map.Entry<Integer, Feature> entry = getEntryByName(name);
        if (null != entry)
            return entry.getKey();
        else
            return null;
    }

    public static Map.Entry<Integer, Feature> getEntryByName(String varName) {
        for (Map.Entry<Integer, Feature> entry : features.entrySet()) {
            Feature feature = entry.getValue();
            if (feature.getVariableName().equals(varName))
                return entry;
        }
        return null;
    }

    public static void setFeatureValueRangeList(Map<FeatureKey, List<Double>> featureValueRangeList) {
        FeatureFactory.featureValueRangeList = featureValueRangeList;
    }
}
