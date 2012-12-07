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
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.graphs.cfg.CFGMethodAdapter;
import org.evosuite.javaagent.BooleanTestabilityTransformation;
import org.evosuite.runtime.FileSystem;
import org.evosuite.testcase.GenericClass;
import org.evosuite.utils.LoggingUtils;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
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

	private static Set<AccessibleObject> dependencyCache = new HashSet<AccessibleObject>();
	
	@SuppressWarnings("unchecked")
	public static void generateCluster(String targetClass,
	        InheritanceTree inheritanceTree, CallTree callTree) throws RuntimeException,
	        ClassNotFoundException {

		TestClusterGenerator.inheritanceTree = inheritanceTree;
		TestCluster.setInheritanceTree(inheritanceTree);
		initializeTargetMethods();

		if (Properties.INSTRUMENT_CONTEXT || Properties.CRITERION == Criterion.DEFUSE) {
			for (String callTreeClass : DependencyAnalysis.getCallTree().getClasses()) {
				try {
					TestGenerationContext.getClassLoader().loadClass(callTreeClass);
				} catch (ClassNotFoundException e) {
					logger.info("Class not found: " + callTreeClass);
				}
			}
		}

		Set<Type> castClasses = new HashSet<Type>();
		Set<Type> parameterClasses = new HashSet<Type>();
		Set<Type> callTreeClasses = new HashSet<Type>();
		Set<Type> invokedClasses = new HashSet<Type>();
		Set<Type> subClasses = new HashSet<Type>();

		for (ClassNode classNode : DependencyAnalysis.getAllClassNodes()) {
			if (classNode == null)
				continue;

			if ((classNode.access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT) {
				logger.info("Found abstract class: " + classNode.name);
				List<String> subs = filterSubclasses(classNode.name,
				                                     inheritanceTree.getSubclasses(classNode.name));
				for (String className : subs) {
					Type subType = Type.getObjectType(className);
					subClasses.add(subType);
				}
			}

			callTreeClasses.add(Type.getObjectType(classNode.name));

			List<MethodNode> methods = classNode.methods;
			for (MethodNode methodNode : methods) {
				// Parameter dependencies
				parameterClasses.addAll(Arrays.asList(Type.getArgumentTypes(methodNode.desc)));

				// Cast dependencies
				InsnList instructions = methodNode.instructions;
				Iterator<AbstractInsnNode> it = instructions.iterator();
				while (it.hasNext()) {
					AbstractInsnNode insn = it.next();
					if (insn.getOpcode() == Opcodes.CHECKCAST) {
						TypeInsnNode typeNode = (TypeInsnNode) insn;
						Type castType = Type.getObjectType(typeNode.desc);
						while(castType.getSort() == Type.ARRAY) {
							castType = castType.getElementType();
						}
						castClasses.add(castType);
					} else if (insn.getOpcode() == Opcodes.INSTANCEOF) {
						TypeInsnNode typeNode = (TypeInsnNode) insn;
						Type castType = Type.getObjectType(typeNode.desc);
						while(castType.getSort() == Type.ARRAY) {
							castType = castType.getElementType();
						}
						castClasses.add(castType);

					} else if (insn instanceof MethodInsnNode) {
						MethodInsnNode methodInsnNode = (MethodInsnNode) insn;
						invokedClasses.add(Type.getObjectType(methodInsnNode.owner));
					}
				}
			}
		}

		// Other classes might have further dependencies which we might need to resolve
		parameterClasses.removeAll(callTreeClasses);

		// TODO: Maybe java.lang.Object should only be assigned one of the castClasses?
		Set<String> classNames = new HashSet<String>();
		classNames.add("java.lang.Object");
		for (Type type : castClasses) {
			classNames.add(type.getClassName());
		}
		
		/*
		 * If we fail to load a class, we skip it, and avoid to try
		 * to load it again (which would result in extra unnecessary logging)
		 */
		Set<String> blackList = new HashSet<String>();
		initBlackListWithPrimitives(blackList);
		
		TestCluster.setCastClasses(classNames);
		addCastClasses(classNames,blackList);

		resolveDependencies(blackList);
		
		if(logger.isDebugEnabled()) {
			logger.debug(TestCluster.getInstance().toString());
		}
		dependencyCache.clear();
	}

	private static void initBlackListWithPrimitives(Set<String> blackList) throws NullPointerException{
		blackList.add("int");
		blackList.add("short");
		blackList.add("float");
		blackList.add("double");
		blackList.add("byte");
		blackList.add("char");
		blackList.add("boolean");
		blackList.add("long");
	}
	
	private static void addCastClasses(Set<String> castClasses, Set<String> blackList) {
		for (String className : castClasses) {
			if(blackList.contains(className)){
				continue;
			}
			try {
				Class<?> clazz = TestGenerationContext.getClassLoader().loadClass(className);
				boolean added = addDependencyClass(clazz, 1); 
				if(!added){
					blackList.add(className);
				}
			} catch (ClassNotFoundException e) {
				logger.error("Problem for "+Properties.TARGET_CLASS+". Class not found",e);
				blackList.add(className);
			}
		}
	}

	/**
	 * Sort the list of classes by their distance to the target class in terms
	 * of package
	 * 
	 * @param targetClass
	 * @param subClasses
	 * @return
	 */
	private static List<String> filterSubclasses(String targetClass,
	        Set<String> subClasses) {
		List<String> subs = new ArrayList<String>();
		subs.addAll(subClasses);
		return subs;
	}

	/**
	 * Continue adding generators for classes that are needed
	 */
	private static void resolveDependencies(Set<String> blackList) {
		dependencies.removeAll(analyzedClasses);

		while (!dependencies.isEmpty()) {
			logger.debug("Dependencies left: " + dependencies.size());

			Iterator<Pair> iterator = dependencies.iterator();
			Pair dependency = iterator.next();
			iterator.remove();
			String className = dependency.getDependencyClass().getName();
			
			if(blackList.contains(className)){
				continue;
			}
			
			boolean added = addDependencyClass(dependency.getDependencyClass(), dependency.getRecursion());
			if(!added){
				blackList.add(className);
			}
			dependencies.removeAll(analyzedClasses);
		}

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

		Set<Class<?>> targetClasses = new HashSet<Class<?>>();
		if (targetClass == null) {
			throw new RuntimeException("Failed to load " + Properties.TARGET_CLASS);
		}
		targetClasses.add(targetClass);
		for (Class<?> c : targetClass.getDeclaredClasses()) {
			logger.info("Adding declared class " + c);
			targetClasses.add(c);
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
				String innerClassName = icn.name.replace('/', '.');
				Class<?> innerClass = TestGenerationContext.getClassLoader().loadClass(innerClassName);
				if (!targetClasses.contains(innerClass)) {
					logger.info("Adding inner class " + innerClassName);
					targetClasses.add(innerClass);
					ClassNode innerClassNode = DependencyAnalysis.getClassNode(innerClassName);
					innerClasses.addAll(innerClassNode.innerClasses);
				}

			} catch (Throwable t) {
				logger.error("Problem for "+Properties.TARGET_CLASS+". Error loading inner class: " + icn.innerName + ", "
				        + icn.name + "," + icn.outerName + ": " + t);
			}
		}

		for (Class<?> clazz : targetClasses) {
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
					cluster.addTestCall(constructor);
					cluster.addGenerator(new GenericClass(clazz), constructor);
					addDependencies(constructor, 1);
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

					cluster.addTestCall(method);
					addDependencies(method, 1);
					GenericClass retClass = new GenericClass(
					        method.getGenericReturnType());

					if (!retClass.isPrimitive() && !retClass.isVoid()
					        && !retClass.isObject())
						cluster.addGenerator(retClass, method);
				} else {
					logger.debug("Method cannot be used: " + method);
				}
			}

			for (Field field : getFields(clazz)) {
				logger.info("Checking target field " + field);

				if (canUse(field)) {
					addDependencies(field, 1);
					cluster.addGenerator(new GenericClass(field.getGenericType()), field);
					if (!Modifier.isFinal(field.getModifiers())) {
						cluster.addTestCall(field);
					}
				}
			}
			analyzedClasses.add(clazz);
			cluster.getAnalyzedClasses().add(clazz);
		}
		if (Properties.INSTRUMENT_PARENT) {
			for (String superClass : inheritanceTree.getSuperclasses(Properties.TARGET_CLASS)) {
				try {
					Class<?> superClazz = TestGenerationContext.getClassLoader().loadClass(superClass);
					dependencies.add(new Pair(0, superClazz));
				} catch (ClassNotFoundException e) {
					logger.error("Problem for "+Properties.TARGET_CLASS+". Class not found: "+superClass,e);
				}

			}
		}

	}

	/**
	 * Get the set of constructors defined in this class and its superclasses
	 * 
	 * @param clazz
	 * @return
	 */
	public static Set<Constructor<?>> getConstructors(Class<?> clazz) {
		Map<String, Constructor<?>> helper = new HashMap<String, Constructor<?>>();

		Set<Constructor<?>> constructors = new HashSet<Constructor<?>>();
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

		Map<String, Method> helper = new HashMap<String, Method>();

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

		Set<Method> methods = new HashSet<Method>();
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
		Map<String, Field> helper = new HashMap<String, Field>();

		Set<Field> fields = new HashSet<Field>();
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
		Set<Field> fields = new HashSet<Field>();
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

		if (Modifier.isPublic(c.getModifiers()))
			return true;

		return false;
	}

	private static boolean canUse(Field f) {

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

		if (Modifier.isPublic(f.getModifiers()))
			return true;

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

		if (m.getDeclaringClass().isEnum()) {
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
		if (m.getName().equals("hashCode") && !m.getDeclaringClass().equals(String.class))
			return false;

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

		if (c.getDeclaringClass().isMemberClass()
		        && !Modifier.isPublic(c.getDeclaringClass().getModifiers()))
			return false;

		if (c.isSynthetic()) {
			logger.debug("Skipping synthetic constructor " + c.getName());
			return false;
		}

		if (!Properties.USE_DEPRECATED && c.getAnnotation(Deprecated.class) != null) {
			logger.debug("Skipping deprecated constructor " + c.getName());
			return false;
		}

		if (Modifier.isPublic(c.getModifiers()))
			return true;
		return false;
	}

	private static Set<Class<?>> analyzedClasses = new HashSet<Class<?>>();

	private static class Pair {private int recursion; private Class<?> dependencyClass;
	
	public Pair(int recursion, Class<?> dependencyClass) {
		this.recursion = recursion;
		this.dependencyClass = dependencyClass;
	}

	public int getRecursion() {
		return recursion;
	}

	public void setRecursion(int recursion) {
		this.recursion = recursion;
	}

	public Class<?> getDependencyClass() {
		return dependencyClass;
	}

	public void setDependencyClass(Class<?> clazz) {
		this.dependencyClass = clazz;
	}};
	
	private static Set<Pair> dependencies = new HashSet<Pair>();

	private static InheritanceTree inheritanceTree = null;

	private static void addDependencies(Constructor<?> constructor, int recursionLevel) {
		if(recursionLevel > Properties.CLUSTER_RECURSION) {
			logger.debug("Maximum recursion level reached, not adding dependencies of {}",  constructor);
			return;
		}
		
		if(dependencyCache.contains(constructor)) {
			return;
		}

		logger.debug("Analyzing dependencies of " + constructor);		
		dependencyCache.add(constructor);
		
		for (Class<?> parameterClass : constructor.getParameterTypes()) {
			logger.debug("Adding dependency " + parameterClass.getName());
			addDependency(parameterClass, recursionLevel);
		}
		
	}

	private static void addDependencies(Method method, int recursionLevel) {
		if(recursionLevel > Properties.CLUSTER_RECURSION) {
			logger.debug("Maximum recursion level reached, not adding dependencies of {}", method);
			return;
		}

		if(dependencyCache.contains(method)) {
			return;
		}

		logger.debug("Analyzing dependencies of " + method);
		dependencyCache.add(method);

		for (Class<?> parameterClass : method.getParameterTypes()) {
			if (parameterClass.isPrimitive() || parameterClass.equals(String.class))
				continue;

			logger.debug("Adding dependency " + parameterClass.getName());
			addDependency(parameterClass, recursionLevel);
		}
		
	}

	private static void addDependencies(Field field, int recursionLevel) {
		if(recursionLevel > Properties.CLUSTER_RECURSION) {
			logger.debug("Maximum recursion level reached, not adding dependencies of {}", field);
			return;
		}

		if(dependencyCache.contains(field)) {
			return;
		}

		if (field.getType().isPrimitive() || field.getType().equals(String.class))
			return;

		logger.debug("Analyzing dependencies of " + field);
		dependencyCache.add(field);

		logger.debug("Adding dependency " + field.getName());
		addDependency(field.getType(), recursionLevel);
		
	}

	private static void addDependency(Class<?> clazz, int recursionLevel) {
		if (analyzedClasses.contains(clazz) || dependencies.contains(clazz))
			return;

		if (clazz.isPrimitive())
			return;

		if (clazz.equals(String.class))
			return;

		if (clazz.isArray()) {
			addDependency(clazz.getComponentType(), recursionLevel);
			return;
		}
		
		if(!checkIfCanUse(clazz.getName()))
			return;

		logger.debug("Getting concrete classes for " + clazz.getName());
		Set<Class<?>> actualClasses = getConcreteClasses(clazz);
		logger.debug("Concrete classes for " + clazz.getName() + ": " + actualClasses);
		for (Class<?> targetClass : actualClasses) {
			dependencies.add(new Pair(recursionLevel, targetClass));
		}
	}

	private static boolean addDependencyClass(Class<?> clazz, int recursionLevel) {
		if(recursionLevel > Properties.CLUSTER_RECURSION) {
			logger.debug("Maximum recursion level reached, not adding dependency {}", clazz.getName());
			return false;
		}

		try{
			TestCluster cluster = TestCluster.getInstance();
			logger.debug("Adding dependency class " + clazz.getName());

			// TODO: Should we include declared classes as well?

			if (!canUse(clazz)) {
				logger.info("*** Cannot use class: " + clazz.getName());
				return false;
			}

			// Add all constructors
			for (Constructor<?> constructor : getConstructors(clazz)) {
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
					cluster.addGenerator(new GenericClass(clazz), constructor);
					addDependencies(constructor, recursionLevel + 1);
					logger.debug("Keeping track of "
							+ constructor.getDeclaringClass().getName() + "."
							+ constructor.getName()
							+ org.objectweb.asm.Type.getConstructorDescriptor(constructor));
				} else {
					logger.debug("Constructor cannot be used: " + constructor);
				}

			}

			// Add all methods
			for (Method method : getMethods(clazz)) {
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
					logger.debug("Adding method " + clazz.getName() + "." + method.getName()
							+ org.objectweb.asm.Type.getMethodDescriptor(method));
					addDependencies(method, recursionLevel + 1);
					cluster.addModifier(clazz, method);
					GenericClass retClass = new GenericClass(method.getGenericReturnType());

					if (!retClass.isPrimitive() && !retClass.isVoid() && !retClass.isObject())
						cluster.addGenerator(retClass, method);
				} else {
					logger.debug("Method cannot be used: " + method);
				}
			}

			// Add all fields
			for (Field field : getFields(clazz)) {
				if (canUse(field)) {
					// logger.info("Adding field " + classname + "." +
					// field.getName());
					cluster.addGenerator(new GenericClass(field.getGenericType()), field);
					if (!Modifier.isFinal(field.getModifiers())) {
						cluster.addModifier(clazz, field);
						addDependencies(field, recursionLevel + 1);
					}
				}
			}
			logger.info("Finished analyzing " + clazz.getName() + " at recursion level "+recursionLevel);
			cluster.getAnalyzedClasses().add(clazz);
			analyzedClasses.add(clazz);
		} catch(Throwable t){
			/*
			 * NOTE: this is a problem we know it can happen in some cases in SF110, but don't
			 * have a real solution now. As it is bound to happen, we try to minimize the logging (eg no
			 * stack trace), although we still need to log it
			 */
			logger.error("Problem for "+Properties.TARGET_CLASS+". Failed to add dependencies for class "+clazz.getName()+": "+t+"\n"+Arrays.asList(t.getStackTrace()));
			
			return false;
		}
		return true;
	}

	private static Set<Class<?>> getConcreteClasses(Class<?> clazz) {

		Set<Class<?>> actualClasses = new HashSet<Class<?>>();
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
							actualClasses.add(subClazz);

						} catch (ClassNotFoundException e) {
							logger.error("Problem for "+Properties.TARGET_CLASS+". Class not found: "+subClass,e);
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
