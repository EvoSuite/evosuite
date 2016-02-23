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

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Immutable class keeping track of which relevant parts of JEE were accessed by the SUT
 *
 * Created by Andrea Arcuri on 11/08/15.
 */
public class JeeData implements Serializable{

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


    public final Set<String> lookedUpContextNames;


    public JeeData(Set<String> httpRequestParameters, Set<String> dispatchers, boolean readContentType,
                   Set<String> partNames, boolean wasAServletInitialized, Set<String> lookedUpContextNames) {

        this.httpRequestParameters = Collections.unmodifiableSet(new LinkedHashSet<>(httpRequestParameters));
        this.dispatchers = Collections.unmodifiableSet(new LinkedHashSet<>(dispatchers));
        this.readContentType = readContentType;
        this.partNames = partNames==null ? null : Collections.unmodifiableSet(new LinkedHashSet<>(partNames));
        this.wasAServletInitialized = wasAServletInitialized;
        this.lookedUpContextNames = Collections.unmodifiableSet(new LinkedHashSet<>(lookedUpContextNames));
    }
}
