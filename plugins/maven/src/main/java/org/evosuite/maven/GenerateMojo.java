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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.eclipse.aether.RepositorySystemSession;
import org.evosuite.Properties;
import org.evosuite.maven.util.EvoSuiteRunner;
import org.evosuite.maven.util.FileUtils;
import org.evosuite.maven.util.HistoryChanges;
import org.evosuite.utils.SpawnProcessKeepAliveChecker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Generate JUnit tests
 */
@Mojo( name = "generate" , requiresDependencyResolution = ResolutionScope.TEST, requiresDependencyCollection = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.COMPILE)
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
	 * Absolute path to a file having the list of CUTs specified. This is needed for operating
	 * systems like Windows that have constraints on the size of input parameters and so could
	 * not use "cuts" parameter instead if too many CUTs are specified
	 */
	@Parameter( property = "cutsFile")
	private String cutsFile;

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

	@Parameter(property = "spawnManagerPort", defaultValue = "")
	private Integer spawnManagerPort;

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

			//the targets we want to generate tests for, ie the CUTs
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

				if(! file.getAbsolutePath().startsWith(project.getBasedir().getAbsolutePath()) ){
					/*
						This can happen in multi-module projects when module A has dependency on
						module B. Then, both A and B source folders will end up on compile classpath,
						although we are interested only in A
					 */
					continue;
				}

				if(target == null){
					target = element;
				} else {
					target = target + File.pathSeparator + element;
				}
			}

			//build the classpath
			Set<String> alreadyAdded = new HashSet<>();
			for(String element : project.getTestClasspathElements()){
				if(element.toLowerCase().contains("powermock")){
					//PowerMock just leads to a lot of troubles, as it includes tools.jar code
					getLog().warn("Skipping PowerMock dependency at: "+element);
					continue;
				}
				if(element.toLowerCase().contains("jmockit")){
					//JMockit has same issue
					getLog().warn("Skipping JMockit dependency at: "+element);
					continue;
				}
				getLog().debug("TEST ELEMENT: "+element);
				cp = addPathIfExists(cp, element, alreadyAdded);
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

		runEvoSuiteOnSeparatedProcess(target, cp, basedir.getAbsolutePath());

	}

	private String addPathIfExists(String cp, String element, Set<String> alreadyExist) {
		File file = new File(element);
		if(!file.exists()){
            /*
             * don't add to CP an element that does not exist
             */
			return cp;
        }

		if(alreadyExist.contains(element)){
			return cp;
		}

		alreadyExist.add(element);

		if(cp == null){
            cp = element;
        } else {
            cp = cp + File.pathSeparator + element;
        }
		return cp;
	}

	private void runEvoSuiteOnSeparatedProcess(String target, String cp, String dir) throws MojoFailureException {
			
		List<String> params = new ArrayList<>();
		params.add("-continuous");
		params.add("execute");
		params.add("-target");
		params.add(target);
		params.add("-Dcriterion=" + criterion);
		params.add("-Dctg_schedule=" + schedule);
		if (schedule.toUpperCase().equals(Properties.AvailableSchedule.HISTORY.toString())) {
			try {
				List<File> files = FileUtils.scan(this.project.getCompileSourceRoots(), this.includes, this.excludes);
				HistoryChanges.keepTrack(dir, files);
			} catch (Exception e) {
				throw new MojoFailureException("", e);
			}

			params.add("-Dctg_history_file=" + dir + File.separator + Properties.CTG_DIR + File.separator + "history_file");
		}
		params.add("-Dctg_memory="+memoryInMB);
		params.add("-Dctg_cores="+numberOfCores);

		int port;
		if(spawnManagerPort != null) {
			SpawnProcessKeepAliveChecker.getInstance().registerToRemoteServerAndDieIfFails(spawnManagerPort);
			port = spawnManagerPort;
		} else {
			port = SpawnProcessKeepAliveChecker.getInstance().startServer();
		}
		params.add("-Dspawn_process_manager_port=" + port);


		if (timeInMinutesPerProject != 0) {
			params.add("-Dctg_time="+timeInMinutesPerProject);
			params.add("-Dctg_min_time_per_job="+timeInMinutesPerClass);
		} else {
			params.add("-Dctg_time_per_class="+timeInMinutesPerClass); // there is no time limit, so test all classes X minutes
		}
		if(cuts!=null){
			params.add("-Dctg_selected_cuts="+cuts);
		}
		if(cutsFile!=null){
			params.add("-Dctg_selected_cuts_file_location="+cutsFile);
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

		String path = writeClasspathToFile(cp);
		params.add("-DCP_file_path="+path);
		//params.add("-DCP=" + cp); //this did not work properly on Windows
		
		EvoSuiteRunner runner = new EvoSuiteRunner(getLog(),artifacts,projectBuilder,repoSession);
		runner.registerShutDownHook();
		boolean ok = runner.runEvoSuite(dir,params);

		if(spawnManagerPort != null) {
			SpawnProcessKeepAliveChecker.getInstance().unRegister();
		} else {
			SpawnProcessKeepAliveChecker.getInstance().stopServer();
		}

		if(!ok){
			throw new MojoFailureException("Failed to correctly execute EvoSuite");
		}
	}

	private String writeClasspathToFile(String classpath) {

		try {
			File file = File.createTempFile("EvoSuite_classpathFile",".txt");
			file.deleteOnExit();

			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			String line = classpath;
			out.write(line);
			out.newLine();
			out.close();

			return file.getAbsolutePath();

		} catch (Exception e) {
			throw new IllegalStateException("Failed to create tmp file for classpath specification: "+e.getMessage());
		}
	}
}
