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

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.evosuite.continuous.ContinuousTestGeneration;

/**
 * <p>
 * When run, EvoSuite generate tests in a specific folder.
 * New runs of EvoSuite can exploit the tests in such folder, and/or modify them.
 * </p>
 *
 * <p>
 * So, with "export" we can copy all generated tests to a specific folder, which
 * by default points to where Maven searches for tests.
 * If another folder is rather used (or if we want to run with Maven the tests in the default EvoSuite folder),
 * then Maven plugins like build-helper-maven-plugin are needed 
 * </p>
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

		try {
			boolean exported = ContinuousTestGeneration.exportToFolder(basedir.getAbsolutePath(),targetFolder);
			if(!exported){
				getLog().info("Nothing to export");
				return;
			}
		} catch (IOException e) {
			String msg = "Error while exporting tests: "+e.getMessage();
			getLog().error(msg);
			throw new MojoFailureException(msg);
		}

		File target = ContinuousTestGeneration.resolveExportFolder(basedir.getAbsolutePath(), targetFolder);
		getLog().info("Exported tests to "+target);
	}

}
