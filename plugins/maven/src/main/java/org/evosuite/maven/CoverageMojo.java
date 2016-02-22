/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.maven;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.eclipse.aether.RepositorySystemSession;
import org.evosuite.maven.util.EvoSuiteRunner;
import org.evosuite.maven.util.ProjectUtils;

/**
 * Execute the manually written test suites (usually located under src/test/java)
 * and return the coverage of each class.
 * 
 * @author José Campos
 */
@Mojo( name = "coverage", requiresProject = true, requiresDependencyResolution = ResolutionScope.TEST, requiresDependencyCollection = ResolutionScope.TEST )
public class CoverageMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject project;

	@Parameter(defaultValue = "${plugin.artifacts}", required = true, readonly = true)
	private List<Artifact> artifacts;

	@Component
	private ProjectBuilder projectBuilder;

	@Parameter(defaultValue="${repositorySystemSession}", required = true, readonly = true)
	private RepositorySystemSession repoSession;

	/**
	 * Coverage criterion. Can define more than one criterion by using a ':' separated list
	 */
	// FIXME would be nice to have the value of Properties.CRITERION but seems to be not possible
	// FIXME OUTPUT:METHOD:METHODNOEXCEPTION relies on Observers, to have coverage of these criteria
	// JUnit test cases have to be converted to some format that EvoSuite can understand
	// and there is no point of using EXCEPTION if we don't know how many exceptions we could have
	@Parameter( property = "criterion", defaultValue = "LINE:BRANCH:CBRANCH:WEAKMUTATION:METHODTRACE" )
	private String criterion;

	/**
	 * Maximum seconds allowed
	 */
	@Parameter( property = "global_timeout", defaultValue = "120" )
	private int global_timeout;

	/**
	 * 
	 */
	@Parameter( property = "output_variables", defaultValue = "TARGET_CLASS,criterion,Coverage,Total_Goals,Covered_Goals"
															+ ",LineCoverage,LineCoverageBitString"
															+ ",BranchCoverage,BranchCoverageBitString"
															+ ",CBranchCoverage,CBranchCoverageBitString"
															+ ",WeakMutationScore,WeakMutationCoverageBitString"
															+ ",MethodTraceCoverage,MethodTraceCoverageBitString" )
	private String output_variables;

	/**
	 * A colon(:) separated list of JUnit suites to execute. Can be a prefix (i.e., package name),
	 * a directory, a jar file, or the full name of a JUnit suite.
	 */
	@Parameter( property = "junit" )
	private String junit;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		getLog().info("Going to measure the coverage of manually written test cases with EvoSuite");

		List<String> target = new ArrayList<String>();
		Set<String> cp = new LinkedHashSet<String>();

		// Get compile elements (i.e., classes under /target/classes)
		target.addAll(ProjectUtils.getCompileClasspathElements(this.project));

		// Get JUnit elements (i.e., classes under /target/test-classes) and compiled
		// elements (i.e., classes under /target/classes)
		cp.addAll(ProjectUtils.getTestClasspathElements(this.project));

		// Get project's dependencies
		cp.addAll(ProjectUtils.getDependencyPathElements(this.project));

		// Get runtime elements
		cp.addAll(ProjectUtils.getRuntimeClasspathElements(this.project));

		if (target.isEmpty() || cp.isEmpty()) {
			getLog().info("Nothing to measure coverage!");
			return ;
		}

		List<String> params = new ArrayList<>();
		params.add("-measureCoverage");
		params.add("-target");
		params.add(ProjectUtils.toClasspathString(target));
		params.add("-DCP=" + ProjectUtils.toClasspathString(cp));
		if (this.junit != null) {
			params.add("-Djunit="+this.junit);
		}

		params.add("-Dcriterion="+this.criterion);
		params.add("-Doutput_variables="+this.output_variables);
		params.add("-Dglobal_timeout="+this.global_timeout);
		// in theory should be safe to execute source-code
		params.add("-Dsandbox=false");
		params.add("-Dvirtual_fs=false");
		params.add("-Dvirtual_net=false");
		params.add("-Dreplace_calls=false");
		params.add("-Dreplace_system_in=false");

		getLog().info("Params:");
		for (String s : params) {
			getLog().info("  " + s);
		}

		EvoSuiteRunner runner = new EvoSuiteRunner(getLog(), this.artifacts, this.projectBuilder, this.repoSession);
		runner.registerShutDownHook();
		boolean ok = runner.runEvoSuite(this.project.getBasedir().toString(), params);

		if (!ok) {
			throw new MojoFailureException("Failed to correctly execute EvoSuite");
		}
    }
}
