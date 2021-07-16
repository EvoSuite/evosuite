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
        if (var instanceof ArrayReference) {
            String className = var.getSimpleClassName();
            // int num = 0;
            // for (VariableReference otherVar : variableNames.keySet()) {
            // if (!otherVar.equals(var)
            // && otherVar.getVariableClass().equals(var.getVariableClass()))
            // num++;
            // }
            String variableName = className.substring(0, 1).toLowerCase()
                    + className.substring(1) + "Array";
            variableName = variableName.replace('.', '_').replace("[]", "");

            if (!nextIndices.containsKey(variableName)) {
                nextIndices.put(variableName, 0);
            }

            int index = nextIndices.get(variableName);
            nextIndices.put(variableName, index + 1);

            variableName += index;

            return variableName;
        } else {
            String className = var.getSimpleClassName();
            // int num = 0;
            // for (VariableReference otherVar : variableNames.keySet()) {
            // if (otherVar.getVariableClass().equals(var.getVariableClass()))
            // num++;
            // }
            String variableName = className.substring(0, 1).toLowerCase()
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

            if (!nextIndices.containsKey(variableName)) {
                nextIndices.put(variableName, 0);
            }

            int index = nextIndices.get(variableName);
            nextIndices.put(variableName, index + 1);

            variableName += index;
            // }

            return variableName;
        }
    }

}
