package org.evosuite.testcase.variable.name;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.CharUtils;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.variable.VariableReference;

/**
 * A name strategy that is based on variable type.
 * <br>
 * <b>Examples:</b> integer0, string0, string1, ...
 *
 * @author Afonso Oliveira
 */
public class TypeBasedVariableNameStrategy extends AbstractVariableNameStrategy {

    protected final Map<String, Integer> nextIndices = new ConcurrentHashMap<>();

    @Override
    public String createNameForVariable(VariableReference var) {
        String variableName = getPlainNameForVariable(var);
        return getIndexIncludingFirstAppearance(variableName);
    }

    public String getPlainNameForVariable(VariableReference var){
        String className = var.getSimpleClassName();
        String variableName;
        if (var instanceof ArrayReference) {
            variableName = className.substring(0, 1).toLowerCase()
                    + className.substring(1) + "Array";
            variableName = variableName.replace('.', '_').replace("[]", "");
        } else {
            variableName = className.substring(0, 1).toLowerCase()
                    + className.substring(1);
            if (variableName.contains("[]")) {
                variableName = variableName.replace("[]", "Array");
            }
            variableName = variableName.replace(".", "_");

            // Need a way to check for exact types, not assignable
            // int numObjectsOfType = test != null ? test.getObjects(var.getType(),
            //                                                      test.size()).size() : 2;
            // if (numObjectsOfType > 1 || className.equals(variableName)) {
            if (CharUtils.isAsciiNumeric(variableName.charAt(variableName.length() - 1)))
                variableName += "_";
            // }

        }
        return variableName;
    }

    /**
     * Returns the variable name + the number of repetitions counting from 0.
     * i.e. If the variable appears only once in the test, it is named as variable0.
     *
     * Mainly used for Type-Based Renaming Strategy (traditional naming in EvoSuite).
     *
     * @return String
     */
    private String getIndexIncludingFirstAppearance(String variableName) {
        if (!nextIndices.containsKey(variableName)) {
            nextIndices.put(variableName, 0);
        }
        int index = nextIndices.get(variableName);
        nextIndices.put(variableName, index + 1);
        return variableName += index;
    }
    public void addVariableInformation(Map<String, Map<VariableReference, String>> information){
        //If needed any information about types
    }

}
