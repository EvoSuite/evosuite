package org.evosuite.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.tasks.TaskAction;

import java.io.File;


/**
 * Created by Andrea Arcuri on 15/04/15.
 */
public class InfoTask extends DefaultTask{

    @TaskAction
    public void evoInfo() {
        this.getProject().getLogger().info("Logger Executing 'evoInfo' task");
        System.out.println("System.out Executing 'evoInfo' task");

        Project p = getProject();
        System.out.println("DH: "+p.getDependencies());
        System.out.println("CH: " + p.getDependencies().getComponents());

        //System.out.println("Conf: "+p.getConfigurations());

        for(File f : p.files(".")){
            System.out.println("File :"+f.getAbsolutePath());
        }

        //p.getConvention().getPlugin(EvoSuiteGradlePlugin.class).

        for(org.gradle.api.artifacts.Configuration c : p.getConfigurations()){
            System.out.println("Conf: "+c);

            for(Dependency d: c.getAllDependencies()){
                System.out.println("Dep: "+d);
            }

            for(PublishArtifact pa : c.getAllArtifacts()){
                System.out.println("PA: "+pa);
            }
        }

        System.out.println("getBuildscript");
        for(org.gradle.api.artifacts.Configuration c : p.getBuildscript().getConfigurations()){
            System.out.println("Conf: "+c);

            for(Dependency d: c.getAllDependencies()){
                System.out.println("Dep: "+d);

                if(d.getName().equals("evosuite-gradle-plugin")){
                    //d.
                }
            }
        }

    }

}
