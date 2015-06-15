package org.evosuite.jenkins.scm;

import com.cloudbees.plugins.credentials.Credentials;

import hudson.Launcher;
import hudson.maven.AbstractMavenProject;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tools.ToolInstallation;

public interface SCM {

	public static String TESTS_DIR_TO_COMMIT = "evosuite-tests";

	public ToolInstallation findInstallation();

	public Credentials getCredentials(AbstractMavenProject<?, ?> project);

	public boolean commit(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener);

	public boolean push(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener);
}
