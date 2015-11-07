/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 *
 */
package org.evosuite.setup;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.TestGenerationContext;
import org.evosuite.TimeController;
import org.evosuite.assertion.CheapPurityAnalyzer;
import org.evosuite.classpath.ResourceList;
import org.evosuite.instrumentation.testability.BooleanTestabilityTransformation;
import org.evosuite.rmi.ClientServices;
import org.evosuite.runtime.mock.MockList;
import org.evosuite.seeding.CastClassAnalyzer;
import org.evosuite.seeding.CastClassManager;
import org.evosuite.seeding.ConstantPoolManager;
import org.evosuite.setup.PutStaticMethodCollector.MethodIdentifier;
import org.evosuite.setup.callgraph.CallGraph;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.statements.FunctionalMockStatement;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.generic.GenericAccessibleObject;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericField;
import org.evosuite.utils.generic.GenericMethod;
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

	private static final List<String> classExceptions = Collections.unmodifiableList(Arrays.asList(new String[] {
	        "com.apple.", "apple.", "sun.", "com.sun.", "com.oracle.", "sun.awt."
	}));


	private final Set<GenericAccessibleObject<?>> dependencyCache = new LinkedHashSet<GenericAccessibleObject<?>>();

	private final static Map<Class<?>, Set<Method>> methodCache = new LinkedHashMap<Class<?>, Set<Method>>();

	private final static Map<Class<?>, Set<Field>> accessibleFieldCache = new LinkedHashMap<Class<?>, Set<Field>>();

	private final Set<GenericClass> genericCastClasses = new LinkedHashSet<GenericClass>();

	private final Set<Class<?>> concreteCastClasses = new LinkedHashSet<Class<?>>();

	private final Set<Class<?>> containerClasses = new LinkedHashSet<Class<?>>();


	private final Set<Pair> dependencies = new LinkedHashSet<Pair>();

	private final Set<GenericClass> analyzedAbstractClasses = new LinkedHashSet<GenericClass>();

	//XXX refactor and move these as paramethers in all methods.
	private InheritanceTree inheritanceTree;
	private CallGraph callGraph;


	/**
	 * Check if we can use the given class directly in a JUnit test
	 *
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public static boolean checkIfCanUse(String className) {

		if (MockList.shouldBeMocked(className)) {
			return false;
		}

		for (String s : classExceptions) {
			if (className.startsWith(s)) {
				return false;
			}
		}
		return true;
	}

	public void generateCluster(String targetClass, InheritanceTree inheritanceTree, CallGraph callGraph) throws RuntimeException, ClassNotFoundException {
		this.inheritanceTree = inheritanceTree;
		this.callGraph = callGraph;
		TestCluster.setInheritanceTree(inheritanceTree);

		if (Properties.INSTRUMENT_CONTEXT || ArrayUtil.contains(Properties.CRITERION, Criterion.DEFUSE)) {
			for (String callTreeClass : callGraph.getClasses()) {
				try {
					if(callGraph.isCalledClass(callTreeClass)){
						if(!Properties.INSTRUMENT_LIBRARIES && !DependencyAnalysis.isTargetProject(callTreeClass)) continue;
						TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(callTreeClass);
					}
				} catch (ClassNotFoundException e) {
					logger.info("Class not found: " + callTreeClass + ": " + e);
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
		Set<String> blackList = new LinkedHashSet<>();
		initBlackListWithEvoSuitePrimitives(blackList);

		logger.info("Handling cast classes");
		handleCastClasses();

		logger.info("Initialising target class");
		initializeTargetMethods();

		logger.info("Resolving dependencies");
		resolveDependencies(blackList);

		TestCluster.getInstance().removeUnusableGenerators();

		if (logger.isDebugEnabled()) {
			logger.debug(TestCluster.getInstance().toString());
		}
		dependencyCache.clear();
		gatherStatistics();
	}

	private void handleCastClasses() {
		// If we include type seeding, then we analyze classes to find types in instanceof and cast instructions
		if (Properties.SEED_TYPES) {
			Set<String> blackList = new LinkedHashSet<>();
			initBlackListWithPrimitives(blackList);

			Set<String> classNames = new LinkedHashSet<>();
			CastClassAnalyzer analyzer = new CastClassAnalyzer();
			Map<Type, Integer> castMap = analyzer.analyze(Properties.TARGET_CLASS);

			for (Entry<Type, Integer> castEntry : castMap.entrySet()) {
				String className = castEntry.getKey().getClassName();
				if (blackList.contains(className))
					continue;

				if (addCastClassDependencyIfAccessible(className, blackList)) {
					CastClassManager.getInstance().addCastClass(className,
					                                            castEntry.getValue());
					classNames.add(castEntry.getKey().getClassName());
				}
			}

			// If SEED_TYPES is false, only Object is a cast class
			// logger.info("Handling cast classes");
			// addCastClasses(classNames, blackList);
			logger.debug("Cast classes used: " + classNames);
		}

	}

	private void gatherStatistics() {
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Analyzed_Classes,
		                                                                 analyzedClasses.size());
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Generators,
		                                                                 TestCluster.getInstance().getGenerators().size());
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Modifiers,
		                                                                 TestCluster.getInstance().getModifiers().size());
	}

	private void initBlackListWithEvoSuitePrimitives(Set<String> blackList)
	        throws NullPointerException {
		blackList.add("int");
		blackList.add("short");
		blackList.add("float");
		blackList.add("double");
		blackList.add("byte");
		blackList.add("char");
		blackList.add("boolean");
		blackList.add("long");
		blackList.add(java.lang.Enum.class.getName());
		blackList.add(java.lang.String.class.getName());
		blackList.add(java.lang.Class.class.getName());
	}

	private void initBlackListWithPrimitives(Set<String> blackList)
	        throws NullPointerException {
		blackList.add("int");
		blackList.add("short");
		blackList.add("float");
		blackList.add("double");
		blackList.add("byte");
		blackList.add("char");
		blackList.add("boolean");
		blackList.add("long");
	}

	private boolean addCastClassDependencyIfAccessible(String className,
	        Set<String> blackList) {
		if (className.equals(java.lang.String.class.getName()))
			return true;

		if (blackList.contains(className)) {
			logger.info("Cast class in blacklist: " + className);
			return false;
		}
		try {
			Class<?> clazz = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(className);
			if (!TestUsageChecker.canUse(clazz)) {
				logger.debug("Cannot use cast class: " + className);
				return false;
			}
			// boolean added =
			addDependency(new GenericClass(clazz), 1);
			genericCastClasses.add(new GenericClass(clazz));
			concreteCastClasses.add(clazz);

			blackList.add(className);
			return true;

		} catch (ClassNotFoundException e) {
			logger.error("Problem for " + Properties.TARGET_CLASS + ". Class not found", e);
			blackList.add(className);
			return false;
		}
	}

	/**
	 * Update
	 *
	 * @param clazz
	 */
	public void addCastClassForContainer(Class<?> clazz) {
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
	private void resolveDependencies(Set<String> blackList) {

		while (!dependencies.isEmpty()
		        && TimeController.getInstance().isThereStillTimeInThisPhase()) {
			logger.debug("Dependencies left: {}", dependencies.size());

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

	public List<List<GenericClass>> getAssignableTypes(GenericClass clazz) {
		List<List<GenericClass>> tuples = new ArrayList<>();
		//logger.info("Parameters of " + clazz.getSimpleName() + ": "
		//        + clazz.getNumParameters());
		boolean first = true;
		for (java.lang.reflect.Type parameterType : clazz.getParameterTypes()) {
			//logger.info("Current parameter: " + parameterType);
			List<GenericClass> assignableClasses = getAssignableTypes(parameterType);
			List<List<GenericClass>> newTuples = new ArrayList<>();

			for (GenericClass concreteClass : assignableClasses) {
				if (first) {
					List<GenericClass> tuple = new ArrayList<>();
					tuple.add(concreteClass);
					newTuples.add(tuple);
				} else {
					for (List<GenericClass> t : tuples) {
						List<GenericClass> tuple = new ArrayList<>(t);
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

	private List<GenericClass> getAssignableTypes(java.lang.reflect.Type type) {
		List<GenericClass> types = new ArrayList<>();
		for (GenericClass clazz : genericCastClasses) {
			if (clazz.isAssignableTo(type)) {
				logger.debug(clazz + " is assignable to " + type);
				types.add(clazz);
			}
		}
		return types;
	}

	private void addDeclaredClasses(Set<Class<?>> targetClasses, Class<?> currentClass) {
		for (Class<?> c : currentClass.getDeclaredClasses()) {
			logger.info("Adding declared class " + c);
			targetClasses.add(c);
			addDeclaredClasses(targetClasses, c);
		}
	}

	/**
	 * All public methods defined directly in the SUT should be covered
	 *
	 * TODO: What if we use instrument_parent?
	 *
	 */
	@SuppressWarnings("unchecked")
	private void initializeTargetMethods() throws RuntimeException,
	        ClassNotFoundException {

		logger.info("Analyzing target class");
		Class<?> targetClass = Properties.getTargetClass();

		TestCluster cluster = TestCluster.getInstance();

		Set<Class<?>> targetClasses = new LinkedHashSet<Class<?>>();
		if (targetClass == null) {
			throw new RuntimeException("Failed to load " + Properties.TARGET_CLASS);
		}
		targetClasses.add(targetClass);
		addDeclaredClasses(targetClasses, targetClass);
		if (Modifier.isAbstract(targetClass.getModifiers())) {
			logger.info("SUT is an abstract class");
			Set<Class<?>> subclasses = getConcreteClasses(targetClass, inheritanceTree);
			logger.info("Found " + subclasses.size() + " concrete subclasses");
			targetClasses.addAll(subclasses);
		}

// load all the interesting classes from the callgraph,
// need more testing, seems to slow down the search
//		if(Properties.INSTRUMENT_CONTEXT){
//			Set<String> toLoad;
//			if(Properties.INSTRUMENT_LIBRARIES){
//				toLoad = callGraph.getClassesUnderTest();
//			}else{
//				toLoad = new HashSet<>();
//				for (String className : callGraph.getClassesUnderTest()) {
//					if (!Properties.INSTRUMENT_LIBRARIES
//							&& !DependencyAnalysis.isTargetProject(className))
//						continue;
//					toLoad.add(className);
//				}
//
//			}
//			targetClasses.addAll(loadClasses(toLoad));
//
//		}

		// To make sure we also have anonymous inner classes double check inner classes using ASM
		ClassNode targetClassNode = DependencyAnalysis.getClassNode(Properties.TARGET_CLASS);
		Queue<InnerClassNode> innerClasses = new LinkedList<InnerClassNode>();
		innerClasses.addAll(targetClassNode.innerClasses);
		while (!innerClasses.isEmpty()) {
			InnerClassNode icn = innerClasses.poll();
			try {
				logger.debug("Loading inner class: " + icn.innerName + ", " + icn.name
				        + "," + icn.outerName);
				String innerClassName = ResourceList.getClassNameFromResourcePath(icn.name);
				Class<?> innerClass = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(innerClassName);
				//if (!canUse(innerClass))
				//	continue;

				// Sometimes strange things appear such as Map$Entry
				if (!targetClasses.contains(innerClass)) {
					//						&& !innerClassName.matches(".*\\$\\d+(\\$.*)?$")) {

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

			if (!TestUsageChecker.canUse(clazz)) {
				logger.info("Cannot access SUT class: " + clazz);
				continue;
			}

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

				if (TestUsageChecker.canUse(constructor)) {
					GenericConstructor genericConstructor = new GenericConstructor(
					        constructor, clazz);
					cluster.addTestCall(genericConstructor);
					// TODO: Add types!
					cluster.addGenerator(new GenericClass(clazz), //.getWithWildcardTypes(),
					                     genericConstructor);
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

				if (TestUsageChecker.canUse(method, clazz)) {
					logger.debug("Adding method " + clazz.getName() + "."
					        + method.getName()
					        + org.objectweb.asm.Type.getMethodDescriptor(method));

					GenericMethod genericMethod = new GenericMethod(method, clazz);
					cluster.addTestCall(genericMethod);
					// TODO: We could restrict this to pure methods here
					//       but for SUT classes without impure methods
					//       this can affect the chances of covering the targets
					//       so for now we keep all pure methods.
					//       In the long run, covered methods maybe should be
					//       removed?
					cluster.addModifier(new GenericClass(clazz),
							genericMethod);
					addDependencies(genericMethod, 1);
					GenericClass retClass = new GenericClass(method.getReturnType());

					if (!retClass.isPrimitive() && !retClass.isVoid()
					        && !retClass.isObject())
						cluster.addGenerator(retClass, //.getWithWildcardTypes(),
						                     genericMethod);
				} else {
					logger.debug("Method cannot be used: " + method);
				}
			}

			for (Field field : getFields(clazz)) {
				logger.info("Checking target field " + field);

				if (TestUsageChecker.canUse(field, clazz)) {
					GenericField genericField = new GenericField(field, clazz);

					addDependencies(genericField, 1);
					cluster.addGenerator(new GenericClass(field.getGenericType()), //.getWithWildcardTypes(),
					                     genericField);
					logger.debug("Adding field " + field);
					if (!Modifier.isFinal(field.getModifiers())) {
						logger.debug("Is not final");
						cluster.addTestCall(new GenericField(field, clazz));
						cluster.addModifier(new GenericClass(clazz),
			                    genericField);
					} else {
						logger.debug("Is final");
						if (Modifier.isStatic(field.getModifiers())
						        && !field.getType().isPrimitive()) {
							logger.debug("Is static non-primitive");
							/*
							 * With this we are trying to cover such cases:
							 *
							public static final DurationField INSTANCE = new MillisDurationField();

							private MillisDurationField() {
							super();
							}
							 */
							try {
								Object o = field.get(null);
								if (o == null) {
									logger.info("Field is not yet initialized: " + field);
								} else {
									Class<?> actualClass = o.getClass();
									logger.debug("Actual class is " + actualClass);
									if (!actualClass.isAssignableFrom(genericField.getRawGeneratedType())
									        && genericField.getRawGeneratedType().isAssignableFrom(actualClass)) {
										GenericField superClassField = new GenericField(
										        field, clazz);
										cluster.addGenerator(new GenericClass(actualClass),
										                     superClassField);
									}
								}
							} catch (IllegalAccessException e) {
								logger.error(e.getMessage());
							}

						}
					}
				} else {
					logger.debug("Can't use field " + field);
				}
			}
			analyzedClasses.add(clazz);
			// TODO: Set to generic type rather than class?
			cluster.getAnalyzedClasses().add(clazz);
		}
		if (Properties.INSTRUMENT_PARENT) {
			for (String superClass : inheritanceTree.getSuperclasses(Properties.TARGET_CLASS)) {
				try {
					Class<?> superClazz = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(superClass);
					dependencies.add(new Pair(0, superClazz));
				} catch (ClassNotFoundException e) {
					logger.error("Problem for " + Properties.TARGET_CLASS
					        + ". Class not found: " + superClass, e);
				}

			}
		}

		if (Properties.HANDLE_STATIC_FIELDS) {

			GetStaticGraph getStaticGraph = GetStaticGraphGenerator.generate(Properties.TARGET_CLASS);

			Map<String, Set<String>> staticFields = getStaticGraph.getStaticFields();
			for (String className : staticFields.keySet()) {
				logger.info("Adding static fields to cluster for class " + className);

				Class<?> clazz;
				try {
					clazz = getClass(className);
				} catch (ExceptionInInitializerError ex) {
					logger.debug("Class class init caused exception " + className);
					continue;
				}
				if (clazz == null) {
					logger.debug("Class not found " + className);
					continue;
				}

				if (!TestUsageChecker.canUse(clazz))
					continue;

				Set<String> fields = staticFields.get(className);
				for (Field field : getFields(clazz)) {
					if (!TestUsageChecker.canUse(field, clazz))
						continue;

					if (fields.contains(field.getName())) {
						if (!Modifier.isFinal(field.getModifiers())) {
							logger.debug("Is not final");
							cluster.addTestCall(new GenericField(field, clazz));
						}
					}
				}
			}

			PutStaticMethodCollector collector = new PutStaticMethodCollector(
			        Properties.TARGET_CLASS, staticFields);

			Set<MethodIdentifier> methodIdentifiers = collector.collectMethods();

			for (MethodIdentifier methodId : methodIdentifiers) {

				Class<?> clazz = getClass(methodId.getClassName());
				if (clazz == null)
					continue;

				if (!TestUsageChecker.canUse(clazz))
					continue;

				Method method = getMethod(clazz, methodId.getMethodName(),
				                          methodId.getDesc());

				if (method == null)
					continue;

				GenericMethod genericMethod = new GenericMethod(method, clazz);

				cluster.addTestCall(genericMethod);

			}
		}

		logger.info("Finished analyzing target class");
	}

	private Method getMethod(Class<?> clazz, String methodName, String desc) {
		for (Method method : clazz.getMethods()) {
			if (method.getName().equals(methodName)
			        && Type.getMethodDescriptor(method).equals(desc))
				return method;
		}
		return null;
	}

	private Class<?> getClass(String className) {
		try {
			Class<?> clazz = Class.forName(className,
			                               true,
			                               TestGenerationContext.getInstance().getClassLoaderForSUT());
			return clazz;
		} catch (ClassNotFoundException e) {
			return null;
		} catch (NoClassDefFoundError e) {
			// an ExceptionInInitializationError might have happened during class initialization.
			return null;
		}
	}

	/**
	 * Get the set of constructors defined in this class and its superclasses
	 *
	 * @param clazz
	 * @return
	 */
	public static Set<Constructor<?>> getConstructors(Class<?> clazz) {
		Map<String, Constructor<?>> helper = new TreeMap<>();

		Set<Constructor<?>> constructors = new LinkedHashSet<>();
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

		// As this is expensive, doing some caching here
		// Note that with the change of a class loader the cached values could
		// be thrown away
		if(methodCache.containsKey(clazz)) {
			return methodCache.get(clazz);
		}
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
		methodCache.put(clazz, methods);
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
		Map<String, Field> helper = new TreeMap<>();

		Set<Field> fields = new LinkedHashSet<>();
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
		if(accessibleFieldCache.containsKey(clazz)) {
			return accessibleFieldCache.get(clazz);
		}

		Set<Field> fields = new LinkedHashSet<Field>();
		try {
			for (Field f : clazz.getFields()) {
				if (TestUsageChecker.canUse(f) && !Modifier.isFinal(f.getModifiers())) {
					fields.add(f);
				}
			}
		} catch (Throwable t) {
			logger.info("Error while accessing fields of class " + clazz.getName()
			        + " - check allowed permissions: " + t);
		}

		accessibleFieldCache.put(clazz, fields);
		return fields;
	}



	private static boolean hasStaticGenerator(Class<?> clazz) {
		for(Method m : clazz.getMethods()) {
			if(Modifier.isStatic(m.getModifiers())) {
				if(clazz.isAssignableFrom(m.getReturnType())) {
					return true;
				}
			}
		}
		return false;
	}





	private final Set<Class<?>> analyzedClasses = new LinkedHashSet<Class<?>>();

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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
			        + ((dependencyClass == null) ? 0 : dependencyClass.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Pair other = (Pair) obj;
			if (dependencyClass == null) {
				if (other.dependencyClass != null)
					return false;
			} else if (!dependencyClass.equals(other.dependencyClass))
				return false;
			return true;
		}

	};

	private void addDependencies(GenericConstructor constructor, int recursionLevel) {
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

	private void addDependencies(GenericMethod method, int recursionLevel) {
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

		// If mocking is enabled, also return values are dependencies
		// as we might attempt to mock the method
		//
		// Only look at the return values of direct dependencies as the
		// number of dependencies otherwise might explode
		if(Properties.P_FUNCTIONAL_MOCKING > 0 && recursionLevel == 1) {
			GenericClass returnClass = method.getGeneratedClass();			
			if (!returnClass.isPrimitive() && !returnClass.isString())
				addDependency(returnClass, recursionLevel);
		}

	}

	private void addDependencies(GenericField field, int recursionLevel) {
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

	private void addDependency(GenericClass clazz, int recursionLevel) {

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

		if (!TestUsageChecker.canUse(clazz.getRawClass()))
			return;

		Class<?> mock = MockList.getMockClass(clazz.getRawClass().getCanonicalName());
		if (mock != null) {
			/*
			 * If we are mocking this class, then such class should not be used
			 * in the generated JUnit test cases, but rather its mock.
			 */
			logger.debug("Adding mock " + mock + " instead of " + clazz);
			clazz = new GenericClass(mock);
		} else {

			if (!checkIfCanUse(clazz.getClassName())) {
				return;
			}
		}

		for (Pair pair : dependencies) {
			if (pair.getDependencyClass().equals(clazz)) {
				return;
			}
		}
		if(analyzedAbstractClasses.contains(clazz)) {
			return;
		}

		logger.debug("Getting concrete classes for " + clazz.getClassName());
		ConstantPoolManager.getInstance().addNonSUTConstant(Type.getType(clazz.getRawClass()));
		List<Class<?>> actualClasses = new ArrayList<>(
		        getConcreteClasses(clazz.getRawClass(), inheritanceTree));
		// Randomness.shuffle(actualClasses);
		logger.debug("Concrete classes for " + clazz.getClassName() + ": "
		        + actualClasses.size());
		//dependencies.add(new Pair(recursionLevel, Randomness.choice(actualClasses)));

		analyzedAbstractClasses.add(clazz);
		for (Class<?> targetClass : actualClasses) {
			logger.debug("Adding concrete class: " + targetClass);
			dependencies.add(new Pair(recursionLevel, targetClass));
			//if(++num >= Properties.NUM_CONCRETE_SUBTYPES)
			//	break;
		}
	}

	private boolean addDependencyClass(GenericClass clazz, int recursionLevel) {
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

		if(clazz.isString()) {
			return false;
		}

		try {
			TestCluster cluster = TestCluster.getInstance();
			logger.debug("Adding dependency class " + clazz.getClassName());

			// TODO: Should we include declared classes as well?

			if (!TestUsageChecker.canUse(clazz.getRawClass())) {
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

				if (TestUsageChecker.canUse(constructor)) {
					GenericConstructor genericConstructor = new GenericConstructor(
					        constructor, clazz);
					try {
						cluster.addGenerator(clazz, //.getWithWildcardTypes(),
						                     genericConstructor);
						addDependencies(genericConstructor, recursionLevel + 1);
						logger.debug("Keeping track of "
						        + constructor.getDeclaringClass().getName()
						        + "."
						        + constructor.getName()
						        + org.objectweb.asm.Type.getConstructorDescriptor(constructor));
					} catch (Throwable t) {
						logger.info("Error adding constructor " + constructor.getName()
						        + ": " + t.getMessage());
					}

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

				if (TestUsageChecker.canUse(method, clazz.getRawClass())
				        && !method.getName().equals("hashCode")) {
					logger.debug("Adding method " + clazz.getClassName() + "."
					        + method.getName()
					        + org.objectweb.asm.Type.getMethodDescriptor(method));
					logger.debug("HashCode: "+(method.getName().equals("hashCode"))+", "+method.getName());
					if (method.getTypeParameters().length > 0) {
						logger.info("Type parameters in methods are not handled yet, skipping "
						        + method);
						continue;
					}
					GenericMethod genericMethod = new GenericMethod(method, clazz);
					try {
						addDependencies(genericMethod, recursionLevel + 1);
						if(!Properties.PURE_INSPECTORS) {
							cluster.addModifier(new GenericClass(clazz),
									genericMethod);
						} else {
							if(!CheapPurityAnalyzer.getInstance().isPure(method)) {
								cluster.addModifier(new GenericClass(clazz),
										genericMethod);
							}
						}

						GenericClass retClass = new GenericClass(method.getReturnType());

						if (!retClass.isPrimitive() && !retClass.isVoid()
						        && !retClass.isObject()) {
							cluster.addGenerator(retClass, //.getWithWildcardTypes(),
							                     genericMethod);
						}
					} catch (Throwable t) {
						logger.info("Error adding method " + method.getName() + ": "
						        + t.getMessage());
					}
				} else {
					logger.debug("Method cannot be used: " + method);
				}
			}

			// Add all fields
			for (Field field : getFields(clazz.getRawClass())) {
				logger.debug("Checking field " + field);
				if (TestUsageChecker.canUse(field, clazz.getRawClass())) {
					logger.debug("Adding field " + field + " for class " + clazz);
					try {
						GenericField genericField = new GenericField(field, clazz);
						cluster.addGenerator(new GenericClass(field.getGenericType()), //.getWithWildcardTypes(),
						                     genericField);
						if (!Modifier.isFinal(field.getModifiers())) {
							cluster.addModifier(clazz, //.getWithWildcardTypes(),
							                    genericField);
							addDependencies(genericField, recursionLevel + 1);
						}
					} catch (Throwable t) {
						logger.info("Error adding field " + field.getName() + ": " + t.getMessage());
					}

				} else {
					logger.debug("Field cannot be used: " + field);
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

	public static Set<Class<?>> loadClasses(Collection<String> classNames){
		Set<Class<?>> loadedClasses = new HashSet<>();
		for (String subClass : classNames) {
				try {
					Class<?> subClazz = Class.forName(subClass,
					                                  false,
					                                  TestGenerationContext.getInstance().getClassLoaderForSUT());
					if (!TestUsageChecker.canUse(subClazz))
						continue;
					if (subClazz.isInterface())
						continue;
					if (Modifier.isAbstract(subClazz.getModifiers())) {
						if(!hasStaticGenerator(subClazz))
							continue;
					}
					Class<?> mock = MockList.getMockClass(subClazz.getCanonicalName());
					if (mock != null) {
						/*
						 * If we are mocking this class, then such class should not be used
						 * in the generated JUnit test cases, but rather its mock.
						 */
//						logger.debug("Adding mock " + mock + " instead of "
//						        + clazz);
						subClazz = mock;
					} else {

						if (!checkIfCanUse(subClazz.getCanonicalName())) {
							continue;
						}
					}

					loadedClasses.add(subClazz);

				} catch (ClassNotFoundException e) {
					logger.error("Problem for " + Properties.TARGET_CLASS
					        + ". Class not found: " + subClass, e);
					logger.error("Removing class from inheritance tree");
				}
			}
		return loadedClasses;
	}


	public static Set<Class<?>> getConcreteClasses(Class<?> clazz,
	        InheritanceTree inheritanceTree) {

		// Some special cases
		if (clazz.equals(java.util.Map.class))
			return getConcreteClassesMap();
		else if (clazz.equals(java.util.List.class))
			return getConcreteClassesList();
		else if (clazz.equals(java.util.Set.class))
			return getConcreteClassesSet();
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
		        || Modifier.isInterface(clazz.getModifiers()) || clazz.equals(Enum.class)) {
			// We have to use getName here and not getCanonicalName
			// because getCanonicalname uses . rather than $ for inner classes
			// but the InheritanceTree uses $
			String className = clazz.getName();
			if(MockList.isAMockClass(className))
				className = clazz.getSuperclass().getName();
			Set<String> subClasses = inheritanceTree.getSubclasses(className);
			logger.debug("Subclasses of " + clazz.getName() + ": " + subClasses);
			Map<String, Integer> classDistance = new HashMap<String, Integer>();
			int maxDistance = -1;
			String name = clazz.getName();
			if (clazz.equals(Enum.class)) {
				name = Properties.TARGET_CLASS;
			}
			for (String subClass : subClasses) {
				int distance = getPackageDistance(subClass, name);
				classDistance.put(subClass, distance);
				maxDistance = Math.max(distance, maxDistance);
			}
			int distance = 0;
			while (actualClasses.isEmpty() && distance <= maxDistance) {
				logger.debug(" Current distance: " + distance);
				for (String subClass : subClasses) {
					if (TestClusterUtils.isAnonymousClass(subClass)) {
						continue;
					}

					if (classDistance.get(subClass) == distance) {
						try {
							TestGenerationContext.getInstance().goingToExecuteSUTCode();
							Class<?> subClazz = Class.forName(subClass,
							                                  false,
							                                  TestGenerationContext.getInstance().getClassLoaderForSUT());
							if (!TestUsageChecker.canUse(subClazz))
								continue;
							if (subClazz.isInterface())
								continue;
							if (Modifier.isAbstract(subClazz.getModifiers())) {
								if(!hasStaticGenerator(subClazz))
									continue;
							}
							Class<?> mock = MockList.getMockClass(subClazz.getCanonicalName());
							if (mock != null) {
								/*
								 * If we are mocking this class, then such class should not be used
								 * in the generated JUnit test cases, but rather its mock.
								 */
								logger.debug("Adding mock " + mock + " instead of "
								        + clazz);
								subClazz = mock;
							} else {

								if (!checkIfCanUse(subClazz.getCanonicalName())) {
									continue;
								}
							}

							actualClasses.add(subClazz);

						} catch (ClassNotFoundException | IncompatibleClassChangeError | NoClassDefFoundError e) {
							logger.error("Problem for " + Properties.TARGET_CLASS
							        + ". Class not found: " + subClass, e);
							logger.error("Removing class from inheritance tree");
							inheritanceTree.removeClass(subClass);
						} finally {
							TestGenerationContext.getInstance().doneWithExecutingSUTCode();
						}
					}
				}
				distance++;
			}
			if(hasStaticGenerator(clazz)) {
				actualClasses.add(clazz);
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
		Set<Class<?>> mapClasses = new LinkedHashSet<>();
		Class<?> mapClazz;
		try {
			mapClazz = Class.forName("java.util.HashMap",
			                         false,
			                         TestGenerationContext.getInstance().getClassLoaderForSUT());
			mapClasses.add(mapClazz);
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
		}
		return mapClasses;
	}

	private static Set<Class<?>> getConcreteClassesList() {
		Set<Class<?>> mapClasses = new LinkedHashSet<Class<?>>();
		Class<?> mapClazz;
		try {
			mapClazz = Class.forName("java.util.LinkedList",
			                         false,
			                         TestGenerationContext.getInstance().getClassLoaderForSUT());
			mapClasses.add(mapClazz);
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
		}
		return mapClasses;
	}

	private static Set<Class<?>> getConcreteClassesSet() {
		Set<Class<?>> mapClasses = new LinkedHashSet<Class<?>>();
		Class<?> setClazz;
		try {
			setClazz = Class.forName("java.util.LinkedHashSet",
			                         false,
			                         TestGenerationContext.getInstance().getClassLoaderForSUT());
			mapClasses.add(setClazz);
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
		}
		return mapClasses;
	}

	private static Set<Class<?>> getConcreteClassesComparable() {
		Set<Class<?>> comparableClasses = new LinkedHashSet<Class<?>>();
		Class<?> comparableClazz;
		try {
			comparableClazz = Class.forName("java.lang.Integer",
			                                false,
			                                TestGenerationContext.getInstance().getClassLoaderForSUT());
			comparableClasses.add(comparableClazz);
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
		}
		return comparableClasses;
	}

	// private Set<Class<?>> getConcreteClassesEnum() {
	//	Set<Class<?>> enumClasses = new LinkedHashSet<Class<?>>();
	//	for (String className : inheritanceTree.getSubclasses("java.lang.Enum")) {
	//		logger.warn("Enum candidate: " + className);
	//	}
    //
	//	return enumClasses;
	//}

	/**
	 * Calculate package distance between two classnames
	 *
	 * @param className1
	 * @param className2
	 * @return
	 */
	private static int getPackageDistance(String className1, String className2) {

		String[] package1 = StringUtils.split(className1, '.');
		String[] package2 = StringUtils.split(className2, '.');

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
