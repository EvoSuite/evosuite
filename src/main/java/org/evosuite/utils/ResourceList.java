/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package org.evosuite.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * list resources available from the classpath @ *
 * </p>
 * 
 * <p>
 * <code>Properties.CP</code> needs to be set </>
 * 
 * @author Gordon Fraser
 */
public class ResourceList {

	private static Logger logger = LoggerFactory.getLogger(ResourceList.class);

	/** Cache of file lists to avoid unnecessary IO */
	private static Map<String, Collection<String>> classPathCache = new LinkedHashMap<String, Collection<String>>();
	
	/**
	 * 
	 * @param className a fully qualified class name
	 * @return
	 */
	public static String getClassAsResource(String className) {
		
		String path = className.replace('.', '/') + ".class";
		String escapedString = java.util.regex.Pattern.quote(path); //Important in case there is $ in the classname
		
		Pattern pattern = Pattern.compile(escapedString);

		String[] cpElements = ClassPathHandler.getInstance().getClassPathElementsForTargetProject(); 
		Collection<String> resources = getResources(cpElements,pattern);

		if (!resources.isEmpty()) {
			return resources.iterator().next();
		}

		if (File.separatorChar != '/') {
			/*
			 * This can happen for example in Windows.
			 * Note: we still need to do scan above in case of Jar files (that would still use '/' inside)
			 */
			path = className.replace(".", "\\\\") + ".class";
			escapedString = java.util.regex.Pattern.quote(path);
			pattern = Pattern.compile(path);
			resources = getResources(cpElements,pattern);
			if (!resources.isEmpty()) {
				return resources.iterator().next();
			}
		}

		return null;
	}

	/**
	 * is the target class among the ones in the SUT classpath?
	 * 
	 * @param className a fully qualified class name
	 * @return
	 */
	public static boolean hasClass(String className) {
		return getClassAsResource(className) != null;
	}

	public static Collection<String> getAllResources() {
		return getResources(Pattern.compile(".*"));
	}
	
	/**
	 * <p>
	 * for all elements of <code>java.class.path</code> and
	 * <code>Properties.CP</code> get a Collection of resources Pattern
	 * </p>
	 * 
	 * <p>
	 * <code>pattern = Pattern.compile(".*");</code> gets all resources
	 * </p>
	 * 
	 * @param pattern
	 *            the pattern to match
	 * @return the resources in the order they are found
	 */
	public static Collection<String> getResources(final Pattern pattern) {
		final ArrayList<String> retval = new ArrayList<String>();

		String[] classPathElements = ClassPathHandler.getInstance().getTargetProjectClasspath().split(File.pathSeparator);
		for (final String element : classPathElements) {
			retval.addAll(getResources(element, pattern));
		}


		classPathElements = System.getProperty("java.class.path", ".").split(File.pathSeparator);
		for (final String element : classPathElements) {
			try{
				retval.addAll(getResources(element, pattern));
			} catch(Exception e){
				//FIXME seems something doggy going on here, with File.exists() returning false on "."
				logger.error("Failed to load resources in "+element+": "+e.getMessage(),e);
			}
		}
		return retval;
	}


	/**
	 * Get all resources in the given arrays of classpath elements (eg folders and jar files)
	 * according to the given pattern
	 * 
	 * @param classPathElements
	 * @param pattern
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static Collection<String> getResources(String[] classPathElements,
			final Pattern pattern) throws IllegalArgumentException {
		ArrayList<String> retval = new ArrayList<String>();
		for(String element : classPathElements){
			retval.addAll(getResources(element,pattern));
		}
		return retval;
	}	

	/**
	 * Get all resources in the given classpath element (eg folder or jar file)
	 * according to the given pattern
	 * 
	 * @param classPathElement
	 * @param pattern
	 * @return
	 */
	public static Collection<String> getResources(final String classPathElement,
			final Pattern pattern) throws IllegalArgumentException {

		if(classPathCache.containsKey(classPathElement)) {
			return classPathCache.get(classPathElement);
		}
		
		final ArrayList<String> retval = new ArrayList<String>();
		final File file = new File(classPathElement);


		if (!file.exists()) {
			throw new IllegalArgumentException("The class path resource "
					+ file.getAbsolutePath() + " does not exist");
		}


		if (file.isDirectory()) {
			try {
				retval.addAll(getResourcesFromDirectory(file, pattern,
						file.getCanonicalPath()));
			} catch (IOException e) {
				logger.error("Error in getting resources", e);
				throw new RuntimeException(e);
			}
		} else if (file.getName().endsWith(".jar")) {
			retval.addAll(getResourcesFromJarFile(file, pattern));
		} else {
			throw new IllegalArgumentException("The class path resource "
					+ file.getAbsolutePath() + " is not valid");
		}
		classPathCache.put(classPathElement, retval);
		return retval;
	}

	private static Collection<String> getResourcesFromJarFile(final File file,
			final Pattern pattern) {

		final ArrayList<String> retval = new ArrayList<String>();
		ZipFile zf;
		try {
			zf = new ZipFile(file);
		} catch (final Exception e) {
			throw new Error(e);
		}

		final Enumeration<?> e = zf.entries();
		while (e.hasMoreElements()) {
			final ZipEntry ze = (ZipEntry) e.nextElement();
			final String fileName = ze.getName();
			final boolean accept = pattern.matcher(fileName).matches();
			if (accept) {
				retval.add(fileName);
			}
		}
		try {
			zf.close();
		} catch (final IOException e1) {
			throw new Error(e1);
		}
		return retval;
	}

	private static Collection<String> getResourcesFromDirectory(final File directory,
			final Pattern pattern, final String classPathFolder) {

		final ArrayList<String> retval = new ArrayList<String>();
		if (!directory.exists()) {
			return retval;
		}
		if (!directory.isDirectory()) {
			return retval;
		}
		if (!directory.canRead()) {
			return retval;
		}
		
		final File[] fileList = directory.listFiles();
		for (final File file : fileList) {
			if (file.isDirectory()) {
				/*
				 * recursion till we get to a file that is not a folder.
				 * The pattern is matched only against files, not folders, and it is based
				 * on their full path names
				 */
				retval.addAll(getResourcesFromDirectory(file, pattern, classPathFolder));
			} else {
				try {

					final String relativeFilePath = file.getCanonicalPath().replace(classPathFolder
							+ File.separator,
							"");
					final boolean accept = pattern.matcher(relativeFilePath).matches();

					if (accept) {
						retval.add(relativeFilePath);
					}
				} catch (final IOException e) {
					throw new Error(e);
				}
			}
		}

		return retval;
	}

}
