package org.evosuite.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public enum Java9InvisiblePackage {

    instance;
    private static boolean classesLoaded = false;
    private static List<String> excludedClasses = new ArrayList<>();
    private static final String FILENAME = "/java9_invisible_packages.txt";

    private void loadExcludedClassNames() {
        if (classesLoaded)
            return;
        classesLoaded = true;
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(
                FILENAME)))) {
            String strLine;
            while ((strLine = br.readLine()) != null) {
                excludedClasses.add(strLine);
            }
        } catch (IOException e) {
            System.err.println("Wrong filename/path/file is missing");
            e.printStackTrace();
        }
    }

    /**
     * <p>
     * getClassesToBeIgnored
     * </p>
     *
     * @return the names of packages invisible in Java9
     */
    public List<String> getClassesToBeIgnored() {
        loadExcludedClassNames();
        return excludedClasses;
    }
}
