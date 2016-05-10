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
package org.evosuite.runtime.agent;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToolsJarLocator {

	private static final Logger logger = LoggerFactory.getLogger(ToolsJarLocator.class);


	/**
	 * Full name of a class in tools.jar that is used inside EvoSuite
	 */
	private static final String EXAMPLE_CLASS =  "com.sun.tools.attach.VirtualMachine";

    private String locationNotOnClasspath;

    private String manuallySpecifiedToolLocation;

    public ToolsJarLocator(String manuallySpecifiedToolLocation) {
        this.manuallySpecifiedToolLocation = manuallySpecifiedToolLocation;
    }

    /**
	 * Try to locate tools.jar and return a classloader for it
	 * 
	 * @throws RuntimeException  if it was not possible to locate/load tools.jar
	 */
	public ClassLoader getLoaderForToolsJar() throws RuntimeException{

		/*
			This was a problem, as PowerMock and JMockit ship with their own version of tools.jar taken from OpenJDK
		 */
//		try {
//			Class.forName(EXAMPLE_CLASS,true,ClassLoader.getSystemClassLoader());
//			logger.info("Tools.jar already on system classloader");
//			return ClassLoader.getSystemClassLoader(); //if this code is reached, the tools.jar is available on system classpath
//		} catch (ClassNotFoundException e) {
//			//OK, it is missing, so lets try to locate it
//		}
//		try {
//			Class.forName(EXAMPLE_CLASS);
//			logger.info("Tools.jar already on current classloader");
//			return ToolsJarLocator.class.getClassLoader(); //if this code is reached, the tools.jar is available on classpath
//		} catch (ClassNotFoundException e) {
//			//OK, it is missing, so lets try to locate it
//		}
		
		if(manuallySpecifiedToolLocation != null){
			//if defined, then use it, and throws exception if it is not valid
			return considerPathInProperties();
		}

		String javaHome = System.getProperty("java.home");
		List<String> locations = new ArrayList<>(Arrays.asList(
				javaHome + "/../lib/tools.jar", 
				javaHome + "/lib/tools.jar", 
				javaHome + "/../Classes/classes.jar" /* this for example happens in Mac */
		));
		
		// Fix for when EvoSuite is wrongly run with a JRE (eg on Windows if JAVA_HOME is not properly set in PATH)
		String javaHomeEnv = System.getenv("JAVA_HOME");
		if(javaHomeEnv!=null && !javaHomeEnv.equals(javaHome)){
			locations.addAll(Arrays.asList(
					javaHomeEnv+"/../lib/tools.jar",
					javaHomeEnv+"/lib/tools.jar",
					javaHomeEnv+"/../Classes/classes.jar"
			));
		}

		for(String location : locations){
			File file = new File(location);
			if(file.exists()){
				return validateAndGetLoader(location);
			}
		}

		throw new RuntimeException("Did not manage to automatically find tools.jar. Use -Dtools_jar_location=<path> property");
	}

	private  ClassLoader considerPathInProperties() {
		if(! manuallySpecifiedToolLocation.endsWith(".jar")){
			throw new RuntimeException("Property tools_jar_location does not point to a jar file: "+manuallySpecifiedToolLocation);
		}

		return validateAndGetLoader(manuallySpecifiedToolLocation);
	}

	private  ClassLoader validateAndGetLoader(String location) {

		ClassLoader loader = null;
		try {
			loader = URLClassLoader.newInstance(
					new URL[] { new File(location).toURI().toURL() },
					//ClassLoader.getSystemClassLoader()
					null
			);
		} catch (MalformedURLException e) {
			throw new RuntimeException("Malformed URL: "+location,e);
		}

		try {
			Class.forName(EXAMPLE_CLASS, true, loader);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Missing "+EXAMPLE_CLASS+" in "+location);
		}

        locationNotOnClasspath = location;

		return loader;
	}

    public String getLocationNotOnClasspath() {
        return locationNotOnClasspath;
    }
}
