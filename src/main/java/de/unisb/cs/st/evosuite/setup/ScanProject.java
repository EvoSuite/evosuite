/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.setup;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.jdt.core.dom.Modifier;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.classcreation.ClassFactory;

/**
 * @author Gordon Fraser
 * 
 */
public class ScanProject {

	static String prefix;

	private static List<Class<?>> findClasses(File directory, String packageName)
	        throws ClassNotFoundException {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			System.out.println("* File: " + file);
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				if (Properties.STUBS) {
					Class<?> clazz = Class.forName(packageName + '.'
					        + file.getName().substring(0, file.getName().length() - 6));
					if (Modifier.isAbstract(clazz.getModifiers()) && !clazz.isInterface()) {
						ClassFactory cf = new ClassFactory();
						Class<?> stub = cf.createClass(clazz);
						if (stub != null)
							classes.add(stub);
					}
				}
				try {
					classes.add(Class.forName(packageName + '.'
					        + file.getName().substring(0, file.getName().length() - 6)));
				} catch (IllegalAccessError e) {
					System.out.println("Cannot access class " + packageName + '.'
					        + file.getName().substring(0, file.getName().length() - 6));
				} catch (NoClassDefFoundError e) {
					System.out.println("Cannot find class " + packageName + '.'
					        + file.getName().substring(0, file.getName().length() - 6));
				} catch (ExceptionInInitializerError e) {
					System.out.println("Exception in initializer of " + packageName + '.'
					        + file.getName().substring(0, file.getName().length() - 6));
				}
			}
		}
		return classes;
	}

	private static Class<?>[] getClasses(String packageName)
	        throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}

		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes.toArray(new Class[classes.size()]);
	}

	/**
	 * Entry point - generate task files
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		//System.out.println("Scanning project for test suite generation.");
		prefix = Properties.PROJECT_PREFIX;
		System.out.println("* Project prefix: " + prefix);
		try {
			getClasses(prefix);
			//TestDetector.scanForTests(prefix);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
