package org.evosuite.runtime.javaee;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Singleton class used to keep track what part of the JavaEE container have been accessed
 * and that can be useful to generate proper test data
 */
public class TestDataJavaEE {

    private static final TestDataJavaEE singleton = new TestDataJavaEE();

    /**
     * Keep track of all parameters that have been checked in a http servlet
     */
    private final Set<String> httpRequestParameters;

    /**
     * Keep track of all http request dispatchers
     */
    private final Set<String> dispatchers;

    private TestDataJavaEE(){
        httpRequestParameters = new CopyOnWriteArraySet<>();
        dispatchers = new CopyOnWriteArraySet<>();
    }

    public static TestDataJavaEE getInstance(){
        return singleton;
    }

    public void reset(){
        httpRequestParameters.clear();
        dispatchers.clear();
    }

    public Set<String> getViewOfHttpRequestParameters(){
        return Collections.unmodifiableSet(httpRequestParameters);
    }

    public Set<String> getViewOfDispatchers(){
        return Collections.unmodifiableSet(dispatchers);
    }

    public void accessedDispatcher(String dispatcherName) throws IllegalArgumentException{
        if(dispatcherName == null){
            throw new IllegalArgumentException("Null input");
        }
        dispatchers.add(dispatcherName);
    }

    public void accessedHttpRequestParameter(String param) throws IllegalArgumentException{
        if(param == null){
            throw new IllegalArgumentException("Null input");
        }
        httpRequestParameters.add(param);
    }
}
