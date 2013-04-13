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

import java.io.EvoSuiteIO;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.graphs.cfg.CFGMethodAdapter;
import org.evosuite.instrumentation.BooleanTestabilityTransformation;
import org.evosuite.primitives.ConstantPoolManager;
import org.evosuite.rmi.ClientServices;
import org.evosuite.runtime.FileSystem;
import org.evosuite.utils.GenericAccessibleObject;
import org.evosuite.utils.GenericClass;
import org.evosuite.utils.GenericConstructor;
import org.evosuite.utils.GenericField;
import org.evosuite.utils.GenericMethod;
import org.evosuite.utils.Utils;
import org.junit.Test;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gordon Fraser
 * 
 */
public class TestClusterGenerator {

	private static Logger logger = LoggerFactory.getLogger(TestClusterGenerator.class);

	private static List<String> classExceptions = Arrays.asList(new String[] {
	        "com.apple", "apple.", "sun.", "com.sun.", "com.oracle.", "sun.awt." });

	/**
	 * Check if we can use the given class
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public static boolean checkIfCanUse(String className) {
		for (String s : classExceptions) {
			if (className.startsWith(s)) {
				return false;
			}
		}
		return true;
	}

	public static void resetCluster() throws RuntimeException, ClassNotFoundException {
		BytecodeInstructionPool.clearAll();
		BranchPool.clear();
		CFGMethodAdapter.methods.clear();
		TestCluster.reset();
		generateCluster(Properties.TARGET_CLASS, TestCluster.getInheritanceTree(),
		                DependencyAnalysis.getCallTree());
	}

	private static Set<GenericAccessibleObject> dependencyCache = new LinkedHashSet<GenericAccessibleObject>();

	private static Set<GenericClass> genericCastClasses = new LinkedHashSet<GenericClass>();

	private static Set<Class<?>> concreteCastClasses = new LinkedHashSet<Class<?>>();

	private static Set<Class<?>> containerClasses = new LinkedHashSet<Class<?>>();

	public static void generateCluster(String targetClass,
	        InheritanceTree inheritanceTree, CallTree callTree) throws RuntimeException,
	        ClassNotFoundException {

		TestClusterGenerator.inheritanceTree = inheritanceTree;
		TestCluster.setInheritanceTree(inheritanceTree);

		if (Properties.INSTRUMENT_CONTEXT || Properties.CRITERION == Criterion.DEFUSE) {
			for (String callTreeClass : DependencyAnalysis.getCallTree().getClasses()) {
				try {
					TestGenerationContext.getClassLoader().loadClass(callTreeClass);
				} catch (ClassNotFoundException e) {
					logger.info("Class not found: " + callTreeClass);
				}
			}
		}

		Set<Type> parameterClasses = new LinkedHashSet<Type>();
		Set<Type> callTreeClasses = new LinkedHashSet<Type>();

		// Other classes might have further dependencies which we might need to resolve
		parameterClasses.removeAll(callTreeClasses);

		/*
		 * If we fail to load a class, we skip it, and avoid to try
		 * to load it again (which would result in extra unnecessary logging)
		 */
		Set<String> blackList = new LinkedHashSet<String>();
		initBlackListWithPrimitives(blackList);

		logger.info("Handling cast classes");
		handleCastClasses();

		logger.info("Initialising target class");
		initializeTargetMethods();

		logger.info("Resolving dependencies");
		resolveDependencies(blackList);

		if (logger.isDebugEnabled()) {
			logger.debug(TestCluster.getInstance().toString());
		}
		dependencyCache.clear();
		gatherStatistics();
	}

	private static void handleCastClasses() {
		// If we include type seeding, then we analyze classes to find types in instanceof and cast instructions
		if (Properties.SEED_TYPES) {
			Set<String> blackList = new LinkedHashSet<String>();
			initBlackListWithPrimitives(blackList);

			Set<String> classNames = new LinkedHashSet<String>();
			classNames.add("java.lang.Object");
			classNames.add("java.lang.String");
			classNames.add("java.lang.Integer");

			for (Entry<Type, Integer> castEntry : CallTreeGenerator.castClassMap.entrySet()) {
				String className = castEntry.getKey().getClassName();
				if(blackList.contains(className))
					continue;
				
				CastClassManager.getInstance().addCastClass(className,
				                                            castEntry.getValue());
				classNames.add(castEntry.getKey().getClassName());
			}

			// If SEED_TYPES is false, only Object is a cast class
			logger.info("Handling cast classes");
			addCastClasses(classNames, blackList);

		}

	}

	private static void gatherStatistics() {
		ClientServices.getInstance().getClientNode().trackOutputVariable("analyzed_classes",
		                                                                 analyzedClasses.size());
		ClientServices.getInstance().getClientNode().trackOutputVariable("generators",
		                                                                 TestCluster.getInstance().getGenerators().size());
		ClientServices.getInstance().getClientNode().trackOutputVariable("modifiers",
		                                                                 TestCluster.getInstance().getModifiers().size());
	}

	private static void initBlackListWithPrimitives(Set<String> blackList)
	        throws NullPointerException {
		blackList.add("int");
		blackList.add("short");
		blackList.add("float");
		blackList.add("double");
		blackList.add("byte");
		blackList.add("char");
		blackList.add("boolean");
		blackList.add("long");
		blackList.add("java.lang.Enum");
		blackList.add("java.lang.String");
		blackList.add("java.lang.Class");
	}

	private static void addCastClasses(Set<String> castClasses, Set<String> blackList) {
		logger.info("Cast classes: " + castClasses);
		for (String className : castClasses) {
			if (blackList.contains(className) && !className.equals("java.lang.String")) {
				logger.info("Cast class in blacklist: " + className);
				continue;
			}
			try {
				Class<?> clazz = TestGenerationContext.getClassLoader().loadClass(className);
				boolean added = addDependencyClass(new GenericClass(clazz), 1);
				genericCastClasses.add(new GenericClass(clazz));
				concreteCastClasses.add(clazz);
				if (!added) {
					blackList.add(className);
				}
			} catch (ClassNotFoundException e) {
				logger.error("Problem for " + Properties.TARGET_CLASS
				        + ". Class not found", e);
				blackList.add(className);
			}
		}
		logger.info("Generic cast classes: " + genericCastClasses);

	}

	/**
	 * Update
	 * 
	 * @param clazz
	 */
	public static void addCastClassForContainer(Class<?> clazz) {
		if (concreteCastClasses.contains(clazz))
			return;

		concreteCastClasses.add(clazz);
		// TODO: What if this is generic again?
		genericCastClasses.add(new GenericClass(clazz));

		CastClassManager.getInstance().addCastClass(clazz, 1);
		TestCluster.getInstance().clearGeneratorCache(new GenericClass(clazz));
	}

	/**
	 * Continue adding generators for classes that are needed
	 */
	private static void resolveDependencies(Set<String> blackList) {
		while (!dependencies.isEmpty()) {
			logger.debug("Dependencies left: " + dependencies.size());

			Iterator<Pair> iterator = dependencies.iterator();
			Pair dependency = iterator.next();
			iterator.remove();

			if (analyzedClasses.contains(dependency.getDependencyClass().getRawClass())) {
				continue;
			}

			String className = dependency.getDependencyClass().getClassName();
			if (blackList.contains(className)) {
				continue;
			}
			boolean added = false;
			/*
			if (dependency.getDependencyClass().isParameterizedType()) {
				for (List<GenericClass> parameterTypes : getAssignableTypes(dependency.getDependencyClass())) {
					GenericClass copy = new GenericClass(
					        dependency.getDependencyClass().getType());
					copy.setParameterTypes(parameterTypes);
					boolean success = addDependencyClass(copy, dependency.getRecursion());
					if (success)
						added = true;
				}
			} else
			*/
			added = addDependencyClass(dependency.getDependencyClass(),
			                           dependency.getRecursion());
			if (!added) {
				blackList.add(className);
			}
			//}
		}

	}

	public static List<List<GenericClass>> getAssignableTypes(GenericClass clazz) {
		List<List<GenericClass>> tuples = new ArrayList<List<GenericClass>>();
		//logger.info("Parameters of " + clazz.getSimpleName() + ": "
		//        + clazz.getNumParameters());
		boolean first = true;
		for (java.lang.reflect.Type parameterType : clazz.getParameterTypes()) {
			//logger.info("Current parameter: " + parameterType);
			List<GenericClass> assignableClasses = getAssignableTypes(parameterType);
			List<List<GenericClass>> newTuples = new ArrayList<List<GenericClass>>();

			for (GenericClass concreteClass : assignableClasses) {
				if (first) {
					List<GenericClass> tuple = new ArrayList<GenericClass>();
					tuple.add(concreteClass);
					newTuples.add(tuple);
				} else {
					for (List<GenericClass> t : tuples) {
						List<GenericClass> tuple = new ArrayList<GenericClass>(t);
						tuple.add(concreteClass);
						newTuples.add(tuple);
					}
				}
			}
			tuples = newTuples;
			first = false;
		}
		return tuples;
	}

	private static List<GenericClass> getAssignableTypes(java.lang.reflect.Type type) {
		List<GenericClass> types = new ArrayList<GenericClass>();
		for (GenericClass clazz : genericCastClasses) {
			if (clazz.isAssignableTo(type)) {
				logger.debug(clazz + " is assignable to " + type);
				types.add(clazz);
			}
		}
		return types;
	}

	/**
	 * All public methods defined directly in the SUT should be covered
	 * 
	 * TODO: What if we use instrument_parent?
	 * 
	 * @param targetClass
	 */
	@SuppressWarnings("unchecked")
	private static void initializeTargetMethods() throws RuntimeException,
	        ClassNotFoundException {
		if (Properties.VIRTUAL_FS) {
			/*
			 * We need to initialize and temporarily enable the VFS here because Properties.getTargetClass() triggers loading of the target class what
			 * could lead to execution of static code blocks that may contain statements altering the file system!
			 */
			FileSystem.reset();
			EvoSuiteIO.enableVFS();
		}

		logger.info("Analyzing target class");
		Class<?> targetClass = Properties.getTargetClass();

		if (Properties.VIRTUAL_FS) {
			EvoSuiteIO.disableVFS(); // disable it again until test case execution
		}

		TestCluster cluster = TestCluster.getInstance();

		Set<Class<?>> targetClasses = new LinkedHashSet<Class<?>>();
		if (targetClass == null) {
			throw new RuntimeException("Failed to load " + Properties.TARGET_CLASS);
		}
		targetClasses.add(targetClass);
		for (Class<?> c : targetClass.getDeclaredClasses()) {
			logger.info("Adding declared class " + c);
			targetClasses.add(c);
		}
		if (Modifier.isAbstract(targetClass.getModifiers())) {
			logger.info("SUT is an abstract class");
			Set<Class<?>> subclasses = getConcreteClasses(targetClass);
			logger.info("Found " + subclasses.size() + " concrete subclasses");
			targetClasses.addAll(subclasses);
		}

		// To make sure we also have anonymous inner classes double check inner classes using ASM
		ClassNode targetClassNode = DependencyAnalysis.getClassNode(Properties.TARGET_CLASS);
		Queue<InnerClassNode> innerClasses = new LinkedList<InnerClassNode>();
		innerClasses.addAll(targetClassNode.innerClasses);
		while (!innerClasses.isEmpty()) {
			InnerClassNode icn = innerClasses.poll();
			try {
				logger.debug("Loading inner class: " + icn.innerName + ", " + icn.name
				        + "," + icn.outerName);
				String innerClassName = Utils.getClassNameFromResourcePath(icn.name);
				Class<?> innerClass = TestGenerationContext.getClassLoader().loadClass(innerClassName);
				if (!targetClasses.contains(innerClass)) {
					logger.info("Adding inner class " + innerClassName);
					targetClasses.add(innerClass);
					ClassNode innerClassNode = DependencyAnalysis.getClassNode(innerClassName);
					innerClasses.addAll(innerClassNode.innerClasses);
				}

			} catch (Throwable t) {
				logger.error("Problem for " + Properties.TARGET_CLASS
				        + ". Error loading inner class: " + icn.innerName + ", "
				        + icn.name + "," + icn.outerName + ": " + t);
			}
		}

		for (Class<?> clazz : targetClasses) {
			logger.info("Current SUT class: " + clazz);

			// Add all constructors
			for (Constructor<?> constructor : getConstructors(clazz)) {
				logger.info("Checking target constructor " + constructor);
				String name = "<init>"
				        + org.objectweb.asm.Type.getConstructorDescriptor(constructor);

				if (Properties.TT) {
					String orig = name;
					name = BooleanTestabilityTransformation.getOriginalNameDesc(clazz.getName(),
					                                                            "<init>",
					                                                            org.objectweb.asm.Type.getConstructorDescriptor(constructor));
					if (!orig.equals(name))
						logger.info("TT name: " + orig + " -> " + name);

				}

				if (canUse(constructor)) {
					GenericConstructor genericConstructor = new GenericConstructor(
					        constructor, clazz);
					cluster.addTestCall(genericConstructor);
					// TODO: Add types!
					cluster.addGenerator(new GenericClass(clazz), genericConstructor);
					addDependencies(genericConstructor, 1);
					logger.debug("Keeping track of "
					        + constructor.getDeclaringClass().getName()
					        + "."
					        + constructor.getName()
					        + org.objectweb.asm.Type.getConstructorDescriptor(constructor));
				} else {
					logger.debug("Constructor cannot be used: " + constructor);
				}

			}

			// Add all methods
			for (Method method : getMethods(clazz)) {
				logger.info("Checking target method " + method);
				String name = method.getName()
				        + org.objectweb.asm.Type.getMethodDescriptor(method);

				if (Properties.TT) {
					String orig = name;
					name = BooleanTestabilityTransformation.getOriginalNameDesc(clazz.getName(),
					                                                            method.getName(),
					                                                            org.objectweb.asm.Type.getMethodDescriptor(method));
					if (!orig.equals(name))
						logger.info("TT name: " + orig + " -> " + name);
				}

				if (canUse(method)) {
					logger.debug("Adding method " + clazz.getName() + "."
					        + method.getName()
					        + org.objectweb.asm.Type.getMethodDescriptor(method));

					GenericMethod genericMethod = new GenericMethod(method, clazz);
					cluster.addTestCall(genericMethod);
					cluster.addModifier(new GenericClass(clazz), genericMethod);
					addDependencies(genericMethod, 1);
					GenericClass retClass = new GenericClass(method.getReturnType());

					if (!retClass.isPrimitive() && !retClass.isVoid()
					        && !retClass.isObject())
						cluster.addGenerator(retClass, genericMethod);
				} else {
					logger.debug("Method cannot be used: " + method);
				}
			}

			for (Field field : getFields(clazz)) {
				logger.info("Checking target field " + field);

				if (canUse(field)) {
					GenericField genericField = new GenericField(field, clazz);
					addDependencies(genericField, 1);
					cluster.addGenerator(new GenericClass(field.getGenericType()),
					                     genericField);
					if (!Modifier.isFinal(field.getModifiers())) {
						cluster.addTestCall(new GenericField(field, clazz));
					}
				}
			}
			analyzedClasses.add(clazz);
			// TODO: Set to generic type rather than class?
			cluster.getAnalyzedClasses().add(clazz);
		}
		if (Properties.INSTRUMENT_PARENT) {
			for (String superClass : inheritanceTree.getSuperclasses(Properties.TARGET_CLASS)) {
				try {
					Class<?> superClazz = TestGenerationContext.getClassLoader().loadClass(superClass);
					dependencies.add(new Pair(0, superClazz));
				} catch (ClassNotFoundException e) {
					logger.error("Problem for " + Properties.TARGET_CLASS
					        + ". Class not found: " + superClass, e);
				}

			}
		}

		logger.info("Finished analyzing target class");
	}

	/**
	 * Get the set of constructors defined in this class and its superclasses
	 * 
	 * @param clazz
	 * @return
	 */
	public static Set<Constructor<?>> getConstructors(Class<?> clazz) {
		Map<String, Constructor<?>> helper = new TreeMap<String, Constructor<?>>();

		Set<Constructor<?>> constructors = new LinkedHashSet<Constructor<?>>();
		try {
			for (Constructor<?> c : clazz.getDeclaredConstructors()) {
				helper.put(org.objectweb.asm.Type.getConstructorDescriptor(c), c);
			}
		} catch (Throwable t) {
			logger.info("Error while analyzing class " + clazz + ": " + t);
		}
		for (Constructor<?> c : helper.values()) {
			constructors.add(c);
		}
		return constructors;
	}

	/**
	 * Get the set of methods defined in this class and its superclasses
	 * 
	 * @param clazz
	 * @return
	 */
	public static Set<Method> getMethods(Class<?> clazz) {

		Map<String, Method> helper = new TreeMap<String, Method>();

		if (clazz.getSuperclass() != null) {
			for (Method m : getMethods(clazz.getSuperclass())) {
				helper.put(m.getName() + org.objectweb.asm.Type.getMethodDescriptor(m), m);
			}
		}
		for (Class<?> in : clazz.getInterfaces()) {
			for (Method m : getMethods(in)) {
				helper.put(m.getName() + org.objectweb.asm.Type.getMethodDescriptor(m), m);
			}
		}

		try {
			for (Method m : clazz.getDeclaredMethods()) {
				helper.put(m.getName() + org.objectweb.asm.Type.getMethodDescriptor(m), m);
			}
		} catch (NoClassDefFoundError e) {
			// TODO: What shall we do?
			logger.info("Error while trying to load methods of class " + clazz.getName()
			        + ": " + e);
		}

		Set<Method> methods = new LinkedHashSet<Method>();
		methods.addAll(helper.values());
		return methods;
	}

	/**
	 * Get the set of fields defined in this class and its superclasses
	 * 
	 * @param clazz
	 * @return
	 */
	public static Set<Field> getFields(Class<?> clazz) {
		// TODO: Helper not necessary here!
		Map<String, Field> helper = new TreeMap<String, Field>();

		Set<Field> fields = new LinkedHashSet<Field>();
		if (clazz.getSuperclass() != null) {
			for (Field f : getFields(clazz.getSuperclass())) {
				helper.put(f.toGenericString(), f);
			}

		}
		for (Class<?> in : clazz.getInterfaces()) {
			for (Field f : getFields(in)) {
				helper.put(f.toGenericString(), f);
			}
		}

		try {
			for (Field f : clazz.getDeclaredFields()) {
				helper.put(f.toGenericString(), f);
			}
		} catch (NoClassDefFoundError e) {
			// TODO: What shall we do?
			logger.info("Error while trying to load fields of class " + clazz.getName()
			        + ": " + e);
		}
		fields.addAll(helper.values());

		return fields;
	}

	/**
	 * Get the set of fields defined in this class and its superclasses
	 * 
	 * @param clazz
	 * @return
	 */
	public static Set<Field> getAccessibleFields(Class<?> clazz) {
		Set<Field> fields = new LinkedHashSet<Field>();
		try {
			for (Field f : clazz.getFields()) {
				if (canUse(f) && !Modifier.isFinal(f.getModifiers())) {
					fields.add(f);
				}
			}
		} catch (Throwable t) {
			logger.info("Error while accessing fields of class " + clazz.getName()
			        + " - check allowed permissions: " + t);
		}
		return fields;
	}

	private static boolean isEvoSuiteClass(Class<?> c) {
		return c.getName().startsWith("org.evosuite")
		        || c.getName().startsWith("edu.uta.cse.dsc")
		        || c.getName().equals("java.lang.String");
	}

	private static boolean canUse(Class<?> c) {
		if (Throwable.class.isAssignableFrom(c))
			return false;
		if (Modifier.isPrivate(c.getModifiers()))
			return false;

		if (!Properties.USE_DEPRECATED && c.isAnnotationPresent(Deprecated.class)) {
			logger.debug("Skipping deprecated class " + c.getName());
			return false;
		}

		if (c.getName().matches(".*\\$\\d+$")) {
			logger.debug(c + " looks like an anonymous class, ignoring it");
			return false;
		}

		if (c.getName().startsWith("junit"))
			return false;

		if (isEvoSuiteClass(c))
			return false;

		if (Modifier.isPublic(c.getModifiers())) {
			return true;
		}

		return false;
	}

	public static boolean canUse(Field f) {

		// TODO we could enable some methods from Object, like getClass
		if (f.getDeclaringClass().equals(java.lang.Object.class))
			return false;// handled here to avoid printing reasons

		if (f.getDeclaringClass().equals(java.lang.Thread.class))
			return false;// handled here to avoid printing reasons

		if (!Properties.USE_DEPRECATED && f.isAnnotationPresent(Deprecated.class)) {
			logger.debug("Skipping deprecated field " + f.getName());
			return false;
		}

		if (f.isSynthetic()) {
			logger.debug("Skipping synthetic field " + f.getName());
			return false;
		}

		if (f.getName().startsWith("ajc$")) {
			logger.debug("Skipping AspectJ field " + f.getName());
			return false;
		}

		if (!f.getType().equals(String.class) && !canUse(f.getType())) {
			return false;
		}

		if (Modifier.isPublic(f.getModifiers())) {
			// It may still be the case that the field is defined in a non-visible superclass of the class
			// we already know we can use. In that case, the compiler would be fine with accessing the 
			// field, but reflection would start complaining about IllegalAccess!
			// Therefore, we set the field accessible to be on the safe side
			f.setAccessible(true);
			return true;
		}

		return false;
	}

	private static boolean canUse(Method m) {

		if (m.isBridge()) {
			logger.debug("Excluding bridge method: " + m.toString());
			return false;
		}

		if (m.isSynthetic()) {
			logger.debug("Excluding synthetic method: " + m.toString());
			return false;
		}

		if (!Properties.USE_DEPRECATED && m.isAnnotationPresent(Deprecated.class)) {
			logger.debug("Excluding deprecated method " + m.getName());
			return false;
		}

		if (m.isAnnotationPresent(Test.class)) {
			logger.debug("Excluding test method " + m.getName());
			return false;
		}

		if (m.getDeclaringClass().equals(java.lang.Object.class)) {
			return false;
		}

		if (!m.getReturnType().equals(String.class) && !canUse(m.getReturnType())) {
			return false;
		}

		if (m.getDeclaringClass().equals(Enum.class)) {
			if (m.getName().equals("valueOf") || m.getName().equals("values")
			        || m.getName().equals("ordinal")) {
				logger.debug("Excluding valueOf for Enum " + m.toString());
				return false;
			}
			// Skip compareTo on enums (like Randoop)
			if (m.getName().equals("compareTo") && m.getParameterTypes().length == 1
			        && m.getParameterTypes()[0].equals(Enum.class))
				return false;
		}

		if (m.getDeclaringClass().equals(java.lang.Thread.class))
			return false;

		// Randoop special case
		// We include hashCode unless it is Object.hashCode, a case that is handled above
		// if (m.getName().equals("hashCode") && !m.getDeclaringClass().equals(String.class))
		//	return false;

		// Randoop special case: just clumps together a bunch of hashCodes, so skip it
		if (m.getName().equals("deepHashCode")
		        && m.getDeclaringClass().equals(Arrays.class))
			return false;

		// Randoop special case: differs too much between JDK installations
		if (m.getName().equals("getAvailableLocales"))
			return false;

		if (m.getName().equals("__STATIC_RESET")) {
			logger.debug("Ignoring static reset class");
			return false;
		}

		if (m.getName().equals("main") && Modifier.isStatic(m.getModifiers())
		        && Modifier.isPublic(m.getModifiers())) {
			logger.debug("Ignoring static main method ");
			return false;
		}

		/*
		if(m.getTypeParameters().length > 0) {
			logger.debug("Cannot handle generic methods at this point");
			if(m.getDeclaringClass().equals(Properties.getTargetClass())) {
				LoggingUtils.getEvoLogger().info("* Skipping method "+m.getName()+": generic methods are not handled yet");
			}
			return false;
		}
		*/

		// If default or
		if (Modifier.isPublic(m.getModifiers()))
			return true;

		return false;
	}

	private static boolean canUse(Constructor<?> c) {

		if (c.isSynthetic()) {
			return false;
		}

		// synthetic constructors are OK
		if (Modifier.isAbstract(c.getDeclaringClass().getModifiers()))
			return false;

		// TODO we could enable some methods from Object, like getClass
		//if (c.getDeclaringClass().equals(java.lang.Object.class))
		//	return false;// handled here to avoid printing reasons

		if (c.getDeclaringClass().equals(java.lang.Thread.class))
			return false;// handled here to avoid printing reasons

		if (c.getDeclaringClass().isAnonymousClass())
			return false;

		if (c.getDeclaringClass().isLocalClass()) {
			logger.debug("Skipping constructor of local class " + c.getName());
			return false;
		}

		if (c.getDeclaringClass().isMemberClass()
		        && !Modifier.isPublic(c.getDeclaringClass().getModifiers()))
			return false;

		if (!Properties.USE_DEPRECATED && c.getAnnotation(Deprecated.class) != null) {
			logger.debug("Skipping deprecated constructor " + c.getName());
			return false;
		}

		if (Modifier.isPublic(c.getModifiers()))
			return true;
		return false;
	}

	private static Set<Class<?>> analyzedClasses = new LinkedHashSet<Class<?>>();

	private static class Pair {
		private final int recursion;
		private final GenericClass dependencyClass;

		public Pair(int recursion, java.lang.reflect.Type dependencyClass) {
			this.recursion = recursion;
			this.dependencyClass = new GenericClass(dependencyClass);
		}

		public int getRecursion() {
			return recursion;
		}

		public GenericClass getDependencyClass() {
			return dependencyClass;
		}

	};

	private static Set<Pair> dependencies = new LinkedHashSet<Pair>();

	private static InheritanceTree inheritanceTree = null;

	private static void addDependencies(GenericConstructor constructor, int recursionLevel) {
		if (recursionLevel > Properties.CLUSTER_RECURSION) {
			logger.debug("Maximum recursion level reached, not adding dependencies of {}",
			             constructor);
			return;
		}

		if (dependencyCache.contains(constructor)) {
			return;
		}

		logger.debug("Analyzing dependencies of " + constructor);
		dependencyCache.add(constructor);

		for (java.lang.reflect.Type parameterClass : constructor.getRawParameterTypes()) {
			logger.debug("Adding dependency " + parameterClass);
			addDependency(new GenericClass(parameterClass), recursionLevel);
		}

	}

	private static void addDependencies(GenericMethod method, int recursionLevel) {
		if (recursionLevel > Properties.CLUSTER_RECURSION) {
			logger.debug("Maximum recursion level reached, not adding dependencies of {}",
			             method);
			return;
		}

		if (dependencyCache.contains(method)) {
			return;
		}

		logger.debug("Analyzing dependencies of " + method);
		dependencyCache.add(method);

		for (java.lang.reflect.Type parameter : method.getRawParameterTypes()) {
			logger.debug("Current parameter " + parameter);
			GenericClass parameterClass = new GenericClass(parameter);
			if (parameterClass.isPrimitive() || parameterClass.isString())
				continue;

			logger.debug("Adding dependency " + parameterClass.getClassName());
			addDependency(parameterClass, recursionLevel);
		}

	}

	private static void addDependencies(GenericField field, int recursionLevel) {
		if (recursionLevel > Properties.CLUSTER_RECURSION) {
			logger.debug("Maximum recursion level reached, not adding dependencies of {}",
			             field);
			return;
		}

		if (dependencyCache.contains(field)) {
			return;
		}

		if (field.getField().getType().isPrimitive()
		        || field.getField().getType().equals(String.class))
			return;

		logger.debug("Analyzing dependencies of " + field);
		dependencyCache.add(field);

		logger.debug("Adding dependency " + field.getName());
		addDependency(new GenericClass(field.getGenericFieldType()), recursionLevel);

	}

	private static void addDependency(GenericClass clazz, int recursionLevel) {

		clazz = clazz.getRawGenericClass();

		if (analyzedClasses.contains(clazz.getRawClass()))
			return;

		if (clazz.isPrimitive())
			return;

		if (clazz.isString())
			return;

		if (clazz.getRawClass().equals(Enum.class))
			return;

		if (clazz.isArray()) {
			addDependency(new GenericClass(clazz.getComponentType()), recursionLevel);
			return;
		}

		if (!canUse(clazz.getRawClass()))
			return;

		if (!checkIfCanUse(clazz.getClassName()))
			return;

		for (Pair pair : dependencies) {
			if (pair.getDependencyClass().equals(clazz)) {
				return;
			}
		}

		logger.debug("Getting concrete classes for " + clazz.getClassName());
		ConstantPoolManager.getInstance().addNonSUTConstant(Type.getType(clazz.getRawClass()));
		List<Class<?>> actualClasses = new ArrayList<Class<?>>(getConcreteClasses(clazz.getRawClass()));
		// Randomness.shuffle(actualClasses);
		logger.debug("Concrete classes for " + clazz.getClassName() + ": "
		        + actualClasses.size());
		//dependencies.add(new Pair(recursionLevel, Randomness.choice(actualClasses)));

		for (Class<?> targetClass : actualClasses) {
			logger.debug("Adding concrete class: " + targetClass);
			dependencies.add(new Pair(recursionLevel, targetClass));
			//if(++num >= Properties.NUM_CONCRETE_SUBTYPES)
			//	break;
		}
	}

	private static boolean addDependencyClass(GenericClass clazz, int recursionLevel) {
		if (recursionLevel > Properties.CLUSTER_RECURSION) {
			logger.debug("Maximum recursion level reached, not adding dependency {}",
			             clazz.getClassName());
			return false;
		}

		clazz = clazz.getRawGenericClass();

		if (analyzedClasses.contains(clazz.getRawClass())) {
			return true;
		}
		analyzedClasses.add(clazz.getRawClass());

		// We keep track of generic containers in case we find other concrete generic components during runtime
		if (clazz.isAssignableTo(Collection.class) || clazz.isAssignableTo(Map.class)) {
			if (clazz.getNumParameters() > 0) {
				containerClasses.add(clazz.getRawClass());
			}
		}

		try {
			TestCluster cluster = TestCluster.getInstance();
			logger.debug("Adding dependency class " + clazz.getClassName());

			// TODO: Should we include declared classes as well?

			if (!canUse(clazz.getRawClass())) {
				logger.info("*** Cannot use class: " + clazz.getClassName());
				return false;
			}

			// Add all constructors
			for (Constructor<?> constructor : getConstructors(clazz.getRawClass())) {
				String name = "<init>"
				        + org.objectweb.asm.Type.getConstructorDescriptor(constructor);

				if (Properties.TT) {
					String orig = name;
					name = BooleanTestabilityTransformation.getOriginalNameDesc(clazz.getClassName(),
					                                                            "<init>",
					                                                            org.objectweb.asm.Type.getConstructorDescriptor(constructor));
					if (!orig.equals(name))
						logger.info("TT name: " + orig + " -> " + name);

				}

				if (canUse(constructor)) {
					GenericConstructor genericConstructor = new GenericConstructor(
					        constructor, clazz);
					cluster.addGenerator(clazz, genericConstructor);
					addDependencies(genericConstructor, recursionLevel + 1);
					logger.debug("Keeping track of "
					        + constructor.getDeclaringClass().getName()
					        + "."
					        + constructor.getName()
					        + org.objectweb.asm.Type.getConstructorDescriptor(constructor));
				} else {
					logger.debug("Constructor cannot be used: " + constructor);
				}

			}

			// Add all methods
			for (Method method : getMethods(clazz.getRawClass())) {
				String name = method.getName()
				        + org.objectweb.asm.Type.getMethodDescriptor(method);

				if (Properties.TT) {
					String orig = name;
					name = BooleanTestabilityTransformation.getOriginalNameDesc(clazz.getClassName(),
					                                                            method.getName(),
					                                                            org.objectweb.asm.Type.getMethodDescriptor(method));
					if (!orig.equals(name))
						logger.info("TT name: " + orig + " -> " + name);
				}

				if (canUse(method)) {
					logger.debug("Adding method " + clazz.getClassName() + "."
					        + method.getName()
					        + org.objectweb.asm.Type.getMethodDescriptor(method));
					if (method.getTypeParameters().length > 0) {
						logger.info("Type parameters in methods are not handled yet, skipping "
						        + method);
						continue;
					}
					GenericMethod genericMethod = new GenericMethod(method, clazz);

					addDependencies(genericMethod, recursionLevel + 1);
					cluster.addModifier(clazz, genericMethod);
					//					GenericClass retClass = new GenericClass(
					//					        genericMethod.getReturnType(), method.getReturnType());
					GenericClass retClass = new GenericClass(method.getReturnType());

					if (!retClass.isPrimitive() && !retClass.isVoid()
					        && !retClass.isObject()) {
						cluster.addGenerator(retClass, genericMethod);
					}
				} else {
					logger.debug("Method cannot be used: " + method);
				}
			}

			// Add all fields
			for (Field field : getFields(clazz.getRawClass())) {
				if (canUse(field)) {
					// logger.info("Adding field " + classname + "." +
					// field.getName());
					GenericField genericField = new GenericField(field, clazz);
					cluster.addGenerator(new GenericClass(field.getGenericType()),
					                     genericField);
					if (!Modifier.isFinal(field.getModifiers())) {
						cluster.addModifier(clazz, genericField);
						addDependencies(genericField, recursionLevel + 1);
					}
				}
			}
			logger.info("Finished analyzing " + clazz.getTypeName()
			        + " at recursion level " + recursionLevel);
			cluster.getAnalyzedClasses().add(clazz.getRawClass());
		} catch (Throwable t) {
			/*
			 * NOTE: this is a problem we know it can happen in some cases in SF110, but don't
			 * have a real solution now. As it is bound to happen, we try to minimize the logging (eg no
			 * stack trace), although we still need to log it
			 */
			logger.error("Problem for " + Properties.TARGET_CLASS
			        + ". Failed to add dependencies for class " + clazz.getClassName()
			        + ": " + t + "\n" + Arrays.asList(t.getStackTrace()));

			return false;
		}
		return true;
	}

	private static Set<Class<?>> getConcreteClasses(Class<?> clazz) {

		// Some special cases
		if (clazz.equals(java.util.Map.class))
			return getConcreteClassesMap();
		else if (clazz.equals(java.util.List.class))
			return getConcreteClassesList();
		else if (clazz.equals(java.util.Collection.class))
			return getConcreteClassesList();
		else if (clazz.equals(java.util.Iterator.class))
			// We don't want to explicitly create iterators
			// This would only pull in java.util.Scanner, the only
			// concrete subclass
			return new LinkedHashSet<Class<?>>();
		else if (clazz.equals(java.util.ListIterator.class))
			// We don't want to explicitly create iterators
			return new LinkedHashSet<Class<?>>();
		else if (clazz.equals(java.io.Serializable.class))
			return new LinkedHashSet<Class<?>>();
		else if (clazz.equals(java.lang.Comparable.class))
			return getConcreteClassesComparable();
		else if (clazz.equals(java.util.Comparator.class))
			return new LinkedHashSet<Class<?>>();

		Set<Class<?>> actualClasses = new LinkedHashSet<Class<?>>();
		if (Modifier.isAbstract(clazz.getModifiers())
		        || Modifier.isInterface(clazz.getModifiers())) {
			Set<String> subClasses = inheritanceTree.getSubclasses(clazz.getName());
			logger.debug("Subclasses of " + clazz.getName() + ": " + subClasses);
			Map<String, Integer> classDistance = new HashMap<String, Integer>();
			int maxDistance = -1;
			for (String subClass : subClasses) {
				int distance = getPackageDistance(subClass, clazz.getName());
				classDistance.put(subClass, distance);
				maxDistance = Math.max(distance, maxDistance);
			}
			int distance = 0;
			while (actualClasses.isEmpty() && distance <= maxDistance) {
				logger.debug(" Current distance: " + distance);
				for (String subClass : subClasses) {
					if (classDistance.get(subClass) == distance) {
						try {
							Class<?> subClazz = Class.forName(subClass,
							                                  false,
							                                  TestGenerationContext.getClassLoader());
							if (!canUse(subClazz))
								continue;
							if (subClazz.isInterface())
								continue;
							if (Modifier.isAbstract(subClazz.getModifiers()))
								continue;
							actualClasses.add(subClazz);

						} catch (ClassNotFoundException e) {
							logger.error("Problem for " + Properties.TARGET_CLASS
							        + ". Class not found: " + subClass, e);
							logger.error("Removing class from inheritance tree");
							inheritanceTree.removeClass(subClass);
						}
					}
				}
				distance++;
			}
			if (actualClasses.isEmpty()) {
				logger.info("Don't know how to instantiate abstract class "
				        + clazz.getName());
			}
		} else {
			actualClasses.add(clazz);
		}

		logger.debug("Subclasses of " + clazz.getName() + ": " + actualClasses);
		return actualClasses;
	}

	private static Set<Class<?>> getConcreteClassesMap() {
		Set<Class<?>> mapClasses = new LinkedHashSet<Class<?>>();
		Class<?> mapClazz;
		try {
			mapClazz = Class.forName("java.util.HashMap", false,
			                         TestGenerationContext.getClassLoader());
			mapClasses.add(mapClazz);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mapClasses;
	}

	private static Set<Class<?>> getConcreteClassesList() {
		Set<Class<?>> mapClasses = new LinkedHashSet<Class<?>>();
		Class<?> mapClazz;
		try {
			mapClazz = Class.forName("java.util.LinkedList", false,
			                         TestGenerationContext.getClassLoader());
			mapClasses.add(mapClazz);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mapClasses;
	}

	private static Set<Class<?>> getConcreteClassesComparable() {
		Set<Class<?>> comparableClasses = new LinkedHashSet<Class<?>>();
		Class<?> comparableClazz;
		try {
			comparableClazz = Class.forName("java.lang.Integer", false,
			                                TestGenerationContext.getClassLoader());
			comparableClasses.add(comparableClazz);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return comparableClasses;
	}

	/**
	 * Calculate package distance between two classnames
	 * 
	 * @param className1
	 * @param className2
	 * @return
	 */
	private static int getPackageDistance(String className1, String className2) {
		String[] package1 = className1.split("\\.|\\$");
		String[] package2 = className2.split("\\.|\\$");

		int distance = 0;
		int same = 1;
		int num = 0;
		while (num < package1.length && num < package2.length
		        && package1[num].equals(package2[num])) {
			same++;
			num++;
		}

		if (package1.length > same)
			distance += package1.length - same;

		if (package2.length > same)
			distance += package2.length - same;

		return distance;
	}

}
