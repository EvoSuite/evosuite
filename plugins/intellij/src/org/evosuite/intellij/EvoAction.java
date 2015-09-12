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
package org.evosuite.intellij;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.evosuite.intellij.util.AsyncGUINotifier;
import org.evosuite.intellij.util.EvoSuiteExecutor;
import org.evosuite.intellij.util.Utils;

import java.io.File;
import java.util.*;

/**
 * Created by arcuri on 9/24/14.
 */
public class EvoAction extends AnAction {

    public EvoAction() {
        super("Run EvoSuite");
    }


    public void actionPerformed(AnActionEvent event) {

        String title = "EvoSuite Plugin";
        Project project = event.getData(PlatformDataKeys.PROJECT);

        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.getToolWindow("EvoSuite");

        final AsyncGUINotifier notifier = IntelliJNotifier.getNotifier(project);

        if (EvoSuiteExecutor.getInstance().isAlreadyRunning()) {
            Messages.showMessageDialog(project, "An instance of EvoSuite is already running",
                    title, Messages.getErrorIcon());
            return;
        }

        Map<String,Set<String>> map = getCUTsToTest(event);
        if(map==null || map.isEmpty()){
            Messages.showMessageDialog(project, "No '.java' file or non-empty source folder was selected in a valid module",
                    title, Messages.getErrorIcon());
            return;
        }

        EvoStartDialog dialog = new EvoStartDialog();
        dialog.initFields(project, EvoParameters.getInstance());
        dialog.setModal(true);
        dialog.setLocationRelativeTo(null);
        //dialog.setLocationByPlatform(true);
        dialog.pack();
        dialog.setVisible(true);

        if (dialog.isWasOK()) {
            toolWindow.show(new Runnable(){@Override public void run(){
                notifier.clearConsole();
            }
            });
            EvoParameters.getInstance().save(project);
            EvoSuiteExecutor.getInstance().run(project,EvoParameters.getInstance(),map,notifier);
        }
    }

    /**
     *
     *
     * @return a map where key is a maven module root path, and value a list of full class names of CUTs
     */
    private Map<String, Set<String>> getCUTsToTest(AnActionEvent event){

        Map<String,Set<String>> map = new LinkedHashMap<String, Set<String>>();

        /*
            full paths of all source root folders.
            this is needed to calculate the Java class names from the .java file paths
         */
        Set<String> roots = new LinkedHashSet<String>();

        Project project = event.getData(PlatformDataKeys.PROJECT);
        String projectDir = new File(project.getBaseDir().getCanonicalPath()).getAbsolutePath(); //note: need "File" to avoid issues in Windows

        Set<String> modulePaths = new LinkedHashSet<String>(); //TODO refactor to include roots in it

        for (Module module : ModuleManager.getInstance(project).getModules()) {
            for(VirtualFile sourceRoot : ModuleRootManager.getInstance(module).getSourceRoots()){
                String path = new File(sourceRoot.getCanonicalPath()).getAbsolutePath();
                roots.add(path);
                /*
                if(getModuleFolder(projectDir, path) != null) {
                    roots.add(path);
                } else {
                    //should never happen? above code comes from when we were only supporting Maven. maybe now deprecated/convoluted?
                }
                */
            }

            modulePaths.add(Utils.getFolderLocation(module));
        }

        if (roots.isEmpty()){
            return null;
        }

        for(VirtualFile virtualFile : event.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY)){
            String path = new File(virtualFile.getCanonicalPath()).getAbsolutePath();
            String module = getModuleFolder(projectDir, path, modulePaths);

            if(module == null){
                continue;
            }

            Set<String> classes = map.get(module);
            if(classes == null){
                classes = new LinkedHashSet<String>();
            }

            String root = getSourceRootForFile(path, roots);

            if(root == null){
                /*
                    the chosen file is not in a source folder.
                    Need to check if its parent of any of them
                 */
                Set<String> included = getIncludedSourceRoots(path,roots);
                if(included==null || included.isEmpty()){
                    continue;
                }

                for(String sourceFolder : included){
                    scanFolder(new File(sourceFolder),classes,sourceFolder);
                }

            } else {
                if(!virtualFile.isDirectory()){
                    if(!path.endsWith(".java")){
                        // likely a resource file
                        continue;
                    }

                    String name = getCUTName(path, root);
                    classes.add(name);
                } else {
                    scanFolder(new File(virtualFile.getCanonicalPath()),classes,root);
                }

            }

            map.put(module, classes);

            //if(map.containsKey(maven) && map.get(maven)==null){
                /*
                    special case: we are already covering the whole module, so no point
                    in also specifying single files inside it

                    FIXME: this actually should not happen anymore. However, now there is
                    potential performance issue of always having to determine every single .java file
                 */
              //  continue;
            //}
        }

        return map;
    }

    private void scanFolder(File file, Set<String> classes, String root) {
        for(File child : file.listFiles()){
            if(child.isDirectory()){
                scanFolder(child, classes, root);
            } else {
                String path = child.getAbsolutePath();
                if(path.endsWith(".java")){
                    String name = getCUTName(path,root);
                    classes.add(name);
                }
            }
        }
    }

    private String getCUTName(String path, String root) {
        String name = path.substring(root.length()+1, path.length() - ".java".length());
        name = name.replace('/','.'); //posix
        name = name.replace("\\", ".");  // windows
        return name;
    }

    private Set<String> getIncludedSourceRoots(String path, Set<String> roots){
        Set<String> set = new HashSet<String>();
        for(String root : roots){
            if(root.startsWith(path)){
               set.add(root);
            }
        }
        return set;
    }

    private String getSourceRootForFile(String path, Set<String> roots){
        for(String root : roots){
            if(path.startsWith(root)){
                return root;
            }
        }
        return null;
    }

    /**
     *
     * @param projectDir
     * @param source
     * @return
     */
    private String getModuleFolder(String projectDir, String source, Set<String> modulePaths){
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
