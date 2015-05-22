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

    /**
     * Did the SUT check the content type of the request or of any of its parts?
     */
    private volatile boolean readContentType;


    /**
     * Keep track of which parts were asked for.
     * The set is {@code null} if no part was ever read, not even with a get all.
     */
    private volatile Set<String> partNames;


    private TestDataJavaEE(){
        httpRequestParameters = new CopyOnWriteArraySet<>();
        dispatchers = new CopyOnWriteArraySet<>();
        readContentType = false;
        partNames = null;
    }

    public static TestDataJavaEE getInstance(){
        return singleton;
    }

    public void reset(){
        httpRequestParameters.clear();
        dispatchers.clear();
        readContentType = false;
        partNames = null;
    }

    public Set<String> getViewOfHttpRequestParameters(){
        return Collections.unmodifiableSet(httpRequestParameters);
    }

    public Set<String> getViewOfDispatchers(){
        return Collections.unmodifiableSet(dispatchers);
    }

    public Set<String> getViewOfParts(){
        if(partNames==null){
            return null;
        }
        return Collections.unmodifiableSet(partNames);
    }

    public boolean wasContentTypeRead(){
        return readContentType;
    }

    public void accessPart(String name){
        if(partNames==null){
            partNames = new CopyOnWriteArraySet<>();
        }
        if(name != null){
            partNames.add(name);
        }
    }

    public void accessContentType(){
        readContentType = true;
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
