package org.evosuite.coverage.dataflow;

import org.evosuite.graphs.cfg.BytecodeInstruction;

import java.util.HashMap;
import java.util.Map;

public class FeatureFactory {

    private static Map<Integer, Feature> features = new HashMap<Integer, Feature>();
    private static Map<BytecodeInstruction, Integer> knownInstructions = new HashMap<BytecodeInstruction, Integer>();
    private static int defCounter = 0;

    public static boolean registerAsFeature(BytecodeInstruction d) {
        if(null != knownInstructions.get(d)){
            return false;
        }else{
            knownInstructions.put(d,defCounter);
            String var = d.getVariableName();
            Feature feature = new Feature();
            feature.setVariableName(var);
            features.put(defCounter, feature);
            defCounter++;
        }
        return true;
    }

    public static int getDefCounter(){
        return defCounter;
    }

    public static boolean isKnownAsDefinition(BytecodeInstruction k){
        return knownInstructions.containsKey(k);
    }

    public static boolean updateFeature(Object val, int id){
        if(features.containsKey(id)){
            Feature feature = features.get(id);
            feature.setValue(val);
            features.put(id, feature);
            return true;
        }else{
            return false;
        }
    }
    public static Map<Integer, Feature> getFeatures(){
        return features;
    }

    public static Feature getFeatureById(Integer id){
        return features.get(id);
    }

}
