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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.testcase.ExecutionTracer;
import org.evosuite.testcase.GenericClass;
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
	private final static List<AccessibleObject> testMethods = new ArrayList<AccessibleObject>();

	/** Static information about how to generate types */
	private final static Map<GenericClass, Set<AccessibleObject>> generators = new LinkedHashMap<GenericClass, Set<AccessibleObject>>();

	/** Cached information about how to generate types */
	private final static Map<GenericClass, Set<AccessibleObject>> generatorCache = new LinkedHashMap<GenericClass, Set<AccessibleObject>>();

	/** Cached information about how to modify types */
	private final static Map<Class<?>, Set<AccessibleObject>> modifiers = new LinkedHashMap<Class<?>, Set<AccessibleObject>>();

	/** Classes to which there are cast statements */
	private static Set<String> castClassNames = new LinkedHashSet<String>();

	private static Set<GenericClass> castClasses = new LinkedHashSet<GenericClass>();

	private static InheritanceTree inheritanceTree = null;

	/**
	 * @return the inheritancetree
	 */
	protected static InheritanceTree getInheritanceTree() {
		return inheritanceTree;
	}

	/**
	 * @param inheritancetree
	 *            the inheritancetree to set
	 */
	protected static void setInheritanceTree(InheritanceTree inheritancetree) {
		inheritanceTree = inheritancetree;
	}

	/**
	 * Instance accessor
	 * 
	 * @return
	 */
	public static synchronized TestCluster getInstance() {
		if (instance == null) {
			instance = new TestCluster();
			instance.init();
		}

		// TODO: Need property to switch between test clusters

		return instance;
	}

	protected void init() {
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

	private static List<String> finalClasses = new ArrayList<String>();

	private static Set<Method> staticInitializers = new LinkedHashSet<Method>();

	public static void setCastClasses(Set<String> classes) {
		castClasses.clear();
		castClassNames.clear();

		for (String castClass : classes) {
			try {
				Class<?> clazz = TestGenerationContext.getClassLoader().loadClass(castClass);
				if (!clazz.isAssignableFrom(Properties.getTargetClass())) {
					castClasses.add(new GenericClass(clazz));
					castClassNames.add(castClass);
				}
			} catch (ClassNotFoundException e) {
				// Ignore
			}
		}
	}

	public static Set<String> getCastClasses() {
		return castClassNames;
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
		castClassNames.clear();
		castClasses.clear();

		instance = null;
	}

	public static void registerStaticInitializer(String className) {
		finalClasses.add(className);
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

	/**
	 * Unload all classes; perform cleanup
	 */
	public void resetCluster() {
		analyzedClasses.clear();
		testMethods.clear();
		generators.clear();
		generatorCache.clear();
		modifiers.clear();
		castClassNames.clear();
		castClasses.clear();
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
	 * @return the analyzedClasses
	 */
	public Set<Class<?>> getAnalyzedClasses() {
		return analyzedClasses;
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
		Collection<String> resources = ResourceList.getAllResources(pattern);
		resources.addAll(ResourceList.getBootResources(pattern));

		Set<String> classes = new LinkedHashSet<String>();
		for (String className : resources) {
			classes.add(className.replace(".class", "").replace("/", "."));
		}

		return classes;
	}

	private void cacheGenerators(GenericClass clazz) {
		if (generatorCache.containsKey(clazz))
			return;
		Set<AccessibleObject> targetGenerators = new LinkedHashSet<AccessibleObject>();

		if (clazz.isObject()) {
			for (GenericClass generatorClazz : generators.keySet()) {
				if (generatorClazz.isObject()) {
					//System.out.println(clazz.getTypeName() + " can be assigned from "
					//        + generatorClazz.getTypeName());
					targetGenerators.addAll(generators.get(generatorClazz));
				}
			}
		} else {
			for (GenericClass generatorClazz : generators.keySet()) {
				if (clazz.isAssignableFrom(generatorClazz)) {
					logger.debug("Adding subtype generator: "+clazz+" is assignable from "+generatorClazz);
					targetGenerators.addAll(generators.get(generatorClazz));
				}
			}
		}
		generatorCache.put(clazz, targetGenerators);
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
	 * Randomly select one generator
	 * 
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	public AccessibleObject getRandomGenerator(GenericClass clazz)
	        throws ConstructionFailedException {
		if (!hasGenerator(clazz))
			throw new ConstructionFailedException("No generators of type " + clazz);

		return Randomness.choice(generatorCache.get(clazz));
	}

	/**
	 * Randomly select one generator
	 * 
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	public AccessibleObject getRandomGenerator(GenericClass clazz,
	        Set<AccessibleObject> excluded) throws ConstructionFailedException {
		cacheGenerators(clazz);
		Set<AccessibleObject> candidates = new LinkedHashSet<AccessibleObject>(
		        generatorCache.get(clazz));
		int before = candidates.size();
		candidates.removeAll(excluded);
		if (candidates.isEmpty())
			throw new ConstructionFailedException("No generators left for "+clazz+" - in total there are "+before);

		return Randomness.choice(candidates);

	}

	/**
	 * Randomly select a generator for an Object.class instance
	 * 
	 * @param target
	 * @return
	 */
	public AccessibleObject getRandomObjectGenerator() {
		return Randomness.choice(getObjectGenerators());
	}

	/**
	 * Determine the set of generators for an Object.class instance
	 * 
	 * @param target
	 * @return
	 */
	public Set<AccessibleObject> getObjectGenerators() {
		Set<AccessibleObject> result = new LinkedHashSet<AccessibleObject>();
		for (GenericClass clazz : castClasses) {
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
	 * Get a list of all generator objects for the type
	 * 
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	public Set<AccessibleObject> getGenerators(GenericClass clazz)
	        throws ConstructionFailedException {
		if (!hasGenerator(clazz))
			throw new ConstructionFailedException("No generators of type " + clazz);

		return generatorCache.get(clazz);
	}

	/**
	 * Retrieve all generators
	 * 
	 * @return
	 */
	public Set<AccessibleObject> getGenerators() {
		Set<AccessibleObject> calls = new LinkedHashSet<AccessibleObject>();
		for (Set<AccessibleObject> generatorCalls : generators.values())
			calls.addAll(generatorCalls);

		return calls;
	}

	/**
	 * Retrieve all modifiers
	 * 
	 * @return
	 */
	public Set<AccessibleObject> getModifiers() {
		Set<AccessibleObject> calls = new LinkedHashSet<AccessibleObject>();
		for (Set<AccessibleObject> modifierCalls : modifiers.values())
			calls.addAll(modifierCalls);

		return calls;
	}

	/**
	 * Return all calls that have a parameter with given type
	 * 
	 * @param type
	 * @return
	 */
	public Set<AccessibleObject> getCallsFor(Class<?> clazz) {
		if (!modifiers.containsKey(clazz))
			return new LinkedHashSet<AccessibleObject>();

		return modifiers.get(clazz);
	}

	/**
	 * Get random method or constructor of unit under test
	 * 
	 * @return
	 * @throws ConstructionFailedException
	 */
	public AccessibleObject getRandomTestCall() {
		return Randomness.choice(testMethods);
	}

	/**
	 * Get a list of all test calls (i.e., constructors and methods)
	 * 
	 * @return
	 */
	public List<AccessibleObject> getTestCalls() {
		return new ArrayList<AccessibleObject>(testMethods);
	}

	/**
	 * Add a test call
	 * 
	 * @return
	 */
	public void addTestCall(AccessibleObject call) {
		testMethods.add(call);
	}

	/**
	 * Add a generator reflection object
	 * 
	 * @param target
	 * @param call
	 */
	public void addGenerator(GenericClass target, AccessibleObject call) {
		if (!generators.containsKey(target))
			generators.put(target, new LinkedHashSet<AccessibleObject>());

		// TODO: Need to add this call to all subclasses/superclasses?
		logger.debug("Adding generator for class "+target+": "+call);
		generators.get(target).add(call);
	}

	/**
	 * Add a generator reflection object
	 * 
	 * @param target
	 * @param call
	 */
	public void addModifier(Class<?> target, AccessibleObject call) {
		if (!modifiers.containsKey(target))
			modifiers.put(target, new LinkedHashSet<AccessibleObject>());

		// TODO: Need to add this call to all subclasses/superclasses?

		modifiers.get(target).add(call);
	}

	/* (non-Javadoc)
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
			try {
				for (AccessibleObject o : getGenerators(clazz)) {
					result.append("  " + clazz.getTypeName() + " <- " + o + " " + "\n");
				}
			} catch (ConstructionFailedException e) {

			}
		}
		result.append("Modifiers:\n");
		for (Class<?> clazz : modifiers.keySet()) {
			result.append(" Modifiers for " + clazz.getName() + ": "
			        + modifiers.get(clazz).size() + "\n");
			for (AccessibleObject o : getCallsFor(clazz)) {
				result.append(" " + clazz.getName() + " <- " + o + "\n");
			}
		}
		result.append("Test calls\n");
		for (AccessibleObject testCall : testMethods) {
			result.append(" " + testCall + "\n");
		}
		return result.toString();
	}

}
