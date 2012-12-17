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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.junit.Test;
import org.junit.runners.Suite;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class performs static analysis before everything else initializes
 * 
 * @author Gordon Fraser
 * 
 */
public class DependencyAnalysis {

	private static Logger logger = LoggerFactory.getLogger(DependencyAnalysis.class);
	
	private static Map<String, ClassNode> classCache = new LinkedHashMap<String, ClassNode>();

	private static CallTree callTree = null;

	private static InheritanceTree inheritanceTree = null;

	/**
	 * @return the inheritanceTree
	 */
	public static InheritanceTree getInheritanceTree() {
		return inheritanceTree;
	}

	/**
	 * Start analysis from target class
	 * 
	 * @param className
	 */
	public static void analyze(String className, List<String> classPath)
	        throws RuntimeException, ClassNotFoundException {

		logger.debug("Calculate inheritance hierarchy");		
		inheritanceTree = InheritanceTreeGenerator.analyze(classPath);

		logger.debug("Calculate call tree");
		callTree = CallTreeGenerator.analyze(className);

		// TODO: Need to make sure that all classes in calltree are instrumented

		logger.debug("Update call tree with calls to overridden methods");
		CallTreeGenerator.update(callTree, inheritanceTree);

		logger.debug("Create test cluster");
		TestClusterGenerator.generateCluster(className, inheritanceTree, callTree);
	}

	public static CallTree getCallTree() {
		return callTree;
	}

	/**
	 * Determine if this class contains JUnit tests
	 * 
	 * @param className
	 * @return
	 */
	private static boolean isTest(String className) {
		// TODO-JRO Identifying tests should be done differently:
		// If the class either contains methods
		// annotated with @Test (> JUnit 4.0)
		// or contains Test or Suite in it's inheritance structure
		try {
			Class<?> clazz = Class.forName(className);
			Class<?> superClazz = clazz.getSuperclass();
			while (!superClazz.equals(Object.class)) {
				if (superClazz.equals(Suite.class))
					return true;
				if (superClazz.equals(Test.class))
					return true;

				superClazz = clazz.getSuperclass();
			}
			for (Method method : clazz.getMethods()) {
				if (method.isAnnotationPresent(Test.class)) {
					return true;
				}
			}
		} catch (ClassNotFoundException e) {
			// TODO
		}
		return false;
	}

	/**
	 * Determine if the given class is the target class
	 * 
	 * @param className
	 * @return
	 */
	public static boolean isTargetClassName(String className) {
		if (!Properties.TARGET_CLASS_PREFIX.isEmpty()
		        && className.startsWith(Properties.TARGET_CLASS_PREFIX)) {
			// exclude existing tests from the target project
			return !isTest(className);
		}
		if (className.equals(Properties.TARGET_CLASS)
		        || className.startsWith(Properties.TARGET_CLASS + "$")) {
			return true;
		}

		return false;

	}

	/**
	 * Determine if the given class should be analyzed or instrumented
	 * 
	 * @param className
	 * @return
	 */
	public static boolean shouldAnalyze(String className) {
		// Always analyze if it is a target class
		if (isTargetClassName(className))
			return true;

		// Also analyze if it is a superclass and instrument_parent = true
		if (Properties.INSTRUMENT_PARENT) {
			if (inheritanceTree.getSuperclasses(Properties.TARGET_CLASS).contains(className))
				return true;
		}

		// Also analyze if it is in the calltree and we are considering the context
		if (Properties.INSTRUMENT_CONTEXT || Properties.CRITERION == Criterion.DEFUSE) {
			if (callTree.isCalledClass(className)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Determine if the given method should be instrumented
	 * 
	 * @param className
	 * @param methodName
	 * @return
	 */
	public static boolean shouldInstrument(String className, String methodName) {
		// Always analyze if it is a target class
		if (isTargetClassName(className))
			return true;

		// Also analyze if it is a superclass and instrument_parent = true
		if (Properties.INSTRUMENT_PARENT) {
			if (inheritanceTree.getSuperclasses(Properties.TARGET_CLASS).contains(className))
				return true;
		}

		// Also analyze if it is in the calltree and we are considering the context
		if (Properties.INSTRUMENT_CONTEXT) {
			if (callTree.isCalledMethod(className, methodName))
				return true;
		}

		return false;
	}

	public static ClassNode getClassNode(String className) {
		if (!classCache.containsKey(className)) {
			try {
				classCache.put(className, loadClassNode(className));
			} catch (IOException e) {
				classCache.put(className, null);
			}
		}

		return classCache.get(className);

	}

	public static Collection<ClassNode> getAllClassNodes() {
		return classCache.values();
	}

	private static ClassNode loadClassNode(String className) throws IOException {
		ClassReader reader = new ClassReader(className);

		ClassNode cn = new ClassNode();
		reader.accept(cn, ClassReader.SKIP_FRAMES); // | ClassReader.SKIP_DEBUG);	
		return cn;
	}
}
