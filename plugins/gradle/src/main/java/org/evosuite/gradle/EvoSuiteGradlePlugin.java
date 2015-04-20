package org.evosuite.gradle;

import org.gradle.api.Project;
import org.gradle.api.Plugin;

/**
 * Created by Andrea Arcuri on 15/04/15.
 */
public class EvoSuiteGradlePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getTasks().create("evoInfo",InfoTask.class);
    }
}
