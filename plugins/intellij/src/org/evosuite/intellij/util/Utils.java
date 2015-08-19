/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.intellij.util;

import com.intellij.openapi.compiler.CompilerPaths;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
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

    public static Module getModule(Project project, String folderLocation){
        for(Module m : ModuleManager.getInstance(project).getModules()){
            if(getFolderLocation(m).equals(folderLocation)){
                return m;
            }
        }
        return null;
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

            if(entry.endsWith("zip")){
                //for some reasons, Java src.zip can end up on dependencies...
                continue;
            }

            cp += File.pathSeparator + entry;
        }
        return cp;
    }
}
