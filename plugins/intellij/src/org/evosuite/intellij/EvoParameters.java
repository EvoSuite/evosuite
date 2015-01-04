package org.evosuite.intellij;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;

/**
 * Created by arcuri on 9/29/14.
 */
public class EvoParameters {

    public static final String CORES_EVOSUITE_PARAM = "cores_evosuite_param";
    public static final String TIME_EVOSUITE_PARAM = "time_evosuite_param";
    public static final String MEMORY_EVOSUITE_PARAM = "memory_evosuite_param";
    public static final String TARGET_FOLDER_EVOSUITE_PARAM = "target_folder_evosuite_param";


    private static final EvoParameters singleton = new EvoParameters();

    private int cores;
    private int memory;
    private int time;
    private String folder;


    public static EvoParameters getInstance(){
        return singleton;
    }

    private EvoParameters(){
    }

    public void load(Project project){
        PropertiesComponent p = PropertiesComponent.getInstance(project);
        cores = p.getOrInitInt(CORES_EVOSUITE_PARAM,1);
        memory = p.getOrInitInt(MEMORY_EVOSUITE_PARAM,500);
        time = p.getOrInitInt(TIME_EVOSUITE_PARAM,1);
        folder = p.getOrInit(TARGET_FOLDER_EVOSUITE_PARAM,"src/evo");
    }

    public void save(Project project){
        PropertiesComponent p = PropertiesComponent.getInstance(project);
        p.setValue(CORES_EVOSUITE_PARAM,""+cores);
        p.setValue(TIME_EVOSUITE_PARAM,""+time);
        p.setValue(MEMORY_EVOSUITE_PARAM,""+memory);
        p.setValue(TARGET_FOLDER_EVOSUITE_PARAM,folder);
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

    public void setFolder(String folder) {
        this.folder = folder;
    }
}
