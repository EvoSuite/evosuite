package org.evosuite.coverage.dataflow;

import org.evosuite.Properties;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureFactory implements Serializable {

    private static Map<Integer, Feature> features = new HashMap<Integer, Feature>();
    private static Map<Integer, Feature> tempMap = new HashMap<>();
    private static Map<FeatureKey, List<Double>> featureValueRangeList = new HashMap<>();
    private static Map<BytecodeInstruction, Integer> knownInstructions = new HashMap<BytecodeInstruction, Integer>();
    private static int defCounter = 0;

    /**
     * This method recognizes all the instructions which ASMWrapper.isDefinition() returns as true.
     * It registers the instruction as a Feature only if the instruction is seen for the first time.
     * Instructions are verified as redundant or unique depending on their variable name.
     * <p>
     * PUTFIELD instructions
     *
     * @param d BytecodeInstruction
     * @return a boolean
     */
    public static boolean registerAsFeature(BytecodeInstruction d) {
        if (null != knownInstructions.get(d)) {
            return false;
        } else {

            if (null != getFeatureByVarName(d.getVariableName()))
                return false;

            if (d.getASMNode().getOpcode() == Opcodes.POP)
                return false;

            if(isStandardJavaVariable(d.getVariableName()))
                return false;

            if(isRecursiveElement(d))
                return false;

            // only for experiment purpose
            if(Properties.INSTRUMENT_ONLY_FIELD){
                if(d.isLocalVariableDefinition() || d.isLocalVariableUse())
                    return false;
            }

            defCounter++;
            knownInstructions.put(d, defCounter);
            String var = d.getVariableName();
            Feature feature = new Feature();
            feature.setVariableName(var);
            feature.setMethodName(d.getMethodName());
            features.put(defCounter, feature);
        }
        return true;
    }

    public static boolean registerAsFeature(String varName, String methodName) {

        defCounter++;
        String var = varName;
        Feature feature = new Feature();
        feature.setVariableName(methodName+'_'+var);
        feature.setMethodName(methodName);
        features.put(defCounter, feature);

        return true;
    }

    /**
     * This method identifies if the accessed field belongs to the original class or to some sub class.
     * For e.g
     * Class A{
     *     B b = new B();
     *     void foo(){
     *         b.j=4;
     *     }
     * }
     * In the above CUT the variable 'j' belongs to class B and not A and thus this method will identify such variables.
     * In novelty search approach we do not need to care about such variables as we already serialize any complex object
     * once after all the modification to such a object has been done. And hence we would still be able to capture any difference
     * to such recursive variables.
     *
     * @param bytecodeInstruction
     * @return
     */
    public static boolean isRecursiveElement(BytecodeInstruction bytecodeInstruction){
        if(bytecodeInstruction.getASMNode().getType() == AbstractInsnNode.FIELD_INSN){
            if(!bytecodeInstruction.getClassName().equals(((FieldInsnNode) bytecodeInstruction.getASMNode()).owner.replace('/','.'))){
                return true;
            }
        }

        return false;

    }

    public static boolean isStandardJavaVariable(String variableName){
        //TODO: read such string prefix from a .txt of .properties file or at least make a constant.
        return variableName.startsWith("java/lang/System");
    }

    /**
     * Just to keep track of total number of features. As a part of evaluation features are added to the
     * featureList (One such place is FeatureNoveltyFunction.executeAndAnalyseFeature()). The newly added
     * feature will not contain any specific value or normalizedValue.
     * This is just to keep a track to total number of features.
     *
     * @param feature
     */
    public static void updateFeatureMap(Integer key, Feature feature){
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

    public static BytecodeInstruction getInstructionById(int id) {
        for (Map.Entry<BytecodeInstruction, Integer> entry : knownInstructions.entrySet()) {
            int val = entry.getValue();
            if (val == id)
                return entry.getKey();
        }
        return null;
    }

    public static Map<FeatureKey, List<Double>> getFeatureValueRangeList() {
        return featureValueRangeList;
    }

    public static void setFeatureValueRangeList(Map<FeatureKey, List<Double>> featureValueRangeList) {
        FeatureFactory.featureValueRangeList = featureValueRangeList;
    }
}
