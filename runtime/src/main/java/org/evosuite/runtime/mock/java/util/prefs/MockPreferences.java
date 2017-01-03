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
