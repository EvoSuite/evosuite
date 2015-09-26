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
package org.evosuite.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
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
import org.evosuite.Properties;
import org.evosuite.maven.util.EvoSuiteRunner;
import org.evosuite.maven.util.FileUtils;
import org.evosuite.maven.util.HistoryChanges;

@Mojo( name = "generate" , requiresDependencyResolution = ResolutionScope.RUNTIME, requiresDependencyCollection = ResolutionScope.RUNTIME)
public class GenerateMojo extends AbstractMojo {

	/**
	 * Total Memory (in MB) that CTG will use
	 */
	@Parameter( property = "memoryInMB", defaultValue = "800" )
	private int memoryInMB;

	/**
	 * Number of cores CTG will use
	 */
	@Parameter( property = "cores", defaultValue = "1" )
	private int numberOfCores;

	/**
	 * Comma ',' separated list of CUTs to use in CTG. If none specified, then test all classes
	 */
	@Parameter( property = "cuts" )
	private String cuts;

	/**
	 * How many minutes to allocate for each class
	 */
	@Parameter( property = "timeInMinutesPerClass", defaultValue = "2" )
	private int timeInMinutesPerClass;

	/**
	 * How many minutes to allocate for each project/module. If this parameter is not set, then the total time will be timeInMinutesPerClass x number_of_classes
	 */
	@Parameter( property = "timeInMinutesPerProject", defaultValue = "0" )
	private int timeInMinutesPerProject;

	/**
	 * Coverage criterion. Can define more than one criterion by using a ':' separated list
	 */
	// FIXME would be nice to have the value of Properties.CRITERION but seems to be not possible
	@Parameter( property = "criterion", defaultValue = "LINE:BRANCH:EXCEPTION:WEAKMUTATION:OUTPUT:METHOD:METHODNOEXCEPTION:CBRANCH" )
	private String criterion;

	@Parameter( property = "extraArgs" , defaultValue = "")
	private String extraArgs;

	/**
	 * Schedule used to run CTG (SIMPLE, BUDGET, SEEDING, BUDGET_AND_SEEDING, HISTORY)
	 */
	@Parameter( property = "schedule", defaultValue = "BUDGET" )
	private String schedule;

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject project;

	@Parameter(defaultValue = "${plugin.artifacts}", required = true, readonly = true)
	private List<Artifact> artifacts;

	@Component
	private ProjectBuilder projectBuilder;

	@Parameter(defaultValue="${repositorySystemSession}", required = true, readonly = true)
	private RepositorySystemSession repoSession;

	/**
	 * Defines files in the source directories to include (all .java files by default).
	 */
	private String[] includes = {"**/*.java"};

	/**
	 * Defines which of the included files in the source directories to exclude (non by default).
	 */
	private String[] excludes;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException{

		getLog().info("Going to generate tests with EvoSuite");
		getLog().info("Total memory: "+memoryInMB+"mb");
		getLog().info("Time per class: "+timeInMinutesPerClass+" minutes");
		getLog().info("Number of used cores: "+numberOfCores);
		
		if(cuts!=null){
			getLog().info("Specified classes under test: "+cuts);
		}
		
		String target = null;
		String cp = null;
		
		try {
			for(String element : project.getCompileClasspathElements()){
				if(element.endsWith(".jar")){  // we only target what has been compiled to a folder
					continue;
				}
				
				File file = new File(element);
				if(!file.exists()){
					/*
					 * don't add to target an element that does not exist
					 */
					continue;
				}
				
				if(target == null){
					target = element;
				} else {
					target = target + File.pathSeparator + element;
				}
			}
			for(String element : project.getRuntimeClasspathElements()){
				
				File file = new File(element);
				if(!file.exists()){
					/*
					 * don't add to CP an element that does not exist
					 */
					continue;
				}
				
				if(cp == null){
					cp = element;
				} else {
					cp = cp + File.pathSeparator + element;
				}
			}						
		} catch (DependencyResolutionRequiredException e) {
			getLog().error("Error: "+e.getMessage(),e);
			return;
		}

		File basedir = project.getBasedir();
		
		getLog().info("Target: "+target);
		getLog().debug("Classpath: "+cp);
		getLog().info("Basedir: "+basedir.getAbsolutePath());
		if(target==null || cp==null || basedir==null){
			getLog().info("Nothing to test");
			return;
		}

		try {
			List<File> files = FileUtils.scan(this.project.getCompileSourceRoots(), this.includes, this.excludes);
			HistoryChanges.keepTrack(basedir.getAbsolutePath(), files);
		} catch (Exception e) {
			throw new MojoExecutionException("", e);
		}

		runEvoSuiteOnSeparatedProcess(target, cp, basedir.getAbsolutePath()); 
	}

	private void runEvoSuiteOnSeparatedProcess(String target, String cp, String dir) throws MojoFailureException {
			
		List<String> params = new ArrayList<>();
		params.add("-continuous");
		params.add("execute");
		params.add("-target");
		params.add(target);
		params.add("-Dcriterion="+criterion);
		params.add("-Dctg_schedule="+schedule);
		if (schedule.toUpperCase().equals(Properties.AvailableSchedule.HISTORY.toString())) {
			params.add("-Dctg_history_file=" + dir + File.separator + Properties.CTG_DIR + File.separator + "history_file");
		}
		params.add("-Dctg_memory="+memoryInMB);
		params.add("-Dctg_cores="+numberOfCores);
		if (timeInMinutesPerProject != 0) {
			params.add("-Dctg_time="+timeInMinutesPerProject);
			params.add("-Dctg_min_time_per_job="+timeInMinutesPerClass);
		} else {
			params.add("-Dctg_time_per_class="+timeInMinutesPerClass); // there is no time limit, so test all classes X minutes
		}
		if(cuts!=null){
			params.add("-Dctg_selected_cuts="+cuts);
		}
		if(extraArgs!=null && !extraArgs.isEmpty()){

			String args = "";

			//note this does not for properly for parameters with strings using spaces
			String[] tokens = extraArgs.split(" ");
			for(String token : tokens){
				token = token.trim();
				if(token.isEmpty()){
					continue;
				}
				if(!token.startsWith("-D")){
					getLog().error("Invalid extra argument \""+token+"\". It should start with a -D");
				} else {
					args += " " +token;
				}
			}

			params.add("-Dctg_extra_args=\""+args+"\"");
		}

		params.add("-DCP="+cp);
		
		EvoSuiteRunner runner = new EvoSuiteRunner(getLog(),artifacts,projectBuilder,repoSession);
		runner.registerShutDownHook();
		boolean ok = runner.runEvoSuite(dir,params);
		
		if(!ok){
			throw new MojoFailureException("Failed to correctly execute EvoSuite");
		}
	}		
}