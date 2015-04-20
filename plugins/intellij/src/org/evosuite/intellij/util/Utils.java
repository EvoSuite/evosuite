package org.evosuite.intellij.util;

/**
 * Created by Andrea Arcuri on 11/03/15.
 */
public class Utils {

    public static boolean isWindows(){
        String OS = System.getProperty("os.name");
        return OS.toLowerCase().contains("windows");
    }
}
