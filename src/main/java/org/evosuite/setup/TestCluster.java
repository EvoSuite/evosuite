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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.testcase.ExecutionTracer;
import org.evosuite.utils.GenericAccessibleObject;
import org.evosuite.utils.GenericClass;
import org.evosuite.utils.GenericConstructor;
import org.evosuite.utils.GenericMethod;
import org.evosuite.utils.GenericUtils;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.ResourceList;
import org.junit.Test;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.gentyref.GenericTypeReflector;

/**
 * @author Gordon Fraser
 * 
 */
public class TestCluster {

	protected static final Logger logger = LoggerFactory.getLogger(TestCluster.class);

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
	private final static List<GenericAccessibleObject<?>> testMethods = new ArrayList<GenericAccessibleObject<?>>();

	/** Static information about how to generate types */
	private final static Map<GenericClass, Set<GenericAccessibleObject<?>>> generators = new LinkedHashMap<GenericClass, Set<GenericAccessibleObject<?>>>();

	/** Cached information about how to generate types */
	private final static Map<GenericClass, Set<GenericAccessibleObject<?>>> generatorCache = new LinkedHashMap<GenericClass, Set<GenericAccessibleObject<?>>>();

	/** Static information about how to modify types */
	private final static Map<GenericClass, Set<GenericAccessibleObject<?>>> modifiers = new LinkedHashMap<GenericClass, Set<GenericAccessibleObject<?>>>();

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
			return inheritanceTree.getSubclasses(Properties.TARGET_CLASS).contains(className);
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
			Class<?> clazz = TestGenerationContext.getClassLoader().loadClass(className);
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
				Class<?> clazz = TestGenerationContext.getClassLoader().loadClass(className);
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
				logger.info("Static: Could not find method clinit in : " + className);
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
	public void addGenerator(GenericClass target, GenericAccessibleObject<?> call) {
		if (!generators.containsKey(target))
			generators.put(target, new LinkedHashSet<GenericAccessibleObject<?>>());

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
	public void addModifier(GenericClass target, GenericAccessibleObject<?> call) {
		if (!modifiers.containsKey(target))
			modifiers.put(target, new LinkedHashSet<GenericAccessibleObject<?>>());

		// TODO: Need to add this call to all subclasses/superclasses?

		modifiers.get(target).add(call);
	}

	/**
	 * Add a test call
	 * 
	 * @return
	 */
	public void addTestCall(GenericAccessibleObject<?> call) {
		testMethods.add(call);
	}

	public void addCastClassForContainer(Class<?> clazz) {
		if (TestClusterGenerator.canUse(clazz)) {
			CastClassManager.getInstance().addCastClass(clazz, 1);
			clearGeneratorCache(new GenericClass(clazz));
		}
	}

	/**
	 * Calculate and cache all generators for a particular (possibly generic)
	 * type
	 * 
	 * @param clazz
	 * @throws ConstructionFailedException
	 */
	private void cacheGenerators(GenericClass clazz) throws ConstructionFailedException {
		if (generatorCache.containsKey(clazz))
			return;
		logger.debug("Caching generators for " + clazz);

		Set<GenericAccessibleObject<?>> targetGenerators = new LinkedHashSet<GenericAccessibleObject<?>>();
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
				//if (generatorClazz == clazz)
				//	continue;
				logger.debug("Considering original generator: " + generatorClazz);

				// First check if the raw classes are assignable
				if (clazz.getRawClass().isAssignableFrom(generatorClazz.getRawClass())) {
					logger.debug("Raw classes are assignable: " + generatorClazz
					        + " for " + clazz);

					// If the types are assignable, everything is fine
					if (clazz.isAssignableFrom(generatorClazz)) {
						logger.debug("Adding subtype generator: " + clazz
						        + " is assignable from " + generatorClazz.getTypeName());
						targetGenerators.addAll(generators.get(generatorClazz));
					} else {

						// If the types are not assignable, we need to check the generic type parameters
						logger.debug(clazz + " is not assignable from "
						        + generatorClazz.getTypeName());

						// generatorClazz can only be a subclass of clazz
						logger.info("Generator type1: " + generatorClazz);
						GenericClass newOwner = generatorClazz.getWithParametersFromSuperclass(clazz);
						logger.info("Resulting type: " + newOwner);
						if (newOwner == null)
							continue;

						logger.info("Generator type2: " + generatorClazz);

						// "newOwner" is the instantiated type of the return value
						// but we need the declaring class of the method!
						for (GenericAccessibleObject<?> generator : generators.get(generatorClazz)) {
							//						for (GenericAccessibleObject<?> generator : getGenerators(generatorClazz)) {
							logger.debug("Candidate generator: " + generator);
							if (generator.getOwnerClass().getNumParameters() == 0) {
								logger.debug("Owner class has no parameters, so we can only assume it would work: "
								        + generator.getName());
								logger.debug(generator
								        + " produces "
								        + clazz
								        + ": "
								        + clazz.isAssignableFrom(generator.getGeneratedType()));
								if (clazz.isAssignableFrom(generator.getGeneratedType()))
									targetGenerators.add(generator);
							} else {
								logger.debug("Trying to get parameters for generator "
								        + generator + " with new owner "
								        + newOwner.getTypeName());
								GenericAccessibleObject<?> newGenerator = generator.copyWithOwnerFromReturnType(newOwner);
								logger.debug("Instantiated generator: " + newGenerator);
								if (newGenerator.getOwnerClass().hasWildcardOrTypeVariables()) {
									GenericClass concreteClass = getGenericInstantiation(newGenerator.getOwnerClass(),
									                                                     clazz.getTypeVariableMap(),
									                                                     0);
									GenericAccessibleObject<?> gen = newGenerator.copyWithNewOwner(concreteClass);
									logger.debug("Got new generator after wildcards are instantiated: "
									        + gen);
									if (gen.hasTypeParameters()) {
										gen = getGenericGeneratorInstantiation(gen, clazz);
									}
									if (clazz.isAssignableFrom(gen.getGeneratedType())) {
										logger.debug("Instantiated type is assignable");
										targetGenerators.add(gen);
									} else {
										logger.debug("Instantiated type is not assignable");
									}
								} else {
									if (newGenerator.hasTypeParameters()) {
										newGenerator = getGenericGeneratorInstantiation(newGenerator,
										                                                clazz);
									}
									if (clazz.isAssignableFrom(newGenerator.getGeneratedType())) {
										logger.debug("Got new generator: " + newGenerator);
										logger.debug("Generates: "
										        + newGenerator.getGeneratedType());
										logger.debug("Can assign "
										        + newGenerator.getGeneratedType()
										        + " to " + clazz);
										targetGenerators.add(newGenerator);
									} else {
										logger.debug("New generator not assignable: "
										        + newGenerator);
									}
								}
								// TODO: Need to check if this is a generic method!
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
			/*
			logger.debug("Have no suitable generic generators, so using raw class");
			for (GenericClass generatorClazz : generators.keySet()) {
				if (clazz.getRawClass().isAssignableFrom(
						generatorClazz.getRawClass())) {
					targetGenerators.addAll(generators.get(generatorClazz));
				}
			}
			*/
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
	 * @throws ConstructionFailedException
	 */
	private Set<GenericAccessibleObject<?>> determineGenericModifiersFor(
	        GenericClass clazz) throws ConstructionFailedException {
		Set<GenericAccessibleObject<?>> genericModifiers = new LinkedHashSet<GenericAccessibleObject<?>>();
		if (clazz.isParameterizedType()) {
			for (Entry<GenericClass, Set<GenericAccessibleObject<?>>> entry : modifiers.entrySet()) {
				if (entry.getKey().getRawClass().equals(clazz.getRawClass())) {
					logger.debug("Considering raw assignable case: " + entry.getKey());
					List<Type> parameters = new ArrayList<Type>();
					List<Type> targetParameters = clazz.getParameterTypes();
					List<Type> modifierParameters = entry.getKey().getParameterTypes();
					if (targetParameters.size() != modifierParameters.size()) {
						continue;
					}

					boolean compatibleParameters = true;
					for (int i = 0; i < targetParameters.size(); i++) {
						logger.debug("Comparing target parameter "
						        + targetParameters.get(i) + " with modifier parameter "
						        + modifierParameters.get(i));
						Type modifierType = modifierParameters.get(i);
						Type targetType = targetParameters.get(i);
						// FIXME: Which one is the lhs and the rhs?
						// if (!TypeUtils.isAssignable(targetType, modifierType)) {
						//						if (!GenericClass.isAssignable(targetType, modifierType)) {
						if (!GenericClass.isAssignable(modifierType, targetType)) {
							compatibleParameters = false;
							logger.debug("Incompatible parameter: " + targetType
							        + " vs. " + modifierType);
							break;
						}
						parameters.add(targetType);
					}
					if (compatibleParameters) {
						logger.debug("Parameters compatible");
						Type[] actualParameters = new Type[parameters.size()];
						parameters.toArray(actualParameters);
						GenericClass newOwner = entry.getKey().getWithParameterTypes(actualParameters);
						for (GenericAccessibleObject<?> modifier : entry.getValue()) {
							logger.debug("Considering modifier: " + modifier);

							if (!modifier.getOwnerClass().isParameterizedType()) {
								logger.debug("Owner class has no parameters, so we can only assume it would work: "
								        + modifier);
								genericModifiers.add(modifier);
							} else {
								// TODO: FIXXME
								//if (modifier.getOwnerClass().getNumParameters() == 0) {
								//	logger.info("Skipping potentially problematic case of parameterized type without parameters (owner likely has types)");
								//	continue;
								//}

								GenericAccessibleObject<?> newModifier = modifier.copyWithOwnerFromReturnType(newOwner);
								logger.debug("Modifier with new owner: " + newModifier);
								if (newModifier.getOwnerClass().hasWildcardOrTypeVariables()) {
									GenericClass concreteClass = getGenericInstantiation(newModifier.getOwnerClass(),
									                                                     clazz.getTypeVariableMap(),
									                                                     0);
									GenericAccessibleObject<?> concreteNewModifier = newModifier.copyWithNewOwner(concreteClass);
									logger.debug("Modifier with new owner and instantiated types: "
									        + concreteNewModifier);
									genericModifiers.add(concreteNewModifier);
								} else {
									logger.debug("Adding modifier directly");
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
	 * @throws ConstructionFailedException
	 */
	public Set<GenericAccessibleObject<?>> getCallsFor(GenericClass clazz, boolean resolve)
	        throws ConstructionFailedException {
		if (clazz.hasWildcardOrTypeVariables()) {
			GenericClass concreteClass = getGenericInstantiation(clazz);
			if (!concreteClass.equals(clazz))
				return getCallsFor(concreteClass, false);
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

	public GenericAccessibleObject<?> getRandomCallFor(GenericClass clazz)
	        throws ConstructionFailedException {
		Set<GenericAccessibleObject<?>> calls = getCallsFor(clazz, true);
		if (calls.isEmpty())
			throw new ConstructionFailedException("No modifiers for " + clazz);

		GenericAccessibleObject<?> call = Randomness.choice(calls);
		if (call.hasTypeParameters()) {
			logger.debug("Modifier has type parameters");
			call = getGenericInstantiationWithCallee(call, clazz);
		}
		return call;
	}

	/**
	 * Get modifiers for special cases
	 * 
	 * @param clazz
	 * @return
	 * @throws ConstructionFailedException
	 */
	private Set<GenericAccessibleObject<?>> getCallsForSpecialCase(GenericClass clazz)
	        throws ConstructionFailedException {
		Set<GenericAccessibleObject<?>> all = new LinkedHashSet<GenericAccessibleObject<?>>();
		if (!modifiers.containsKey(clazz)) {
			all.addAll(determineGenericModifiersFor(clazz));
		} else {
			all.addAll(modifiers.get(clazz));
		}
		Set<GenericAccessibleObject<?>> calls = new LinkedHashSet<GenericAccessibleObject<?>>();

		if (clazz.isAssignableTo(Collection.class)) {
			for (GenericAccessibleObject<?> call : all) {
				if (call.isConstructor() && call.getNumParameters() == 0) {
					calls.add(call);
				} else if (call.isMethod()
				        && ((GenericMethod) call).getName().equals("add")
				        && call.getNumParameters() == 1) {
					calls.add(call);
				} else {
					if (Randomness.nextDouble() < Properties.P_SPECIAL_TYPE_CALL) {
						calls.add(call);
					}
				}
			}
		}

		else if (clazz.isAssignableTo(Map.class)) {
			for (GenericAccessibleObject<?> call : all) {
				if (call.isConstructor() && call.getNumParameters() == 0) {
					calls.add(call);
				} else if (call.isMethod()
				        && ((GenericMethod) call).getName().equals("put")) {
					calls.add(call);
				} else {
					if (Randomness.nextDouble() < Properties.P_SPECIAL_TYPE_CALL) {
						calls.add(call);
					}
				}
			}
		}

		else if (clazz.isAssignableTo(Number.class)) {
			if (modifiers.containsKey(clazz)) {
				for (GenericAccessibleObject<?> call : modifiers.get(clazz)) {
					if (!call.getName().startsWith("java.lang")
					        || Randomness.nextDouble() < Properties.P_SPECIAL_TYPE_CALL) {
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
			        || clazz.getName().equals(Properties.CLASS_PREFIX + "." + name)
			        || clazz.getName().equals(Properties.CLASS_PREFIX + "."
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
			TestGenerationContext.getClassLoader().loadClass("java.lang." + name);
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
	public Set<GenericAccessibleObject<?>> getGenerators() {
		Set<GenericAccessibleObject<?>> calls = new LinkedHashSet<GenericAccessibleObject<?>>();
		for (Set<GenericAccessibleObject<?>> generatorCalls : generators.values())
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
	public Set<GenericAccessibleObject<?>> getGenerators(GenericClass clazz,
	        boolean resolve) throws ConstructionFailedException {

		if (clazz.hasWildcardOrTypeVariables()) {
			GenericClass concreteClass = getGenericInstantiation(clazz);
			if (!concreteClass.equals(clazz))
				return getGenerators(concreteClass, false);
		}

		if (!hasGenerator(clazz))
			throw new ConstructionFailedException("No generators of type " + clazz);

		if (isSpecialCase(clazz)) {
			return getGeneratorsForSpecialCase(clazz);
		}

		return generatorCache.get(clazz);
	}

	public Set<GenericAccessibleObject<?>> getExactGenerators(GenericClass clazz)
	        throws ConstructionFailedException {

		if (clazz.hasWildcardOrTypeVariables()) {
			GenericClass concreteClass = getGenericInstantiation(clazz);
			return getExactGenerators(concreteClass);
		}

		if (!generators.containsKey(clazz))
			throw new ConstructionFailedException("No generators of type " + clazz);

		if (isSpecialCase(clazz)) {
			return getGeneratorsForSpecialCase(clazz);
		}

		return generators.get(clazz);
	}

	/**
	 * Predetermined generators for special cases
	 * 
	 * @param clazz
	 * @return
	 * @throws ConstructionFailedException
	 */
	private Set<GenericAccessibleObject<?>> getGeneratorsForSpecialCase(GenericClass clazz)
	        throws ConstructionFailedException {
		logger.debug("Getting generator for special case: " + clazz);
		Set<GenericAccessibleObject<?>> calls = new LinkedHashSet<GenericAccessibleObject<?>>();

		if (clazz.isAssignableTo(Collection.class) || clazz.isAssignableTo(Map.class)) {
			Set<GenericAccessibleObject<?>> all = new LinkedHashSet<GenericAccessibleObject<?>>();
			if (!generatorCache.containsKey(clazz)) {
				cacheGenerators(clazz);
			}
			all.addAll(generatorCache.get(clazz));

			for (GenericAccessibleObject<?> call : all) {
				if (call.isConstructor() && call.getNumParameters() == 0) {
					calls.add(call);
				} else if (!Collection.class.isAssignableFrom(call.getDeclaringClass())
				        && !Map.class.isAssignableFrom(call.getDeclaringClass())) {
					// Methods that return collections are candidates, unless they are methods of the collections
					calls.add(call);
				} else if (!call.getDeclaringClass().getName().startsWith("java")) {
					calls.add(call);
				} else {
					if (Randomness.nextDouble() < Properties.P_SPECIAL_TYPE_CALL) {
						calls.add(call);
					}
				}
			}
		} else if (clazz.isAssignableTo(Number.class)) {
			Set<GenericAccessibleObject<?>> all = new LinkedHashSet<GenericAccessibleObject<?>>();
			if (!generatorCache.containsKey(clazz)) {
				cacheGenerators(clazz);
			}
			all.addAll(generatorCache.get(clazz));

			for (GenericAccessibleObject<?> call : all) {
				if (call.isConstructor() && call.getNumParameters() == 1) {
					if (!((GenericConstructor) call).getRawParameterTypes()[0].equals(String.class))
						calls.add(call);
				} else if (call.isField()) {
					calls.add(call);
				} else {
					if (Randomness.nextDouble() < Properties.P_SPECIAL_TYPE_CALL) {
						calls.add(call);
					}
				}
			}
		}

		return calls;
	}

	public GenericClass getGenericInstantiation(GenericClass clazz)
	        throws ConstructionFailedException {
		return getGenericInstantiation(clazz, 0);
	}

	/**
	 * Instantiate type parameters for a parameterized type
	 * 
	 * @param clazz
	 * @return
	 * @throws ConstructionFailedException
	 */
	public GenericClass getGenericInstantiation(GenericClass clazz, int recursionLevel)
	        throws ConstructionFailedException {
		return getGenericInstantiation(clazz, new HashMap<TypeVariable<?>, Type>(),
		                               recursionLevel);
	}

	/**
	 * If the inner class already has typevariables instantiated, then we need
	 * to use the same types here
	 * 
	 * @param clazz
	 * @param innerClass
	 * @param recursionLevel
	 * @return
	 * @throws ConstructionFailedException
	 */
	public GenericClass getGenericInstantiation(GenericClass clazz,
	        Map<TypeVariable<?>, Type> typeMap, int recursionLevel)
	        throws ConstructionFailedException {
		logger.debug("Getting generic instantiation of " + clazz.getTypeName()
		        + " with type map " + typeMap);
		if (clazz.isArray()) {
			GenericClass componentClass = clazz.getComponentClass();
			if (componentClass.hasWildcardOrTypeVariables()) {
				componentClass = getGenericInstantiation(componentClass, recursionLevel);
				return clazz.getWithComponentClass(componentClass);
			}
			return clazz;

		} else if (clazz.getType() instanceof TypeVariable<?>) {
			logger.debug("Is type variable ");

			GenericClass selectedClass = CastClassManager.getInstance().selectCastClass((TypeVariable<?>) clazz.getType(),
			                                                                            true,
			                                                                            typeMap);
			if (selectedClass.hasWildcardOrTypeVariables()) {
				return getGenericInstantiation(selectedClass, recursionLevel + 1);
			}
			return selectedClass;
		} else if (!clazz.isParameterizedType())
			return clazz;
		// else if(clazz.isClass())
		//	return clazz;

		List<Type> parameterTypes = new ArrayList<Type>();
		List<TypeVariable<?>> typeParameters = clazz.getTypeVariables();
		int numParam = 0;
		Map<TypeVariable<?>, Type> mergedTypeMap = new HashMap<TypeVariable<?>, Type>();
		mergedTypeMap.putAll(typeMap);
		mergedTypeMap.putAll(clazz.getTypeVariableMap());
		boolean isClass = clazz.isClass();

		for (java.lang.reflect.Type parameterType : clazz.getParameterTypes()) {
			logger.debug("Parameter type " + parameterType + " of class "
			        + parameterType.getClass());
			if (parameterType instanceof WildcardType) {
				if (((WildcardType) parameterType).getLowerBounds().length == 0) {
					Type[] bounds = ((WildcardType) parameterType).getUpperBounds();
					if (bounds.length == 1 && bounds[0] == Object.class) {
						logger.info("Using typevariable instead of wildcard because there are no bounds on wildcard");
						parameterTypes.add(getRandomCastClass(
						                                      typeParameters.get(numParam),
						                                      recursionLevel,
						                                      mergedTypeMap).getType());
						if (isClass
						        && GenericTypeReflector.erase(parameterTypes.get(numParam)).equals(Class.class)) {
							throw new ConstructionFailedException(
							        "Cannot instantiate a Class with a Class");
						}
						numParam++;
						continue;
					}
				}
				parameterTypes.add(getRandomCastClass((WildcardType) parameterType,
				                                      recursionLevel, typeMap).getType());
			} else if (parameterType instanceof TypeVariable) {
				if (typeMap.containsKey(parameterType)) {
					parameterTypes.add(typeMap.get(parameterType));
				} else {
					parameterTypes.add(getRandomCastClass(
					                                      (TypeVariable<?>) parameterType,
					                                      recursionLevel + 1, typeMap).getType());
				}
			} else if (parameterType instanceof GenericArrayType) {
				logger.debug("ADDING GENERIC ARRAY PARAMETER: " + parameterType);
				parameterTypes.add(parameterType);
			} else {
				parameterTypes.add(parameterType);
			}
			if (isClass
			        && GenericTypeReflector.erase(parameterTypes.get(numParam)).equals(Class.class)) {
				throw new ConstructionFailedException(
				        "Cannot instantiate a Class with a Class");
			}
			numParam++;
		}

		if (clazz.hasOwnerType()) {
			clazz = clazz.getWithOwnerType(getGenericInstantiation(clazz.getOwnerType(),
			                                                       clazz.getWithParameterTypes(parameterTypes).getTypeVariableMap(),
			                                                       recursionLevel));
		}

		return clazz.getWithParameterTypes(parameterTypes);
	}

	public GenericAccessibleObject<?> getGenericInstantiation(
	        GenericAccessibleObject<?> accessibleObject)
	        throws ConstructionFailedException {
		logger.debug("Getting random generic instantiation of method: "
		        + accessibleObject);
		GenericAccessibleObject<?> copy = accessibleObject.copy();
		List<GenericClass> typeParameters = new ArrayList<GenericClass>();

		// TODO: The bounds of this type parameter need to be updataed for the owner of the call
		// which may instantiate some of the type parameters
		for (TypeVariable<?> parameter : accessibleObject.getTypeParameters()) {
			GenericClass concreteType = getRandomCastClass(parameter,
			                                               0,
			                                               copy.getOwnerClass().getTypeVariableMap());
			logger.debug("Setting parameter " + parameter + " to type "
			        + concreteType.getTypeName());
			typeParameters.add(concreteType);
		}
		copy.setTypeParameters(typeParameters);

		return copy;
	}

	public GenericAccessibleObject<?> getGenericInstantiationWithCallee(
	        GenericAccessibleObject<?> accessibleObject, GenericClass calleeType)
	        throws ConstructionFailedException {
		logger.debug("Getting generic instantiation for callee " + calleeType
		        + " of method: " + accessibleObject + " for callee " + calleeType);
		GenericAccessibleObject<?> copy = accessibleObject.copy();
		Class<?> rawClass = copy.getRawGeneratedType();

		TypeVariable<?>[] returnVariables = rawClass.getTypeParameters();

		List<Type> calleeTypes = calleeType.getParameterTypes();

		Map<TypeVariable<?>, Type> concreteTypes = calleeType.getTypeVariableMap();
		for (int i = 0; i < calleeTypes.size() && i < returnVariables.length; i++) {
			concreteTypes.put(returnVariables[i], calleeTypes.get(i));
		}

		List<GenericClass> typeParameters = new ArrayList<GenericClass>();
		for (TypeVariable<?> parameter : accessibleObject.getTypeParameters()) {
			if (concreteTypes.containsKey(parameter)) {
				GenericClass concreteType = new GenericClass(concreteTypes.get(parameter));
				logger.debug("(R) Setting parameter " + parameter + " to type "
				        + concreteType.getTypeName());
				typeParameters.add(concreteType);
			} else {
				GenericClass concreteType = getRandomCastClass(parameter, 0,
				                                               concreteTypes);
				logger.debug("(I) Setting parameter " + parameter + " to type "
				        + concreteType.getTypeName());
				typeParameters.add(concreteType);
			}
		}
		copy.setTypeParameters(typeParameters);

		return copy;
	}

	public GenericAccessibleObject<?> getGenericGeneratorInstantiation(
	        GenericAccessibleObject<?> accessibleObject, GenericClass generatedType)
	        throws ConstructionFailedException {
		logger.debug("Getting generic instantiation for generator " + generatedType
		        + " of method: " + accessibleObject + " to generate " + generatedType);
		GenericAccessibleObject<?> copy = accessibleObject.copy();

		Map<TypeVariable<?>, Type> concreteTypes = generatedType.getTypeVariableMap();
		logger.debug("Generic returned Type: "
		        + accessibleObject.getGenericGeneratedType());
		logger.debug("Type variables of generated Type: " + concreteTypes);
		Type genericReturnType = accessibleObject.getGenericGeneratedType();

		// for(Entry<TypeVariable<?>, Type> entry : concreteTypes.entrySet()) {
		// 	genericReturnType = GenericUtils.replaceTypeVariableByName(genericReturnType, entry.getKey(), entry.getValue());
		// }
		// logger.debug("Updated with type variables of generated Type: "+genericReturnType);

		if (genericReturnType instanceof ParameterizedType
		        && generatedType.isParameterizedType()) {
			logger.debug("Return value is a parameterized type, matching variables");
			concreteTypes.putAll(GenericUtils.getMatchingTypeParameters((ParameterizedType) generatedType.getType(),
			                                                            (ParameterizedType) genericReturnType));
		} else if (genericReturnType instanceof TypeVariable<?>) {
			logger.debug("Return value is a type variable, checking if the bounds match the required type");
			TypeVariable<?> tvar = (TypeVariable<?>) genericReturnType;
			if (GenericUtils.isAssignable(generatedType.getType(), tvar)) {
				genericReturnType = generatedType.getType();
				logger.debug("Returning type variable, setting to " + genericReturnType);
			} else {
				logger.debug("They don't");
				for (Type boundType : tvar.getBounds()) {
					Type resolvedBoundType = GenericUtils.replaceTypeVariable(boundType,
					                                                          tvar,
					                                                          generatedType.getType());
					if (!GenericClass.isAssignable(resolvedBoundType,
					                               generatedType.getType())) {
						logger.debug("Not assignable: " + generatedType.getType()
						        + " to bound " + resolvedBoundType);
						break;
					}
				}
			}
		}

		// logger.debug("Actually returned Type: "+accessibleObject.getGeneratedType());
		if (genericReturnType instanceof ParameterizedType) {
			logger.debug("Return value is a parameterized type");
			logger.debug("Type mapping: " + concreteTypes);
			ParameterizedType pType = (ParameterizedType) genericReturnType;

			for (Type t : pType.getActualTypeArguments()) {
				if (t instanceof TypeVariable<?>) {
					TypeVariable<?> var = (TypeVariable<?>) t;
					if (concreteTypes.containsKey(var))
						continue;

					//if(generatedType.getParameterTypes().size() > pos) {
					//	Type actualType = generatedType.getParameterTypes().get(pos);
					//	concreteTypes.put(var, actualType);
					//}// else {
					GenericClass castClass = getRandomCastClass(var, 1, concreteTypes);
					concreteTypes.put(var, castClass.getType());
					//}
				}
			}
		}
		//logger.debug("Updated with type variables of random types: "+genericReturnType);

		List<GenericClass> typeParameters = new ArrayList<GenericClass>();
		for (TypeVariable<?> parameter : accessibleObject.getTypeParameters()) {
			// If type variable is assigned to type variable
			logger.debug("Looking for type variable " + parameter);
			if (concreteTypes.containsKey(parameter)) {
				GenericClass concreteType = new GenericClass(concreteTypes.get(parameter));
				logger.debug("(R) Setting parameter " + parameter + " to type "
				        + concreteType.getTypeName());
				typeParameters.add(concreteType);
				for (Type bound : parameter.getBounds()) {
					if (!GenericClass.isAssignable(bound, concreteType.getType()))
						throw new ConstructionFailedException("Generics error");
				}
			} else {
				for (TypeVariable<?> otherVar : concreteTypes.keySet()) {
					logger.debug(parameter + " vs " + otherVar);
					if (otherVar.equals(parameter)) {
						logger.debug("-> Equal");
					} else {
						logger.debug("-> Not equal");
					}
					logger.debug(parameter.getName() + " vs " + otherVar.getName());
					logger.debug(parameter.getGenericDeclaration() + " vs "
					        + otherVar.getGenericDeclaration());
					logger.debug(Arrays.asList(parameter.getBounds()) + " vs "
					        + Arrays.asList(otherVar.getBounds()));
				}
				GenericClass concreteType = getRandomCastClass(parameter, 0,
				                                               concreteTypes);
				logger.debug("(I) Setting parameter " + parameter + " to type "
				        + concreteType.getTypeName());
				typeParameters.add(concreteType);
			}
		}
		copy.setTypeParameters(typeParameters);
		logger.debug("Resulting generator: " + copy + " and owner "
		        + copy.getOwnerClass());

		return copy;
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
	public Set<GenericAccessibleObject<?>> getModifiers() {
		Set<GenericAccessibleObject<?>> calls = new LinkedHashSet<GenericAccessibleObject<?>>();
		for (Set<GenericAccessibleObject<?>> modifierCalls : modifiers.values())
			calls.addAll(modifierCalls);

		return calls;
	}

	/**
	 * Determine the set of generators for an Object.class instance
	 * 
	 * @param target
	 * @return
	 */
	public Set<GenericAccessibleObject<?>> getObjectGenerators() {
		// TODO: Use probabilities based on distance to SUT
		Set<GenericAccessibleObject<?>> result = new LinkedHashSet<GenericAccessibleObject<?>>();
		List<GenericClass> classes = new ArrayList<GenericClass>(
		        CastClassManager.getInstance().getCastClasses());
		for (GenericClass clazz : classes) {
			try {
				result.addAll(getGenerators(clazz, true));
			} catch (ConstructionFailedException e) {
				// ignore
			}
		}
		try {
			result.addAll(getGenerators(new GenericClass(Object.class), true));
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
	 * @throws ConstructionFailedException
	 */
	private GenericClass getRandomCastClass(WildcardType targetType, int recursionLevel,
	        Map<TypeVariable<?>, Type> ownerVariableMap)
	        throws ConstructionFailedException {
		boolean allowRecursion = recursionLevel <= Properties.MAX_GENERIC_DEPTH;
		GenericClass castClass = CastClassManager.getInstance().selectCastClass(targetType,
		                                                                        allowRecursion,
		                                                                        ownerVariableMap);
		if (castClass == null) {
			throw new ConstructionFailedException("Found no cast class for " + targetType);
		}
		logger.debug("Got cast class " + castClass + ", recursion allowed: "
		        + allowRecursion);
		if (castClass.hasWildcardOrTypeVariables()) {
			return getGenericInstantiation(castClass, recursionLevel + 1);
		}
		return castClass;
	}

	private GenericClass getRandomCastClass(TypeVariable<?> targetType,
	        int recursionLevel, Map<TypeVariable<?>, Type> ownerVariableMap)
	        throws ConstructionFailedException {
		boolean allowRecursion = recursionLevel <= Properties.MAX_GENERIC_DEPTH;
		logger.debug("Getting random cast class for type variable " + targetType
		        + " with bounds " + Arrays.asList(targetType.getBounds()) + " and map "
		        + ownerVariableMap);
		GenericClass castClass = CastClassManager.getInstance().selectCastClass(targetType,
		                                                                        allowRecursion,
		                                                                        ownerVariableMap);
		if (castClass.hasWildcardOrTypeVariables()) {
			logger.debug("Cast class has generic type, getting concrete instance");
			return getGenericInstantiation(castClass, recursionLevel + 1);
		}
		assert (castClass != null);
		assert (castClass.getRawClass() != null);
		assert (castClass.getType() != null);
		return castClass;
	}

	/**
	 * Randomly select one generator
	 * 
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	public GenericAccessibleObject<?> getRandomGenerator(GenericClass clazz)
	        throws ConstructionFailedException {

		if (clazz.hasWildcardOrTypeVariables()) {
			GenericClass concreteClass = getGenericInstantiation(clazz);
			return getRandomGenerator(concreteClass);
		}

		if (!hasGenerator(clazz))
			throw new ConstructionFailedException("No generators of type " + clazz);

		GenericAccessibleObject<?> generator = null;
		if (isSpecialCase(clazz)) {
			generator = Randomness.choice(getGeneratorsForSpecialCase(clazz));
		} else {
			generator = Randomness.choice(generatorCache.get(clazz));
		}

		if (generator.hasTypeParameters()) {
			generator = getGenericGeneratorInstantiation(generator, clazz);
		}
		return generator;
	}

	/**
	 * Randomly select one generator
	 * 
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	public GenericAccessibleObject<?> getRandomGenerator(GenericClass clazz,
	        Set<GenericAccessibleObject<?>> excluded) throws ConstructionFailedException {

		logger.debug("Getting random generator for " + clazz);

		// Instantiate generics
		if (clazz.hasWildcardOrTypeVariables()) {
			logger.debug("Target class is generic: " + clazz);
			GenericClass concreteClass = getGenericInstantiation(clazz);
			if (!concreteClass.equals(clazz)) {
				logger.debug("Target class is generic: " + clazz
				        + ", getting instantiation " + concreteClass);
				return getRandomGenerator(concreteClass, excluded);
			}
		}

		GenericAccessibleObject<?> generator = null;

		// Collection, Map, Number
		if (isSpecialCase(clazz)) {
			generator = Randomness.choice(getGeneratorsForSpecialCase(clazz));
			if (generator == null)
				throw new ConstructionFailedException(
				        "Have no generators for special case: " + clazz);

		} else {
			cacheGenerators(clazz);

			Set<GenericAccessibleObject<?>> candidates = new LinkedHashSet<GenericAccessibleObject<?>>(
			        generatorCache.get(clazz));
			int before = candidates.size();
			candidates.removeAll(excluded);
			logger.debug("Candidate generators for " + clazz + ": " + candidates.size());

			if (candidates.isEmpty())
				throw new ConstructionFailedException("No generators left for " + clazz
				        + " - in total there are " + before);

			generator = Randomness.choice(candidates);
			logger.debug("Chosen generator: " + generator);
		}
		if (generator.getOwnerClass().hasWildcardOrTypeVariables()) {
			logger.debug("Owner class has a wildcard: " + clazz.getTypeName());
			generator = generator.copyWithNewOwner(getGenericInstantiation(generator.getOwnerClass()));
		}
		if (generator.hasTypeParameters()) {
			logger.debug("Generator has a type parameter: " + generator);
			generator = getGenericGeneratorInstantiation(generator, clazz);
		}

		return generator;

	}

	/**
	 * Randomly select a generator for an Object.class instance
	 * 
	 * @param target
	 * @return
	 * @throws ConstructionFailedException
	 */
	public GenericAccessibleObject<?> getRandomObjectGenerator()
	        throws ConstructionFailedException {
		logger.debug("Getting random object generator");
		GenericAccessibleObject<?> generator = Randomness.choice(getObjectGenerators());
		if (generator.getOwnerClass().hasWildcardOrTypeVariables()) {
			logger.debug("Generator has wildcard or type: " + generator);
			GenericClass concreteClass = getGenericInstantiation(generator.getOwnerClass());
			generator = generator.copyWithNewOwner(concreteClass);
		}
		if (generator.hasTypeParameters()) {
			logger.debug("Generator has type parameters");

			generator = getGenericInstantiation(generator);
		}

		return generator;

	}

	/**
	 * Get random method or constructor of unit under test
	 * 
	 * @return
	 * @throws ConstructionFailedException
	 */
	public GenericAccessibleObject<?> getRandomTestCall()
	        throws ConstructionFailedException {
		GenericAccessibleObject<?> choice = Randomness.choice(testMethods);
		logger.debug("Chosen call: " + choice);
		if (choice.getOwnerClass().hasWildcardOrTypeVariables()) {
			GenericClass concreteClass = getGenericInstantiation(choice.getOwnerClass());
			logger.debug("Concrete class is: " + concreteClass.getTypeName());
			choice = choice.copyWithNewOwner(concreteClass);
			logger.debug("Concrete class is: " + choice.getOwnerClass().getTypeName());
			logger.debug("Type variables: " + choice.getOwnerClass().getTypeVariableMap());
			logger.debug(Arrays.asList(choice.getTypeParameters()).toString());
			logger.debug("Chosen call with generic parameter set: " + choice);
			logger.debug("Call owner type: " + choice.getOwnerClass().getTypeName());
		}
		if (choice.hasTypeParameters()) {
			choice = getGenericInstantiation(choice);
		}
		return choice;
	}

	public int getNumTestCalls() {
		return testMethods.size();
	}

	/**
	 * Get a list of all test calls (i.e., constructors and methods)
	 * 
	 * @return
	 * @throws ConstructionFailedException
	 */
	public List<GenericAccessibleObject<?>> getTestCalls() {
		// TODO: Check for generic methods
		List<GenericAccessibleObject<?>> result = new ArrayList<GenericAccessibleObject<?>>(
		        testMethods);
		for (GenericAccessibleObject<?> ao : testMethods) {
			if (ao.getOwnerClass().hasWildcardOrTypeVariables()) {
				try {
					GenericClass concreteClass = getGenericInstantiation(ao.getOwnerClass());
					result.add(ao.copyWithNewOwner(concreteClass));
				} catch (ConstructionFailedException e) {
					logger.warn(e.getMessage());
				}
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
		try {
			cacheGenerators(clazz);
		} catch (ConstructionFailedException e) {
			// TODO
		}
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
	 * Some standard classes need to be treated specially to increase
	 * performance
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
			for (GenericAccessibleObject<?> o : generators.get(clazz)) {
				result.append("  " + clazz.getTypeName() + " <- " + o + " " + "\n");
			}
		}
		result.append("Modifiers:\n");
		for (GenericClass clazz : modifiers.keySet()) {
			result.append(" Modifiers for " + clazz.getSimpleName() + ": "
			        + modifiers.get(clazz).size() + "\n");
			try {
				for (GenericAccessibleObject<?> o : getCallsFor(clazz, true)) {
					result.append(" " + clazz.getSimpleName() + " <- " + o + "\n");
				}
			} catch (ConstructionFailedException e) {
				result.append("ERROR");
			}
		}
		result.append("Test calls\n");
		for (GenericAccessibleObject<?> testCall : testMethods) {
			result.append(" " + testCall + "\n");
		}
		return result.toString();
	}

}
