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
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.evosuite.Properties;
import org.evosuite.classpath.ResourceList;
import org.evosuite.runtime.InitializingListener;

/**
 * Workaround mojo to overcome bug in Maven.
 * Needed when EvoSuite tests are run together with manual ones
 *
 *
 */
@Mojo( name = "prepare")
public class PrepareMojo extends AbstractMojo{

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject project;
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		getLog().info("Preparing EvoSuite tests for execution");
		
		
		String dirName =	 project.getBuild().getTestOutputDirectory();
		if(dirName == null || dirName.trim().isEmpty()){
			getLog().error("Cannot determine folder for compiled tests");
			return;
		} 
		
		File dir = new File(dirName);
		getLog().info("Analyzing test folder: "+dir.getAbsolutePath());
		//NOTE: this test can fail, likely due to permissions...
		//if(!dir.isDirectory()){
		//	getLog().error("Target folder for compiled tests is not a folder: "+dir.getAbsolutePath());
		//	return;
		//}
		
		if(!dir.exists()){
			getLog().warn("Target folder for compiled tests does not exist: "+dir.getAbsolutePath());
			return;
		}
		
		List<String> list = new ArrayList<>();
		
		Iterator<File> iterator =  FileUtils.iterateFiles(dir, new String[]{"class"}, true);
		while(iterator.hasNext()){
			File file = iterator.next();
			/*
			 * not a robust check, as that parameter could had been changed.
			 * but anyway, this class is a tmp hack till Maven gets fixed
			 */
			if(! file.getName().endsWith(Properties.SCAFFOLDING_SUFFIX+".class")){
				continue;
			}
			
			String resource = file.getAbsolutePath().substring(dir.getAbsolutePath().length() + 1 , file.getAbsolutePath().length());
			String className = ResourceList.getClassNameFromResourcePath(resource);
			list.add(className);
		}
		
		getLog().info("Found "+list.size()+" EvoSuite scaffolding files");
		
		File scaffolding = new File(project.getBasedir() + File.separator + InitializingListener.SCAFFOLDING_LIST_FILE_STRING);
		try {
			PrintWriter out = new PrintWriter(scaffolding);
			for(String s : list){
				out.println(s);
				getLog().debug("Class: "+s);
			}
			out.close();
		} catch (FileNotFoundException e) {
			getLog().error("Error while generating "+scaffolding.getAbsolutePath()+" : "+e.getMessage());
		}
		
	}
}
