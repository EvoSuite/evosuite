package org.evosuite.coverage.dataflow;

import jdk.internal.org.objectweb.asm.Opcodes;
import org.evosuite.graphs.cfg.BytecodeInstruction;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureFactory {

    private static Map<Integer, Feature> features = new HashMap<Integer, Feature>();
    private static Map<BytecodeInstruction, Integer> knownInstructions = new HashMap<BytecodeInstruction, Integer>();
    private static int defCounter = 0;

    /**
     *
     * defCounter is incremented only if the instruction is a pure data definition and one feature is made per
     * variable name.
     * for e.g. XSTORE or PUTFIELD instructions. For IINC and POP defCounter is not incremented
     * as it is not required by FeatureInstrumentation.java
     * Returns true if the instruction is not a duplicate one. The case in which it returns false
     * would be rare.
     * PUTFIELD instructions
     * @param d
     * @return a boolean
     */
    public static boolean registerAsFeature(BytecodeInstruction d) {
        if(null != knownInstructions.get(d)){
            return false;
        }else{

            if(d.getASMNode().getOpcode() == Opcodes.IINC){
                // do not incr defCounter
                // instead we need to update the same variable
                // check if the variable already exists - Just for safety
                String var = d.getVariableName();

                /*Collection<Feature> featureList = features.values();
                for (Feature feature:featureList) {
                    if(feature.getVariableName().equals(var))
                        return true;
                }*/
                if(null != getFeatureByVarName(var))
                    return true;

            }
            if(d.getASMNode().getOpcode() == Opcodes.POP)
                return true;

            if(null != getFeatureByVarName(d.getVariableName()))
                return true;


            defCounter++;
            knownInstructions.put(d,defCounter);
            String var = d.getVariableName();
            //String type = d.getFieldType();
            Feature feature = new Feature();
            feature.setVariableName(var);
            //feature.setTypeClass(type);
            features.put(defCounter, feature);
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
    public static Feature getFeatureByVarName(String name){
        Map.Entry<Integer, Feature> entry = getEntryByName(name);
        if(null != entry)
            return entry.getValue();
        else
            return null;
    }

    public static Integer getFeatureIdByVarName(String name){
        Map.Entry<Integer, Feature> entry = getEntryByName(name);
        if(null != entry)
            return entry.getKey();
        else
            return null;
    }

    public static Map.Entry<Integer, Feature> getEntryByName(String varName){
        for(Map.Entry<Integer, Feature> entry:features.entrySet()){
            Feature feature = entry.getValue();
            if(feature.getVariableName().equals(varName))
                return entry;
        }
        return null;
    }

}
