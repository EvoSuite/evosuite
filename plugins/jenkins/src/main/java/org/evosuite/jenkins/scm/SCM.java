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
package org.evosuite.jenkins.scm;

import hudson.maven.AbstractMavenProject;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;

public interface SCM {

    public static String COMMIT_MSG_PREFIX = "EvoSuite Jenkins Plugin #";

    /**
     * @param project
     * @param build
     * @param listener
     * @param branchName
     * @param ctgBestsDir
     * @return the number of committed files. -1 if the commit command failed, otherwise it returns
     * the total number of files added and committed.
     */
    public int commit(AbstractMavenProject<?, ?> project, AbstractBuild<?, ?> build, BuildListener listener, String branchName, String ctgBestsDir);

    public boolean push(AbstractBuild<?, ?> build, BuildListener listener, String branchName);

    public void rollback(AbstractBuild<?, ?> build, BuildListener listener);
}
