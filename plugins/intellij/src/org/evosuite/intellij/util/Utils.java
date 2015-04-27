package org.evosuite.intellij.util;

import com.intellij.openapi.compiler.CompilerPaths;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.vfs.VirtualFile;

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

    public static String getFolderLocation(Module module){
        ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
        VirtualFile[] contentRoots = rootManager.getContentRoots(); //TODO check why IntelliJ does return an array here
        return new File(contentRoots[0].getCanonicalPath()).getAbsolutePath();
    }

    public static String getFullClassPath(Module m){
        String cp = "";

        cp += CompilerPaths.getModuleOutputPath(m,false);

        for(VirtualFile vf : OrderEnumerator.orderEntries(m).recursively().getClassesRoots()){
            String entry = new File(vf.getPath()).getAbsolutePath();
            if(entry.endsWith("!/")){ //not sure why it happens in the returned paths
                entry = entry.substring(0,entry.length()-2);
            }
            if(entry.endsWith("!")){
                entry = entry.substring(0,entry.length()-1);
            }
            cp += File.pathSeparator + entry;
        }
        return cp;
    }
}
