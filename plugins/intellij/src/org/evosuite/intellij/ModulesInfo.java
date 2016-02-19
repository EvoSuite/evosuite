/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.intellij;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.evosuite.intellij.util.Utils;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by Andrea Arcuri on 11/02/16.
 */
public class ModulesInfo {

        /*
            full paths of all source root folders.
            this is needed to calculate the Java class names from the .java file paths
        */
    private final Set<String> roots = new LinkedHashSet<>();
    private final Set<String> modulePaths = new LinkedHashSet<>();
    private final String projectDir;

    public ModulesInfo(Project project){
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            for(VirtualFile sourceRoot : ModuleRootManager.getInstance(module).getSourceRoots()){
                String path = new File(sourceRoot.getCanonicalPath()).getAbsolutePath();
                roots.add(path);
            }

            String mp = Utils.getFolderLocation(module);
            if(mp != null) {
                modulePaths.add(mp);
            }
        }

        projectDir = new File(project.getBaseDir().getCanonicalPath()).getAbsolutePath(); //note: need "File" to avoid issues in Windows
    }

    public boolean hasRoots(){
        return ! roots.isEmpty();
    }

    public String getSourceRootForFile(String path){
        for(String root : roots){
            if(path.startsWith(root)){
                return root;
            }
        }
        return null;
    }

    public Set<String> getIncludedSourceRoots(String path){
        Set<String> set = new HashSet<>();
        for(String root : roots){
            if(root.startsWith(path)){
                set.add(root);
            }
        }
        return set;
    }

    public Set<String> getModulePathsView(){
        return Collections.unmodifiableSet(modulePaths);
    }

    /**
     *
     * @param source
     * @return
     */
    public String getModuleFolder(String source){
        File file = new File(source);
        while(file != null){

            String path = file.getAbsolutePath();

            if(! path.startsWith(projectDir)){
                //return projectDir; //we went too up in the hierarchy
                return null;
            }

            if(file.isDirectory()){
                File pom = new File(file,"pom.xml");
                if(pom.exists()){
                    return path;
                }
                //with new check, maybe pom.xml is not needed any more
                if(modulePaths.contains(path)){
                    return path;
                }
            }

            file = file.getParentFile();
        }
        return projectDir;
    }
}
