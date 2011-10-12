/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.googlecode.gentyref.GenericTypeReflector;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.ConstructionFailedException;
import de.unisb.cs.st.evosuite.setup.ClusterAnalysis;
import de.unisb.cs.st.evosuite.utils.Randomness;

/**
 * @author Gordon Fraser
 * 
 */
public class LazyTestCluster extends TestCluster {

	/** Instance variable */
	private static LazyTestCluster instance = null;

	/** Set of all classes already analyzed */
	private final static Set<Class<?>> analyzedClasses = new HashSet<Class<?>>();

	/** Methods we want to cover when testing */
	private final static List<Method> testMethods = new ArrayList<Method>();

	/** Constructors we want to cover when testing */
	private final static List<Constructor<?>> testConstructors = new ArrayList<Constructor<?>>();

	/** Fields of the SUT */
	private final static List<Field> testFields = new ArrayList<Field>();

	/** Cached information about how to generate types */
	private final static Map<Class<?>, Set<AccessibleObject>> generators = new HashMap<Class<?>, Set<AccessibleObject>>();

	/**
	 * Cached information about what calls we cannot add because of missing
	 * dependencies
	 */
	private final static Set<AccessibleObject> excluded = new HashSet<AccessibleObject>();

	/**
	 * Private constructor
	 */
	private LazyTestCluster() {
		analyzeTarget();
	}

	/**
	 * Singleton accessor
	 * 
	 * @return
	 */
	public static LazyTestCluster getInstance() {
		if (instance == null)
			instance = new LazyTestCluster();

		return instance;
	}

	/**
	 * Return all calls for a given class. This method is called for existing
	 * types, so we do not include constructors.
	 * 
	 * @param type
	 * @return
	 */
	@Override
	public List<AccessibleObject> getCallsFor(Type type) {
		Class<?> clazz = GenericTypeReflector.erase(type);

		List<AccessibleObject> relevantCalls = new ArrayList<AccessibleObject>();

		try {
			for (Method m : getMethods(clazz)) {
				if (canUse(m)) {
					relevantCalls.add(m);
				}
			}
			for (Field f : getFields(clazz)) {
				if (canUse(f) && !Modifier.isFinal(f.getModifiers())) {
					relevantCalls.add(f);
				}
			}
		} catch (Throwable t) {
			logger.warn("Failed to get methods for " + clazz.getSimpleName() + ": " + t);
		}
		return relevantCalls;
	}

	/**
	 * Return all calls that have a parameter with given type
	 * 
	 * @param type
	 * @return
	 */
	@Override
	public List<AccessibleObject> getTestCallsWith(Type type) {
		List<AccessibleObject> calls = new ArrayList<AccessibleObject>();
		calls.addAll(getTestConstructorsWith(type));
		calls.addAll(getTestFieldsWith(type));
		calls.addAll(getTestMethodsWith(type));
		return calls;
	}

	/**
	 * Create list of all methods using a certain type as parameter
	 * 
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	private List<Method> getTestMethodsWith(Type type) {
		List<Method> suitable_methods = new ArrayList<Method>();

		for (Method m : testMethods) {
			if (Arrays.asList(m.getGenericParameterTypes()).contains(type))
				suitable_methods.add(m);
		}
		return suitable_methods;
	}

	/**
	 * Create list of all methods using a certain type as parameter
	 * 
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	private List<Field> getTestFieldsWith(Type type) {
		List<Field> suitable_fields = new ArrayList<Field>();

		for (Field f : testFields) {
			if (f.getGenericType().equals(type))
				suitable_fields.add(f);
		}
		return suitable_fields;
	}

	/**
	 * Create list of all methods using a certain type as parameter
	 * 
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	private List<Constructor<?>> getTestConstructorsWith(Type type) {
		List<Constructor<?>> suitable_constructors = new ArrayList<Constructor<?>>();

		for (Constructor<?> c : testConstructors) {
			if (Arrays.asList(c.getGenericParameterTypes()).contains(type))
				suitable_constructors.add(c);
		}
		return suitable_constructors;
	}

	/**
	 * Randomly select one generator
	 * 
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	public AccessibleObject getRandomGenerator(Class<?> clazz)
	        throws ConstructionFailedException {
		if (!generators.containsKey(clazz)) {
			loadClass(clazz);
		}

		if (!generators.containsKey(clazz))
			return null;

		return Randomness.choice(generators.get(clazz));
	}

	private static int getDistance(String class1, String class2) {
		String[] class1Parts = class1.split("\\.");
		String[] class2Parts = class2.split("\\.");
		for (int i = 0; i < class1Parts.length; i++) {
			if (i >= class2Parts.length)
				return class1Parts.length - i;

			if (!class1Parts[i].equals(class2Parts[i]))
				return class1Parts.length - i;
		}

		return 0;
	}

	private static Class<?> getDeclaringClass(AccessibleObject o) {
		if (o instanceof Method) {
			return ((Method) o).getDeclaringClass();
		} else if (o instanceof Constructor<?>) {
			return ((Constructor<?>) o).getDeclaringClass();
		} else if (o instanceof Field) {
			return ((Field) o).getDeclaringClass();
		} else {
			assert (false);
		}
		return null;
	}

	/**
	 * Randomly select one generator
	 * 
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	@Override
	@SuppressWarnings("deprecation")
	public AccessibleObject getRandomGenerator(Type type, Set<AccessibleObject> excluded)
	        throws ConstructionFailedException {
		Class<?> clazz = GenericTypeReflector.erase(type);

		if (!generators.containsKey(clazz)) {
			logger.info("Have no generators yet for class " + clazz.getName());
			loadClass(clazz);
		}

		if (!generators.containsKey(clazz))
			return null;

		//return Randomness.choice(generators.get(clazz));

		List<AccessibleObject> choice = new ArrayList<AccessibleObject>(
		        generators.get(clazz));
		logger.debug("Removing " + excluded.size() + " from " + choice.size()
		        + " generators");
		choice.removeAll(excluded);
		if (!excluded.isEmpty())
			logger.debug("Result: " + choice.size() + " generators");
		if (choice.isEmpty())
			return null;

		int num = 0;
		int param = 1000;
		AccessibleObject ret = null;
		int lastDistance = Integer.MAX_VALUE;
		for (int i = 0; i < Properties.GENERATOR_TOURNAMENT; i++) {
			int new_num = Randomness.nextInt(choice.size());
			AccessibleObject o = choice.get(new_num);
			Class<?> declaringClass = getDeclaringClass(o);
			int distance = getDistance(Properties.TARGET_CLASS, declaringClass.getName());
			if (ret == null || distance < lastDistance) {
				ret = o;
				lastDistance = distance;
			}
			/*
			if (o instanceof Constructor<?>) {
				Constructor<?> c = (Constructor<?>) o;
				if (c.getParameterTypes().length < param) {
					param = c.getParameterTypes().length;
					num = new_num;
				} else if (o instanceof Method) {
					Method m = (Method) o;
					int p = m.getParameterTypes().length;
					if (!Modifier.isStatic(m.getModifiers()))
						p++;
					if (p < param) {
						param = p;
						num = new_num;
					}
				} else if (o instanceof Field) {
					// param = 2;
					// num = new_num;
					Field f = (Field) o;
					int p = 0;
					if (!Modifier.isStatic(f.getModifiers()))
						p++;
					if (p < param) {
						param = p;
						num = new_num;
					}
				}
			}
			*/
		}
		return ret;
		//		return choice.get(num);
		// return randomness.choice(choice);

	}

	/**
	 * Get a list of all generator objects for the type
	 * 
	 * @param type
	 * @return
	 */
	@Override
	public boolean hasGenerator(Type type) {
		return hasGenerator(GenericTypeReflector.erase(type));
	}

	/**
	 * Get a list of all generator objects for the type
	 * 
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	@Override
	public Set<AccessibleObject> getGenerators(Type type)
	        throws ConstructionFailedException {
		return getGenerators(GenericTypeReflector.erase(type));
	}

	/**
	 * Get a list of all generator objects for the type
	 * 
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	public Set<AccessibleObject> getGenerators(Class<?> clazz)
	        throws ConstructionFailedException {
		if (!generators.containsKey(clazz))
			throw new ConstructionFailedException("Have no generators for " + clazz);

		return generators.get(clazz);
	}

	/**
	 * Get a list of all generator objects for the type
	 * 
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	public boolean hasGenerator(Class<?> clazz) {
		return generators.containsKey(clazz);
	}

	/**
	 * Get random method or constructor of unit under test
	 * 
	 * @return
	 * @throws ConstructionFailedException
	 */
	@Override
	public AccessibleObject getRandomTestCall() {
		int num_methods = testMethods.size();
		int num_constructors = testConstructors.size();
		int num_fields = testFields.size();

		// If there are no methods, there should always be a default constructor
		if (num_methods == 0 && num_fields == 0) {
			if (num_constructors == 0)
				return null;
			return Randomness.choice(testConstructors);
		}

		int num = Randomness.nextInt(num_methods + num_constructors + num_fields);
		if (num < num_constructors) {
			return testConstructors.get(num); // - num_methods - num_fields);
		} else if (num < (num_methods + num_constructors)) {
			return testMethods.get(num - num_constructors);
		} else {
			return testFields.get(num - num_constructors - num_methods);
		}
	}

	/**
	 * Load the target class and all its defined classes, or subclasses if
	 * abstract
	 */
	public void analyzeTarget() {
		Class<?> targetClass = Properties.getTargetClass();
		logger.info("Analyzing target class");
		Set<Class<?>> targetClasses = new HashSet<Class<?>>();
		// TODO: Might be null if class not found (e.g. not compiled)
		targetClasses.add(targetClass);
		if (Modifier.isAbstract(targetClass.getModifiers())) {
			for (String className : ClusterAnalysis.getSubclasses(Properties.TARGET_CLASS)) {
				try {
					Class<?> clazz = classLoader.loadClass(className);
					targetClasses.add(clazz);
				} catch (ClassNotFoundException e) {

				}
			}
		}
		for (Class<?> memberClass : targetClass.getDeclaredClasses()) {
			logger.info("Adding member testclass " + memberClass);
			targetClasses.add(memberClass);
		}
		// TODO: Need to recursively add memberclasses
		for (Class<?> target : targetClasses) {
			logger.info("Current testclass: " + target);
			for (Constructor<?> c : getConstructors(target)) {
				if (canUse(c)) {
					testConstructors.add(c);
					addConstructor(c.getDeclaringClass(), c);
				}
			}
			for (Method m : getMethods(target)) {
				if (canUse(m)) {
					if (m.getDeclaringClass().equals(target)) {
						logger.info("Adding testclass method " + m);
						testMethods.add(m);
					}
					addMethod(m.getReturnType(), m);
				}
			}
			for (Field f : getFields(target)) {
				if (canUse(f)) {
					if (!Modifier.isFinal(f.getModifiers()))
						testFields.add(f);
					addField(f.getType(), f);
				}
			}
		}
	}

	/**
	 * Analyze a class and integrate into test cluster
	 * 
	 * @param name
	 * @return
	 */
	public Class<?> loadClass(String name) {
		try {
			Class<?> clazz = classLoader.loadClass(name);
			if (analyzedClasses.contains(clazz))
				return clazz;

			logger.info("Analyzing class (name) " + name);

			if (canUse(clazz)) {
				loadClass(clazz);
				return clazz;
			} else {
				return null;
			}
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	/**
	 * Analyze a class and integrate into test cluster
	 * 
	 * @param name
	 * @return
	 */
	public void loadClass(Class<?> target) {
		//assert (target.getClassLoader() == classLoader) : "Class " + target.getName()
		//       + ": " + target.getClassLoader() + " vs. " + classLoader;
		if (target.getName().equals("java.lang.Object")) {
			logger.info("Skipping Object");
			return;
		}

		if (analyzedClasses.contains(target)) {
			logger.info("Have already seen class, skipping.");
			return;
		}
		logger.info("Analyzing class " + target.getName());

		if (analyzedClasses.isEmpty()) // && Modifier.isAbstract(target.getModifiers()))
			ClusterAnalysis.readAllClasses();
		else if (analyzedClasses.isEmpty()) {
			logger.info("Read no classes!");
			return;
		}

		analyzedClasses.add(target);

		for (Constructor<?> c : getConstructors(target)) {
			addConstructor(target, c);
		}
		for (Method m : getMethods(target)) {
			addMethod(m.getReturnType(), m);
		}
		for (Field f : getFields(target)) {
			addField(f.getType(), f);
		}
		logger.info("Loading subclasses of " + target.getName());

		for (String subclass : ClusterAnalysis.getSubclasses(target.getName())) {
			if (subclass.equals(target.getName()))
				continue;
			logger.info("Checking subclass of " + target.getName() + ": " + subclass);
			try {
				loadClass(subclass);
			} catch (Throwable t) {
				logger.info("Exception while loading subclass: " + subclass);
			}

		}
		logger.info("Loading generators of " + target.getName());
		for (String generator : ClusterAnalysis.getGenerators(target.getName())) {
			try {
				logger.info("Checking generator of " + target.getName() + ": "
				        + generator);
				Class<?> generatorClass = classLoader.loadClass(generator);
				if (canUse(generatorClass))
					loadGenerators(target, generatorClass);
			} catch (ClassNotFoundException e) {
				logger.info("Exception while loading class: " + e);
			} catch (NoClassDefFoundError e) {
				logger.info("Exception while loading class: " + e);
			} catch (Throwable t) {
				logger.info("Exception while loading class: " + t);
			}
		}
		if (generators.containsKey(target)) {
			logger.info("Generators for class " + target.getName() + ": "
			        + generators.get(target).size());
		} else {
			logger.info("Found no generators for class " + target.getName());
		}

	}

	/**
	 * Get all the generators from a given class
	 * 
	 * @param target
	 * @param generatorClass
	 */
	// Problem 1: Generators map does not store right target class
	// Problem 2: Do we need to include all superclass constructors etc?
	private void loadGenerators(Class<?> target, Class<?> generatorClass) {
		logger.info("Constructors of " + generatorClass.getName() + ": "
		        + getConstructors(generatorClass).size());
		for (Constructor<?> c : generatorClass.getDeclaredConstructors()) {
			if (target.isAssignableFrom(c.getDeclaringClass())) {
				logger.info("Keeping constructor " + c.toString());
				addConstructor(target, c);
			} //else
			  //	logger.info("Not using constructor for " + target.getName() + ": "
			  //	        + generatorClass.getName());
		}
		for (Method m : generatorClass.getDeclaredMethods()) {
			if (target.isAssignableFrom(m.getReturnType()))
				addMethod(target, m);
			//else
			//	logger.info("Not using method for " + target.getName() + ": "
			//	        + m.getReturnType().getName());
		}
		for (Field f : generatorClass.getDeclaredFields()) {
			if (target.isAssignableFrom(f.getType()))
				addField(target, f);
			//else
			//	logger.info("Not using field for " + target.getName() + ": "
			//	        + f.getType().getName());
		}
	}

	private static Set<Class<?>> getSuperClasses(Class<?> clazz) {
		Set<Class<?>> superClasses = new HashSet<Class<?>>();
		if (clazz.getSuperclass() != null) {
			superClasses.add(clazz.getSuperclass());
			superClasses.addAll(getSuperClasses(clazz.getSuperclass()));
		}
		for (Class<?> iface : clazz.getInterfaces()) {
			superClasses.add(iface);
		}

		return superClasses;
	}

	/**
	 * Insert a constructor into the test cluster
	 * 
	 * @param c
	 */
	private void addConstructor(Class<?> targetClass, Constructor<?> c) {
		if (!canUse(c)) {
			logger.info("Not usable: " + c.toString());
			return;
		}

		if (c.getDeclaringClass().isMemberClass()
		        && !Modifier.isStatic(c.getDeclaringClass().getModifiers())) {
			logger.debug("Field is defined in non-static member class");
			return;
		}

		if (!generators.containsKey(targetClass)) {
			generators.put(targetClass, new HashSet<AccessibleObject>());
		}

		logger.debug("Learned new generator for " + targetClass.getSimpleName() + ": "
		        + c);
		generators.get(targetClass).add(c);
		for (Class<?> clazz : getSuperClasses(targetClass)) {
			if (!generators.containsKey(clazz))
				generators.put(clazz, new HashSet<AccessibleObject>());
			generators.get(clazz).add(c);
		}
	}

	/**
	 * Insert a method into the test cluster
	 * 
	 * @param m
	 */
	private void addMethod(Class<?> targetClass, Method m) {
		if (!canUse(m))
			return;

		if (m.getDeclaringClass().isMemberClass()
		        && !Modifier.isStatic(m.getDeclaringClass().getModifiers())) {
			logger.debug("Field is defined in non-static member class");
			return;
		}

		if (m.getReturnType().isPrimitive())
			return;

		if (!generators.containsKey(targetClass))
			generators.put(targetClass, new HashSet<AccessibleObject>());

		logger.debug("Learned new generator for " + targetClass.getSimpleName() + ": "
		        + m);
		generators.get(targetClass).add(m);
		for (Class<?> clazz : getSuperClasses(targetClass)) {
			if (!generators.containsKey(clazz))
				generators.put(clazz, new HashSet<AccessibleObject>());
			generators.get(clazz).add(m);
		}
	}

	/**
	 * Insert a field into the test cluster
	 * 
	 * @param f
	 */
	private void addField(Class<?> targetClass, Field f) {
		if (!canUse(f))
			return;

		if (f.getDeclaringClass().isMemberClass()
		        && !Modifier.isStatic(f.getDeclaringClass().getModifiers())) {
			logger.debug("Field is defined in non-static member class");
			return;
		}

		if (!canUse(f.getDeclaringClass()))
			return;

		if (f.getType().isPrimitive())
			return;

		if (!generators.containsKey(targetClass))
			generators.put(targetClass, new HashSet<AccessibleObject>());

		logger.debug("Learned new generator for " + targetClass.getSimpleName() + ": "
		        + f);
		generators.get(targetClass).add(f);
		for (Class<?> clazz : getSuperClasses(targetClass)) {
			if (!generators.containsKey(clazz))
				generators.put(clazz, new HashSet<AccessibleObject>());
			generators.get(clazz).add(f);
		}
	}

	/**
	 * Determine if a class can be used
	 * 
	 * @param c
	 * @return
	 */
	private static boolean canUse(Class<?> c) {
		// Skip throwables?
		if (Throwable.class.isAssignableFrom(c))
			return false;

		// No private classes
		if (Modifier.isPrivate(c.getModifiers())) {
			logger.debug(c + " looks like a private class, ignoring it");
			return false;
		}

		/*
		 * if(Modifier.isAbstract(c.getModifiers())) return false;
		 * 
		 * if(c.isLocalClass() || c.isAnonymousClass()) return false;
		 */

		if (c.getName().matches(".*\\$\\d+$")) {
			logger.debug(c + " looks like an anonymous class, ignoring it");
			return false;
		}

		if (c.getName().startsWith("junit"))
			return false;

		if (Modifier.isPublic(c.getModifiers())) {
			logger.debug(c + " looks like a public class, keeping it");
			return true;
		}

		logger.debug(c + " looks like an unusable class, ignoring it");

		return false;
	}

	/**
	 * Determine whether a field can be used in test generation
	 * 
	 * @param f
	 * @return
	 */
	private static boolean canUse(Field f) {
		// No fields from Object
		if (f.getDeclaringClass().equals(java.lang.Object.class))
			return false;// handled here to avoid printing reasons

		// No fields from Thread
		if (f.getDeclaringClass().equals(java.lang.Thread.class))
			return false;// handled here to avoid printing reasons

		if (excluded.contains(f)) {
			return false;
		}

		// Only public fields
		if (Modifier.isPublic(f.getModifiers()))
			return true;

		return false;
	}

	/**
	 * Determine whether a method can be used in test generation
	 * 
	 * @param m
	 * @return
	 */
	private static boolean canUse(Method m) {

		if (m.isBridge()) {
			logger.debug("Excluding bridge method " + m.toString());
			return false;
		}

		if (m.isSynthetic()) {
			logger.debug("Excluding synthetic method " + m.toString());
			return false;
		}

		// Only if public
		if (!Modifier.isPublic(m.getModifiers()))
			return false;

		if (!Properties.USE_DEPRECATED && m.getAnnotation(Deprecated.class) != null) {
			logger.debug("Skipping deprecated method " + m.getName());
			return false;
		}

		if (excluded.contains(m)) {
			return false;
		}

		// No methods from Object
		if (m.getDeclaringClass().equals(java.lang.Object.class)) {
			return false;
		}

		// No methods from Enum
		if (m.getDeclaringClass().equals(java.lang.Enum.class)) {
			return false;
		}

		// No methods from String, as Strings are primitives?
		if (m.getDeclaringClass().equals(java.lang.String.class)) {
			return false;
		}

		// No methods from Thread
		if (m.getDeclaringClass().equals(java.lang.Thread.class))
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

		if (m.getDeclaringClass().getCanonicalName() != null
		        && m.getDeclaringClass().getCanonicalName().equals("java.lang.Enum")
		        && m.getName().equals("compareTo") && m.getParameterTypes().length == 1
		        && m.getParameterTypes()[0].equals(Enum.class))
			return false;

		if (m.getName().equals("hashCode") && !m.getDeclaringClass().equals(String.class))
			return false;

		if (m.getName().equals("deepHashCode")
		        && m.getDeclaringClass().equals(Arrays.class))
			return false;

		return true;
	}

	/**
	 * Determine whether constructor is usable for test generation
	 * 
	 * @param c
	 * @return
	 */
	private static boolean canUse(Constructor<?> c) {

		// synthetic constructors are OK
		if (Modifier.isAbstract(c.getDeclaringClass().getModifiers())) {
			logger.debug("Constructor is abstract");
			return false;
		}

		if (c.getDeclaringClass().equals(java.lang.Object.class)) {
			logger.debug("Constructor is defined in Object");
			return false;
		}

		if (excluded.contains(c)) {
			return false;
		}

		if (c.getDeclaringClass().equals(java.lang.Thread.class)) {
			logger.debug("Constructor is defined in Thread");
			return false;
		}

		if (c.getDeclaringClass().isAnonymousClass()) {
			logger.debug("Constructor is defined in anonymous class");
			return false;
		}

		if (c.getDeclaringClass().isMemberClass()
		        && !Modifier.isStatic(c.getDeclaringClass().getModifiers())) {
			logger.debug("Constructor is defined in non-static member class");
			return false;
		}

		if (!Properties.USE_DEPRECATED && c.getAnnotation(Deprecated.class) != null) {
			logger.debug("Skipping deprecated method " + c.getName());
			return false;
		}

		if (Modifier.isPublic(c.getModifiers()))
			return true;

		logger.debug("Constructor is not public: " + c);

		return false;
	}

	/**
	 * Determine if we have generators for all parameters, and delete method if
	 * not
	 * 
	 * @param m
	 */
	public void checkDependencies(Method m) {
		List<Class<?>> dependencies = new ArrayList<Class<?>>();
		if (!Modifier.isStatic(m.getModifiers()))
			dependencies.add(m.getDeclaringClass());
		dependencies.addAll(Arrays.asList(m.getParameterTypes()));
		for (Class<?> clazz : dependencies) {
			if (!hasGenerator(clazz)) {
				testMethods.remove(m);
				excluded.add(m);
				for (Class<?> c : generators.keySet()) {
					generators.get(c).remove(m);
				}
				return;
			}
		}
	}

	/**
	 * Determine if we have generators for all parameters, and delete method if
	 * not
	 * 
	 * @param m
	 */
	public void checkDependencies(Constructor<?> c) {
		List<Class<?>> dependencies = new ArrayList<Class<?>>();
		dependencies.addAll(Arrays.asList(c.getParameterTypes()));
		for (Class<?> clazz : dependencies) {
			if (!hasGenerator(clazz)) {
				logger.warn("DELETING CONSTRUCTOR " + c);
				testConstructors.remove(c);
				excluded.add(c);
				for (Class<?> c2 : generators.keySet()) {
					generators.get(c2).remove(c);
				}
				return;
			}
		}
	}

	/**
	 * Determine if we have generators for all parameters, and delete method if
	 * not
	 * 
	 * @param m
	 */
	public void checkDependencies(Field f) {
		if (!hasGenerator(f.getDeclaringClass())) {
			logger.warn("DELETING FIELD " + f);
			testFields.remove(f);
			excluded.add(f);
			for (Class<?> c : generators.keySet()) {
				generators.get(c).remove(f);
			}
			return;
		}
	}

	@Override
	public void checkDependencies(AccessibleObject o) {
		if (o instanceof Method) {
			checkDependencies((Method) o);
		} else if (o instanceof Constructor<?>) {
			checkDependencies((Constructor<?>) o);
		} else if (o instanceof Field) {
			checkDependencies((Field) o);
		}
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestCluster#resetCluster()
	 */
	@Override
	public void resetCluster() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestCluster#getClass(java.lang.String)
	 */
	@Override
	public Class<?> getClass(String name) throws ClassNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestCluster#importClass(java.lang.String)
	 */
	@Override
	public Class<?> importClass(String name) throws ClassNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestCluster#getRandomGenerator(java.lang.reflect.Type)
	 */
	@Override
	public AccessibleObject getRandomGenerator(Type type)
	        throws ConstructionFailedException {
		return getRandomGenerator(type, new HashSet<AccessibleObject>());
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestCluster#getTestCalls()
	 */
	@Override
	public List<AccessibleObject> getTestCalls() {
		List<AccessibleObject> testCalls = new ArrayList<AccessibleObject>();
		testCalls.addAll(testConstructors);
		testCalls.addAll(testMethods);
		return testCalls;
	}
}
