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
package org.evosuite.runtime.mock.java.util.prefs;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

/**
 * Created by gordon on 26/12/2016.
 */
public class PreferencesImpl extends AbstractPreferences {

    private Map<String, String> values = new LinkedHashMap<>();

    private Map<String, PreferencesImpl> children = new LinkedHashMap<>();

    public PreferencesImpl(PreferencesImpl parent, String name) {
        super(parent, name);
    }

    @Override
    protected String getSpi(String key) {
        return values.get(key);
    }

    @Override
    protected void putSpi(String key, String value) {
        values.put(key, value);
    }

    @Override
    protected void removeSpi(String key) {
        values.remove(key);
    }

    @Override
    protected AbstractPreferences childSpi(String name) {
        if(children.containsKey(name))
            return children.get(name);
        else {
            PreferencesImpl child = new PreferencesImpl(this, name);
            children.put(name, child);
            return child;
        }
    }

    @Override
    protected void removeNodeSpi() throws BackingStoreException {
        values.clear();
        children.clear();
    }

    @Override
    protected String[] keysSpi() throws BackingStoreException {
        return values.keySet().stream().toArray(String[]::new);
    }

    @Override
    protected String[] childrenNamesSpi() throws BackingStoreException {
        return children.keySet().stream().toArray(String[]::new);
    }

    @Override
    protected void syncSpi() throws BackingStoreException {
        // No-op
    }

    @Override
    protected void flushSpi() throws BackingStoreException {
        // No-op
    }
}
