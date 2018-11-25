package org.evosuite.utils;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

public class Java9InvisiblePackage {
    private static boolean classesLoaded = false;
    public static List<String> excludedClasses = new ArrayList<>();

    private static void loadExcludedClassNames() {
        if (classesLoaded)
            return;
        ResourceBundle rb = ResourceBundle.getBundle("java9_invisible_packages");
        classesLoaded = true;
        Enumeration<String> keys = rb.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            String value = rb.getString(key);
            excludedClasses.add(value);
        }
    }

    /**
     * <p>
     * getClassesToBeIgnored
     * </p>
     *
     * @return the names of packages invisible in Java9
     */
    public static List<String> getClassesToBeIgnored() {
        loadExcludedClassNames();
        return excludedClasses;
    }
}
