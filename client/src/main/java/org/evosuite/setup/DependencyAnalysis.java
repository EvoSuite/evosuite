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
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.TestGenerationContext;
import org.evosuite.classpath.ResourceList;
import org.evosuite.coverage.branch.BranchCoverageFactory;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.dataflow.DefUsePool;
import org.evosuite.coverage.mutation.MutationPool;
import org.evosuite.graphs.cfg.CFGMethodAdapter;
import org.evosuite.instrumentation.LinePool;
import org.evosuite.rmi.ClientServices;
import org.evosuite.setup.callgraph.CallGraphGenerator;
import org.evosuite.setup.callgraph.CallGraph;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.ArrayUtil;
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

	private static CallGraph callGraph = null;

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
	public static void analyze(String className, List<String> classPath) throws RuntimeException,
			ClassNotFoundException {
		logger.debug("Calculate inheritance hierarchy");
		inheritanceTree = InheritanceTreeGenerator.createFromClassPath(classPath);
		InheritanceTreeGenerator.gatherStatistics(inheritanceTree);

		if (!inheritanceTree.hasClass(Properties.TARGET_CLASS)) {
			throw new ClassNotFoundException("Target class not found in inheritance tree");
		}

		logger.debug("Calculate call tree");
		callGraph = CallGraphGenerator.analyze(className);
		loadCallTreeClasses();

		// include all the project classes in the inheritance tree and in the
		// callgraph.
		if (ArrayUtil.contains(Properties.CRITERION, Criterion.IBRANCH)
				|| Properties.INSTRUMENT_CONTEXT) { 
 
			for (String classn : inheritanceTree.getAllClasses()) {
				if (isTargetProject(classn)) {
					CallGraphGenerator.analyzeOtherClasses(callGraph, classn);
				}
			}
		}

		// TODO: Need to make sure that all classes in calltree are instrumented

		logger.debug("Update call tree with calls to overridden methods");
		CallGraphGenerator.update(callGraph, inheritanceTree);

		logger.debug("Create test cluster");

		// if a class is not instrumented but part of the callgraph, the
		// generateCluster method will instrument it
		// update: we instrument only classes reachable from the class
		// under test, the callgraph is populated with all classes, but only the
		// set of relevant ones are instrumented - mattia
		TestClusterGenerator clusterGenerator = new TestClusterGenerator();
		clusterGenerator.generateCluster(className, inheritanceTree, callGraph);

		gatherStatistics();
	}

	private static void loadCallTreeClasses() {
		for (String className : callGraph.getClasses()) {
			if (className.startsWith(Properties.TARGET_CLASS + "$")) {
				try {
					Class.forName(className, true,
							TestGenerationContext.getInstance().getClassLoaderForSUT());
				} catch (ClassNotFoundException e) {
					logger.debug("Error loading " + className + ": " + e);
				}
			}
		}
	}

	public static CallGraph getCallGraph() {
		return callGraph;
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

	// TODO implement something that takes parameters using properties -
	// generalize this method.
	public static boolean isTargetProject(String className) {
		return (className.startsWith(Properties.PROJECT_PREFIX) || (!Properties.TARGET_CLASS_PREFIX
				.isEmpty() && className.startsWith(Properties.TARGET_CLASS_PREFIX)))
				&& !className.startsWith("java.")
				&& !className.startsWith("sun.")
				&& !className.startsWith("org.evosuite")
				&& !className.startsWith("org.exsyst")
				&& !className.startsWith("de.unisb.cs.st.evosuite")
				&& !className.startsWith("de.unisb.cs.st.specmate")
				&& !className.startsWith("javax.")
				&& !className.startsWith("org.xml")
				&& !className.startsWith("org.w3c")
				&& !className.startsWith("apple.")
				&& !className.startsWith("com.apple.")
				&& !className.startsWith("org.omg.")
				&& !className.startsWith("sunw.")
				&& !className.startsWith("org.jcp.")
				&& !className.startsWith("org.ietf.") 
				&& !className.startsWith("daikon.");
	}

//	private static String getProjectPackageApprox(String qualifiedName) {
//		if (qualifiedName == null)
//			throw new IllegalArgumentException();
//		String[] splitted = qualifiedName.split("\\.");
//		String result = "";
//		if (splitted.length == 0)
//			result = qualifiedName;
//		else if (splitted.length == 1)
//			result = splitted[0];
//		else if (splitted.length == 2)
//			result = splitted[0];
//		else if (splitted[0].equals("com") || splitted[0].equals("org")
//				|| splitted[0].equals("net") || splitted[0].equals("de")
//				|| splitted[0].equals("it") || splitted[0].equals("ch") || splitted[0].equals("fr")
//				|| splitted[0].equals("br") || splitted[0].equals("edu")
//				|| splitted[0].equals("osa") || splitted[0].equals("uk")
//				|| splitted[0].equals("gov") || splitted[0].equals("dk")) {
//			result = splitted[0] + "." + splitted[1];
//		} else
//			result = splitted[0];
//
//		return result;
//	}
	
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

		if (inheritanceTree == null) {
			return false;
		}
		// Also analyze if it is a superclass and instrument_parent = true
		if (Properties.INSTRUMENT_PARENT) {
			if (inheritanceTree.getSuperclasses(Properties.TARGET_CLASS).contains(className))
				return true;
		}

		// Also analyze if it is in the calltree and we are considering the
		// context
		if (Properties.INSTRUMENT_CONTEXT
				|| ArrayUtil.contains(Properties.CRITERION, Criterion.DEFUSE)) {
			if (callGraph.isCalledClass(className)) {
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

		// Also analyze if it is in the calltree and we are considering the
		// context
		if (Properties.INSTRUMENT_CONTEXT) {
			
			if (callGraph.isCalledMethod(className, methodName)){
				if(Properties.INSTRUMENT_LIBRARIES || DependencyAnalysis.isTargetProject(className))
				return true;
			}
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
		
		InputStream classStream = ResourceList.getClassAsStream(className);
		if(classStream == null) {
			// This used to throw an IOException that leads to null being
			// returned, so for now we're just returning null directly
			// TODO: Proper treatment of missing classes (can also be
			//       invalid calls, e.g. [L/java/lang/Object;)
			logger.info("Could not find class file: "+className);
			return null;
		}
		ClassNode cn = new ClassNode();
		try {
			ClassReader reader = new ClassReader(classStream);
			reader.accept(cn, ClassReader.SKIP_FRAMES); // |
			// ClassReader.SKIP_DEBUG);
		} finally {
			classStream.close(); // ASM does not close the stream
		}
		return cn;
	}


	private static void gatherStatistics() {
		ClientServices.getInstance().getClientNode()
				.trackOutputVariable(RuntimeVariable.Predicates, BranchPool.getBranchCounter());
		ClientServices
				.getInstance()
				.getClientNode()
				.trackOutputVariable(RuntimeVariable.Total_Branches,
						(BranchPool.getBranchCounter()) * 2);
		ClientServices
				.getInstance()
				.getClientNode()
				.trackOutputVariable(RuntimeVariable.Branchless_Methods,
						BranchPool.getBranchlessMethods().size());
		ClientServices
				.getInstance()
				.getClientNode()
				.trackOutputVariable(RuntimeVariable.Total_Methods,
						CFGMethodAdapter.getNumMethods());

		ClientServices.getInstance().getClientNode()
				.trackOutputVariable(RuntimeVariable.Lines, LinePool.getNumLines());

		for (Properties.Criterion pc : Properties.CRITERION) {
			switch (pc) {
			case DEFUSE:
			case ALLDEFS:
				ClientServices
						.getInstance()
						.getClientNode()
						.trackOutputVariable(RuntimeVariable.Definitions,
								DefUsePool.getDefCounter());
				ClientServices.getInstance().getClientNode()
						.trackOutputVariable(RuntimeVariable.Uses, DefUsePool.getUseCounter());
				break;

			case WEAKMUTATION:
			case STRONGMUTATION:
			case MUTATION:
				ClientServices
						.getInstance()
						.getClientNode()
						.trackOutputVariable(RuntimeVariable.Mutants,
								MutationPool.getMutantCounter());
				break;

			default:
				break;
			}
		}
	}
}
