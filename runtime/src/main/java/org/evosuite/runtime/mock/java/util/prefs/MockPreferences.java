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

import org.evosuite.runtime.mock.StaticReplacementMock;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Created by gordon on 26/12/2016.
 */
public class MockPreferences implements StaticReplacementMock {
    @Override
    public String getMockedClassName() {
        return Preferences.class.getName();
    }

    private static PreferencesImpl userRoot = new PreferencesImpl(null, "");

    private static PreferencesImpl systemRoot = new PreferencesImpl(null, "");

    public static void resetPreferences() {
        try {
            userRoot.removeNodeSpi();
            systemRoot.removeNodeSpi();
        } catch (BackingStoreException e) {
            // Can't actually happen
        }
    }

    public static Preferences userRoot() {
        return userRoot;
    }

    public static Preferences systemRoot() {
        return systemRoot;
    }

    public static Preferences systemNodeForPackage(Class<?> c) {
        return systemRoot().node(nodeName(c));
    }

    public static Preferences userNodeForPackage(Class<?> c) {
        return userRoot().node(nodeName(c));
    }

    private static String nodeName(Class c) {
        if (c.isArray())
            throw new IllegalArgumentException("Arrays have no associated preferences node.");
        String className = c.getName();
        int pkgEndIndex = className.lastIndexOf('.');
        if (pkgEndIndex < 0)
            return "/<unnamed>";
        String packageName = className.substring(0, pkgEndIndex);
        return "/" + packageName.replace('.', '/');
    }
}
