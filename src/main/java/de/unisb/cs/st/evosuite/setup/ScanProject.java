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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.Modifier;
import org.objectweb.asm.ClassReader;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.classcreation.ClassFactory;
import de.unisb.cs.st.evosuite.javaagent.TestSuitePreMain;
import de.unisb.cs.st.evosuite.utils.Utils;

/**
 * @author Gordon Fraser
 * 
 */
public class ScanProject {

	protected static Logger logger = Logger.getLogger(ScanProject.class);

	public final static class ZipClassLoader extends ClassLoader {
		private final ZipFile file;

		private final Map<String, Class<?>> classMap = new HashMap<String, Class<?>>();

		public ZipClassLoader(String filename) throws IOException {
			this.file = new ZipFile(filename);
		}

		public ZipClassLoader(File file) throws IOException {
			this.file = new ZipFile(file);
		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {

			name = name.replace("/", ".");
			if (classMap.containsKey(name))
				return classMap.get(name);

			ZipEntry entry = this.file.getEntry(name.replace('.', '/') + ".class");
			if (entry == null) {
				throw new ClassNotFoundException(name);
			}

			try {
				byte[] array = new byte[(int) entry.getSize()];
				InputStream in = this.file.getInputStream(entry);
				ByteArrayOutputStream out = new ByteArrayOutputStream(array.length);
				int length = in.read(array);
				while (length > 0) {
					out.write(array, 0, length);
					length = in.read(array);
				}
				ClassReader reader = new ClassReader(array);
				Class<?> clazz = defineClass(reader.getClassName().replace("/", "."),
				                             out.toByteArray(), 0, out.size());
				classMap.put(name, clazz);
				return clazz;
			} catch (IOException exception) {
				throw new ClassNotFoundException(name, exception);
			}
		}
	}

	public final static class FileClassLoader extends ClassLoader {

		private final Map<String, Class<?>> classMap = new HashMap<String, Class<?>>();

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {

			name = name.replace("/", ".");
			if (classMap.containsKey(name))
				return classMap.get(name);

			File file = new File(name);
			try {
				byte[] array = new byte[(int) file.length()];
				InputStream in = new FileInputStream(file);
				ByteArrayOutputStream out = new ByteArrayOutputStream(array.length);
				int length = in.read(array);
				while (length > 0) {
					out.write(array, 0, length);
					length = in.read(array);
				}
				ClassReader reader = new ClassReader(array);
				Class<?> clazz = defineClass(reader.getClassName().replace("/", "."),
				                             out.toByteArray(), 0, out.size());
				classMap.put(name, clazz);
				return clazz;
			} catch (IOException exception) {
				throw new ClassNotFoundException(name, exception);
			}
		}
	}

	private static Set<Class<?>> findClasses(File directory, String packageName)
	        throws ClassNotFoundException {
		Set<Class<?>> classes = new HashSet<Class<?>>();
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

	private static Set<Class<?>> loadClass(File file, String packageName)
	        throws ClassNotFoundException {
		Set<Class<?>> set = new HashSet<Class<?>>();
		if (file.isDirectory()) {
			assert !file.getName().contains(".");
			set.addAll(findClasses(file, packageName + "." + file.getName()));
		} else if (file.getName().endsWith(".class")) {
			System.out.println("    "
			        + file.toString().replace(".class", "").replace("/", "."));
			if (Properties.STUBS) {
				Class<?> clazz = Class.forName(file.toString().replace(".class", "").replace("/",
				                                                                             "."));
				if (Modifier.isAbstract(clazz.getModifiers()) && !clazz.isInterface()) {
					ClassFactory cf = new ClassFactory();
					cf.createClass(clazz);
				}
			}
			try {
				set.add(Class.forName(file.toString().replace(".class", "").replace("/",
				                                                                    ".")));
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
		return set;
	}

	/**
	 * Analyze all classes of a given package prefix that can be found in the
	 * classpath
	 */
	private static Set<Class<?>> getClasses(String packageName)
	        throws ClassNotFoundException, IOException {
		Set<Class<?>> set = new HashSet<Class<?>>();

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		Collection<String> list = ResourceList.getResources(Pattern.compile(packageName
		        + "/.*\\.class$"));
		for (String name : list) {
			set.addAll(loadClass(new File(name), packageName));
		}

		return set;
	}

	/**
	 * Analyze all classes that can be found in a given directory
	 * 
	 * @param directory
	 * @throws ClassNotFoundException
	 */
	private static Set<Class<?>> getClasses(File directory) throws ClassNotFoundException {
		if (directory.getName().endsWith(".jar")) {
			return getClassesJar(directory);
		} else if (directory.getName().endsWith(".class")) {
			Set<Class<?>> set = new HashSet<Class<?>>();
			try {
				System.out.println("* Loading class " + directory.getName());
				set.add(new FileClassLoader().findClass(directory.getPath()));
			} catch (IllegalAccessError e) {
				System.out.println("Cannot access class "
				        + directory.getName().substring(0,
				                                        directory.getName().length() - 6));
			} catch (NoClassDefFoundError e) {
				System.out.println("Cannot find class "
				        + directory.getName().substring(0,
				                                        directory.getName().length() - 6)
				        + " in path " + directory);
			} catch (ExceptionInInitializerError e) {
				System.out.println("Exception in initializer of "
				        + directory.getName().substring(0,
				                                        directory.getName().length() - 6));
			}
			return set;
		} else if (directory.isDirectory()) {
			Set<Class<?>> set = new HashSet<Class<?>>();
			for (File file : directory.listFiles()) {
				set.addAll(getClasses(file));
			}
			return set;
		} else {
			return new HashSet<Class<?>>();
			//			throw new RuntimeException("Don't know how to handle: " + directory);
		}
	}

	private static Set<Class<?>> getClassesJar(File file) throws ClassNotFoundException {

		Set<Class<?>> set = new HashSet<Class<?>>();

		ZipFile zf;
		ZipClassLoader zcl;
		try {
			zcl = new ZipClassLoader(file);
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
			if (!fileName.endsWith(".class"))
				continue;
			if (fileName.contains("$"))
				continue;

			try {
				System.out.println("* Loading class " + fileName + " from jar file");
				set.add(zcl.findClass(fileName.replace(".class", "")));
				//				Class.forName(fileName.replace(".class", "").replace("/", "."));
			} catch (IllegalAccessError ex) {
				System.out.println("Cannot access class "
				        + file.getName().substring(0, file.getName().length() - 6));
			} catch (NoClassDefFoundError ex) {
				System.out.println("Cannot find class "
				        + file.getName().substring(0, file.getName().length() - 6));
			} catch (ExceptionInInitializerError ex) {
				System.out.println("Exception in initializer of "
				        + file.getName().substring(0, file.getName().length() - 6));
			}
		}
		try {
			zf.close();
		} catch (final IOException e1) {
			throw new Error(e1);
		}
		return set;

	}

	/**
	 * Entry point - generate task files
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		//System.out.println("Scanning project for test suite generation.");
		Set<Class<?>> classes = new HashSet<Class<?>>();
		try {
			if (Properties.PROJECT_PREFIX != null) {
				System.out.println("* Analyzing project prefix: "
				        + Properties.PROJECT_PREFIX);
				classes.addAll(getClasses(Properties.PROJECT_PREFIX));
			} else if (args.length > 0) {
				for (String arg : args) {
					System.out.println("* Analyzing project directory: " + arg);
					classes.addAll(getClasses(new File(arg)));
				}
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
		System.out.println("* Creating test files");
		//		TestTaskGenerator.suggestTasks(Properties.PROJECT_PREFIX);
		TestTaskGenerator.suggestTasks(classes);
	}
}
