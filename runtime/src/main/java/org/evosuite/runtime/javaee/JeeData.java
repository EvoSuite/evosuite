package org.evosuite.runtime.javaee;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Immutable class keeping track of which relevant parts of JEE were accessed by the SUT
 *
 * Created by Andrea Arcuri on 11/08/15.
 */
public class JeeData {

    /**
     * Keep track of all parameters that have been checked in a http servlet
     */
    public final Set<String> httpRequestParameters;

    /**
     * Keep track of all http request dispatchers
     */
    public final Set<String> dispatchers;

    /**
     * Did the SUT check the content type of the request or of any of its parts?
     */
    public final boolean readContentType;


    /**
     * Keep track of which parts were asked for.
     * The set is {@code null} if no part was ever read, not even with a get all.
     */
    public final Set<String> partNames;

    /**
     * Check if there was any servlet that was initialized.
     * Note: servlet initialization is automatically added every time
     * a new servlet is instantiated with "new"
     *
     */
    public final boolean wasAServletInitialized;


    public JeeData(Set<String> httpRequestParameters, Set<String> dispatchers, boolean readContentType,
                   Set<String> partNames, boolean wasAServletInitialized) {

        this.httpRequestParameters = Collections.unmodifiableSet(new LinkedHashSet<>(httpRequestParameters));
        this.dispatchers = Collections.unmodifiableSet(new LinkedHashSet<>(dispatchers));
        this.readContentType = readContentType;
        this.partNames = Collections.unmodifiableSet(new LinkedHashSet<>(partNames));
        this.wasAServletInitialized = wasAServletInitialized;
    }
}
