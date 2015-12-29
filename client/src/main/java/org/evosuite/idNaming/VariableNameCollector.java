package org.evosuite.idNaming;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gordon on 23/12/2015.
 */
public class VariableNameCollector {
    private static VariableNameCollector ourInstance = new VariableNameCollector();

    public static VariableNameCollector getInstance() {
        return ourInstance;
    }

    private Map<String, Map<String, List<String>>> parameterMap = new LinkedHashMap<>();

    private VariableNameCollector() {
    }

    public void addParameterName(String className, String methodName, int numParam, String name) {
        if(!parameterMap.containsKey(className))
            parameterMap.put(className, new LinkedHashMap<>());
        if(!parameterMap.get(className).containsKey(methodName))
            parameterMap.get(className).put(methodName, new ArrayList<>());
        parameterMap.get(className).get(methodName).add(name);
    }

    public boolean hasParameterName(String className, String methodName, int numParam) {
        System.out.println("Parameters: "+parameterMap);
        if(!parameterMap.containsKey(className))
            return false;
        if(!parameterMap.get(className).containsKey(methodName))
            return false;
        if(parameterMap.get(className).get(methodName).size() <= numParam)
            return false;
        return true;
    }

    public String getParameterName(String className, String methodName, int numParam) {

        return parameterMap.get(className).get(methodName).get(numParam);
    }
}
