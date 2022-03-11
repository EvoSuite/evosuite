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

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by arcuri on 9/24/14.
 */
public class EvoAction extends AnAction {

    public EvoAction() {
        super("Run EvoSuite",
                "Open GUI dialog to configure and start running EvoSuite to generate JUnit tests automatically",
                loadIcon());
    }

    private static Icon loadIcon() {
        try {
            Image image = ImageIO.read(EvoAction.class.getClassLoader().getResourceAsStream("evosuite.png"));
            image = image.getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH);
            ImageIcon icon = new ImageIcon(image);

            return icon;
        } catch (IOException e) {
            e.printStackTrace(); //should not really happen
        }
        return null;
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

        Map<String, Set<String>> map = getCUTsToTest(event);
        if (map == null || map.isEmpty() || map.values().stream().mapToInt(Set::size).sum() == 0) {
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
            toolWindow.show(() -> notifier.clearConsole());
            EvoParameters.getInstance().save(project);
            EvoSuiteExecutor.getInstance().run(project, EvoParameters.getInstance(), map, notifier);
        }
    }

    /**
     * @return a map where key is a maven module root path, and value a list of full class names of CUTs
     */
    private Map<String, Set<String>> getCUTsToTest(AnActionEvent event) {

        Map<String, Set<String>> map = new LinkedHashMap<>();

        Project project = event.getData(PlatformDataKeys.PROJECT);

        ModulesInfo modulesInfo = new ModulesInfo(project);

        if (!modulesInfo.hasRoots()) {
            return null;
        }

        Set<String> alreadyHandled = new LinkedHashSet<>();

        for (VirtualFile virtualFile : event.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY)) {
            String selectedFilePath = new File(virtualFile.getCanonicalPath()).getAbsolutePath();
            recursiveHandle(map, modulesInfo, alreadyHandled, selectedFilePath);
        }

        return map;
    }


    private void recursiveHandle(Map<String, Set<String>> map, ModulesInfo modulesInfo, Set<String> alreadyHandled, String path) {

        if (alreadyHandled.contains(path)) {
            return;
        }

        Set<String> skip = handleSelectedPath(map, modulesInfo, path);
        alreadyHandled.add(path);

        for (String s : skip) {
            recursiveHandle(map, modulesInfo, alreadyHandled, s);
        }
    }


    private Set<String> handleSelectedPath(Map<String, Set<String>> map, ModulesInfo modulesInfo, String selectedFilePath) {

         /*
                if Module A includes sub-module B, the source roots in B should
                not be marked for A
             */
        Set<String> skip = new LinkedHashSet<>();

        String module = modulesInfo.getModuleFolder(selectedFilePath);
        File selectedFile = new File(selectedFilePath);

        if (module == null) {
            return skip;
        }

        Set<String> classes = map.getOrDefault(module, new LinkedHashSet<>());

        String root = modulesInfo.getSourceRootForFile(selectedFilePath);

        if (root == null) {
            /*
                the chosen file is not in a source folder.
                Need to check if its parent of any of them
             */
            Set<String> included = modulesInfo.getIncludedSourceRoots(selectedFilePath);
            if (included == null || included.isEmpty()) {
                return skip;
            }

            for (String otherModule : modulesInfo.getModulePathsView()) {

                if (otherModule.length() > module.length() && otherModule.startsWith(module)) {
                    //the considered module has a sub-module
                    included.stream().filter(inc -> inc.startsWith(otherModule)).forEach(skip::add);
                }
            }

            for (String sourceFolder : included) {
                if (skip.contains(sourceFolder)) {
                    continue;
                }
                scanFolder(new File(sourceFolder), classes, sourceFolder);
            }

        } else {
            if (!selectedFile.isDirectory()) {
                if (!selectedFilePath.endsWith(".java")) {
                    // likely a resource file
                    return skip;
                }

                String name = getCUTName(selectedFilePath, root);
                classes.add(name);
            } else {
                scanFolder(selectedFile, classes, root);
            }

        }

        if (!classes.isEmpty()) {
            map.put(module, classes);
        }

        return skip;
    }

    private void scanFolder(File file, Set<String> classes, String root) {
        for (File child : file.listFiles()) {
            if (child.isDirectory()) {
                scanFolder(child, classes, root);
            } else {
                String path = child.getAbsolutePath();
                if (path.endsWith(".java")) {
                    String name = getCUTName(path, root);
                    classes.add(name);
                }
            }
        }
    }

    private String getCUTName(String path, String root) {
        String name = path.substring(root.length() + 1, path.length() - ".java".length());
        name = name.replace('/', '.'); //posix
        name = name.replace("\\", ".");  // windows
        return name;
    }
}
