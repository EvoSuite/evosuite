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

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import org.evosuite.intellij.util.Utils;

import java.io.File;
import java.util.List;

/**
 * Created by arcuri on 9/29/14.
 */
public class EvoParameters {

    public static final String CORES_EVOSUITE_PARAM = "cores_evosuite_param";
    public static final String TIME_EVOSUITE_PARAM = "time_evosuite_param";
    public static final String MEMORY_EVOSUITE_PARAM = "memory_evosuite_param";
    public static final String TARGET_FOLDER_EVOSUITE_PARAM = "target_folder_evosuite_param";
    public static final String MVN_LOCATION = "mvn_location";
    public static final String JAVA_HOME = "JAVA_HOME";
    public static final String EVOSUITE_JAR_LOCATION = "evosuite_jar_location";
    public static final String EXECUTION_MODE = "execution_mode";
    public static final String GUI_DIALOG_WIDTH = "evosuite_gui_dialog_width";
    public static final String GUI_DIALOG_HEIGHT = "evosuite_gui_dialog_height";

    public static final String EXECUTION_MODE_MVN = "JAR";
    public static final String EXECUTION_MODE_JAR = "MVN";

    private static final EvoParameters singleton = new EvoParameters();

    private int cores;
    private int memory;
    private int time;
    private String folder;
    private String mvnLocation;
    private String javaHome;
    private String evosuiteJarLocation;
    private String executionMode;
    private int guiWidth;
    private int guiHeight;


    public static EvoParameters getInstance() {
        return singleton;
    }

    private EvoParameters() {
    }

    public boolean usesMaven() {
        return executionMode.equals(EXECUTION_MODE_MVN);
    }

    public void load(Project project) {
        PropertiesComponent p = PropertiesComponent.getInstance(project);
        cores = p.getInt(CORES_EVOSUITE_PARAM, 1);
        memory = p.getInt(MEMORY_EVOSUITE_PARAM, 2000);
        time = p.getInt(TIME_EVOSUITE_PARAM, 3);
        folder = p.getValue(TARGET_FOLDER_EVOSUITE_PARAM, "src/evo");

        String envJavaHome = System.getenv("JAVA_HOME");
        javaHome = p.getValue(JAVA_HOME, envJavaHome != null ? envJavaHome : "");
        mvnLocation = p.getValue(MVN_LOCATION, "");
        evosuiteJarLocation = p.getValue(EVOSUITE_JAR_LOCATION, "");
        executionMode = p.getValue(EXECUTION_MODE, EXECUTION_MODE_MVN);

        guiWidth = p.getInt(GUI_DIALOG_WIDTH, 570);
        guiHeight = p.getInt(GUI_DIALOG_HEIGHT, 300);
    }

    public void save(Project project) {
        PropertiesComponent p = PropertiesComponent.getInstance(project);
        p.setValue(CORES_EVOSUITE_PARAM, "" + cores);
        p.setValue(TIME_EVOSUITE_PARAM, "" + time);
        p.setValue(MEMORY_EVOSUITE_PARAM, "" + memory);
        p.setValue(TARGET_FOLDER_EVOSUITE_PARAM, folder);
        p.setValue(JAVA_HOME, javaHome);
        p.setValue(MVN_LOCATION, getPossibleLocationForMvn());
        p.setValue(EVOSUITE_JAR_LOCATION, evosuiteJarLocation);
        p.setValue(EXECUTION_MODE, executionMode);
        p.setValue(GUI_DIALOG_WIDTH, "" + guiWidth);
        p.setValue(GUI_DIALOG_HEIGHT, "" + guiHeight);
    }

    private String getPossibleLocationForMvn() {

        List<String> mvnExeList = Utils.getMvnExecutableNames();

        String mvnHome = System.getenv("MAVEN_HOME");
        if (mvnHome == null || mvnHome.isEmpty()) {
            mvnHome = System.getenv("M2_HOME");
        }
        if (mvnHome == null || mvnHome.isEmpty()) {
            mvnHome = System.getenv("MVN_HOME");
        }

        if (mvnHome == null || mvnHome.isEmpty()) {
            //check in PATH
            String path = System.getenv("PATH");
            String[] tokens = path.split(File.pathSeparator);
            for (String location : tokens) {
                if (!(location.contains("maven") || location.contains("mvn"))) {
                    continue;
                }
                for (String mvnExe : mvnExeList) {
                    File exe = new File(location, mvnExe);
                    if (exe.exists()) {
                        return exe.getAbsolutePath();
                    }
                }
            }
            return "";

        } else {
            for (String mvnExe : mvnExeList) {
                File exe = new File(mvnHome, "bin");
                exe = new File(exe, mvnExe);
                if (exe.exists()) {
                    return exe.getAbsolutePath();
                }
            }
            return "";
        }
    }

    public int getCores() {
        return cores;
    }

    public void setCores(int cores) {
        this.cores = cores;
    }

    public int getMemory() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getFolder() {
        return folder;
    }

    // TODO: Could check if that folder exists and is set as a source folder
    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getMvnLocation() {
        return mvnLocation;
    }

    public void setMvnLocation(String mvnLocation) {
        this.mvnLocation = mvnLocation;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    public String getEvosuiteJarLocation() {
        return evosuiteJarLocation;
    }

    public void setEvosuiteJarLocation(String evosuiteJarLocation) {
        this.evosuiteJarLocation = evosuiteJarLocation;
    }

    public String getExecutionMode() {
        return executionMode;
    }

    public void setExecutionMode(String executionMode) {
        this.executionMode = executionMode;
    }

    public int getGuiWidth() {
        return guiWidth;
    }

    public void setGuiWidth(int guiWidth) {
        this.guiWidth = guiWidth;
    }

    public int getGuiHeight() {
        return guiHeight;
    }

    public void setGuiHeight(int guiHeight) {
        this.guiHeight = guiHeight;
    }
}
