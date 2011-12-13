/**
 * 
 */
package de.unisb.cs.st.evosuite.utils;

/**
 * @author fraser
 * 
 */
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import de.unisb.cs.st.evosuite.Properties;

/**
 * list resources available from the classpath @ *
 */
public class ResourceList {

	/**
	 * for all elements of java.class.path get a Collection of resources Pattern
	 * pattern = Pattern.compile(".*"); gets all resources
	 * 
	 * @param pattern
	 *            the pattern to match
	 * @return the resources in the order they are found
	 */
	public static Collection<String> getResources(final Pattern pattern) {
		final ArrayList<String> retval = new ArrayList<String>();
		//final String classPath = System.getProperty("java.class.path", ".");
		final String[] classPathElements = Properties.CP.split(":");
		for (final String element : classPathElements) {
			retval.addAll(getResources(element, pattern));
		}
		return retval;
	}

	public static Collection<String> getAllResources(final Pattern pattern) {
		final ArrayList<String> retval = new ArrayList<String>();
		String[] classPathElements = System.getProperty("java.class.path", ".").split(":");
		for (final String element : classPathElements) {
			retval.addAll(getResources(element, pattern));
		}
		return retval;
	}

	/**
	 * for all elements of java.class.path get a Collection of resources Pattern
	 * pattern = Pattern.compile(".*"); gets all resources
	 * 
	 * @param pattern
	 *            the pattern to match
	 * @return the resources in the order they are found
	 */
	public static Collection<String> getBootResources(final Pattern pattern) {
		Collection<String> result = getResources(pattern);
		String classPath = System.getProperty("sun.boot.class.path", ".");
		String[] classPathElements = classPath.split(":");
		for (final String element : classPathElements) {
			result.addAll(getResources(element, pattern));
		}
		return result;
	}

	private static Collection<String> getResources(final String element,
	        final Pattern pattern) {
		final ArrayList<String> retval = new ArrayList<String>();
		final File file = new File(element);
		if (file.isDirectory()) {
			try {
				retval.addAll(getResourcesFromDirectory(file, pattern,
				                                        file.getCanonicalPath()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (!file.exists()) {
			//do nothing
			//System.out.println(file.getAbsolutePath()
			//        + " is on the class path, but doesn't exist");

		} else if (file.getName().endsWith(".jar")) {
			retval.addAll(getResourcesFromJarFile(file, pattern));
		}
		return retval;
	}

	private static Collection<String> getResourcesFromJarFile(final File file,
	        final Pattern pattern) {
		final ArrayList<String> retval = new ArrayList<String>();
		ZipFile zf;
		try {
			zf = new ZipFile(file);
		} catch (final ZipException e) {
			throw new Error(e);
		} catch (final IOException e) {
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
	        final Pattern pattern, final String dirName) {
		final ArrayList<String> retval = new ArrayList<String>();
		final File[] fileList = directory.listFiles();
		for (final File file : fileList) {
			if (file.isDirectory()) {
				retval.addAll(getResourcesFromDirectory(file, pattern, dirName));
			} else {
				try {
					final String fileName = file.getCanonicalPath().replace(dirName + "/",
					                                                        "");
					final boolean accept = pattern.matcher(fileName).matches();
					if (accept) {
						retval.add(fileName);
					}
				} catch (final IOException e) {
					throw new Error(e);
				}
			}
		}
		return retval;
	}

}
