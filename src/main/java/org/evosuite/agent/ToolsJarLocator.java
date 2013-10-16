package org.evosuite.agent;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.evosuite.Properties;
import org.evosuite.utils.ClassPathHacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToolsJarLocator {

	private static final Logger logger = LoggerFactory.getLogger(ToolsJarLocator.class);


	/**
	 * Full name of a class in tools.jar that is used inside EvoSuite
	 */
	private static final String EXAMPLE_CLASS =  "com.sun.tools.attach.VirtualMachine";

	/**
	 * Try to locate tools.jar and return a classloader for it
	 * 
	 * @throws RuntimeException  if it was not possible to locate/load tools.jar
	 */
	public static ClassLoader getLoaderForToolsJar() throws RuntimeException{

		try {
			Class.forName(EXAMPLE_CLASS,true,ClassLoader.getSystemClassLoader());
			logger.info("Tools.jar already on system classloader");
			return ClassLoader.getSystemClassLoader(); //if this code is reached, the tools.jar is available on system classpath
		} catch (ClassNotFoundException e) {
			//OK, it is missing, so lets try to locate it
		}

		try {
			Class.forName(EXAMPLE_CLASS);
			logger.info("Tools.jar already on current classloader");
			return ToolsJarLocator.class.getClassLoader(); //if this code is reached, the tools.jar is available on classpath
		} catch (ClassNotFoundException e) {
			//OK, it is missing, so lets try to locate it
		}
		
		if(Properties.TOOLS_JAR_LOCATION != null){
			//if defined, then use it, and throws exception if it is not valid
			return considerPathInProperties();
		}

		String javaHome = System.getProperty("java.home");
		String[] locations = new String[]{
				javaHome+"/../lib/tools.jar",
				javaHome+"/lib/tools.jar",
				javaHome+"/../Classes/classes.jar" /* this for example happens in Mac */
		}; 

		for(String location : locations){
			File file = new File(location);
			if(file.exists()){
				return validateAndGetLoader(location);
			}
		}

		throw new RuntimeException("Did not manage to automatically find tools.jar. Use -Dtools_jar_location=<path> property");
	}

	private static ClassLoader considerPathInProperties() {
		if(!Properties.TOOLS_JAR_LOCATION.endsWith(".jar")){
			throw new RuntimeException("Property tools_jar_location does not point to a jar file: "+Properties.TOOLS_JAR_LOCATION);
		}

		return validateAndGetLoader(Properties.TOOLS_JAR_LOCATION);		
	}

	private static ClassLoader validateAndGetLoader(String location) {

		ClassLoader loader = null;
		try {
			loader = URLClassLoader.newInstance(
					new URL[] { new File(location).toURI().toURL() },
					//ToolsJarLocator.class.getClassLoader()
					ClassLoader.getSystemClassLoader());
		} catch (MalformedURLException e) {
			throw new RuntimeException("Malformed URL: "+location,e);
		}

		try {
			Class.forName(EXAMPLE_CLASS, true, loader);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Missing "+EXAMPLE_CLASS+" in "+location);
		}

		try {
			/*
			 * it is important that tools.jar ends up in the classpath of the _system_ classloader,
			 * otherwise exceptions in EvoSuite classes using tools.jar
			 */
			logger.info("Using JDK libraries at: "+location); 
			ClassPathHacker.addFile(location);  //FIXME needs refactoring
		} catch (IOException e) {
			throw new RuntimeException("Failed to add "+location+" to system classpath");
		}

		return loader;
	}

}
