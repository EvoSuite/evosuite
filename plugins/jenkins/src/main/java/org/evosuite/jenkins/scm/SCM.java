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
package org.evosuite.jenkins.scm;

import com.cloudbees.plugins.credentials.Credentials;

import hudson.Launcher;
import hudson.maven.AbstractMavenProject;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tools.ToolInstallation;

public interface SCM {

	public static String TESTS_DIR_TO_COMMIT = "evosuite-tests";

	public static String EVOSUITE_BRANCH = "evosuite-tests";

	public ToolInstallation findInstallation();

	public Credentials getCredentials(AbstractMavenProject<?, ?> project);

	public boolean commit(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener);

	public boolean push(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener);
}
