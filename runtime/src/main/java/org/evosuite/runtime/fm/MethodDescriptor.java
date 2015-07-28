package org.evosuite.runtime.fm;

/**
 * Created by Andrea Arcuri on 27/07/15.
 */
public class MethodDescriptor {

    private final String methodName;
    private final String inputParameterMatchers;
    /**
     * How often the method was called
     */
    private int counter;

    public MethodDescriptor(String methodName, String inputParameterMatchers) {
        this.methodName = methodName;
        this.inputParameterMatchers = inputParameterMatchers;
        counter = 0;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getInputParameterMatchers() {
        return inputParameterMatchers;
    }

    public String getID(){
        return getMethodName() + " : " + getInputParameterMatchers();
    }

    public int getCounter() {
        return counter;
    }

    public void increaseCounter(){
        counter++;
    }
}
