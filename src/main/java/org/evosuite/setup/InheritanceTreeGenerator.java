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
/**
 * 
 */
package org.evosuite.setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.utils.LoggingUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

/**
 * @author Gordon Fraser
 * 
 */
public class InheritanceTreeGenerator {

	private static Logger logger = LoggerFactory.getLogger(InheritanceTreeGenerator.class);

	/**
	 * Iterate over items in classpath and analyze them
	 * 
	 * @param classPath
	 * @return
	 */
	public static InheritanceTree analyze(List<String> classPath) {
		InheritanceTree inheritanceTree = readJDKData();

		for (String classPathEntry : classPath) {
			if (classPathEntry.isEmpty())
				continue;

			if (classPathEntry.matches(".*evosuite-.*\\.jar"))
				continue;

			logger.debug("Analyzing classpath entry " + classPathEntry);
			LoggingUtils.getEvoLogger().info("  - " + classPathEntry);
			analyze(inheritanceTree, classPathEntry);
		}
		return inheritanceTree;
	}

	/**
	 * 
	 * @param inheritanceTree
	 * @param entry
	 */
	private static void analyze(InheritanceTree inheritanceTree, String entry) {
		analyze(inheritanceTree, new File(entry));
	}

	/**
	 * 
	 * @param inheritanceTree
	 * @param entry
	 */
	private static void analyze(InheritanceTree inheritanceTree, File file) {

		if (file.getName().endsWith(".jar")) {
			// handle jar file
			analyzeJarFile(inheritanceTree, file);
		} else if (file.getName().endsWith(".class")) {
			// handle individual class
			analyzeClassFile(inheritanceTree, file);
		} else if (file.isDirectory()) {
			// handle directory
			analyzeDirectory(inheritanceTree, file);
		} else {
			// Invalid entry?
		}
	}

	private static void analyzeJarFile(InheritanceTree inheritanceTree, File jarFile) {

		ZipFile zf;
		try {
			zf = new ZipFile(jarFile);
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

			try {
				analyzeClassStream(inheritanceTree, zf.getInputStream(ze));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		try {
			zf.close();
		} catch (final IOException e1) {
			throw new Error(e1);
		}
	}

	private static void analyzeDirectory(InheritanceTree inheritanceTree, File directory) {
		for (File file : directory.listFiles()) {
			analyze(inheritanceTree, file);
		}
	}

	private static void analyzeClassFile(InheritanceTree inheritanceTree, File classFile) {
		try {
			analyzeClassStream(inheritanceTree, new FileInputStream(classFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private static void analyzeClassStream(InheritanceTree inheritanceTree,
	        InputStream inputStream) {
		try {
			ClassReader reader = new ClassReader(inputStream);
			ClassNode cn = new ClassNode();
			reader.accept(cn, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG
			        | ClassReader.SKIP_CODE);
			logger.debug("Analyzing class " + cn.name);

			if (cn.superName != null)
				inheritanceTree.addSuperclass(cn.name, cn.superName, cn.access);

			List<String> interfaces = cn.interfaces;
			for (String interfaceName : interfaces) {
				inheritanceTree.addInterface(cn.name, interfaceName);
			}

			// TODO: Should we store the ClassNode?

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static List<String> classExceptions = Arrays.asList(new String[] {
	        "java/lang/Class", "java/lang/Object", "java/lang/String",
	        "java/lang/Comparable", "java/io/Serializable" });

	/**
	 * During runtime, we do not want to consider standard classes to safe some
	 * time, so we perform this analysis only once.
	 */
	public static void generateJDKCluster() {
		Collection<String> list = getAllResources();
		InheritanceTree inheritanceTree = new InheritanceTree();

		for (String name : list) {
			//			if (!name.startsWith("de/unisb")) {
			// We do not consider sun.* and apple.* and com.* 
			if (!classExceptions.contains(name) && (name.startsWith("java/") // || name.startsWith("sun") || name.startsWith("com/sun") 
			        || name.startsWith("javax/"))) { // || name.startsWith("java/awt")) {
				InputStream stream = TestGenerationContext.getClassLoader().getResourceAsStream(name);
				analyzeClassStream(inheritanceTree, stream);
			}
		}

		// Set<String> writers = new HashSet<String>();
		// writers.add("java.io.StringWriter");
		// generatorData.put("java.io.Writer", writers);

		// Write data to XML file
		try {
			FileOutputStream stream = new FileOutputStream(
			        new File("JDK_inheritance.xml"));
			XStream xstream = new XStream();
			xstream.toXML(inheritanceTree, stream);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static InheritanceTree readJDKData() {
		XStream xstream = new XStream();
		InputStream inheritance = InheritanceTreeGenerator.class.getResourceAsStream("/JDK_inheritance.xml");
		if (inheritance != null)
			return (InheritanceTree) xstream.fromXML(inheritance);
		else
			return new InheritanceTree();
	}

	public static Collection<String> getResources() {
		return getResources(Properties.CP);
	}

	public static Collection<String> getAllResources() {
		Collection<String> retval = getResources(System.getProperty("java.class.path",
		                                                            "."));
		retval.addAll(getResources(System.getProperty("sun.boot.class.path")));
		return retval;
	}

	private static Collection<String> getResources(String classPath) {
		final ArrayList<String> retval = new ArrayList<String>();
		//String classPath = System.getProperty("java.class.path", ".");
		//String classPath = Properties.CP;
		String[] classPathElements = classPath.split(":");
		Pattern pattern = Pattern.compile(".*\\.class$");

		for (final String element : classPathElements) {
			if (element.contains("evosuite-0.1-SNAPSHOT-dependencies.jar"))
				continue;
			if (element.endsWith("jpf-annotations.jar"))
				continue;
			if (element.endsWith("jpf-classes.jar"))
				continue;
			if (element.contains("evosuite"))
				continue;
			retval.addAll(ResourceList.getResources(element, pattern));
		}

		return retval;
	}

	public static void main(String[] args) {
		generateJDKCluster();
	}

}
