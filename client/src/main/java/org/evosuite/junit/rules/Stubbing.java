/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.junit.rules;

import javax.swing.*;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Stubbing extends BaseRule {

    private final Map<String, String> propertiesToSet = new HashMap<>();

    private final Set<String> propertiesToClear = new LinkedHashSet<>();

    private static final java.util.Properties defaultProperties = (java.util.Properties) java.lang.System.getProperties().clone();

    private PrintStream systemOut = null;

    private PrintStream systemErr = null;

    private PrintStream logStream = null;

    public Stubbing() {
        org.evosuite.Properties.REPLACE_CALLS = true;
        initProperties();
    }

    public void initProperties() {

    }

    public void setProperty(String key, String value) {
        propertiesToSet.put(key, value);
    }

    public void clearProperty(String key) {
        propertiesToClear.add(key);
    }

    @Override
    protected void before() {
        org.evosuite.runtime.Runtime.getInstance().resetRuntime();
        systemErr = System.err;
        systemOut = System.out;
        logStream = DebugGraphics.logStream();
        for (String key : propertiesToSet.keySet()) {
            java.lang.System.setProperties((java.util.Properties) defaultProperties.clone());
            java.lang.System.setProperty(key, propertiesToSet.get(key));
        }
        for (String key : propertiesToClear) {
            java.lang.System.clearProperty(key);
        }

    }

    @Override
    protected void after() {
        System.setErr(systemErr);
        System.setOut(systemOut);
        DebugGraphics.setLogStream(logStream);
        java.lang.System.setProperties((java.util.Properties) defaultProperties.clone());
    }

}
