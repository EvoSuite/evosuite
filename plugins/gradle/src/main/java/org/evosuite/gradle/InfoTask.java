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
public class InfoTask extends DefaultTask {

    @TaskAction
    public void evoInfo() {
        this.getProject().getLogger().info("Logger Executing 'evoInfo' task");
        System.out.println("System.out Executing 'evoInfo' task");

        Project p = getProject();
        System.out.println("DH: " + p.getDependencies());
        System.out.println("CH: " + p.getDependencies().getComponents());

        //System.out.println("Conf: "+p.getConfigurations());

        for (File f : p.files(".")) {
            System.out.println("File :" + f.getAbsolutePath());
        }

        //p.getConvention().getPlugin(EvoSuiteGradlePlugin.class).

        for (org.gradle.api.artifacts.Configuration c : p.getConfigurations()) {
            System.out.println("Conf: " + c);

            for (Dependency d : c.getAllDependencies()) {
                System.out.println("Dep: " + d);
            }

            for (PublishArtifact pa : c.getAllArtifacts()) {
                System.out.println("PA: " + pa);
            }
        }

        System.out.println("getBuildscript");
        for (org.gradle.api.artifacts.Configuration c : p.getBuildscript().getConfigurations()) {
            System.out.println("Conf: " + c);

            for (Dependency d : c.getAllDependencies()) {
                System.out.println("Dep: " + d);

                if (d.getName().equals("evosuite-gradle-plugin")) {
                    //d.
                }
            }
        }

    }

}
