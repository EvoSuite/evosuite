package org.evosuite.intellij.util;

import com.intellij.openapi.project.Project;

import java.io.File;

/**
 * Created by Andrea Arcuri on 11/03/15.
 */
public class Utils {

    public static boolean isWindows(){
        String OS = System.getProperty("os.name");
        return OS.toLowerCase().contains("windows");
    }

    public static boolean isMavenProject(Project project){
        File pom = new File(project.getBasePath() , "pom.xml");
        return pom.exists();
    }
}
