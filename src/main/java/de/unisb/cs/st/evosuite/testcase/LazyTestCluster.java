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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.gentyref.GenericTypeReflector;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.ConstructionFailedException;
import de.unisb.cs.st.evosuite.javaagent.InstrumentingClassLoader;
import de.unisb.cs.st.evosuite.setup.ClusterAnalysis;
import de.unisb.cs.st.evosuite.utils.Randomness;

/**
 * @author Gordon Fraser
 * 
 */
public class LazyTestCluster {

	/** Logger */
	private static Logger logger = LoggerFactory.getLogger(LazyTestCluster.class);

	/** Instance variable */
	private static LazyTestCluster instance = null;

	private static ClassLoader classLoader = new InstrumentingClassLoader();

	/** Set of all classes already analyzed */
	private final Set<Class<?>> analyzedClasses = new HashSet<Class<?>>();

	private final Set<Field> fields = new HashSet<Field>();

	private final Set<Method> methods = new HashSet<Method>();

	private final Set<Constructor<?>> constructors = new HashSet<Constructor<?>>();

	private final List<Method> testMethods = new ArrayList<Method>();

	private final List<Constructor<?>> testConstructors = new ArrayList<Constructor<?>>();

	private final List<Field> testFields = new ArrayList<Field>();

	private final Map<Class<?>, Set<AccessibleObject>> generators = new HashMap<Class<?>, Set<AccessibleObject>>();

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
	 * Return all calls for a given class
	 * 
	 * @param type
	 * @return
	 */
	public List<AccessibleObject> getCallsFor(Class<?> clazz) {
		List<AccessibleObject> relevantCalls = new ArrayList<AccessibleObject>();
		for (Constructor<?> c : getConstructors(clazz)) {
			if (canUse(c)) {
				relevantCalls.add(c);
			}
		}
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
		return relevantCalls;
	}

	/**
	 * Return all calls that have a parameter with given type
	 * 
	 * @param type
	 * @return
	 */
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

	/**
	 * Randomly select one generator
	 * 
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	@SuppressWarnings("deprecation")
	public AccessibleObject getRandomGenerator(Type type, Set<AccessibleObject> excluded)
	        throws ConstructionFailedException {
		Class<?> clazz = GenericTypeReflector.erase(type);

		if (!generators.containsKey(clazz)) {
			loadClass(clazz);
		}

		if (!generators.containsKey(clazz))
			return null;

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
		for (int i = 0; i < Properties.GENERATOR_TOURNAMENT; i++) {
			int new_num = Randomness.nextInt(choice.size());
			AccessibleObject o = choice.get(new_num);
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
		}
		return choice.get(num);
		// return randomness.choice(choice);
	}

	/**
	 * Get a list of all generator objects for the type
	 * 
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	public boolean hasGenerator(Type type) throws ConstructionFailedException {
		return hasGenerator(GenericTypeReflector.erase(type));
	}

	/**
	 * Get a list of all generator objects for the type
	 * 
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
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
	public boolean hasGenerator(Class<?> clazz) throws ConstructionFailedException {
		return generators.containsKey(clazz);
	}

	/**
	 * Get random method or constructor of unit under test
	 * 
	 * @return
	 * @throws ConstructionFailedException
	 */
	public AccessibleObject getRandomTestCall() throws ConstructionFailedException {
		int num_methods = testMethods.size();
		int num_constructors = testConstructors.size();
		int num_fields = testFields.size();

		// If there are no methods, there should always be a default constructor
		if (num_methods == 0 && num_fields == 0) {
			if (num_constructors == 0)
				throw new ConstructionFailedException("Have no constructors!");
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
	 * Load the target class
	 */
	public void analyzeTarget() {
		Class<?> target = Properties.getTargetClass();
		logger.info("Analyzing target class");
		for (Constructor<?> c : getConstructors(target)) {
			if (canUse(c)) {
				testConstructors.add(c);
				addConstructor(c.getDeclaringClass(), c);
			}
		}
		for (Method m : getMethods(target)) {
			if (canUse(m)) {
				testMethods.add(m);
				addMethod(m.getReturnType(), m);
			}
		}
		for (Field f : getFields(target)) {
			if (canUse(f)) {
				if (!Modifier.isFinal(target.getModifiers()))
					testFields.add(f);
				addField(f.getType(), f);
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
		logger.info("Analyzing class " + target.getName());
		if (target.getName().equals("java.lang.Object")) {
			logger.info("Skipping Object");
			return;
		}

		if (analyzedClasses.contains(target)) {
			logger.info("Have already seen class, skipping.");
			return;
		}

		if (analyzedClasses.isEmpty() && Modifier.isAbstract(target.getModifiers()))
			ClusterAnalysis.readAllClasses();
		else if (analyzedClasses.isEmpty())
			return;

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
			logger.info("Checking subclass of " + target.getName() + ": " + subclass);
			loadClass(subclass);
		}
		logger.info("Loading generators of " + target.getName());
		for (String generator : ClusterAnalysis.getGenerators(target.getName())) {
			try {
				logger.info("Checking generator of " + target.getName() + ": "
				        + generator);
				Class<?> generatorClass = classLoader.loadClass(generator);
				loadGenerators(target, generatorClass);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
			} else
				logger.info("Not using constructor for " + target.getName() + ": "
				        + generatorClass.getName());
		}
		for (Method m : generatorClass.getDeclaredMethods()) {
			if (target.isAssignableFrom(m.getReturnType()))
				addMethod(target, m);
			else
				logger.info("Not using method for " + target.getName() + ": "
				        + m.getReturnType().getName());
		}
		for (Field f : generatorClass.getDeclaredFields()) {
			if (target.isAssignableFrom(f.getType()))
				addField(target, f);
			else
				logger.info("Not using field for " + target.getName() + ": "
				        + f.getType().getName());
		}
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

		constructors.add(c);
		if (!generators.containsKey(targetClass))
			generators.put(targetClass, new HashSet<AccessibleObject>());

		generators.get(targetClass).add(c);
	}

	/**
	 * Insert a method into the test cluster
	 * 
	 * @param m
	 */
	private void addMethod(Class<?> targetClass, Method m) {
		if (!canUse(m))
			return;

		methods.add(m);
		if (!generators.containsKey(targetClass))
			generators.put(targetClass, new HashSet<AccessibleObject>());

		generators.get(targetClass).add(m);
	}

	/**
	 * Insert a field into the test cluster
	 * 
	 * @param f
	 */
	private void addField(Class<?> targetClass, Field f) {
		if (!canUse(f))
			return;

		fields.add(f);
		if (!generators.containsKey(targetClass))
			generators.put(targetClass, new HashSet<AccessibleObject>());

		generators.get(targetClass).add(f);
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
		if (Modifier.isPrivate(c.getModifiers()))
			return false;

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

		if (Modifier.isPublic(c.getModifiers()))
			return true;

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
		if (Modifier.isAbstract(c.getDeclaringClass().getModifiers()))
			return false;

		if (c.getDeclaringClass().equals(java.lang.Object.class))
			return false;

		if (c.getDeclaringClass().equals(java.lang.Thread.class))
			return false;

		if (c.getDeclaringClass().isAnonymousClass())
			return false;

		if (c.getDeclaringClass().isMemberClass()
		        && !Modifier.isStatic(c.getDeclaringClass().getModifiers()))
			return false;

		if (!Properties.USE_DEPRECATED && c.getAnnotation(Deprecated.class) != null) {
			logger.debug("Skipping deprecated method " + c.getName());
			return false;
		}

		if (Modifier.isPublic(c.getModifiers()))
			return true;

		return false;
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
		if (clazz.getSuperclass() != null) {
			// constructors.addAll(getConstructors(clazz.getSuperclass()));
			for (Constructor<?> c : getConstructors(clazz.getSuperclass())) {
				helper.put(org.objectweb.asm.Type.getConstructorDescriptor(c), c);
			}
		}
		for (Class<?> in : clazz.getInterfaces()) {
			for (Constructor<?> c : getConstructors(in)) {
				helper.put(org.objectweb.asm.Type.getConstructorDescriptor(c), c);
			}
			// constructors.addAll(getConstructors(in));
		}

		// for(Constructor c : clazz.getConstructors()) {
		// constructors.add(c);
		// }
		for (Constructor<?> c : clazz.getDeclaredConstructors()) {
			// constructors.add(c);
			helper.put(org.objectweb.asm.Type.getConstructorDescriptor(c), c);
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
			// constructors.addAll(getConstructors(clazz.getSuperclass()));
			for (Method m : getMethods(clazz.getSuperclass())) {
				helper.put(m.getName() + org.objectweb.asm.Type.getMethodDescriptor(m), m);
			}
		}
		for (Class<?> in : clazz.getInterfaces()) {
			for (Method m : getMethods(in)) {
				helper.put(m.getName() + org.objectweb.asm.Type.getMethodDescriptor(m), m);
			}
			// constructors.addAll(getConstructors(in));
		}

		// for(Constructor c : clazz.getConstructors()) {
		// constructors.add(c);
		// }
		for (Method m : clazz.getDeclaredMethods()) {
			helper.put(m.getName() + org.objectweb.asm.Type.getMethodDescriptor(m), m);
		}

		Set<Method> methods = new HashSet<Method>();
		methods.addAll(helper.values());
		/*
		for (Method m : helper.values()) {
			String name = m.getName() + "|"
			        + org.objectweb.asm.Type.getMethodDescriptor(m);

			methods.add(m);
		}
		*/
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
			// fields.addAll(getFields(clazz.getSuperclass()));
			for (Field f : getFields(clazz.getSuperclass())) {
				helper.put(f.toGenericString(), f);
			}

		}
		for (Class<?> in : clazz.getInterfaces()) {
			// fields.addAll(getFields(in));
			for (Field f : getFields(in)) {
				helper.put(f.toGenericString(), f);
			}
		}

		for (Field f : clazz.getDeclaredFields()) {
			// fields.add(m);
			helper.put(f.toGenericString(), f);
		}
		// for(Field m : clazz.getDeclaredFields()) {
		// fields.add(m);
		// }
		fields.addAll(helper.values());

		return fields;
	}

}
