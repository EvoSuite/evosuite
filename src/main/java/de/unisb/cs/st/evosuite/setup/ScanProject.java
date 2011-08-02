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

import org.eclipse.jdt.core.dom.Modifier;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.commons.EmptyVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.callgraph.ConnectionData;
import de.unisb.cs.st.evosuite.callgraph.DistanceClassAdapter;
import de.unisb.cs.st.evosuite.callgraph.DistanceTransformer;
import de.unisb.cs.st.evosuite.callgraph.DistanceTransformer.ClassEntry;
import de.unisb.cs.st.evosuite.classcreation.ClassFactory;
import de.unisb.cs.st.evosuite.javaagent.CIClassAdapter;
import de.unisb.cs.st.evosuite.utils.Utils;

/**
 * @author Gordon Fraser
 * 
 */
public class ScanProject {

	protected static Logger logger = LoggerFactory.getLogger(ScanProject.class);

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

		/* (non-Javadoc)
		 * @see java.lang.ClassLoader#loadClass(java.lang.String)
		 */
		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			return findClass(name);
		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {

			//name = name.replace(".", ".");
			String classNameWithDots = name.replace("/", ".");
			if (classMap.containsKey(classNameWithDots)) {
				System.out.println("Attempting to load what we already have: "
				        + classNameWithDots);
				return classMap.get(classNameWithDots);
			}

			try {
				Class<?> clazz = findSystemClass(name);
				classMap.put(clazz.getName(), clazz);
				return clazz;
			} catch (Exception e) {
			}

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
				String realName = reader.getClassName().replace("/", ".");
				if (classMap.containsKey(realName)) {
					System.out.println("Got tricked by path name: " + realName);
					return classMap.get(realName);
				}
				Class<?> clazz = defineClass(reader.getClassName().replace("/", "."),
				                             out.toByteArray(), 0, out.size());
				classMap.put(clazz.getName(), clazz);
				System.out.println("Keeping " + clazz.getName());
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
					System.out.println("* Stubs enabled");
					Class<?> clazz = Class.forName(packageName + '.'
					        + file.getName().substring(0, file.getName().length() - 6));

					if (Modifier.isAbstract(clazz.getModifiers()) && !clazz.isInterface()) {
						System.out.println("* Creating concrete stub for abstract class "
						        + clazz.getName());
						ClassFactory cf = new ClassFactory();
						Class<?> stub = cf.createClass(clazz);
						if (stub != null)
							classes.add(stub);
					} else {
						System.out.println("* Not creating concrete stub for abstract class "
						        + clazz.getName());

					}
				} else {
					System.out.println("* Stubs disabled");

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

	private static Set<Class<?>> loadClass(File file, String packageName,
	        boolean printOutput) throws ClassNotFoundException {
		Set<Class<?>> set = new HashSet<Class<?>>();
		if (file.isDirectory()) {
			assert !file.getName().contains(".");
			set.addAll(findClasses(file, packageName + "." + file.getName()));
		} else if (file.getName().endsWith(".class")) {
			if (printOutput)
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
				System.out.println("[L] Cannot access class " + packageName + '.'
				        + file.getName().substring(0, file.getName().length() - 6) + ": "
				        + e);
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
	 * Generate mocks for the classes from <class_name>.CIs file.
	 * 
	 * @param className
	 */
	private static void generateMocksAndStubs(String className) {
		if(className == null)
			return;
		Set<String> classNames = new HashSet<String>();
		classNames.addAll(Utils.readFile("evosuite-files/"
		        + className.replace("/", ".").replace("class", "") + ".CIs"));
		
		Set<Class<?>> classes = new HashSet<Class<?>>();
		try {
			for (String cn : classNames) 
				classes.addAll(loadClass(new File(cn.replace("/", ".") + ".class"),
				                         cn.split("/")[0], false));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		ClassFactory cf = new ClassFactory();
		for (Class<?> c : classes) {
			String packageName = Utils.getPackageName(c);			
			if (packageName.startsWith("java.") || packageName.startsWith("sun."))
				continue;
			cf.createClass(c);
		}

	}

	/**
	 * Analyze all classes of a given package prefix that can be found in the
	 * classpath
	 */
	public static Set<Class<?>> getClasses(String packageName, boolean silent)
	        throws ClassNotFoundException, IOException {
		Set<Class<?>> set = new HashSet<Class<?>>();

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		Collection<String> list = ResourceList.getResources(Pattern.compile(packageName
		        + "/.*\\.class$"));
		for (String name : list) {
			System.out.println("* Loading class " + name);
			set.addAll(loadClass(new File(name), packageName, silent));
		}

		return set;
	}

	/**
	 * Analyze all classes that can be found in a given directory
	 * 
	 * @param directory
	 * @throws ClassNotFoundException
	 */
	public static Set<Class<?>> getClasses(File directory) {
		if (directory.getName().endsWith(".jar")) {
			return getClassesJar(directory);
		} else if (directory.getName().endsWith(".class")) {
			Set<Class<?>> set = new HashSet<Class<?>>();
			try {
				System.out.println("* Loading class " + directory.getName());
				File file = new File(directory.getPath());
				byte[] array = new byte[(int) file.length()];
				InputStream in = new FileInputStream(file);
				ByteArrayOutputStream out = new ByteArrayOutputStream(array.length);
				int length = in.read(array);
				while (length > 0) {
					out.write(array, 0, length);
					length = in.read(array);
				}
				ClassReader reader = new ClassReader(array);
				String className = reader.getClassName();

				// Use default classLoader
				Class<?> clazz = Class.forName(className.replace("/", "."));
				//classLoader.loadClass(directory.getPath());
				set.add(clazz);
				if (Properties.STUBS) {
					if (Modifier.isAbstract(clazz.getModifiers()) && !clazz.isInterface()) {
						ClassFactory cf = new ClassFactory();
						cf.createClass(clazz);
					}
				}
			} catch (IllegalAccessError e) {
				System.out.println("Cannot access class "
				        + directory.getName().substring(0,
				                                        directory.getName().length() - 6)
				        + ": " + e);
			} catch (NoClassDefFoundError e) {
				System.out.println("Error while loading "
				        + directory.getName().substring(0,
				                                        directory.getName().length() - 6)
				        + " in path " + directory + ": Cannot find " + e.getMessage());
				//e.printStackTrace();
			} catch (ExceptionInInitializerError e) {
				System.out.println("Exception in initializer of "
				        + directory.getName().substring(0,
				                                        directory.getName().length() - 6));
			} catch (ClassNotFoundException e) {
				System.out.println("Class not found: "
				        + directory.getName().substring(0,
				                                        directory.getName().length() - 6));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

	public static Set<Class<?>> getClassesJar(File file) {

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
				System.out.println("* Loading class " + fileName + " from jar file "
				        + file.getName());
				Class<?> clazz = zcl.findClass(fileName.replace(".class", ""));
				set.add(clazz);
				if (Properties.STUBS) {
					if (Modifier.isAbstract(clazz.getModifiers()) && !clazz.isInterface()) {
						ClassFactory cf = new ClassFactory();
						cf.createClass(clazz);
					}
				}
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
			} catch (ClassNotFoundException ex) {
				System.out.println("Cannot find class "
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

	public static void analyzeClasses(Set<Class<?>> classes) {

		System.out.println("* Analyzing target classes");
		ConnectionData data = new ConnectionData();
		Set<ClassEntry> classEntries = new HashSet<ClassEntry>();

		Set<String> packageClasses = new HashSet<String>();
		for (Class<?> clazz : classes) {
			packageClasses.add(clazz.getName());
		}

		for (Class<?> clazz : classes) {
			String className = clazz.getName().replace(".", "/");
			try {

				InputStream bytecode = clazz.getResourceAsStream("/" + className
				        + ".class");

				ClassReader reader = new ClassReader(bytecode);
				DistanceTransformer.SuperClassAdapter cv = new DistanceTransformer.SuperClassAdapter(
				        new EmptyVisitor());
				DistanceClassAdapter distance = new DistanceClassAdapter(cv, data,
				        packageClasses);
				CIClassAdapter ci = new CIClassAdapter(distance);

				reader.accept(ci, ClassReader.SKIP_FRAMES);
				
				
				classEntries.add(new ClassEntry(clazz.getName(), cv.getSupers()));
				generateMocksAndStubs(className);
			} catch (IOException e) {
				System.out.println(e + ": /" + clazz.getName().replace(".", "/")
				        + ".class");
				System.out.println(className + ".class" + ": "
				        + clazz.getResource(className + ".class"));
				System.out.println(className.substring(className.lastIndexOf("/"))
				        + ".class"
				        + ": "
				        + clazz.getResource(className.substring(className.lastIndexOf("/"))
				                + ".class"));
				System.out.println(className.substring(className.lastIndexOf("/") + 1)
				        + ".class"
				        + ": "
				        + clazz.getResource(className.substring(className.lastIndexOf("/") + 1)
				                + ".class"));
				// e.printStackTrace();
				// Ignore unloadable classes
			}
		}
		data.save();
		Utils.writeXML(classEntries, Properties.OUTPUT_DIR + "/"
		        + Properties.HIERARCHY_DATA);

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
			if (Properties.PROJECT_PREFIX != null
			        && !Properties.PROJECT_PREFIX.equals("")) {
				System.out.println("* Analyzing project prefix: "
				        + Properties.PROJECT_PREFIX);
				classes.addAll(getClasses(Properties.PROJECT_PREFIX, false));
			} else if (args.length > 0) {
				for (String arg : args) {
					System.out.println("* Analyzing project directory: " + arg);
					classes.addAll(getClasses(new File(arg)));
					// TODO: Do analysis here
				}
			} else {
				System.out.println("* Please specify either project prefix or directory");
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		analyzeClasses(classes);
		//TestSuitePreMain.distanceTransformer.saveData();
		Utils.addURL(ClassFactory.getStubDir() + "/classes/");
		TestTaskGenerator.hierarchy.calculateSubclasses();
		if (Properties.CALCULATE_CLUSTER) {
			System.out.println("* Analyzing classpath");
			ClusterAnalysis.readAllClasses();
		}

		System.out.println("* Creating test files");
		//		TestTaskGenerator.suggestTasks(Properties.PROJECT_PREFIX);
		TestTaskGenerator.suggestTasks(classes);
	}
}
