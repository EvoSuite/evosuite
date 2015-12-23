package org.evosuite.idNaming;

/**
 * Created by gordon on 23/12/2015.
 */
public class VariableNameCollector {
    private static VariableNameCollector ourInstance = new VariableNameCollector();

    public static VariableNameCollector getInstance() {
        return ourInstance;
    }

    private VariableNameCollector() {
    }

    public void addParameterName(String className, String methodName, int numParam, String name) {

    }

    public String getParameterName(String className, String methodName, int numParam) {
        return null;
    }
}
