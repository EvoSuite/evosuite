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

package de.unisb.cs.st.evosuite.mutation;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import de.unisb.cs.st.ds.util.io.Io;
import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.javalanche.coverage.distance.Hierarchy;
import de.unisb.cs.st.javalanche.mutation.javaagent.classFileTransfomer.mutationDecision.Excludes;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;
import de.unisb.cs.st.javalanche.mutation.results.persistence.QueryManager;

/**
 * This class generates for each testable unit: - A mutation task - A list of
 * potential inspector methods - A list of callable methods and constructors
 * 
 * @author Gordon Fraser
 * 
 */
public class TGTaskCreator extends de.unisb.cs.st.javalanche.mutation.run.task.MutationTaskCreator {

	private static Logger logger = Logger.getLogger(TGTaskCreator.class);

	static Hierarchy hierarchy = Hierarchy.readFromDefaultLocation();

	static Excludes excludes = Excludes.getInstance();

	static String prefix;

	static Map<String, List<String>> method_excludes = getExcludesFromFile();

	/**
	 * Entry point - generate task files
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("* Creating mutation files");
		// MutationProperties.checkProperty(MutationProperties.PROJECT_PREFIX_KEY);
		// HandleUnsafeMutations.handleUnsafeMutations(HibernateUtil.getSessionFactory());
		prefix = Properties.PROJECT_PREFIX;

		hierarchy.calculateSubclasses();
		suggestTasks(prefix);
	}

	/**
	 * Get list of all classes in the given prefix
	 * 
	 * @param prefix
	 *            Package prefix
	 * @return List of classnames in prefix that are not excluded The list is
	 *         ascending sorted by number of subclasses
	 */
	private static List<String> getClasses(String prefix) {
		logger.info("Getting list of classes");
		Set<String> all_classes = hierarchy.getAllClasses();
		logger.info("Number of classes: " + all_classes.size());

		TreeMap<Integer, Set<String>> classes = new TreeMap<Integer, Set<String>>();
		// hierarchy.calculateInferiors();
		for (String classname : all_classes) {
			if (classname.startsWith(prefix)) {
				if (!excludes.shouldExclude(classname)) {
					int num_subclasses = hierarchy.getAllSubclasses(classname).size();
					if (!classes.containsKey(num_subclasses)) {
						classes.put(num_subclasses, new HashSet<String>());
					}
					classes.get(num_subclasses).add(classname);
				}
			}
		}

		List<String> sorted_classes = new ArrayList<String>();
		for (Entry<Integer, Set<String>> entry : classes.entrySet()) {
			logger.debug(entry.getKey() + " subclasses: ");
			for (String name : entry.getValue()) {
				logger.debug("   " + name);
			}
		}
		for (Set<String> classset : classes.values()) {
			sorted_classes.addAll(classset);
		}
		logger.info("Number of sorted classes: " + sorted_classes.size());

		return sorted_classes;
	}

	/**
	 * Read classes to be excluded
	 * 
	 * @return Map from classname to methods that should not be used
	 */
	private static Map<String, List<String>> getExcludesFromFile() {
		String property = System.getProperty("test.excludes");
		Map<String, List<String>> objs = new HashMap<String, List<String>>();
		if (property == null) {
			return objs;
		}
		File file = new File(property);
		if (!file.exists()) {
			logger.warn("Exclude file " + property + " does not exist, skipping");
			return objs;
		}
		List<String> lines = Io.getLinesFromFile(file);
		for (String line : lines) {
			line = line.trim();
			// Skip comments
			if (line.startsWith("#")) {
				continue;
			}

			String[] parameters = line.split(",");
			if (parameters.length != 2) {
				continue;
			}
			if (!objs.containsKey(parameters[0])) {
				objs.put(parameters[0], new ArrayList<String>());
			}

			objs.get(parameters[0]).add(parameters[1]);
		}
		return objs;
	}

	/**
	 * Get list of mutation IDs for the given list of classes
	 * 
	 * @param classes
	 *            Classes for which we want mutants
	 * 
	 * @return List of IDs
	 */
	private static List<Long> getMutations(List<String> classes) {
		List<Long> mutations = new ArrayList<Long>();
		for (String classname : classes) {
			List<Mutation> ms = QueryManager.getMutationsForClass(classname);
			for (Mutation m : ms) {
				mutations.add(m.getId());
				logger.debug(" Mutation in " + m.getClassName() + "." + m.getMethodName() + ":" + m.getLineNumber());
			}
		}
		logger.info("Got " + mutations.size() + " mutations");
		return mutations;
	}

	/**
	 * Get all usable subclasses of the given class
	 * 
	 * @param classname
	 *            Name of given class
	 * @return All subclasses that are within prefix and not excluded
	 */
	private static List<String> getOwnedClasses(String classname) {
		Set<String> subclasses = hierarchy.getAllClasses();
		List<String> ret = new ArrayList<String>();
		for (String sub : subclasses) {
			if (sub.startsWith(classname + "$")) {
				if (!excludes.shouldExclude(sub)) {
					ret.add(sub);
				}
			}
		}

		return ret;
	}

	/**
	 * Central function of the task creator. Creates test task files, mutation
	 * task files, and inspector files
	 * 
	 * @param prefix
	 *            Project prefix
	 */
	private static void suggestTasks(String prefix) {
		List<String> classes = getClasses(prefix);

		int num_mutants = 0;
		for (String classname : classes) {
			Class<?> clazz = null;
			try {
				clazz = Class.forName(classname);
			} catch (ClassNotFoundException e) {
				logger.warn("TG: Class not found: " + classname + ", ignoring for tests");
				continue;
			} catch (NoClassDefFoundError e) {
				logger.warn("NoClassDefFoundError " + classname);
				continue;
			} catch (ExceptionInInitializerError e) {
				logger.warn("ExceptionInInitializerError " + classname);
				continue;
			}
			if (clazz.isInterface()) {
				logger.info("Ignoring interface " + classname);
				continue;
			}
			if ((clazz.getDeclaredMethods().length == 0) && (clazz.getDeclaredConstructors().length == 0)) {
				logger.info("Ignoring class without methods: " + classname);
				continue;
			}
			if (clazz.isMemberClass()) {
				logger.info("Ignoring member class " + classname);
				continue;
			}
			if (clazz.isLocalClass()) {
				logger.info("Ignoring local class " + classname);
				continue;
			}
			if (clazz.isAnonymousClass()) {
				logger.info("Ignoring anonymous class " + classname);
				continue;
			}
			if (Throwable.class.isAssignableFrom(clazz)) {
				logger.info("Ignoring exception class " + classname);
				continue;
			}
			if (Modifier.isPrivate(clazz.getModifiers())) {
				logger.info("Ignoring private class " + classname);
				continue;
			}

			if (classname.matches(".*\\$\\d+$")) {
				logger.info("Ignoring anonymous class");
				continue;
			}

			List<String> mutant_classes = new ArrayList<String>();
			mutant_classes.add(classname);
			mutant_classes.addAll(getOwnedClasses(clazz.getName()));
			List<Long> mutationIds = getMutations(mutant_classes);
			if (mutationIds.isEmpty()) {
				System.out.println("* No mutants found for class " + classname);
				continue;
			}

			num_mutants += mutationIds.size();
			String classfilename = classname.replace("$", "_");
			writeListToFile(classfilename, mutationIds);

		}

	}

	private static File writeListToFile(String className, List<Long> list) {
		File file = new File(Properties.OUTPUT_DIR, className + ".mutants");
		StringBuilder sb = new StringBuilder();
		for (Long l : list) {
			sb.append(l);
			sb.append("\n");
		}
		Io.writeFile(sb.toString(), file);
		System.out.println("* Mutation task created: " + file.getPath());
		return file;
	}
}
