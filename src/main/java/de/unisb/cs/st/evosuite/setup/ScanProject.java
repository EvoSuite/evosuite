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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.Modifier;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.classcreation.ClassFactory;
import de.unisb.cs.st.evosuite.javaagent.TestSuitePreMain;
import de.unisb.cs.st.utils.Utils;

/**
 * @author Gordon Fraser
 * 
 */
public class ScanProject {

	private static List<Class<?>> findClasses(File directory, String packageName)
	        throws ClassNotFoundException {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		System.out.println("* Searching in: " + directory);
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			System.out.println("* Found class file: " + file);
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

	private static void loadClass(File file, String packageName)
	        throws ClassNotFoundException {
		if (file.isDirectory()) {
			assert !file.getName().contains(".");
			findClasses(file, packageName + "." + file.getName());
		} else if (file.getName().endsWith(".class")) {
			System.out.println("    "
			        + file.toString().replace(".class", "").replace("/", "."));
			if (Properties.STUBS) {
				Class<?> clazz = Class.forName(file.toString().replace(".class", "").replace("/",
				                                                                             "."));
				if (Modifier.isAbstract(clazz.getModifiers()) && !clazz.isInterface()) {
					ClassFactory cf = new ClassFactory();
					Class<?> stub = cf.createClass(clazz);
				}
			}
			try {
				Class.forName(file.toString().replace(".class", "").replace("/", "."));
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

	/**
	 * 
	 */
	private static void getClasses(String packageName) throws ClassNotFoundException,
	        IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		Collection<String> list = ResourceList.getResources(Pattern.compile(packageName
		        + "/.*\\.class$"));
		for (String name : list) {
			loadClass(new File(name), packageName);
		}
	}

	private static void getClasses(File directory) throws ClassNotFoundException {
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				getClasses(file);
			} else {
				if (file.getName().endsWith(".class")) {
					try {
						Class.forName(file.toString().replace(".class", "").replace("/",
						                                                            "."));
					} catch (IllegalAccessError e) {
						System.out.println("Cannot access class "
						        + file.getName().substring(0, file.getName().length() - 6));
					} catch (NoClassDefFoundError e) {
						System.out.println("Cannot find class "
						        + file.getName().substring(0, file.getName().length() - 6));
					} catch (ExceptionInInitializerError e) {
						System.out.println("Exception in initializer of "
						        + file.getName().substring(0, file.getName().length() - 6));
					}
				}
			}
		}
	}

	/**
	 * Entry point - generate task files
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		//System.out.println("Scanning project for test suite generation.");
		try {
			if (Properties.PROJECT_PREFIX != null) {
				System.out.println("* Analyzing project prefix: "
				        + Properties.PROJECT_PREFIX);
				getClasses(Properties.PROJECT_PREFIX);
			} else if (Properties.PROJECT_DIR != null) {
				System.out.println("* Analyzing project directory: "
				        + Properties.PROJECT_DIR);
				getClasses(new File(Properties.PROJECT_DIR));
			} else {
				System.out.println("* Please specify either project prefix or directory");
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		TestSuitePreMain.distanceTransformer.saveData();
		Utils.addURL(ClassFactory.getStubDir() + "/classes/");
		TestTaskGenerator.hierarchy.calculateSubclasses();
		System.out.println("* Creating test files for " + Properties.PROJECT_PREFIX);
		TestTaskGenerator.suggestTasks(Properties.PROJECT_PREFIX);
	}
}
