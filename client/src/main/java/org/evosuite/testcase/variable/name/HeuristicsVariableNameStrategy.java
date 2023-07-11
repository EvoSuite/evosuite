package org.evosuite.testcase.variable.name;

import org.apache.commons.lang3.StringUtils;
import org.evosuite.testcase.utils.HeuristicsUtil;
import org.evosuite.testcase.variable.VariableReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HeuristicsVariableNameStrategy extends AbstractVariableNameStrategy{

    protected final Map<String, Integer> nextIndices = new ConcurrentHashMap<>();
    /**
     * Dictionaries for naming information
     */
    protected Map<VariableReference, String> methodNames = new HashMap<>();
    protected Map<VariableReference, String> argumentNames = new HashMap<>();

    private TypeBasedVariableNameStrategy typeBasedVariableNameStrategy = new TypeBasedVariableNameStrategy();
    @Override
    public String createNameForVariable(VariableReference variable) {
        String typeBasedName = typeBasedVariableNameStrategy.getPlainNameForVariable(variable);
        return getPrioritizedName(variable, typeBasedName);
    }

    /**
     * Returns the variable name + the corresponding index if and only if there is more than one repetition of the name,
     * otherwise, it returns the name without an index at last.
     *
     * Mainly used for Heuristic Renaming Strategy.
     *
     * @return String
     */
    private String getVariableWithIndexExcludingFirstAppearance(String variableName) {
        if (!this.nextIndices.containsKey(variableName)) {
            this.nextIndices.put(variableName, 0);
        }
        else {
            final int index = this.nextIndices.get(variableName);
            this.nextIndices.put(variableName, index + 1);
            variableName += this.nextIndices.get(variableName);
        }
        return variableName;
    }
    /**
     * Retrieve a suggested name based on method, argument and type information.
     *
     * The followed order for prioritizing is:
     * 1. Use argument suggestion, if not possible
     * 2. Use method suggestion + reductions, if not possible
     * 3. Use type suggestion, traditional naming.
     *
     * @return String
     */
    private String getPrioritizedName(final VariableReference var, String variableName) {
        final String methodCode = this.methodNames.get(var);
        final String arguments = this.argumentNames.get(var);
        if (arguments != null) {
            variableName = arguments;
        }
        else if (methodCode != null) {
            variableName = analyzeMethodName(methodCode);
        }
        if(variableName.equals(var.getSimpleClassName())){
            variableName = "_" + variableName;
        }
        return variableName;
    }

    /**
     * Returns the suggested method name controlling camel case and excluding some particles
     * of the method names.
     *
     * @return String
     */
    private String analyzeMethodName(String methodCode) {
        String name = "";
        ArrayList<String> methodName = HeuristicsUtil.separateByCamelCase(methodCode);
        if(methodCode.length() > 0){
            if(HeuristicsUtil.containsAvoidableParticle(methodName.get(0)) && methodName.size() > 1){
                name = StringUtils.join(methodName.subList(1,methodName.size()), "");
                final char[] auxCharArray = name.toCharArray();
                auxCharArray[0] = Character.toLowerCase(auxCharArray[0]);
                return new String(auxCharArray);
            }
        }
        return methodCode;
    }

    public void addVariableInformation(Map<String, Map<VariableReference, String>> information){
        methodNames = information.get("MethodNames");
        argumentNames = information.get("ArgumentNames");
    }

}
