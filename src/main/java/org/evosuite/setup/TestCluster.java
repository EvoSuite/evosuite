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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.testcase.ExecutionTracer;
import org.evosuite.utils.GenericAccessibleObject;
import org.evosuite.utils.GenericClass;
import org.evosuite.utils.GenericMethod;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.ResourceList;
import org.junit.Test;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gordon Fraser
 * 
 */
public class TestCluster {

	protected static final Logger logger = LoggerFactory
			.getLogger(TestCluster.class);

	/**
	 * This is the classloader that does the instrumentation - it needs to be
	 * used by all test code
	 */
	// public static ClassLoader classLoader = new InstrumentingClassLoader();

	/** Singleton instance */
	private static TestCluster instance = null;

	/** Set of all classes already analyzed */
	@Deprecated
	private final static Set<Class<?>> analyzedClasses = new LinkedHashSet<Class<?>>();

	/** Methods we want to cover when testing */
	private final static List<GenericAccessibleObject> testMethods = new ArrayList<GenericAccessibleObject>();

	/** Static information about how to generate types */
	private final static Map<GenericClass, Set<GenericAccessibleObject>> generators = new LinkedHashMap<GenericClass, Set<GenericAccessibleObject>>();

	/** Cached information about how to generate types */
	private final static Map<GenericClass, Set<GenericAccessibleObject>> generatorCache = new LinkedHashMap<GenericClass, Set<GenericAccessibleObject>>();

	/** Static information about how to modify types */
	private final static Map<GenericClass, Set<GenericAccessibleObject>> modifiers = new LinkedHashMap<GenericClass, Set<GenericAccessibleObject>>();

	private static InheritanceTree inheritanceTree = null;

	private static List<String> finalClasses = new ArrayList<String>();

	private static Set<Method> staticInitializers = new LinkedHashSet<Method>();

	/**
	 * @return the inheritancetree
	 */
	protected static InheritanceTree getInheritanceTree() {
		return inheritanceTree;
	}

	/**
	 * Instance accessor
	 * 
	 * @return
	 */
	public static synchronized TestCluster getInstance() {
		if (instance == null) {
			instance = new TestCluster();
		}

		// TODO: Need property to switch between test clusters

		return instance;
	}

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

		if (Properties.INSTRUMENT_PARENT) {
			return inheritanceTree.getSubclasses(Properties.TARGET_CLASS)
					.contains(className);
		}

		return false;

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
			Class<?> clazz = TestGenerationContext.getClassLoader().loadClass(
					className);
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
			logger.info("Could not load class: ", className);
		}
		return false;
	}

	private static void loadStaticInitializers() {
		for (String className : finalClasses) {
			try {
				Class<?> clazz = TestGenerationContext.getClassLoader()
						.loadClass(className);
				Method m = clazz.getMethod("__STATIC_RESET", (Class<?>[]) null);
				m.setAccessible(true);
				staticInitializers.add(m);
				logger.info("Adding static class: " + className);
			} catch (ClassNotFoundException e) {
				logger.info("Static: Could not find class: " + className);
			} catch (SecurityException e) {
				logger.info("Static: Security exception: " + className);
				// TODO Auto-generated catch block
				// e.printStackTrace();
			} catch (NoSuchMethodException e) {
				logger.info("Static: Could not find method clinit in : "
						+ className);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		finalClasses.clear();
	}

	public static void registerStaticInitializer(String className) {
		finalClasses.add(className);
	}

	public static void reset() {
		// classLoader = new InstrumentingClassLoader();
		finalClasses.clear();
		staticInitializers.clear();

		analyzedClasses.clear();
		testMethods.clear();
		generators.clear();
		generatorCache.clear();
		modifiers.clear();
		CastClassManager.getInstance().clear();

		instance = null;
	}

	/**
	 * @param inheritancetree
	 *            the inheritancetree to set
	 */
	protected static void setInheritanceTree(InheritanceTree inheritancetree) {
		inheritanceTree = inheritancetree;
	}

	/**
	 * Add a generator reflection object
	 * 
	 * @param target
	 * @param call
	 */
	public void addGenerator(GenericClass target, GenericAccessibleObject call) {
		if (!generators.containsKey(target))
			generators
					.put(target, new LinkedHashSet<GenericAccessibleObject>());

		// TODO: Need to add this call to all subclasses/superclasses?
		logger.debug("Adding generator for class " + target + ": " + call);
		generators.get(target).add(call);
	}

	/**
	 * Add a generator reflection object
	 * 
	 * @param target
	 * @param call
	 */
	public void addModifier(GenericClass target, GenericAccessibleObject call) {
		if (!modifiers.containsKey(target))
			modifiers.put(target, new LinkedHashSet<GenericAccessibleObject>());

		// TODO: Need to add this call to all subclasses/superclasses?

		modifiers.get(target).add(call);
	}

	/**
	 * Add a test call
	 * 
	 * @return
	 */
	public void addTestCall(GenericAccessibleObject call) {
		testMethods.add(call);
	}

	
	/**
	 * Calculate and cache all generators for a particular (possibly generic) type
	 * 
	 * @param clazz
	 */
	private void cacheGenerators(GenericClass clazz) {
		if (generatorCache.containsKey(clazz))
			return;
		logger.debug("Caching generators for " + clazz);
		Set<GenericAccessibleObject> targetGenerators = new LinkedHashSet<GenericAccessibleObject>();
		if (clazz.isObject()) {
			logger.debug("Target class is object: " + clazz);
			for (GenericClass generatorClazz : generators.keySet()) {
				if (generatorClazz.isObject()) {
					// System.out.println(clazz.getTypeName() +
					// " can be assigned from "
					// + generatorClazz.getTypeName());
					targetGenerators.addAll(generators.get(generatorClazz));
				}
			}
		} else {
			for (GenericClass generatorClazz : generators.keySet()) {
				if(generatorClazz == clazz)
					continue;
				
				// First check if the raw classes are assignable
				if (clazz.getRawClass().isAssignableFrom(
						generatorClazz.getRawClass())) {
					logger.debug("Raw classes are assignable: "+generatorClazz +" for "+clazz);

					
					// If the types are assignable, everything is fine
					if (clazz.isAssignableFrom(generatorClazz)) {
						logger.debug("Adding subtype generator: " + clazz
								+ " is assignable from "
								+ generatorClazz.getTypeName());
						try {
							targetGenerators.addAll(getGenerators(generatorClazz));
						} catch(ConstructionFailedException e) {
							logger.debug("Error adding generators for "+generatorClazz+": "+e);
						}
					} else {
						
						// If the types are not assignable, we need to check the generic type parameters
						logger.debug(clazz + " is not assignable from "
								+ generatorClazz.getTypeName());
						
						// Type parameters for the class of the generator
						List<Type> parameters = new ArrayList<Type>();
						
						// Type parameters of the target type
						List<Type> targetParameters = clazz.getParameterTypes();
						
						// Type parameters of the type that is generated
						List<Type> generatorParameters = generatorClazz
								.getParameterTypes();
						if (targetParameters.size() != generatorParameters
								.size()) {
							continue;
						}

						boolean compatibleParameters = true;
						for (int i = 0; i < targetParameters.size(); i++) {
							logger.debug("Comparing target parameter "
									+ targetParameters.get(i)
									+ " with generator parameter "
									+ generatorParameters.get(i));
							Type generatorType = generatorParameters.get(i);
							Type targetType = targetParameters.get(i);
							if (!TypeUtils.isAssignable(targetType,
									generatorType)) {
								compatibleParameters = false;
								logger.debug("Incompatible parameter: "+targetType+" vs. "+generatorType);
								break;
							}
							parameters.add(targetType);
						}
						if (compatibleParameters) {
							logger.debug("Parameter are compatible, trying to construct class");

							Type[] actualParameters = new Type[parameters
									.size()];
							parameters.toArray(actualParameters);
							GenericClass newOwner = generatorClazz
									.getWithParameterTypes(actualParameters);
							logger.debug("New owner is: "+newOwner);
							
							// "newOwner" is the instantiated type of the return value
							// but we need the declaring class of the method!
							
							for (GenericAccessibleObject generator : generators
									.get(generatorClazz)) {
								logger.debug("Candidate generator: "+generator);
								if(generator.getOwnerClass().getNumParameters() == 0) {
									// logger.debug("Owner class has no parameters, so we can only assume it would work: "+generator.getName());
									targetGenerators.add(generator);
								} else {
									GenericAccessibleObject newGenerator = generator.copyWithOwnerFromReturnType((ParameterizedType)newOwner.getType());
									// logger.debug("Instantiated generator: "+newGenerator.getName());
									if(newGenerator.getOwnerClass().hasWildcardOrTypeVariables()) {
										GenericClass concreteClass = getGenericInstantiation(newGenerator.getOwnerClass());
										targetGenerators.add(newGenerator.copyWithNewOwner(concreteClass));
									} else {
										targetGenerators.add(newGenerator);
									}
								}
							}
						}
					}
				}
			}
		}
		// There is a bit of a problem in resolving some generics
		// For example, ArrayList(Collection<? extends E>) is not resolved
		// by Java, and there is no generator for Collection<? extends E> in the
		// end
		// Therefore, we try to add everything that fits on the raw type
		if (targetGenerators.isEmpty() && clazz.getNumParameters() > 0) {
			logger.debug("Have no suitable generic generators, so using raw class");
			for (GenericClass generatorClazz : generators.keySet()) {
				if (clazz.getRawClass().isAssignableFrom(
						generatorClazz.getRawClass())) {
					targetGenerators.addAll(generators.get(generatorClazz));
				}
			}
		}
		generatorCache.put(clazz, targetGenerators);
	}

	public void clearGeneratorCache(GenericClass target) {
		generatorCache.clear();
	}

	/**
	 * Get modifiers for instantiation of a generic type
	 * 
	 * @param clazz
	 * @return
	 */
	private Set<GenericAccessibleObject> determineGenericModifiersFor(
			GenericClass clazz) {
		Set<GenericAccessibleObject> genericModifiers = new LinkedHashSet<GenericAccessibleObject>();
		if (clazz.isParameterizedType()) {
			for (Entry<GenericClass, Set<GenericAccessibleObject>> entry : modifiers
					.entrySet()) {
				if (entry.getKey().getRawClass().equals(clazz.getRawClass())) {
					List<Type> parameters = new ArrayList<Type>();
					List<Type> targetParameters = clazz.getParameterTypes();
					List<Type> modifierParameters = entry.getKey()
							.getParameterTypes();
					if (targetParameters.size() != modifierParameters.size()) {
						continue;
					}

					boolean compatibleParameters = true;
					for (int i = 0; i < targetParameters.size(); i++) {
						logger.debug("Comparing target parameter "
								+ targetParameters.get(i)
								+ " with generator parameter "
								+ modifierParameters.get(i));
						Type modifierType = modifierParameters.get(i);
						Type targetType = targetParameters.get(i);
						if (!TypeUtils.isAssignable(targetType, modifierType)) {
							compatibleParameters = false;
							logger.debug("Incompatible parameter: "+targetType+" vs. "+modifierType);
							break;
						}
						parameters.add(targetType);
					}
					if (compatibleParameters) {
						Type[] actualParameters = new Type[parameters.size()];
						parameters.toArray(actualParameters);
						GenericClass newOwner = entry.getKey()
								.getWithParameterTypes(actualParameters);
						for (GenericAccessibleObject modifier : entry
								.getValue()) {
							if(modifier.getOwnerClass().getNumParameters() == 0) {
								logger.debug("Owner class has no parameters, so we can only assume it would work");
								genericModifiers.add(modifier);
							} else {
								GenericAccessibleObject newModifier = modifier.copyWithOwnerFromReturnType((ParameterizedType)newOwner.getType());
								if(newModifier.getOwnerClass().hasWildcardOrTypeVariables()) {
									GenericClass concreteClass = getGenericInstantiation(newModifier.getOwnerClass());
									genericModifiers.add(newModifier.copyWithNewOwner(concreteClass));
								} else {
									genericModifiers.add(newModifier);
								}
							}
						}
					}
				}
			}
		}
		return genericModifiers;
	}

	/**
	 * @return the analyzedClasses
	 */
	public Set<Class<?>> getAnalyzedClasses() {
		return analyzedClasses;
	}

	/**
	 * Return all calls that have a parameter with given type
	 * 
	 * @param type
	 * @return
	 */
	public Set<GenericAccessibleObject> getCallsFor(GenericClass clazz) {
		if (clazz.hasWildcardOrTypeVariables()) {
			GenericClass concreteClass = getGenericInstantiation(clazz);
			return getCallsFor(concreteClass);
		}

		if (isSpecialCase(clazz)) {
			logger.debug("Getting modifiers for special case " + clazz);
			return getCallsForSpecialCase(clazz);
		}
		logger.debug("Getting modifiers for regular case " + clazz);

		if (!modifiers.containsKey(clazz)) {
			return determineGenericModifiersFor(clazz);
		}

		return modifiers.get(clazz);
	}

	/**
	 * Get modifiers for special cases
	 * 
	 * @param clazz
	 * @return
	 */
	private Set<GenericAccessibleObject> getCallsForSpecialCase(
			GenericClass clazz) {
		Set<GenericAccessibleObject> all = new LinkedHashSet<GenericAccessibleObject>();
		if (!modifiers.containsKey(clazz)) {
			all.addAll(determineGenericModifiersFor(clazz));
		} else {
			all.addAll(modifiers.get(clazz));
		}
		Set<GenericAccessibleObject> calls = new LinkedHashSet<GenericAccessibleObject>();

		if (clazz.isAssignableTo(Collection.class)) {
			for (GenericAccessibleObject call : all) {
				if (call.isConstructor() && call.getNumParameters() == 0) {
					calls.add(call);
				} else if (call.isMethod()
						&& ((GenericMethod) call).getName().equals("add")
						&& call.getNumParameters() == 1) {
					calls.add(call);
				} else {
					if(Randomness.nextDouble() < Properties.P_SPECIAL_TYPE_CALL) {
						calls.add(call);
					}
				}
			}
		}

		else if (clazz.isAssignableTo(Map.class)) {
			for (GenericAccessibleObject call : all) {
				if (call.isConstructor() && call.getNumParameters() == 0) {
					calls.add(call);
				} else if (call.isMethod()
						&& ((GenericMethod) call).getName().equals("put")) {
					calls.add(call);
				} else {
					if(Randomness.nextDouble() < Properties.P_SPECIAL_TYPE_CALL) {
						calls.add(call);
					}
				}
			}
		}

		else if (clazz.isAssignableTo(Number.class)) {
			if(modifiers.containsKey(clazz)) {
				for(GenericAccessibleObject call : modifiers.get(clazz)) {
					if(Randomness.nextDouble() < Properties.P_SPECIAL_TYPE_CALL) {
						calls.add(call);
					}
				}
			}
			return calls;
		}

		return calls;
	}

	/**
	 * Find a class that matches the given name
	 * 
	 * @param name
	 * @return
	 * @throws ClassNotFoundException
	 */
	public Class<?> getClass(String name) throws ClassNotFoundException {
		// First try to find exact match
		for (Class<?> clazz : analyzedClasses) {
			if (clazz.getName().equals(name)
					|| clazz.getName().equals(
							Properties.CLASS_PREFIX + "." + name)
					|| clazz.getName().equals(
							Properties.CLASS_PREFIX + "."
									+ name.replace(".", "$"))) {
				return clazz;
			}
		}

		// Then try to match a postfix
		for (Class<?> clazz : analyzedClasses) {
			if (clazz.getName().endsWith("." + name)) {
				return clazz;
			}
		}

		// Or try java.lang
		try {
			TestGenerationContext.getClassLoader().loadClass(
					"java.lang." + name);
		} catch (ClassNotFoundException e) {
			// Ignore it as we throw our own
		}

		throw new ClassNotFoundException(name);

	}

	/**
	 * Retrieve all generators
	 * 
	 * @return
	 */
	public Set<GenericAccessibleObject> getGenerators() {
		Set<GenericAccessibleObject> calls = new LinkedHashSet<GenericAccessibleObject>();
		for (Set<GenericAccessibleObject> generatorCalls : generators.values())
			calls.addAll(generatorCalls);

		return calls;
	}

	/**
	 * Get a list of all generator objects for the type
	 * 
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	public Set<GenericAccessibleObject> getGenerators(GenericClass clazz)
			throws ConstructionFailedException {

		if (clazz.hasWildcardOrTypeVariables()) {
			GenericClass concreteClass = getGenericInstantiation(clazz);
			return getGenerators(concreteClass);
		}

		if (!hasGenerator(clazz))
			throw new ConstructionFailedException("No generators of type "
					+ clazz);

		if (isSpecialCase(clazz)) {
			return getGeneratorsForSpecialCase(clazz);
		}

		return generatorCache.get(clazz);
	}

	/**
	 * Predetermined generators for special cases 
	 * 
	 * @param clazz
	 * @return
	 */
	private Set<GenericAccessibleObject> getGeneratorsForSpecialCase(
			GenericClass clazz) {
		Set<GenericAccessibleObject> calls = new LinkedHashSet<GenericAccessibleObject>();

		if (clazz.isAssignableTo(Collection.class)
				|| clazz.isAssignableTo(Map.class)) {
			Set<GenericAccessibleObject> all = new LinkedHashSet<GenericAccessibleObject>();
			if (!generatorCache.containsKey(clazz)) {
				cacheGenerators(clazz);
			}
			all.addAll(generatorCache.get(clazz));

			for (GenericAccessibleObject call : all) {
				if (call.isConstructor() && call.getNumParameters() == 0) {
					calls.add(call);
				} else if(!Collection.class.isAssignableFrom(call.getDeclaringClass()) && Map.class.isAssignableFrom(call.getDeclaringClass())) {
					// Methods that return collections are candidates, unless they are methods of the collections
					calls.add(call);
				} else {
					if(Randomness.nextDouble() < Properties.P_SPECIAL_TYPE_CALL) {
						calls.add(call);
					}
				}
			}
		} else if (clazz.isAssignableTo(Number.class)) {
			Set<GenericAccessibleObject> all = new LinkedHashSet<GenericAccessibleObject>();
			if (!generatorCache.containsKey(clazz)) {
				cacheGenerators(clazz);
			}
			all.addAll(generatorCache.get(clazz));

			for (GenericAccessibleObject call : all) {
				if (call.isConstructor() && call.getNumParameters() == 1) {
					calls.add(call);
				} else if (call.isField()) {
					calls.add(call);
				} else {
					if(Randomness.nextDouble() < Properties.P_SPECIAL_TYPE_CALL) {
						calls.add(call);
					}
				}
			}
		}

		return calls;
	}

	/**
	 * Instantiate type parameters for a parameterized type
	 * 
	 * @param clazz
	 * @return
	 */
	public GenericClass getGenericInstantiation(GenericClass clazz) {
		if (!clazz.isParameterizedType())
			return clazz;

		List<Type> parameterTypes = new ArrayList<Type>();
		for (java.lang.reflect.Type parameterType : clazz.getParameterTypes()) {
			if (parameterType instanceof WildcardType) {
				parameterTypes.add(getRandomCastClass(parameterType).getType());
			} else if (parameterType instanceof TypeVariable) {
				parameterTypes.add(getRandomCastClass(parameterType).getType());
			} else {
				parameterTypes.add(parameterType);
			}
		}
		
		if(clazz.hasOwnerType()) {
			clazz.setOwnerType(getGenericInstantiation(clazz.getOwnerType()));
		}
		
		return clazz.getWithParameterTypes(parameterTypes);
	}

	/**
	 * Retrieve all classes that match the given postfix
	 * 
	 * @param name
	 * @return
	 */
	public Collection<Class<?>> getKnownMatchingClasses(String name) {
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		for (Class<?> c : analyzedClasses) {
			if (c.getName().endsWith(name))
				classes.add(c);
		}
		return classes;
	}

	/**
	 * Retrieve all classes in the classpath that match the given postfix
	 * 
	 * @param name
	 * @return
	 */
	public Collection<String> getMatchingClasses(String name) {
		Pattern pattern = Pattern.compile(".*" + name + ".class");
		// Pattern pattern = Pattern.compile(".*");
		Collection<String> resources = ResourceList.getResources(pattern);
		resources.addAll(ResourceList.getBootResources(pattern));

		Set<String> classes = new LinkedHashSet<String>();
		for (String className : resources) {
			classes.add(className.replace(".class", "").replace("/", "."));
		}

		return classes;
	}

	/**
	 * Retrieve all modifiers
	 * 
	 * @return
	 */
	public Set<GenericAccessibleObject> getModifiers() {
		Set<GenericAccessibleObject> calls = new LinkedHashSet<GenericAccessibleObject>();
		for (Set<GenericAccessibleObject> modifierCalls : modifiers.values())
			calls.addAll(modifierCalls);

		return calls;
	}

	/**
	 * Determine the set of generators for an Object.class instance
	 * 
	 * @param target
	 * @return
	 */
	public Set<GenericAccessibleObject> getObjectGenerators() {
		// TODO: Use probabilities based on distance to SUT
		Set<GenericAccessibleObject> result = new LinkedHashSet<GenericAccessibleObject>();
		for(GenericClass clazz : CastClassManager.getInstance().getCastClasses()) {
			try {
				result.addAll(getGenerators(clazz));
			} catch (ConstructionFailedException e) {
				// ignore
			}
		}
		try {
			result.addAll(getGenerators(new GenericClass(Object.class)));
		} catch (ConstructionFailedException e) {
			// ignore
		}
		return result;
	}

	/**
	 * Choose one of the seeded types for which we found casts
	 * 
	 * @param targetType
	 * @return
	 */
	private GenericClass getRandomCastClass(Type targetType) {
		GenericClass castClass = CastClassManager.getInstance().selectCastClass(targetType);
		if(castClass.hasWildcardOrTypeVariables()) {
			return getGenericInstantiation(castClass);
		}
		return castClass;
		
		// TODO: Use probabilities based on distance to SUT
		/*
		Set<GenericClass> candidateClasses = new LinkedHashSet<GenericClass>();
		logger.debug("Checking cast classes: " + castClasses);
		for (GenericClass candidate : castClasses) {
			if (candidate.isAssignableTo(targetType))
				candidateClasses.add(candidate);
			else {
				logger.debug("Not assignable: " + candidate
						+ " for parameter of type " + targetType);
			}
		}
		if (candidateClasses.isEmpty()) {
			logger.debug("Nothing is assignable to " + targetType + "?");
			throw new RuntimeException();
		}

		return Randomness.choice(candidateClasses);
		*/
	}

	/**
	 * Randomly select one generator
	 * 
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	public GenericAccessibleObject getRandomGenerator(GenericClass clazz)
			throws ConstructionFailedException {

		if (clazz.hasWildcardOrTypeVariables()) {
			GenericClass concreteClass = getGenericInstantiation(clazz);
			return getRandomGenerator(concreteClass);
		}

		if (!hasGenerator(clazz))
			throw new ConstructionFailedException("No generators of type "
					+ clazz);

		if (isSpecialCase(clazz)) {
			return Randomness.choice(getGeneratorsForSpecialCase(clazz));
		}

		return Randomness.choice(generatorCache.get(clazz));
	}

	/**
	 * Randomly select one generator
	 * 
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	public GenericAccessibleObject getRandomGenerator(GenericClass clazz,
			Set<GenericAccessibleObject> excluded)
			throws ConstructionFailedException {

		logger.debug("getting random generator for "+clazz);
		
		// Instantiate generics
		if (clazz.hasWildcardOrTypeVariables()) {
			GenericClass concreteClass = getGenericInstantiation(clazz);
			return getRandomGenerator(concreteClass, excluded);
		}

		logger.debug("Caching generators for "+clazz);
		cacheGenerators(clazz);

		// Collection, Map, Number
		if (isSpecialCase(clazz)) {
			return Randomness.choice(getGeneratorsForSpecialCase(clazz));
		}

		Set<GenericAccessibleObject> candidates = new LinkedHashSet<GenericAccessibleObject>(
				generatorCache.get(clazz));
		int before = candidates.size();
		candidates.removeAll(excluded);
		logger.debug("Candidate generators for "+clazz +": "+candidates.size());

		if (candidates.isEmpty())
			throw new ConstructionFailedException("No generators left for "
					+ clazz + " - in total there are " + before);

		GenericAccessibleObject generator = Randomness.choice(candidates);
		if(generator.getOwnerClass().hasWildcardOrTypeVariables()) {
			logger.debug("Owner class has a wildcard: "+clazz);
			return generator.copyWithNewOwner(getGenericInstantiation(generator.getOwnerClass()));
		}
		return generator;

	}

	/**
	 * Randomly select a generator for an Object.class instance
	 * 
	 * @param target
	 * @return
	 */
	public GenericAccessibleObject getRandomObjectGenerator() {
		GenericAccessibleObject generator = Randomness.choice(getObjectGenerators());
		if (generator.getOwnerClass().hasWildcardOrTypeVariables()) {
			GenericClass concreteClass = getGenericInstantiation(generator.getOwnerClass());
			return generator.copyWithNewOwner(concreteClass);
		}
		return generator;

	}

	/**
	 * Get random method or constructor of unit under test
	 * 
	 * @return
	 * @throws ConstructionFailedException
	 */
	public GenericAccessibleObject getRandomTestCall() {
		GenericAccessibleObject choice = Randomness.choice(testMethods);
		if(choice.getOwnerClass().hasWildcardOrTypeVariables()) {
			GenericClass concreteClass = getGenericInstantiation(choice.getOwnerClass());
			return choice.copyWithNewOwner(concreteClass);
		}
		return choice;
	}

	/**
	 * Get a list of all test calls (i.e., constructors and methods)
	 * 
	 * @return
	 */
	public List<GenericAccessibleObject> getTestCalls() {
		List<GenericAccessibleObject> result = new ArrayList<GenericAccessibleObject>(testMethods);
		for(GenericAccessibleObject ao : testMethods) {
			if(ao.getOwnerClass().hasWildcardOrTypeVariables()) {
				GenericClass concreteClass = getGenericInstantiation(ao.getOwnerClass());
				result.add(ao.copyWithNewOwner(concreteClass));
			} else {
				result.add(ao);
			}
		}
		return result;
	}

	/**
	 * Determine if there are generators
	 * 
	 * @param type
	 * @return
	 */
	public boolean hasGenerator(GenericClass clazz) {
		cacheGenerators(clazz);
		return generatorCache.containsKey(clazz);
	}

	/**
	 * Determine if there are generators
	 * 
	 * @param type
	 * @return
	 */
	public boolean hasGenerator(Type type) {
		return hasGenerator(new GenericClass(type));
	}

	/**
	 * Integrate a new class into the test cluster
	 * 
	 * @param name
	 * @throws ClassNotFoundException
	 */
	public Class<?> importClass(String name) throws ClassNotFoundException {
		throw new RuntimeException("Not implemented");
	}

	/**
	 * Some standard classes need to be treated specially to increase performance
	 * 
	 * @param clazz
	 * @return
	 */
	private boolean isSpecialCase(GenericClass clazz) {
		if (clazz.isAssignableTo(Collection.class))
			return true;

		if (clazz.isAssignableTo(Map.class))
			return true;

		if (clazz.isAssignableTo(Number.class))
			return true;

		return false;
	}

	/**
	 * Unload all classes; perform cleanup
	 */
	public void resetCluster() {
		analyzedClasses.clear();
		testMethods.clear();
		generators.clear();
		generatorCache.clear();
		modifiers.clear();
		CastClassManager.getInstance().clear();
	}

	/**
	 * Call each of the duplicated static constructors
	 */
	public void resetStaticClasses() {
		boolean tracerEnabled = ExecutionTracer.isEnabled();
		if (tracerEnabled)
			ExecutionTracer.disable();
		loadStaticInitializers();
		logger.debug("Static initializers: " + staticInitializers.size());
		for (Method m : staticInitializers) {
			// if (!m.getDeclaringClass().equals(Properties.getTargetClass()))
			// continue;

			// logger.warn("Resetting " + m);

			try {
				m.invoke(null, (Object[]) null);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}

		}
		if (tracerEnabled)
			ExecutionTracer.enable();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("Analyzed classes:\n");
		for (Class<?> clazz : analyzedClasses) {
			result.append(clazz.getName());
			result.append("\n");
		}
		result.append("Generators:\n");
		for (GenericClass clazz : generators.keySet()) {
			result.append(" Generators for " + clazz.getTypeName() + ": "
					+ generators.get(clazz).size() + "\n");
			for (GenericAccessibleObject o : generators.get(clazz)) {
				result.append("  " + clazz.getTypeName() + " <- " + o + " "
						+ "\n");
			}
		}
		result.append("Modifiers:\n");
		for (GenericClass clazz : modifiers.keySet()) {
			result.append(" Modifiers for " + clazz.getSimpleName() + ": "
					+ modifiers.get(clazz).size() + "\n");
			for (GenericAccessibleObject o : getCallsFor(clazz)) {
				result.append(" " + clazz.getSimpleName() + " <- " + o + "\n");
			}
		}
		result.append("Test calls\n");
		for (GenericAccessibleObject testCall : testMethods) {
			result.append(" " + testCall + "\n");
		}
		return result.toString();
	}

}
