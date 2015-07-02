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

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.junit.CoverageAnalysis;
import org.evosuite.seeding.CastClassManager;
import org.evosuite.testcase.TestCase;
import org.evosuite.utils.generic.GenericAccessibleObject;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericMethod;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gordon Fraser
 *
 */
public class TestCluster {

	protected static final Logger logger = LoggerFactory.getLogger(TestCluster.class);


	/** Singleton instance */
	private static TestCluster instance = null;

	/** Set of all classes already analyzed */
	@Deprecated
	private final static Set<Class<?>> analyzedClasses = new LinkedHashSet<>();

	/** Methods we want to cover when testing */
	private final static Set<GenericAccessibleObject<?>> testMethods = new LinkedHashSet<>();

	/** Static information about how to generate types */
	private final static Map<GenericClass, Set<GenericAccessibleObject<?>>> generators = new LinkedHashMap<>();

	/** Cached information about how to generate types */
	private final static Map<GenericClass, Set<GenericAccessibleObject<?>>> generatorCache = new LinkedHashMap<>();

	/** Static information about how to modify types */
	private final static Map<GenericClass, Set<GenericAccessibleObject<?>>> modifiers = new LinkedHashMap<>();

	private static InheritanceTree inheritanceTree = null;

    private final EnvironmentTestClusterAugmenter environmentAugmenter;

    //-------------------------------------------------------------------

    protected TestCluster(){
        environmentAugmenter = new EnvironmentTestClusterAugmenter(this);
    }

    public void handleRuntimeAccesses(TestCase test) {
        environmentAugmenter.handleRuntimeAccesses(test);
    }

	/**
	 * @return the inheritancetree
	 */
	public static InheritanceTree getInheritanceTree() {
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
			try {
				Class<?> clazz = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(className);
				return !CoverageAnalysis.isTest(clazz);
			} catch (ClassNotFoundException e) {
				logger.info("Could not load class: ", className);
			}
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

	public static void reset() {
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
	 *            is assumed to have wildcard types
	 * @param call
	 */
	public void addGenerator(GenericClass target, GenericAccessibleObject<?> call) {
		if (!generators.containsKey(target))
			generators.put(target, new LinkedHashSet<GenericAccessibleObject<?>>());

		logger.debug("Adding generator for class " + target + ": " + call);
		generators.get(target).add(call);
	}

	/**
	 * Add a modifier reflection object
	 *
	 * @param target
	 *            is assumed to have wildcard types
	 * @param call
	 */
	public void addModifier(GenericClass target, GenericAccessibleObject<?> call) {
		if (!modifiers.containsKey(target))
			modifiers.put(target, new LinkedHashSet<GenericAccessibleObject<?>>());

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

	public void removeTestCall(GenericAccessibleObject<?> call) {
		testMethods.remove(call);
	}

	/**
	 * Add a new class observed at runtime for container methods
	 *
	 * @param clazz
	 */
	public void addCastClassForContainer(Class<?> clazz) {
		if (TestUsageChecker.canUse(clazz)) {
			CastClassManager.getInstance().addCastClass(clazz, 1);
			clearGeneratorCache(new GenericClass(clazz));
		}
	}

	/**
	 * Calculate and cache all generators for a particular type. All generic
	 * types on the generator are instantiated according to the produced type
	 *
	 * @param clazz
	 * @throws ConstructionFailedException
	 */
	private void cacheGenerators(GenericClass clazz) throws ConstructionFailedException {
		if (generatorCache.containsKey(clazz))
			return;
		logger.debug("1. Caching generators for " + clazz);

		Set<GenericAccessibleObject<?>> targetGenerators = new LinkedHashSet<GenericAccessibleObject<?>>();
		if (clazz.isObject()) {
			logger.debug("2. Target class is object: " + clazz);
			for (GenericClass generatorClazz : generators.keySet()) {
				if (generatorClazz.isObject()) {
					targetGenerators.addAll(generators.get(generatorClazz));
				}
			}
		} else {
			logger.debug("2. Target class is not object: " + clazz);
			for (GenericClass generatorClazz : generators.keySet()) {
				logger.debug("3. Considering original generator: " + generatorClazz
				        + " for " + clazz);

				if (generatorClazz.canBeInstantiatedTo(clazz)) {
					logger.debug("4. generator " + generatorClazz + " can be instantiated to " + clazz);
					GenericClass instantiatedGeneratorClazz = generatorClazz.getWithParametersFromSuperclass(clazz);
					logger.debug("Instantiated type: " + instantiatedGeneratorClazz
					        + " for " + generatorClazz + " and superclass " + clazz);
					for (GenericAccessibleObject<?> generator : generators.get(generatorClazz)) {
						logger.debug("5. current instantiated generator: " + generator);
						try {
							// Set owner type parameters from new return type
							GenericAccessibleObject<?> newGenerator = generator.copyWithOwnerFromReturnType(instantiatedGeneratorClazz);

							boolean hadTypeParameters = false;

							// Instantiate potential further type variables based on type variables of return type
							if (newGenerator.getOwnerClass().hasWildcardOrTypeVariables()) {
								logger.debug("Instantiating type parameters of owner type: "
								        + newGenerator.getOwnerClass());
								GenericClass concreteClass = newGenerator.getOwnerClass().getGenericInstantiation(clazz.getTypeVariableMap());
								newGenerator = newGenerator.copyWithNewOwner(concreteClass);
								hadTypeParameters = true;
							}

							// If it is a generic method, instantiate generic type variables for the produced class
							if (newGenerator.hasTypeParameters()) {
								logger.debug("Instantiating type parameters");
								/*
								 * TODO:
								 * public class Foo<X> {
								 *   public <X> Foo<X> getFoo() {
								 *     // ...
								 *   }
								 * }
								 *
								 * Here X and X are two different type variables, and these need to be matched here!
								 *
								 */
								newGenerator = newGenerator.getGenericInstantiationFromReturnValue(clazz);
								hadTypeParameters = true;
								// newGenerator = newGenerator.getGenericInstantiation(clazz);
							}

							logger.debug("Current generator: " + newGenerator);
							if ((!hadTypeParameters && generatorClazz.equals(clazz))
							        || clazz.isAssignableFrom(newGenerator.getGeneratedType())) {
								logger.debug("Got new generator: " + newGenerator
								        + " which generated: "
								        + newGenerator.getGeneratedClass());
								targetGenerators.add(newGenerator);
							} else if (logger.isDebugEnabled()) {
								logger.debug("New generator not assignable: "
								        + newGenerator);
								logger.debug("Had type parameters: " + hadTypeParameters);
								logger.debug("generatorClazz.equals(clazz): "
								        + generatorClazz.equals(clazz));
								try {
									logger.debug("clazz.isAssignableFrom("
									        + newGenerator.getGeneratedType() + "): ");
									logger.debug("                        "
									        + clazz.isAssignableFrom(newGenerator.getGeneratedType()));
								} catch (Throwable t) {
									logger.debug("Error: " + t);
								}
							}
						} catch (ConstructionFailedException e) {
							logger.debug("5. ERROR: " + e);
						}
					}
				} else {
					logger.debug("4. generator " + generatorClazz + " CANNOT be instantiated to " + clazz);
					for(GenericClass boundClass : generatorClazz.getGenericBounds()) {
						CastClassManager.getInstance().addCastClass(boundClass, 0);
					}
				}
			}
			logger.debug("Found generators for " + clazz + ": " + targetGenerators.size());
		}

		for (GenericAccessibleObject<?> targetGenerator : targetGenerators) {
			logger.debug("XXX Setting generator cache " + clazz + ": "
			        + String.valueOf(targetGenerator));
		}
		logger.debug("]");
		generatorCache.put(clazz, targetGenerators);
	}

	/**
	 * Forget everything we have cached
	 *
	 * @param target
	 */
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
			logger.debug("Is parameterized class");
			for (Entry<GenericClass, Set<GenericAccessibleObject<?>>> entry : modifiers.entrySet()) {
				logger.debug("Considering " + entry.getKey());
				//if (entry.getKey().canBeInstantiatedTo(clazz)) {

				if (entry.getKey().getWithWildcardTypes().isGenericSuperTypeOf(clazz)) {
					logger.debug(entry.getKey() + " can be instantiated to " + clazz);
					for (GenericAccessibleObject<?> modifier : entry.getValue()) {
						try {
							GenericAccessibleObject<?> newModifier = modifier.getGenericInstantiation(clazz);
							logger.debug("Adding new modifier: " + newModifier);
							genericModifiers.add(newModifier);
						} catch (ConstructionFailedException e) {
							// This may happen on generic methods with bounded types?
							logger.debug("Cannot be added: " + modifier);
						}
					}
				} else {
					logger.debug("Nope");
				}
				/*
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
									GenericClass concreteClass = newModifier.getOwnerClass().getGenericInstantiation(clazz.getTypeVariableMap());
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
				*/
			}
		} else {
			logger.debug("Is NOT parameterized class!");
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
		logger.debug("Getting calls for "+clazz);
		if (clazz.hasWildcardOrTypeVariables()) {
			logger.debug("Resolving generic type before getting modifiers");
			GenericClass concreteClass = clazz.getGenericInstantiation();
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
		logger.debug("Possible modifiers for " + clazz + ": " + calls);
		GenericAccessibleObject<?> call = Randomness.choice(calls);
		if (call.hasTypeParameters()) {
			logger.debug("Modifier has type parameters");
			call = call.getGenericInstantiation(clazz);
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
			logger.debug("Don't have that specific class, so have to check generic modifiers");
			all.addAll(determineGenericModifiersFor(clazz));
		} else {
			logger.debug("Got modifiers");
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
			TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass("java.lang."
			                                                                             + name);
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

		// Instantiate generic type
		if (clazz.hasWildcardOrTypeVariables()) {
			GenericClass concreteClass = clazz.getGenericInstantiation();
			if (!concreteClass.equals(clazz))
				return getGenerators(concreteClass, false);
		}

		if (isSpecialCase(clazz)) {
			return getGeneratorsForSpecialCase(clazz);
		}

		if (!hasGenerator(clazz))
			throw new ConstructionFailedException("No generators of type " + clazz);

		return generatorCache.get(clazz);
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
			if(!hasGenerator(clazz)) {
				throw new ConstructionFailedException("No generators of type " + clazz);
			}

			all.addAll(generatorCache.get(clazz));

			for (GenericAccessibleObject<?> call : all) {
				// TODO: Need to instantiate, or check?
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
			// This may happen e.g. for java.util.concurrent.ArrayBlockingQueue which has no default constructor
			if(calls.isEmpty()) {
				calls.addAll(all);
			}
		} else if (clazz.isAssignableTo(Number.class)) {
			logger.debug("Found special case " + clazz);

			Set<GenericAccessibleObject<?>> all = new LinkedHashSet<GenericAccessibleObject<?>>();
			if (!generatorCache.containsKey(clazz)) {
				cacheGenerators(clazz);
			}
			all.addAll(generatorCache.get(clazz));

			if (all.isEmpty()) {
				addNumericConstructor(clazz);
				all.addAll(generatorCache.get(clazz));
			}

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
			logger.debug("Generators for special case " + clazz + ": " + calls);
			// FIXXME: This is a workaround for the temporary workaround.
			if (calls.isEmpty()) {
				addNumericConstructor(clazz);
				return generatorCache.get(clazz);

			}
		}

		return calls;
	}

	/**
	 * FIXME: This is a workaround for a bug where Integer is not contained in
	 * the generatorCache, but there is a key. No idea how it comes to place
	 *
	 * @param clazz
	 */
	private void addNumericConstructor(GenericClass clazz) {
		if (!generatorCache.containsKey(clazz)) {
			generatorCache.put(clazz, new LinkedHashSet<GenericAccessibleObject<?>>());
		}
		if (!generators.containsKey(clazz)) {
			generators.put(clazz, new LinkedHashSet<GenericAccessibleObject<?>>());
		}
		logger.info("addNumericConstructor for class " + clazz);
		for (Constructor<?> constructor : clazz.getRawClass().getConstructors()) {
			if (constructor.getParameterTypes().length == 1) {
				Class<?> parameterClass = constructor.getParameterTypes()[0];
				if (!parameterClass.equals(String.class)) {
					GenericConstructor genericConstructor = new GenericConstructor(
					        constructor, clazz);
					generatorCache.get(clazz).add(genericConstructor);
					generators.get(clazz).add(genericConstructor);
				}
			}
		}
		logger.info("Constructors for class " + clazz + ": "
		        + generators.get(clazz).size());

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
	 * @return a collection of all cast classes stored in {@link CastClassManager}; cannot be <code>null</code>>.
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
	 * Randomly select one generator
	 *
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	public GenericAccessibleObject<?> getRandomGenerator(GenericClass clazz)
	        throws ConstructionFailedException {

		if (clazz.hasWildcardOrTypeVariables()) {
			GenericClass concreteClass = clazz.getGenericInstantiation();
			if(concreteClass.hasWildcardOrTypeVariables())
				throw new ConstructionFailedException("Could not found concrete instantiation of generic type");
			return getRandomGenerator(concreteClass);
		}


		GenericAccessibleObject<?> generator = null;
		if (isSpecialCase(clazz)) {
			Collection<GenericAccessibleObject<?>> generators = getGeneratorsForSpecialCase(clazz);
			if (generators.isEmpty()) {
				logger.warn("No generators for class: " + clazz);
			}
			generator = Randomness.choice(generators);
		} else {
			if (!hasGenerator(clazz))
				throw new ConstructionFailedException("No generators of type " + clazz);

			generator = Randomness.choice(generatorCache.get(clazz));
		}

		if (generator == null)
			throw new ConstructionFailedException("No generators of type " + clazz);

		if (generator.hasTypeParameters()) {
			generator = generator.getGenericInstantiation(clazz);
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
			GenericClass concreteClass = clazz.getGenericInstantiation();
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
			if (generator == null) {
				logger.warn("No generator for special case class: " + clazz);
				throw new ConstructionFailedException(
				        "Have no generators for special case: " + clazz);
			}
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
			generator = generator.copyWithNewOwner(generator.getOwnerClass().getGenericInstantiation());
		}
		if (generator.hasTypeParameters()) {
			logger.debug("Generator has a type parameter: " + generator);
			generator = generator.getGenericInstantiationFromReturnValue(clazz);
			if(!generator.getGeneratedClass().isAssignableTo(clazz)) {
				throw new ConstructionFailedException("Generics error");
			}
		}

		return generator;

	}

	/**
	 * Randomly select a generator for an Object.class instance
	 *
	 * @return a generator of type GenericAccessibleObject<?> or <code>null</code>
	 * @throws ConstructionFailedException
	 */
	public GenericAccessibleObject<?> getRandomObjectGenerator()
	        throws ConstructionFailedException {
		logger.debug("Getting random object generator");
		GenericAccessibleObject<?> generator = Randomness.choice(getObjectGenerators());
		if (generator == null) {
			// should NOT occur (from getObjectGenerators())
			logger.warn("Random object generator is null");
			throw new ConstructionFailedException("Random object generator is null");
		}

		if (generator.getOwnerClass().hasWildcardOrTypeVariables()) {
			logger.debug("Generator has wildcard or type: " + generator);
			GenericClass concreteClass = generator.getOwnerClass().getGenericInstantiation();
			generator = generator.copyWithNewOwner(concreteClass);
		}
		if (generator.hasTypeParameters()) {
			logger.debug("Generator has type parameters");

			generator = generator.getGenericInstantiation();
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
		if(testMethods.isEmpty()) {
			logger.debug("No more calls");
			// TODO: return null, or throw ConstructionFailedException?
			return null;
		}
		GenericAccessibleObject<?> choice = Randomness.choice(testMethods);
		logger.debug("Chosen call: " + choice);
		if (choice.getOwnerClass().hasWildcardOrTypeVariables()) {
			GenericClass concreteClass = choice.getOwnerClass().getGenericInstantiation();
			logger.debug("Concrete class is: " + concreteClass.getTypeName());
			choice = choice.copyWithNewOwner(concreteClass);
			logger.debug("Concrete class is: " + choice.getOwnerClass().getTypeName());
			logger.debug("Type variables: " + choice.getOwnerClass().getTypeVariableMap());
			logger.debug(Arrays.asList(choice.getTypeParameters()).toString());
			logger.debug("Chosen call with generic parameter set: " + choice);
			logger.debug("Call owner type: " + choice.getOwnerClass().getTypeName());
		}
		if (choice.hasTypeParameters()) {
			logger.debug("Instantiating chosen call: " + choice);
			choice = choice.getGenericInstantiation();
			logger.debug("Chosen instantiation: " + choice);
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
		List<GenericAccessibleObject<?>> result = new ArrayList<GenericAccessibleObject<?>>();
		//testMethods);
		for (GenericAccessibleObject<?> ao : testMethods) {
			if (ao.getOwnerClass().hasWildcardOrTypeVariables()) {
				try {
					GenericClass concreteClass = ao.getOwnerClass().getGenericInstantiation();
					result.add(ao.copyWithNewOwner(concreteClass));
				} catch (ConstructionFailedException e) {
					logger.debug("Failed to instantiate " + ao);
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
		if (!generatorCache.containsKey(clazz))
			return false;

		return !generatorCache.get(clazz).isEmpty();
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
			for (GenericAccessibleObject<?> o : modifiers.get(clazz)) { //getCallsFor(clazz, true)) {
				result.append(" " + clazz.getSimpleName() + " <- " + o + "\n");
			}
		}
		result.append("Test calls\n");
		for (GenericAccessibleObject<?> testCall : testMethods) {
			result.append(" " + testCall + "\n");
		}
		return result.toString();
	}

}
