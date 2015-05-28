package org.evosuite.maven;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
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
import org.evosuite.Properties;
import org.evosuite.continuous.persistency.StorageManager;

/**
 * When run, EvoSuite generate tests in a specific folder.
 * New runs of EvoSuite can exploit the tests in such folder, and/or modify them.
 * 
 * <p>
 * So, with "export" we can copy all generated tests to a specific folder, which
 * by default points to where Maven searches for tests.
 * If another folder is rather used (or if we want to run with Maven the tests in the default EvoSuite folder),
 * then Maven plugins like build-helper-maven-plugin are needed 
 * 
 * @author arcuri
 *
 */
@Mojo( name = "export")
public class ExportMojo extends AbstractMojo{


	@Parameter( property = "targetFolder", defaultValue = "src/test/java" )
	private String targetFolder;
	
	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject project;
	
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		getLog().info("Exporting tests");

		File basedir = project.getBasedir();

		String evoFolderName = Properties.CTG_DIR+File.separator+ Properties.CTG_BESTS_DIR;
		File evoFolder = new File(basedir.getAbsolutePath()+File.separator+evoFolderName);
		
		File[] children = evoFolder.listFiles();
		boolean isEmpty = children==null || children.length==0;
		
		if(isEmpty){
			getLog().info("Nothing to export");
			return;
		}
		
		File target = new File(basedir.getAbsolutePath()+File.separator+targetFolder);
		
		try {
			FileUtils.copyDirectory(evoFolder, target);
		} catch (IOException e) {
			String msg = "Error while exporting tests: "+e.getMessage();
			getLog().error(msg);
			throw new MojoFailureException(msg); 
		}
		
		getLog().info("Exported tests from "+evoFolder+" to "+target);
	}

}
