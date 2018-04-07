/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.TestGenerationContext;
import org.evosuite.TimeController;
import org.evosuite.assertion.CheapPurityAnalyzer;
import org.evosuite.classpath.ResourceList;
import org.evosuite.instrumentation.testability.BooleanTestabilityTransformation;
import org.evosuite.rmi.ClientServices;
import org.evosuite.runtime.PrivateAccess;
import org.evosuite.runtime.classhandling.ModifiedTargetStaticFields;
import org.evosuite.runtime.javaee.injection.Injector;
import org.evosuite.runtime.mock.MockList;
import org.evosuite.runtime.sandbox.Sandbox;
import org.evosuite.runtime.util.Inputs;
import org.evosuite.seeding.CastClassAnalyzer;
import org.evosuite.seeding.CastClassManager;
import org.evosuite.seeding.ConstantPoolManager;
import org.evosuite.setup.PutStaticMethodCollector.MethodIdentifier;
import org.evosuite.setup.callgraph.CallGraph;
import org.evosuite.statistics.RuntimeVariable;
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

	private final Set<GenericAccessibleObject<?>> dependencyCache = new LinkedHashSet<>();

	private final Set<GenericClass> genericCastClasses = new LinkedHashSet<>();

	private final Set<Class<?>> concreteCastClasses = new LinkedHashSet<>();

	private final Set<Class<?>> containerClasses = new LinkedHashSet<>();

	private final Set<DependencyPair> dependencies = new LinkedHashSet<DependencyPair>();

	private final Set<GenericClass> analyzedAbstractClasses = new LinkedHashSet<GenericClass>();

	private final Set<Class<?>> analyzedClasses = new LinkedHashSet<>();

	private final InheritanceTree inheritanceTree;

	// -------- public methods -----------------

	public TestClusterGenerator(InheritanceTree tree) {
		inheritanceTree = tree;
	}

	public void generateCluster(CallGraph callGraph) throws RuntimeException, ClassNotFoundException {

		TestCluster.setInheritanceTree(inheritanceTree);

		if (Properties.INSTRUMENT_CONTEXT || ArrayUtil.contains(Properties.CRITERION, Criterion.DEFUSE)) {
			for (String callTreeClass : callGraph.getClasses()) {
				try {
					if (callGraph.isCalledClass(callTreeClass)) {
						if (!Properties.INSTRUMENT_LIBRARIES && !DependencyAnalysis.isTargetProject(callTreeClass))
							continue;
						TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(callTreeClass);
					}
				} catch (ClassNotFoundException e) {
					logger.info("Class not found: " + callTreeClass + ": " + e);
				}
			}
		}

		dependencyCache.clear();

		/*
		 * If we fail to load a class, we skip it, and avoid to try to load it
		 * again (which would result in extra unnecessary logging)
		 */
		Set<String> blackList = new LinkedHashSet<>();
		initBlackListWithEvoSuitePrimitives(blackList);

		logger.info("Handling cast classes");
		handleCastClasses();

		logger.info("Initialising target class");
		initializeTargetMethods();

		logger.info("Resolving dependencies");
		resolveDependencies(blackList);

		handleSpecialCases();

		if (Properties.JEE) {
			addInjectionDependencies(blackList);
		}

		logger.info("Removing unusable generators");
		TestCluster.getInstance().removeUnusableGenerators();

		if (logger.isDebugEnabled()) {
			logger.debug(TestCluster.getInstance().toString());
		}

		gatherStatistics();
	}

	public void addNewDependencies(Collection<Class<?>> rawTypes) {

		Inputs.checkNull(rawTypes);

		Set<String> blackList = new LinkedHashSet<>();
		initBlackListWithEvoSuitePrimitives(blackList);

		rawTypes.stream().forEach(c -> dependencies.add(new DependencyPair(0, new GenericClass(c).getRawClass())));

		resolveDependencies(blackList);
	}

	// -----------------------------------------------------------------------------

	private void addInjectionDependencies(Set<String> blackList) {

		Set<Class<?>> toAdd = new LinkedHashSet<>();

		try {
			analyzedClasses.stream().flatMap(c -> Injector.getAllFieldsToInject(c).stream()).map(f -> f.getType())
					.forEach(t -> addInjectionRecursively(t, toAdd, blackList));
		} catch(Throwable t) {
			logger.warn("Error during initialisation of injection dependencies: "+t+", continuing anyway.");
		}
		toAdd.stream().forEach(c -> dependencies.add(new DependencyPair(0, new GenericClass(c).getRawClass())));
		resolveDependencies(blackList);
	}

	private void addInjectionRecursively(Class<?> target, Set<Class<?>> toAdd, Set<String> blackList) {

		if (toAdd.contains(target) || blackList.contains(target.getName())) {
			return;
		}

		toAdd.add(target);

		for (Field f : Injector.getAllFieldsToInject(target)) {
			addInjectionRecursively(f.getType(), toAdd, blackList);
		}
	}

	private void handleSpecialCases() {

		if (Properties.P_REFLECTION_ON_PRIVATE > 0 && Properties.REFLECTION_START_PERCENT < 1) {

			// Check if we should add
			// PrivateAccess.callDefaultConstructorOfTheClassUnderTest()

			Class<?> target = Properties.getTargetClassAndDontInitialise();

			Constructor<?> constructor = null;
			try {
				constructor = target.getDeclaredConstructor();
			} catch (NoSuchMethodException e) {
			}

			if (constructor != null && Modifier.isPrivate(constructor.getModifiers())
					&& target.getDeclaredConstructors().length == 1
					// Not enums
					&& !target.isEnum()) {

				Method m = null;
				try {
					m = PrivateAccess.class.getDeclaredMethod("callDefaultConstructorOfTheClassUnderTest");
				} catch (NoSuchMethodException e) {
					logger.error("Missing method: " + e.toString());
					return;
				}

				GenericMethod gm = new GenericMethod(m, PrivateAccess.class);

				// It is not really an environment method, but not sure how else
				// to handle it...
				TestCluster.getInstance().addEnvironmentTestCall(gm);
			}
		}

	}

	private void handleCastClasses() {
		// If we include type seeding, then we analyze classes to find types in
		// instanceof and cast instructions
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
					CastClassManager.getInstance().addCastClass(className, castEntry.getValue());
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

	private void initBlackListWithEvoSuitePrimitives(Set<String> blackList) throws NullPointerException {
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
		blackList.add(java.lang.ThreadGroup.class.getName()); // may lead to EvoSuite killing all threads
	}

	private void initBlackListWithPrimitives(Set<String> blackList) throws NullPointerException {
		blackList.add("int");
		blackList.add("short");
		blackList.add("float");
		blackList.add("double");
		blackList.add("byte");
		blackList.add("char");
		blackList.add("boolean");
		blackList.add("long");
	}

	private boolean addCastClassDependencyIfAccessible(String className, Set<String> blackList) {
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
	 * Continue adding generators for classes that are needed
	 */
	private void resolveDependencies(Set<String> blackList) {

		while (!dependencies.isEmpty() && TimeController.getInstance().isThereStillTimeInThisPhase()) {
			logger.debug("Dependencies left: {}", dependencies.size());

			Iterator<DependencyPair> iterator = dependencies.iterator();
			DependencyPair dependency = iterator.next();
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
			 * if (dependency.getDependencyClass().isParameterizedType()) { for
			 * (List<GenericClass> parameterTypes :
			 * getAssignableTypes(dependency.getDependencyClass())) {
			 * GenericClass copy = new GenericClass(
			 * dependency.getDependencyClass().getType());
			 * copy.setParameterTypes(parameterTypes); boolean success =
			 * addDependencyClass(copy, dependency.getRecursion()); if (success)
			 * added = true; } } else
			 */
			added = addDependencyClass(dependency.getDependencyClass(), dependency.getRecursion());
			if (!added) {
				blackList.add(className);
			}
			// }
		}

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
	private void initializeTargetMethods() throws RuntimeException, ClassNotFoundException {

		logger.info("Analyzing target class");
		Class<?> targetClass = Properties.getTargetClassAndDontInitialise();

		TestCluster cluster = TestCluster.getInstance();

		Set<Class<?>> targetClasses = new LinkedHashSet<Class<?>>();
		if (targetClass == null) {
			throw new RuntimeException("Failed to load " + Properties.TARGET_CLASS);
		}
		targetClasses.add(targetClass);
		addDeclaredClasses(targetClasses, targetClass);
		if (Modifier.isAbstract(targetClass.getModifiers())) {
			logger.info("SUT is an abstract class");
			Set<Class<?>> subclasses = ConcreteClassAnalyzer.getInstance().getConcreteClasses(targetClass,
					inheritanceTree);
			logger.info("Found " + subclasses.size() + " concrete subclasses");
			targetClasses.addAll(subclasses);
		}

		// load all the interesting classes from the callgraph,
		// need more testing, seems to slow down the search
		// if(Properties.INSTRUMENT_CONTEXT){
		// Set<String> toLoad;
		// if(Properties.INSTRUMENT_LIBRARIES){
		// toLoad = callGraph.getClassesUnderTest();
		// }else{
		// toLoad = new HashSet<>();
		// for (String className : callGraph.getClassesUnderTest()) {
		// if (!Properties.INSTRUMENT_LIBRARIES
		// && !DependencyAnalysis.isTargetProject(className))
		// continue;
		// toLoad.add(className);
		// }
		//
		// }
		// targetClasses.addAll(loadClasses(toLoad));
		//
		// }

		// To make sure we also have anonymous inner classes double check inner
		// classes using ASM

		// because the loop changes 'targetClasses' set we cannot iterate over
		// it, not even
		// using an iterator. a simple workaround is to create a temporary set
		// with the content
		// of 'targetClasses' and iterate that one
		Set<Class<?>> tmp_targetClasses = new LinkedHashSet<Class<?>>(targetClasses);
		for (Class<?> _targetClass : tmp_targetClasses) {
			ClassNode targetClassNode = DependencyAnalysis.getClassNode(_targetClass.getName());
			Queue<InnerClassNode> innerClasses = new LinkedList<InnerClassNode>();
			innerClasses.addAll(targetClassNode.innerClasses);
			while (!innerClasses.isEmpty()) {
				InnerClassNode icn = innerClasses.poll();
				try {
					logger.debug("Loading inner class: " + icn.innerName + ", " + icn.name + "," + icn.outerName);
					String innerClassName = ResourceList.getClassNameFromResourcePath(icn.name);
					if(!innerClassName.startsWith(Properties.TARGET_CLASS)) {
						// TODO: Why does ASM report inner classes that are not actually inner classes?
						// Let's ignore classes that don't start with the SUT name for now.
						logger.debug("Ignoring inner class that is outside SUT {}", innerClassName);
						continue;
					}
					Class<?> innerClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
							.loadClass(innerClassName);
					// if (!canUse(innerClass))
					// continue;

					// Sometimes strange things appear such as Map$Entry
					if (!targetClasses.contains(innerClass)
							/*
							 * FIXME: why all the checks were removed? without
							 * the following, for example
							 * com.google.javascript.jscomp.IdMappingUtil in
							 * 124_closure-compiler is not testable
							 */
							&& !innerClassName.contains("Map$Entry")) {
						// && !innerClassName.matches(".*\\$\\d+(\\$.*)?$")) {

						logger.info("Adding inner class {}", innerClassName);
						targetClasses.add(innerClass);
						ClassNode innerClassNode = DependencyAnalysis.getClassNode(innerClassName);
						innerClasses.addAll(innerClassNode.innerClasses);
					}

				} catch (Throwable t) {
					logger.error("Problem for " + Properties.TARGET_CLASS + ". Error loading inner class: "
							+ icn.innerName + ", " + icn.name + "," + icn.outerName + ": " + t);
				}
			}
		}

		for (Class<?> clazz : targetClasses) {
			logger.info("Current SUT class: " + clazz);

			if (!TestUsageChecker.canUse(clazz)) {
				logger.info("Cannot access SUT class: " + clazz);
				continue;
			}

			// Add all constructors
			for (Constructor<?> constructor : TestClusterUtils.getConstructors(clazz)) {
				logger.info("Checking target constructor " + constructor);
				String name = "<init>" + org.objectweb.asm.Type.getConstructorDescriptor(constructor);

				if (Properties.TT) {
					String orig = name;
					name = BooleanTestabilityTransformation.getOriginalNameDesc(clazz.getName(), "<init>",
							org.objectweb.asm.Type.getConstructorDescriptor(constructor));
					if (!orig.equals(name))
						logger.info("TT name: " + orig + " -> " + name);

				}

				if (TestUsageChecker.canUse(constructor)) {
					GenericConstructor genericConstructor = new GenericConstructor(constructor, clazz);
					if (constructor.getDeclaringClass().equals(clazz))
						cluster.addTestCall(genericConstructor);
					// TODO: Add types!
					cluster.addGenerator(new GenericClass(clazz), // .getWithWildcardTypes(),
							genericConstructor);
					addDependencies(genericConstructor, 1);
					logger.debug("Keeping track of " + constructor.getDeclaringClass().getName() + "."
							+ constructor.getName() + org.objectweb.asm.Type.getConstructorDescriptor(constructor));
				} else {
					logger.debug("Constructor cannot be used: " + constructor);
				}

			}

			// Add all methods
			for (Method method : TestClusterUtils.getMethods(clazz)) {
				logger.info("Checking target method " + method);
				String name = method.getName() + org.objectweb.asm.Type.getMethodDescriptor(method);

				if (Properties.TT) {
					String orig = name;
					name = BooleanTestabilityTransformation.getOriginalNameDesc(clazz.getName(), method.getName(),
							org.objectweb.asm.Type.getMethodDescriptor(method));
					if (!orig.equals(name))
						logger.info("TT name: " + orig + " -> " + name);
				}

				if (TestUsageChecker.canUse(method, clazz)) {
					logger.debug("Adding method " + clazz.getName() + "." + method.getName()
							+ org.objectweb.asm.Type.getMethodDescriptor(method));

					GenericMethod genericMethod = new GenericMethod(method, clazz);
					if (method.getDeclaringClass().equals(clazz))
						cluster.addTestCall(genericMethod);

					// This is now enabled, as the test calls are managed by the
					// test archive
					// However, there previously were concerns that:
					// For SUT classes without impure methods
					// this can affect the chances of covering the targets
					// so for now we keep all pure methods.
					// In the long run, covered methods maybe should be
					// removed?
					if (!CheapPurityAnalyzer.getInstance().isPure(method)) {
						cluster.addModifier(new GenericClass(clazz), genericMethod);
					}
					addDependencies(genericMethod, 1);
					GenericClass retClass = new GenericClass(method.getReturnType());

					// For the CUT, we may want to use primitives and Object return types as generators
					//if (!retClass.isPrimitive() && !retClass.isVoid() && !retClass.isObject())
					if (!retClass.isVoid())
						cluster.addGenerator(retClass, // .getWithWildcardTypes(),
								genericMethod);
				} else {
					logger.debug("Method cannot be used: " + method);

					// If we do reflection on private methods, we still need to consider dependencies
					if(Properties.P_REFLECTION_ON_PRIVATE > 0 && method.getDeclaringClass().equals(clazz)) {
						GenericMethod genericMethod = new GenericMethod(method, clazz);
						addDependencies(genericMethod, 1);
					}
				}
			}

			for (Field field : TestClusterUtils.getFields(clazz)) {
				logger.info("Checking target field " + field);

				if (TestUsageChecker.canUse(field, clazz)) {
					GenericField genericField = new GenericField(field, clazz);

					addDependencies(genericField, 1);
					cluster.addGenerator(new GenericClass(field.getGenericType()), // .getWithWildcardTypes(),
							genericField);
					logger.debug("Adding field " + field);
					final boolean isFinalField = isFinalField(field);
					if (!isFinalField) {
						logger.debug("Is not final");
						// Setting fields does not contribute to coverage, so we will only count it as a modifier
						// if (field.getDeclaringClass().equals(clazz))
						//	cluster.addTestCall(new GenericField(field, clazz));
						cluster.addModifier(new GenericClass(clazz), genericField);
					} else {
						logger.debug("Is final");
						if (Modifier.isStatic(field.getModifiers()) && !field.getType().isPrimitive()) {
							logger.debug("Is static non-primitive");
							/*
							 * With this we are trying to cover such cases:
							 *
							 * public static final DurationField INSTANCE = new
							 * MillisDurationField();
							 * 
							 * private MillisDurationField() { super(); }
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
										GenericField superClassField = new GenericField(field, clazz);
										cluster.addGenerator(new GenericClass(actualClass), superClassField);
									}
								}
							} catch (IllegalAccessException e) {
								logger.error(e.getMessage());
							}

						}
					}
				} else {
					logger.debug("Can't use field " + field);
					// If reflection on private is used, we still need to make sure dependencies are handled
					// TODO: Duplicate code here
					if(Properties.P_REFLECTION_ON_PRIVATE > 0) {
						if(Modifier.isPrivate(field.getModifiers())
								&& !field.isSynthetic()
								&& !field.getName().equals("serialVersionUID")
								// primitives cannot be changed
								&& !(field.getType().isPrimitive())
								// changing final strings also doesn't make much sense
								&& !(Modifier.isFinal(field.getModifiers()) && field.getType().equals(String.class))
								//static fields lead to just too many problems... although this could be set as a parameter
								&& !Modifier.isStatic(field.getModifiers())
								) {
							GenericField genericField = new GenericField(field, clazz);
							addDependencies(genericField, 1);
						}
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
					Class<?> superClazz = TestGenerationContext.getInstance().getClassLoaderForSUT()
							.loadClass(superClass);
					dependencies.add(new DependencyPair(0, superClazz));
				} catch (ClassNotFoundException e) {
					logger.error("Problem for " + Properties.TARGET_CLASS + ". Class not found: " + superClass, e);
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
					Sandbox.goingToExecuteUnsafeCodeOnSameThread();
					clazz = TestClusterUtils.getClass(className);
				} catch (ExceptionInInitializerError ex) {
					logger.debug("Class class init caused exception " + className);
					continue;
				} finally {
					Sandbox.doneWithExecutingUnsafeCodeOnSameThread();
				}
				if (clazz == null) {
					logger.debug("Class not found " + className);
					continue;
				}

				if (!TestUsageChecker.canUse(clazz))
					continue;

				Set<String> fields = staticFields.get(className);
				for (Field field : TestClusterUtils.getFields(clazz)) {
					if (!TestUsageChecker.canUse(field, clazz))
						continue;

					if (fields.contains(field.getName())) {
						if (!isFinalField(field)) {
							logger.debug("Is not final");
							// cluster.addTestCall(new GenericField(field, clazz));
							// Count static field as modifier of SUT, not as test call:
							GenericField genericField = new GenericField(field, clazz);
							cluster.addModifier(new GenericClass(Properties.getTargetClassAndDontInitialise()), genericField);
						}
					}
				}
			}

			PutStaticMethodCollector collector = new PutStaticMethodCollector(Properties.TARGET_CLASS, staticFields);

			Set<MethodIdentifier> methodIdentifiers = collector.collectMethods();

			for (MethodIdentifier methodId : methodIdentifiers) {

				Class<?> clazz = TestClusterUtils.getClass(methodId.getClassName());
				if (clazz == null)
					continue;

				if (!TestUsageChecker.canUse(clazz))
					continue;

				Method method = TestClusterUtils.getMethod(clazz, methodId.getMethodName(), methodId.getDesc());

				if (method == null)
					continue;

				GenericMethod genericMethod = new GenericMethod(method, clazz);

				// Setting static fields is a modifier of a SUT
				// cluster.addTestCall(genericMethod);
				cluster.addModifier(new GenericClass(Properties.getTargetClassAndDontInitialise()), genericMethod);

			}
		}

		logger.info("Finished analyzing target class");
	}

	/**
	 * This method returns is a given field is final or not. 
	 * Since we might have removed the <code>final</code> modifier 
	 * during our instrumentation, we also check the list of those
	 * static fields we have modified during the instrumentation.
	 * 
	 * @param field field to check
	 * @return 
	 */
	public static boolean isFinalField(Field field) {
		if (Properties.RESET_STATIC_FINAL_FIELDS) {
			if (Modifier.isFinal(field.getModifiers())) {
				return true;
			} else {
				String fieldName = field.getName();
				final boolean isModifiedStaticField = ModifiedTargetStaticFields.getInstance().containsField(fieldName);
				return isModifiedStaticField;
			}
		} else {
			final boolean isFinalField = Modifier.isFinal(field.getModifiers());
			return isFinalField;
		}
	}

	private void addDependencies(GenericConstructor constructor, int recursionLevel) {
		if (recursionLevel > Properties.CLUSTER_RECURSION) {
			logger.debug("Maximum recursion level reached, not adding dependencies of {}", constructor);
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
			logger.debug("Maximum recursion level reached, not adding dependencies of {}", method);
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
		if (Properties.P_FUNCTIONAL_MOCKING > 0 && recursionLevel == 1) {
			GenericClass returnClass = method.getGeneratedClass();
			if (!returnClass.isPrimitive() && !returnClass.isString())
				addDependency(returnClass, recursionLevel);
		}

	}

	private void addDependencies(GenericField field, int recursionLevel) {
		if (recursionLevel > Properties.CLUSTER_RECURSION) {
			logger.debug("Maximum recursion level reached, not adding dependencies of {}", field);
			return;
		}

		if (dependencyCache.contains(field)) {
			return;
		}

		if (field.getField().getType().isPrimitive() || field.getField().getType().equals(String.class))
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
			logger.debug("Adding mock {} instead of {}", mock, clazz);
			clazz = new GenericClass(mock);
		} else {

			if (!TestClusterUtils.checkIfCanUse(clazz.getClassName())) {
				return;
			}
		}

		for (DependencyPair pair : dependencies) {
			if (pair.getDependencyClass().equals(clazz)) {
				return;
			}
		}
		if (analyzedAbstractClasses.contains(clazz)) {
			return;
		}

		logger.debug("Getting concrete classes for " + clazz.getClassName());
		ConstantPoolManager.getInstance().addNonSUTConstant(Type.getType(clazz.getRawClass()));
		List<Class<?>> actualClasses = new ArrayList<>(
				ConcreteClassAnalyzer.getInstance().getConcreteClasses(clazz.getRawClass(), inheritanceTree));
		// Randomness.shuffle(actualClasses);
		logger.debug("Concrete classes for " + clazz.getClassName() + ": " + actualClasses.size());
		// dependencies.add(new Pair(recursionLevel,
		// Randomness.choice(actualClasses)));

		analyzedAbstractClasses.add(clazz);
		for (Class<?> targetClass : actualClasses) {
			logger.debug("Adding concrete class: " + targetClass);
			dependencies.add(new DependencyPair(recursionLevel, targetClass));
			// if(++num >= Properties.NUM_CONCRETE_SUBTYPES)
			// break;
		}
	}

	private boolean addDependencyClass(GenericClass clazz, int recursionLevel) {
		if (recursionLevel > Properties.CLUSTER_RECURSION) {
			logger.debug("Maximum recursion level reached, not adding dependency {}", clazz.getClassName());
			return false;
		}

		clazz = clazz.getRawGenericClass();

		if (analyzedClasses.contains(clazz.getRawClass())) {
			return true;
		}
		analyzedClasses.add(clazz.getRawClass());

		// We keep track of generic containers in case we find other concrete
		// generic components during runtime
		if (clazz.isAssignableTo(Collection.class) || clazz.isAssignableTo(Map.class)) {
			if (clazz.getNumParameters() > 0) {
				containerClasses.add(clazz.getRawClass());
			}
		}

		if (clazz.isString()) {
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
			for (Constructor<?> constructor : TestClusterUtils.getConstructors(clazz.getRawClass())) {
				String name = "<init>" + org.objectweb.asm.Type.getConstructorDescriptor(constructor);

				if (Properties.TT) {
					String orig = name;
					name = BooleanTestabilityTransformation.getOriginalNameDesc(clazz.getClassName(), "<init>",
							org.objectweb.asm.Type.getConstructorDescriptor(constructor));
					if (!orig.equals(name))
						logger.info("TT name: " + orig + " -> " + name);

				}

				if (TestUsageChecker.canUse(constructor)) {
					GenericConstructor genericConstructor = new GenericConstructor(constructor, clazz);
					try {
						cluster.addGenerator(clazz, // .getWithWildcardTypes(),
								genericConstructor);
						addDependencies(genericConstructor, recursionLevel + 1);
						if (logger.isDebugEnabled()) {
							logger.debug("Keeping track of " + constructor.getDeclaringClass().getName() + "."
									+ constructor.getName()
									+ org.objectweb.asm.Type.getConstructorDescriptor(constructor));
						}
					} catch (Throwable t) {
						logger.info("Error adding constructor {}: {}", constructor.getName(), t.getMessage());
					}

				} else {
					logger.debug("Constructor cannot be used: {}", constructor);
				}

			}

			// Add all methods
			for (Method method : TestClusterUtils.getMethods(clazz.getRawClass())) {
				String name = method.getName() + org.objectweb.asm.Type.getMethodDescriptor(method);

				if (Properties.TT) {
					String orig = name;
					name = BooleanTestabilityTransformation.getOriginalNameDesc(clazz.getClassName(), method.getName(),
							org.objectweb.asm.Type.getMethodDescriptor(method));
					if (!orig.equals(name))
						logger.info("TT name: " + orig + " -> " + name);
				}

				if (TestUsageChecker.canUse(method, clazz.getRawClass()) && !method.getName().equals("hashCode")) {
					logger.debug("Adding method " + clazz.getClassName() + "." + method.getName()
							+ org.objectweb.asm.Type.getMethodDescriptor(method));
					// TODO: Generic methods cause some troubles, but
//					if (method.getTypeParameters().length > 0) {
//						logger.info("Type parameters in methods are not handled yet, skipping " + method);
//						continue;
//					}
					GenericMethod genericMethod = new GenericMethod(method, clazz);
					try {
						addDependencies(genericMethod, recursionLevel + 1);
						if (!Properties.PURE_INSPECTORS) {
							cluster.addModifier(new GenericClass(clazz), genericMethod);
						} else {
							if (!CheapPurityAnalyzer.getInstance().isPure(method)) {
								cluster.addModifier(new GenericClass(clazz), genericMethod);
							}
						}

						GenericClass retClass = new GenericClass(method.getReturnType());

						// Only use as generator if its not any of the types with special treatment
						if (!retClass.isPrimitive() && !retClass.isVoid() && !retClass.isObject() && !retClass.isString()) {
							cluster.addGenerator(retClass, // .getWithWildcardTypes(),
									genericMethod);
						}
					} catch (Throwable t) {
						logger.info("Error adding method " + method.getName() + ": " + t.getMessage());
					}
				} else {
					logger.debug("Method cannot be used: " + method);
				}
			}

			// Add all fields
			for (Field field : TestClusterUtils.getFields(clazz.getRawClass())) {
				logger.debug("Checking field " + field);
				if (TestUsageChecker.canUse(field, clazz.getRawClass())) {
					logger.debug("Adding field " + field + " for class " + clazz);
					try {
						GenericField genericField = new GenericField(field, clazz);
						GenericClass retClass = new GenericClass(field.getType());
						// Only use as generator if its not any of the types with special treatment
						if(!retClass.isPrimitive() && !retClass.isObject() && !retClass.isString())
							cluster.addGenerator(new GenericClass(field.getGenericType()), genericField);
						final boolean isFinalField = isFinalField(field);
						if (!isFinalField) {
							cluster.addModifier(clazz, // .getWithWildcardTypes(),
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
			logger.info("Finished analyzing " + clazz.getTypeName() + " at recursion level " + recursionLevel);
			cluster.getAnalyzedClasses().add(clazz.getRawClass());
		} catch (Throwable t) {
			/*
			 * NOTE: this is a problem we know it can happen in some cases in
			 * SF110, but don't have a real solution now. As it is bound to
			 * happen, we try to minimize the logging (eg no stack trace),
			 * although we still need to log it
			 */
			logger.error("Problem for " + Properties.TARGET_CLASS + ". Failed to add dependencies for class "
					+ clazz.getClassName() + ": " + t + "\n" + Arrays.asList(t.getStackTrace()));

			return false;
		}
		return true;
	}

	// ----------------------
	// unused old methods
	// ----------------------

	private static Set<Class<?>> loadClasses(Collection<String> classNames) {
		Set<Class<?>> loadedClasses = new LinkedHashSet<>();
		for (String subClass : classNames) {
			try {
				Class<?> subClazz = Class.forName(subClass, false,
						TestGenerationContext.getInstance().getClassLoaderForSUT());
				if (!TestUsageChecker.canUse(subClazz))
					continue;
				if (subClazz.isInterface())
					continue;
				if (Modifier.isAbstract(subClazz.getModifiers())) {
					if (!TestClusterUtils.hasStaticGenerator(subClazz))
						continue;
				}
				Class<?> mock = MockList.getMockClass(subClazz.getCanonicalName());
				if (mock != null) {
					/*
					 * If we are mocking this class, then such class should not
					 * be used in the generated JUnit test cases, but rather its
					 * mock.
					 */
					// logger.debug("Adding mock " + mock + " instead of "
					// + clazz);
					subClazz = mock;
				} else {

					if (!TestClusterUtils.checkIfCanUse(subClazz.getCanonicalName())) {
						continue;
					}
				}

				loadedClasses.add(subClazz);

			} catch (ClassNotFoundException e) {
				logger.error("Problem for " + Properties.TARGET_CLASS + ". Class not found: " + subClass, e);
				logger.error("Removing class from inheritance tree");
			}
		}
		return loadedClasses;
	}

	// private Set<Class<?>> getConcreteClassesEnum() {
	// Set<Class<?>> enumClasses = new LinkedHashSet<Class<?>>();
	// for (String className : inheritanceTree.getSubclasses("java.lang.Enum"))
	// {
	// logger.warn("Enum candidate: " + className);
	// }
	//
	// return enumClasses;
	// }

	private List<List<GenericClass>> getAssignableTypes(GenericClass clazz) {
		List<List<GenericClass>> tuples = new ArrayList<>();
		// logger.info("Parameters of " + clazz.getSimpleName() + ": "
		// + clazz.getNumParameters());
		boolean first = true;
		for (java.lang.reflect.Type parameterType : clazz.getParameterTypes()) {
			// logger.info("Current parameter: " + parameterType);
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

	/**
	 * Update
	 *
	 * @param clazz
	 */
	private void addCastClassForContainer(Class<?> clazz) {
		if (concreteCastClasses.contains(clazz))
			return;

		concreteCastClasses.add(clazz);
		// TODO: What if this is generic again?
		genericCastClasses.add(new GenericClass(clazz));

		CastClassManager.getInstance().addCastClass(clazz, 1);
		TestCluster.getInstance().clearGeneratorCache(new GenericClass(clazz));
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
}
