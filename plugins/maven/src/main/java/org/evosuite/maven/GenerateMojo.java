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
import org.evosuite.maven.util.EvoSuiteRunner;


@Mojo( name = "generate" , requiresDependencyResolution = ResolutionScope.RUNTIME, requiresDependencyCollection = ResolutionScope.RUNTIME)
public class GenerateMojo extends AbstractMojo{

	@Parameter( property = "memoryInMB", defaultValue = "800" )
	private int memoryInMB;

	@Parameter( property = "cores", defaultValue = "1" )
	private int numberOfCores;

	@Parameter( property = "cuts" )
	private String cuts;

	@Parameter( property = "timeInMinutesPerClass", defaultValue = "2" )
	private int timeInMinutesPerClass;

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject project;

	@Parameter(defaultValue = "${plugin.artifacts}", required = true, readonly = true)
	private List<Artifact> artifacts;

	@Component
	private ProjectBuilder projectBuilder;

	@Parameter(defaultValue="${repositorySystemSession}", required = true, readonly = true)
	private RepositorySystemSession repoSession;

	
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
		
		runEvoSuiteOnSeparatedProcess(target, cp, basedir.getAbsolutePath()); 
	}


	private void runEvoSuiteOnSeparatedProcess(String target, String cp, String dir) throws MojoFailureException {
			
		List<String> params = new ArrayList<>();
		params.add("-continuous");
		params.add("execute");
		params.add("-target");
		params.add(target);
		params.add("-Dctg_memory="+memoryInMB);
		params.add("-Dctg_cores="+numberOfCores);
		params.add("-Dctg_time_per_class="+timeInMinutesPerClass);
		if(cuts!=null){
			params.add("-Dctg_selected_cuts="+cuts);
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