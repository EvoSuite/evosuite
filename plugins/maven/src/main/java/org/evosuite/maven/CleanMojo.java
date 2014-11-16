package org.evosuite.maven;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.eclipse.aether.RepositorySystemSession;
import org.evosuite.maven.util.EvoSuiteRunner;

@Mojo( name = "clean")
public class CleanMojo extends AbstractMojo{

	@Parameter(defaultValue = "${plugin.artifacts}", required = true, readonly = true)
	private List<Artifact> artifacts;

	@Component
	private ProjectBuilder projectBuilder;

	@Parameter(defaultValue="${repositorySystemSession}", required = true, readonly = true)
	private RepositorySystemSession repoSession;

	
	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject project;
	
	public void execute() throws MojoExecutionException,MojoFailureException{

		getLog().info("Going to clean all EvoSuite data");
		
		List<String> params = new ArrayList<>();
		params.add("-continuous");
		params.add("clean");
		
		EvoSuiteRunner runner = new EvoSuiteRunner(getLog(),artifacts,projectBuilder,repoSession);
		runner.registerShutDownHook();
		boolean ok = runner.runEvoSuite(project.getBasedir().toString(),params);
		
		if(!ok){
			throw new MojoFailureException("Failed to correctly execute EvoSuite");
		}
	}
}