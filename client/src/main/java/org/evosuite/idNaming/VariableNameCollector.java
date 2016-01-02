package org.evosuite.idNaming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gordon on 23/12/2015.
 */
public class VariableNameCollector {

    private static final Logger logger = LoggerFactory.getLogger(VariableNameCollector.class);

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
        if(!parameterMap.containsKey(className)) {
            logger.debug("Don't have class "+className +": "+parameterMap.keySet());
            return false;
        }
        if(!parameterMap.get(className).containsKey(methodName)) {
            logger.debug("Don't have method "+methodName +": "+parameterMap.get(className).keySet());
            return false;
        }
        if(parameterMap.get(className).get(methodName).size() <= numParam) {
            logger.debug("Don't have parameter "+numParam+": "+parameterMap.get(className).get(methodName));
            return false;
        }
        return true;
    }

    public String getParameterName(String className, String methodName, int numParam) {

        return parameterMap.get(className).get(methodName).get(numParam);
    }
}
