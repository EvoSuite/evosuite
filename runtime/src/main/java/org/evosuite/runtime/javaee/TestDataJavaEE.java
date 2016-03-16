/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.runtime.javaee;

import org.evosuite.runtime.util.Inputs;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Singleton class used to keep track what part of the JavaEE container have been accessed
 * and that can be useful to generate proper test data
 */
public class TestDataJavaEE {

    public static final String HTTP_REQUEST_CONTENT_TYPE= "javaee.servlet.http.contentType";
    public static final String HTTP_REQUEST_PARAM = "javaee.servlet.http.param";
    public static final String HTTP_REQUEST_PART = "javaee.servlet.http.part";
    public static final String HTTP_REQUEST_PRINCIPAL = "javaee.servlet.http.principal";

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

    /**
     * Check if there was any servlet that was initialized.
     * Note: servlet initialization is automatically added every time
     * a new servlet is instantiated with "new"
     *
     */
    private volatile boolean wasAServletInitialized;

    private final Set<String> lookedUpContextNames;


    private TestDataJavaEE(){
        httpRequestParameters = new CopyOnWriteArraySet<>();
        dispatchers = new CopyOnWriteArraySet<>();
        lookedUpContextNames = new CopyOnWriteArraySet<>();
        readContentType = false;
        partNames = null;
    }

    public static TestDataJavaEE getInstance(){
        return singleton;
    }

    public void reset(){
        httpRequestParameters.clear();
        dispatchers.clear();
        lookedUpContextNames.clear();
        readContentType = false;
        partNames = null;
        wasAServletInitialized = false;
    }

    public JeeData getJeeData(){
        return new JeeData(httpRequestParameters,dispatchers,readContentType,partNames,wasAServletInitialized,lookedUpContextNames);
    }

    public Set<String> getViewOfLookedUpContextNames(){
        return Collections.unmodifiableSet(lookedUpContextNames);
    }

    public void accessLookUpContextName(String name){
        Inputs.checkNull(name);
        lookedUpContextNames.add(name);
    }

    public boolean isWasAServletInitialized() {
        return wasAServletInitialized;
    }

    public void setWasAServletInitialized(boolean wasAServletInitialized) {
        this.wasAServletInitialized = wasAServletInitialized;
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
        Inputs.checkNull(dispatcherName);
        dispatchers.add(dispatcherName);
    }

    public void accessedHttpRequestParameter(String param) throws IllegalArgumentException{
        Inputs.checkNull(param);
        httpRequestParameters.add(param);
    }
}
