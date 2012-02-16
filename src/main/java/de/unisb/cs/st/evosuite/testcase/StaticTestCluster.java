/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.testcase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.callgraph.ConnectionData;
import de.unisb.cs.st.evosuite.callgraph.Hierarchy;
import de.unisb.cs.st.evosuite.callgraph.MethodDescription;
import de.unisb.cs.st.evosuite.callgraph.Tuple;
import de.unisb.cs.st.evosuite.cfg.BytecodeInstructionPool;
import de.unisb.cs.st.evosuite.cfg.CFGMethodAdapter;
import de.unisb.cs.st.evosuite.cfg.CFGPool;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;
import de.unisb.cs.st.evosuite.ga.ConstructionFailedException;
import de.unisb.cs.st.evosuite.javaagent.BooleanTestabilityTransformation;
import de.unisb.cs.st.evosuite.javaagent.InstrumentingClassLoader;
import de.unisb.cs.st.evosuite.javaagent.StaticInitializationClassAdapter;
import de.unisb.cs.st.evosuite.sandbox.Sandbox;
import de.unisb.cs.st.evosuite.utils.Randomness;
import de.unisb.cs.st.evosuite.utils.Utils;

/**
 * The test cluster contains the information about all classes and their members
 * in the target package
 * 
 * @author Gordon Fraser
 * 
 */
public class StaticTestCluster extends TestCluster {

	/** The usable methods of the class under test */
	public List<Method> test_methods = new ArrayList<Method>();

	/** The usable constructor of the class under test */
	private final List<Constructor<?>> test_constructors = new ArrayList<Constructor<?>>();

	/** The usable fields of the class under test */
	private final List<Field> test_fields = new ArrayList<Field>();

	/** Cache results about generators */
	private final HashMap<Type, List<AccessibleObject>> generators = new HashMap<Type, List<AccessibleObject>>();

	private final HashMap<Type, List<AccessibleObject>> calls_with = new HashMap<Type, List<AccessibleObject>>();

	private final HashMap<Type, List<AccessibleObject>> calls_for = new HashMap<Type, List<AccessibleObject>>();

	/** The entire set of calls available */
	private final Set<AccessibleObject> calls = new HashSet<AccessibleObject>();

	private final Set<Class<?>> analyzedClasses = new HashSet<Class<?>>();

	private final List<Method> static_initializers = new ArrayList<Method>();

	public static final List<String> EXCLUDE = Arrays.asList("<clinit>", "__STATIC_RESET");

	private static Hierarchy hierarchy;

	private static Hierarchy getHierarchy() {
		if (hierarchy == null) {
			hierarchy = Hierarchy.readFromDefaultLocation();
		}
		return hierarchy;
	}

	private static Map<String, List<String>> test_excludes = getExcludesFromFile();

	public int num_defined_methods = 0;

	/**
	 * Private constructor
	 */
	protected StaticTestCluster() {
	}

	@Override
	protected void init() {
		populate();
		addIncludes();
		analyzeTarget();
		if (Properties.REMOTE_TESTING)
			addRemoteCalls();
		countTargetFunctions();
		/*
		 * for(Method m : TestHelper.class.getDeclaredMethods()) { calls.add(m);
		 * test_methods.add(m); }
		 */

		getStaticClasses();
		ExecutionTracer.enable();
	}

	// New dependency algorithm:
	// 1. Load list of all classes in classpath
	// 2. Identify all parameters of TARGET_CLASS
	// 3. For each parameter:
	// 3a. Load class
	// 3b. If abstract, find all subclasses, add to classlist to handle
	// 3c. For all parameters, put on classlist if not already handled
	//
	// In setup script, add all jars / classes found in local dir to classpath?

	private final static Pattern testPattern = Pattern.compile(".*?\\.(Test[a-zA-Z0-9]+)|([a-zA-Z0-9]+Test)");

	private static boolean isTest(String className) {
		// TODO-JRO Identifying tests should be done differently:
		// If the class either contains methods
		// annotated with @Test (> JUnit 4.0)
		// or contains Test or Suite in it's inheritance structure
		Matcher testMatcher = testPattern.matcher(className);
		return testMatcher.find();
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
			return getHierarchy().getAllSupers(Properties.TARGET_CLASS).contains(className);
		}

		return false;
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
		cacheGeneratorType(type);
		if (!generators.containsKey(type))
			throw new ConstructionFailedException("Have no generators for " + type);

		return new LinkedHashSet<AccessibleObject>(generators.get(type));
	}

	/**
	 * Determine if there are generators
	 * 
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	@Override
	public boolean hasGenerator(Type type) {
		cacheGeneratorType(type);
		if (!generators.containsKey(type))
			return false;
		return !generators.get(type).isEmpty();
	}

	/**
	 * Randomly select one generator
	 * 
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	@Override
	public AccessibleObject getRandomGenerator(Type type)
	        throws ConstructionFailedException {
		cacheGeneratorType(type);
		if (!generators.containsKey(type))
			return null;

		return Randomness.choice(generators.get(type));
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
		cacheGeneratorType(type);
		if (!generators.containsKey(type))
			return null;

		List<AccessibleObject> choice = new ArrayList<AccessibleObject>(
		        generators.get(type));
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

	private void cacheSuperGeneratorType(Type type, List<AccessibleObject> g) {
		// if(generators.containsKey(type))
		// return;

		logger.debug("Checking superconstructors for class " + type);
		if (!(type instanceof Class<?>))
			return;
		Class<?> clazz = (Class<?>) type;
		if (clazz.isAnonymousClass() || clazz.isLocalClass()
		        || clazz.getCanonicalName().startsWith("java.")) {
			logger.debug("Skipping superconstructors for class " + type);
			return;
		} else if (logger.isDebugEnabled()) {
			logger.debug(clazz.getCanonicalName());
		}

		// List<AccessibleObject> g = new ArrayList<AccessibleObject>();

		for (AccessibleObject o : calls) {
			if (o instanceof Constructor<?>) {
				Constructor<?> c = (Constructor<?>) o;
				if (GenericClass.isSubclass(c.getDeclaringClass(), type)
				        && c.getDeclaringClass().getName().startsWith(Properties.PROJECT_PREFIX)) {
					g.add(o);
				}
			} else if (o instanceof Method) {
				Method m = (Method) o;
				if (GenericClass.isSubclass(m.getGenericReturnType(), type)
				        && m.getReturnType().getName().startsWith(Properties.PROJECT_PREFIX)) {
					g.add(o);
				}
				// else if(m.getReturnType().isAssignableFrom(type) &&
				// m.getName().equals("getInstance"))
				// g.add(o);
			} else if (o instanceof Field) {
				Field f = (Field) o;
				if (GenericClass.isSubclass(f.getGenericType(), type)
				        && f.getType().getName().startsWith(Properties.PROJECT_PREFIX)) {
					g.add(f);
				}
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Found " + g.size() + " generators for superclasses of " + type);
			for (AccessibleObject o : g) {
				logger.debug(o.toString());
			}
		}
		// generators.put(type, g);

	}

	public void addGenerator(Type type, AccessibleObject call) {
		if (!generators.containsKey(type)) {
			cacheGeneratorType(type);
		}
		generators.get(type).add(call);
	}

	public void removeGenerator(Type type, AccessibleObject call) {
		if (generators.containsKey(type)) {
			generators.get(type).remove(call);
		}
	}

	/**
	 * Fill cache with information about generators
	 * 
	 * @param type
	 */
	private void cacheGeneratorType(Type type) {
		if (generators.containsKey(type))
			return;

		// TODO: At this point check the files that redefine signatures?
		// -> This covers changed return types
		// -> But what about changed parameters?

		List<AccessibleObject> g = new ArrayList<AccessibleObject>();

		for (AccessibleObject o : calls) {
			if (o instanceof Constructor<?>) {
				Constructor<?> c = (Constructor<?>) o;
				if (GenericClass.isAssignable(type, c.getDeclaringClass())) {
					g.add(o);
				}
			} else if (o instanceof Method) {
				Method m = (Method) o;
				if (GenericClass.isAssignable(type, m.getGenericReturnType())) {
					g.add(o);
				}
				// else if(m.getReturnType().isAssignableFrom(type) &&
				// m.getName().equals("getInstance"))
				// g.add(o);
			} else if (o instanceof Field) {
				Field f = (Field) o;
				if (GenericClass.isAssignable(type, f.getGenericType())) {
					g.add(f);
				} else if (type.toString().contains("EvosuiteFile")) {
					logger.info("Found EvosuiteFile generator " + type + "/" + f);
				}
			}
		}
		if (g.isEmpty()) {
			cacheSuperGeneratorType(type, g);
		}
		// } else
		generators.put(type, g);
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
	 * Return all calls that have a parameter with given type
	 * 
	 * @param type
	 * @return
	 */
	public List<AccessibleObject> getCallsWith(Type type) {
		if (calls_with.containsKey(type))
			return calls_with.get(type);

		List<AccessibleObject> relevant_calls = new ArrayList<AccessibleObject>();
		for (AccessibleObject call : calls) {
			List<Type> parameters = new ArrayList<Type>();

			if (call instanceof Method) {
				parameters.addAll(Arrays.asList(((Method) call).getGenericParameterTypes()));
			} else if (call instanceof Constructor<?>) {
				parameters.addAll(Arrays.asList(((Constructor<?>) call).getGenericParameterTypes()));
			}

			if (parameters.contains(type))
				relevant_calls.add(call);
		}

		calls_with.put(type, relevant_calls);
		return relevant_calls;
	}

	/**
	 * Return all calls that have a parameter with given type
	 * 
	 * @param type
	 * @return
	 */
	@Override
	public List<AccessibleObject> getCallsFor(Type type) {
		if (calls_for.containsKey(type))
			return calls_for.get(type);

		List<AccessibleObject> relevant_calls = new ArrayList<AccessibleObject>();
		for (AccessibleObject call : calls) {
			if (call instanceof Method) {
				if (((Method) call).getDeclaringClass().isAssignableFrom((Class<?>) type))
					relevant_calls.add(call);
			} else if (call instanceof Field) {
				if (((Field) call).getDeclaringClass().isAssignableFrom((Class<?>) type)
				        && !Modifier.isFinal(((Field) call).getModifiers()))
					relevant_calls.add(call);
			}
		}
		calls_for.put(type, relevant_calls);
		return relevant_calls;
	}

	/**
	 * Get random method or constructor of unit under test
	 * 
	 * @return
	 * @throws ConstructionFailedException
	 */
	@Override
	public AccessibleObject getRandomTestCall() {
		int num_methods = test_methods.size();
		int num_constructors = test_constructors.size();
		int num_fields = test_fields.size();

		// If there are no methods, there should always be a default constructor
		if (num_methods == 0 && num_fields == 0) {
			if (num_constructors == 0)
				return null;
			return Randomness.choice(test_constructors);
		}

		int num = Randomness.nextInt(num_methods + num_constructors + num_fields);
		if (num < num_constructors) {
			return test_constructors.get(num); // - num_methods - num_fields);
		} else if (num < (num_methods + num_constructors)) {
			return test_methods.get(num - num_constructors);
		} else {
			return test_fields.get(num - num_constructors - num_methods);
		}
	}

	/**
	 * Get entirely random call
	 */
	public AccessibleObject getRandomCall() {
		return Randomness.choice(calls);
	}

	// ----------------------------------------------------------------------------------

	/**
	 * Create list of all methods using a certain type as parameter
	 * 
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	private List<Method> getTestMethodsWith(Type type) {
		List<Method> suitable_methods = new ArrayList<Method>();

		for (Method m : test_methods) {
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

		for (Field f : test_fields) {
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

		for (Constructor<?> c : test_constructors) {
			if (Arrays.asList(c.getGenericParameterTypes()).contains(type))
				suitable_constructors.add(c);
		}
		return suitable_constructors;
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
				// FIXXME: Final is only a problem for write access...
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

	/**
	 * Load test methods from test task files
	 * 
	 * @return Map from classname to list of methodnames
	 */
	private Map<String, List<String>> getTestObjectsFromFile() {
		// Iterate over all task files, find ones matching TARGET_CLASS_PREFIX
		File outputDir = new File(Properties.OUTPUT_DIR);
		File[] taskFiles = outputDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				int suffixIdx = name.toLowerCase().lastIndexOf(".task");
				boolean suffixFound = suffixIdx != -1;

				if (!suffixFound)
					return false;

				String className = name.substring(0, suffixIdx);
				return StaticTestCluster.isTargetClassName(className);
			}
		});

		Map<String, List<String>> objs = new HashMap<String, List<String>>();

		for (File file : taskFiles) {
			logger.info("Reading test methods from " + file.getName());
			List<String> lines = Utils.readFile(file);

			for (String line : lines) {
				line = line.trim();
				// Skip comments
				if (line.startsWith("#"))
					continue;

				String[] parameters = line.split(",");
				if (parameters.length != 2)
					continue;
				if (!objs.containsKey(parameters[0]))
					objs.put(parameters[0], new ArrayList<String>());

				String name = parameters[1];
				objs.get(parameters[0]).add(name);
			}
		}

		return objs;
	}

	private static boolean canUse(Class<?> c) {
		// if(Modifier.isAbstract(c.getModifiers()))
		// return false;

		if (Throwable.class.isAssignableFrom(c))
			return false;
		if (Modifier.isPrivate(c.getModifiers())) // &&
		                                          // !(Modifier.isProtected(c.getModifiers())))
			return false;

		if (!Properties.USE_DEPRECATED && c.isAnnotationPresent(Deprecated.class)) {
			logger.debug("Skipping deprecated class " + c.getName());
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

		if (Modifier.isPublic(c.getModifiers()))
			return true;
		/*
		 * 
		 * if(Modifier.isProtected(c.getModifiers())) return true;
		 */

		return false;
	}

	private static boolean canUse(Field f) {
		// if(Modifier.isPrivate(f.getDeclaringClass().getModifiers()))
		// //Modifier.isProtected(f.getDeclaringClass().getModifiers()) ||
		// return false;

		// TODO we could enable some methods from Object, like getClass
		if (f.getDeclaringClass().equals(java.lang.Object.class))
			return false;// handled here to avoid printing reasons

		if (f.getDeclaringClass().equals(java.lang.Thread.class))
			return false;// handled here to avoid printing reasons

		if (!Properties.USE_DEPRECATED && f.isAnnotationPresent(Deprecated.class)) {
			logger.debug("Skipping deprecated field " + f.getName());
			return false;
		}

		if (Modifier.isPublic(f.getModifiers()))
			return true;

		/*
		 * if(Modifier.isProtected(f.getModifiers())) return true;
		 */
		/*
		 * if(!(Modifier.isPrivate(f.getModifiers()))) // &&
		 * !(Modifier.isProtected(f.getModifiers()))) return true;
		 */
		return false;
	}

	private static boolean canUse(Method m) {

		if (EXCLUDE.contains(m.getName())) {
			logger.debug("Excluding method");
			return false;
		}

		if (m.isBridge()) {
			logger.debug("Will not use: " + m.toString());
			logger.debug("  reason: it's a bridge method");
			return false;
		}

		if (m.isSynthetic()) {
			logger.debug("Will not use: " + m.toString());
			logger.debug("  reason: it's a synthetic method");
			return false;
		}

		if (!Properties.USE_DEPRECATED && m.isAnnotationPresent(Deprecated.class)) {
			logger.debug("Skipping deprecated method " + m.getName());
			return false;
		}

		// if(!Modifier.isPublic(m.getModifiers()))
		// return false;

		// if (Modifier.isPrivate(m.getModifiers())) // ||
		// Modifier.isProtected(m.getModifiers()))
		// return false;

		// TODO?
		// if(Modifier.isProtected(m.getDeclaringClass().getModifiers()) ||
		// Modifier.isPrivate(m.getDeclaringClass().getModifiers()))
		// if(Modifier.isPrivate(m.getDeclaringClass().getModifiers()))
		// return false;

		// TODO we could enable some methods from Object, like getClass

		if (m.getDeclaringClass().equals(java.lang.Object.class)) {
			return false;
			// if(!m.getName().equals("toString") &&
			// !m.getName().equals("getClass"))
			// return false;//handled here to avoid printing reasons
		}

		if (m.getDeclaringClass().equals(java.lang.Enum.class)) {
			return false;
		}

		if (m.getDeclaringClass().equals(java.lang.Thread.class))
			return false;// handled here to avoid printing reasons

		String reason = doNotUseSpecialCase(m);
		if (reason != null) {
			logger.debug("Will not use: " + m.toString());
			logger.debug("  reason: " + reason);
			return false;
		}

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
		if (Modifier.isPublic(m.getModifiers())) // ||
		                                         // Modifier.isProtected(m.getModifiers()))
			return true;

		return false;
	}

	private static String doNotUseSpecialCase(Method m) {

		// Special case 1:
		// We're skipping compareTo method in enums - you can call it only with
		// the same type as receiver
		// but the signature does not tell you that
		if (m.getDeclaringClass().getCanonicalName() != null
		        && m.getDeclaringClass().getCanonicalName().equals("java.lang.Enum")
		        && m.getName().equals("compareTo") && m.getParameterTypes().length == 1
		        && m.getParameterTypes()[0].equals(Enum.class))
			return "We're skipping compareTo method in enums";

		// Special case 2:
		// hashCode is bad in general but String.hashCode is fair game
		if (m.getName().equals("hashCode") && !m.getDeclaringClass().equals(String.class))
			return "hashCode";
		// if (m.getName().equals("hashCode") &&
		// m.getDeclaringClass().equals(Object.class))
		// return "hashCode";

		// Special case 3: (just clumps together a bunch of hashCodes, so skip
		// it)
		if (m.getName().equals("deepHashCode")
		        && m.getDeclaringClass().equals(Arrays.class))
			return "deepHashCode";

		// Special case 4: (differs too much between JDK installations)
		if (m.getName().equals("getAvailableLocales"))
			return "getAvailableLocales";
		return null;
	}

	private static boolean canUse(Constructor<?> c) {

		// synthetic constructors are OK
		if (Modifier.isAbstract(c.getDeclaringClass().getModifiers()))
			return false;

		// TODO we could enable some methods from Object, like getClass
		if (c.getDeclaringClass().equals(java.lang.Object.class))
			return false;// handled here to avoid printing reasons

		if (c.getDeclaringClass().equals(java.lang.Thread.class))
			return false;// handled here to avoid printing reasons

		if (c.getDeclaringClass().isAnonymousClass())
			return false;

		if (c.getDeclaringClass().isMemberClass()
		        && !Modifier.isPublic(c.getDeclaringClass().getModifiers()))
			// && !Modifier.isStatic(c.getDeclaringClass().getModifiers()))
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
		// if (!Modifier.isPrivate(c.getModifiers())) // &&
		// !Modifier.isProtected(c.getModifiers()))
		// return true;
		return false;
	}

	/**
	 * Check whether the name is matched by one of the regular expressions
	 * 
	 * @param name
	 * @param regexs
	 * @return
	 */
	private static boolean matches(String name, List<String> regexs) {
		for (String regex : regexs) {
			if (name.matches(regex))
				return true;
		}
		return false;
	}

	private static boolean matchesDebug(String name, List<String> regexs) {
		for (String regex : regexs) {
			if (name.matches(regex)) {
				logger.info(name + " does matches " + regex);
				return true;
			} else {
				logger.info(name + " does not match " + regex);
			}
		}
		return false;
	}

	private void countTargetFunctions() {
		num_defined_methods = CFGMethodAdapter.methods.size();
		if (Properties.INSTRUMENT_PARENT)
			num_defined_methods = getMethods(Properties.getTargetClass()).size();
		logger.info("Target class has " + num_defined_methods + " functions");
		logger.info("Target class has " + BranchPool.getBranchCounter() + " branches");
		logger.info("Target class has "
		        + BranchPool.getBranchlessMethods(Properties.TARGET_CLASS).size()
		        + " methods without branches");
		logger.info("That means for coverage information: "
		        + (BranchPool.getBranchlessMethods(Properties.TARGET_CLASS).size() + 2 * BranchPool.getBranchCountForClass(Properties.TARGET_CLASS)));
	}

	private static String getName(AccessibleObject o) {
		if (o instanceof java.lang.reflect.Method) {
			java.lang.reflect.Method method = (java.lang.reflect.Method) o;
			return method.getName() + org.objectweb.asm.Type.getMethodDescriptor(method);
		} else if (o instanceof java.lang.reflect.Constructor<?>) {
			java.lang.reflect.Constructor<?> constructor = (java.lang.reflect.Constructor<?>) o;
			return "<init>"
			        + org.objectweb.asm.Type.getConstructorDescriptor(constructor);
		} else if (o instanceof java.lang.reflect.Field) {
			java.lang.reflect.Field field = (Field) o;
			return field.getName();
		}
		return ""; // TODO
	}

	private static AccessibleObject getMethod(Class<?> clazz, String methodName) {
		if (methodName.startsWith("<init>")) {
			for (Constructor<?> c : getConstructors(clazz)) {
				String name = getName(c);
				if (name.equals(methodName) && !Modifier.isPrivate(c.getModifiers())) {
					return c;
				}
			}
		} else {
			for (Method m : getMethods(clazz)) {
				String name = getName(m);
				// logger.info("Comparing "+clazz.getName()+"."+methodName+" with "+clazz.getName()+"."+name);
				if (name.equals(methodName) && !Modifier.isPrivate(m.getModifiers())) {
					return m;
				}
			}
			for (Field f : clazz.getFields()) {
				String name = getName(f);
				if (name.equals(methodName) && !Modifier.isPrivate(f.getModifiers()))
					return f;
			}
		}
		return null; // not found
	}

	private static DirectedGraph<MethodDescription, DefaultEdge> getCallGraph() {
		ConnectionData data = ConnectionData.read();
		Set<Tuple> connections = data.getConnections();
		DirectedGraph<MethodDescription, DefaultEdge> graph = new DefaultDirectedGraph<MethodDescription, DefaultEdge>(
		        DefaultEdge.class);
		for (Tuple tuple : connections) {
			MethodDescription start = tuple.getStart();
			MethodDescription end = tuple.getEnd();
			if (!start.equals(end)) {
				if (!graph.containsVertex(start)) {
					graph.addVertex(start);
				}
				if (!graph.containsVertex(end)) {
					graph.addVertex(end);
				}
				graph.addEdge(start, end);
			}
		}
		return graph;
	}

	/**
	 * Add remote calls: If we are doing remote testing, then not just the
	 * methods of the UUT are candidates, but also all methods that indirectly
	 * call the UUT
	 */
	public void addRemoteCalls() {
		DirectedGraph<MethodDescription, DefaultEdge> graph = getCallGraph();

		Set<MethodDescription> remoteCalls = new HashSet<MethodDescription>();
		Set<MethodDescription> visited = new HashSet<MethodDescription>();
		Queue<MethodDescription> queue = new LinkedList<MethodDescription>();
		for (AccessibleObject call : test_methods) {
			Method m = (Method) call;
			MethodDescription md = new MethodDescription(Properties.TARGET_CLASS,
			        m.getName(), org.objectweb.asm.Type.getMethodDescriptor(m));
			queue.add(md);
		}
		for (AccessibleObject call : test_constructors) {
			Constructor<?> c = (Constructor<?>) call;
			MethodDescription md = new MethodDescription(Properties.TARGET_CLASS,
			        "<init>", org.objectweb.asm.Type.getConstructorDescriptor(c));
			queue.add(md);
		}
		while (!queue.isEmpty()) {
			MethodDescription md = queue.remove();
			if (!graph.containsVertex(md))
				continue;
			for (DefaultEdge edge : graph.incomingEdgesOf(md)) {
				MethodDescription source = graph.getEdgeSource(edge);
				if (!visited.contains(source)) {
					remoteCalls.add(source);
					queue.add(source);
				}
			}
			visited.add(md);
		}
		for (MethodDescription md : remoteCalls) {
			try {
				// Class<?> clazz = Class.forName(md.getClassName());
				Class<?> clazz = classLoader.loadClass(md.getClassName());
				AccessibleObject call = getMethod(clazz,
				                                  md.getMethodName() + md.getDesc());
				if (call == null) {
					logger.debug("Cannot use remote call: " + md.getClassName() + "."
					        + md.getMethodName() + md.getDesc());
				} else if (call instanceof Method) {
					logger.info("Adding remote method: " + (call));
					test_methods.add((Method) call);
				} else {
					logger.info("Adding remote constructor: " + (call));
					test_constructors.add((Constructor<?>) call);
				}
			} catch (ClassNotFoundException e) {

			}
			// add to test_methods / test_constructors
		}
	}

	/**
	 * Read information from task file
	 */
	public void populate() {
		// Parse test task
		Map<String, List<String>> allowed = getTestObjectsFromFile();
		Set<String> target_functions = new HashSet<String>();

		// Analyze each entry of test task
		for (String classname : allowed.keySet()) {
			try {
				Class<?> clazz = classLoader.loadClass(classname);
				// Class<?> clazz = Class.forName(classname);

				logger.debug("Analysing class " + classname);
				List<String> restriction = allowed.get(classname);

				// Add all constructors
				for (Constructor<?> constructor : getConstructors(clazz)) {
					String name = "<init>"
					        + org.objectweb.asm.Type.getConstructorDescriptor(constructor);

					if (Properties.TT) {
						String orig = name;
						name = BooleanTestabilityTransformation.getOriginalNameDesc(clazz.getName(),
						                                                            "<init>",
						                                                            org.objectweb.asm.Type.getConstructorDescriptor(constructor));
						logger.info("TT name: " + orig + " -> " + name);

					}

					if (isTargetClassName(constructor.getDeclaringClass().getName())
					        && !constructor.isSynthetic()
					        && !Modifier.isAbstract(constructor.getModifiers())) {
						target_functions.add(constructor.getDeclaringClass().getName()
						        + "."
						        + constructor.getName()
						        + org.objectweb.asm.Type.getConstructorDescriptor(constructor));
						// num_defined_methods++;
						logger.debug("Keeping track of "
						        + constructor.getDeclaringClass().getName()
						        + "."
						        + constructor.getName()
						        + org.objectweb.asm.Type.getConstructorDescriptor(constructor));
						logger.debug(constructor.getDeclaringClass().getName()
						        + " starts with " + classname);
					}

					if (canUse(constructor) && matches(name, restriction)) {
						logger.debug("[TestCall] Adding constructor "
						        + classname
						        + "."
						        + constructor.getName()
						        + org.objectweb.asm.Type.getConstructorDescriptor(constructor));
						test_constructors.add(constructor);
						calls.add(constructor);

					} else {
						if (!canUse(constructor)) {
							logger.debug("[TestCall] Constructor cannot be used: "
							        + constructor);
						} else {
							logger.debug("[TestCall] Constructor does not match: "
							        + constructor);
						}
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
						logger.info("TT name: " + orig + " -> " + name);
					}

					if (isTargetClassName(method.getDeclaringClass().getName())
					        && !method.isSynthetic()
					        && !Modifier.isAbstract(method.getModifiers())) {
						target_functions.add(method.getDeclaringClass().getName() + "."
						        + method.getName()
						        + org.objectweb.asm.Type.getMethodDescriptor(method));
						// num_defined_methods++;
						logger.debug("Keeping track of "
						        + method.getDeclaringClass().getName() + "."
						        + method.getName()
						        + org.objectweb.asm.Type.getMethodDescriptor(method));
					}

					if (canUse(method) && matches(name, restriction)) {
						logger.debug("[TestCall] Adding method " + classname + "."
						        + method.getName()
						        + org.objectweb.asm.Type.getMethodDescriptor(method));
						test_methods.add(method);
						calls.add(method);
					} else {
						if (!canUse(method)) {
							logger.debug("[TestCall] Method cannot be used: " + method);
						} else {
							logger.debug("[TestCall] Method does not match: " + name);
							matchesDebug(name, restriction);
						}
					}
				}

				// Add all fields
				for (Field field : getFields(clazz)) {
					if (canUse(field) && matches(field.getName(), restriction)) {
						// logger.info("Adding field " + classname + "." +
						// field.getName());
						if (!Modifier.isFinal(field.getModifiers())) {
							calls.add(field);
							test_fields.add(field);
						}
						// addGenerator(field, field.getType());
					}
				}

			} catch (ClassNotFoundException e) {

				if (e.getCause() != null)
					e.getCause().printStackTrace();
				logger.error("Error loading class: " + classname + ": " + e.getCause()
				        + " -  ignoring for tests");
				continue;
			} catch (ExceptionInInitializerError e) {
				logger.error("Error in static constructor while trying to load class "
				        + classname + ": " + e.getCause());
				e.getCause().printStackTrace();
				continue;
				// } catch (VerifyError e) {
				// logger.warn("Ignoring class with verify error: " + classname
				// + ": "
				// + e.getCause());
				// continue;
			}
		}
		logger.info("Found " + test_constructors.size() + " constructors");
		logger.info("Found " + test_methods.size() + " methods");
		logger.info("Found " + test_fields.size() + " fields");

		// num_defined_methods = target_functions.size();
		// logger.info("Target class has "+num_defined_methods+" functions");

	}

	private static Map<String, List<String>> getIncludesFromFile() {
		String property = Properties.TEST_INCLUDES;
		Map<String, List<String>> objs = new HashMap<String, List<String>>();
		if (property == null) {
			logger.debug("No include file specified");
			return objs;
		}

		File file = new File(property);
		if (!file.exists()) {
			file = new File(Properties.OUTPUT_DIR + "/" + property);
			if (!file.exists() || !file.isFile()) {
				logger.debug("No include file specified");
				return objs;
			}
		}
		List<String> lines = Utils.readFile(file);
		for (String line : lines) {
			line = line.trim();
			// Skip comments
			if (line.startsWith("#"))
				continue;

			String[] parameters = line.split(",");
			if (parameters.length != 2)
				continue;
			if (!objs.containsKey(parameters[0]))
				objs.put(parameters[0], new ArrayList<String>());

			objs.get(parameters[0]).add(parameters[1]);
		}
		return objs;
	}

	private void addStandardIncludes() {
		try {
			// Primitives
			calls.add(Integer.class.getConstructor(int.class));
			calls.add(Double.class.getConstructor(double.class));
			calls.add(Float.class.getConstructor(float.class));
			calls.add(Long.class.getConstructor(long.class));
			calls.add(Short.class.getConstructor(short.class));
			calls.add(Character.class.getConstructor(char.class));
			calls.add(Boolean.class.getConstructor(boolean.class));
			calls.add(Byte.class.getConstructor(byte.class));

			// Streams without IO
			calls.add(ByteArrayInputStream.class.getConstructor(byte[].class));
			calls.add(ByteArrayOutputStream.class.getConstructor());
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}
	}

	/**
	 * Add all classes that are explicitly requested by the user
	 */
	private void addIncludes() {
		addStandardIncludes();

		Map<String, List<String>> include_map = getIncludesFromFile();
		int num = 0;
		for (String classname : include_map.keySet()) {
			try {
				// Class<?> clazz = Class.forName(classname);
				Class<?> clazz = classLoader.loadClass(classname);
				boolean found = false;
				for (String methodname : include_map.get(classname)) {
					for (Method m : getMethods(clazz)) {
						String signature = m.getName()
						        + org.objectweb.asm.Type.getMethodDescriptor(m);
						if (canUse(m) && signature.matches(methodname)) {
							logger.trace("Adding included method " + m);
							calls.add(m);
							num++;
							found = true;
						}
					}
					for (Constructor<?> c : getConstructors(clazz)) {
						String signature = "<init>"
						        + org.objectweb.asm.Type.getConstructorDescriptor(c);
						if (canUse(c) && signature.matches(methodname)) {
							logger.trace("Adding included constructor " + c + " "
							        + signature);
							calls.add(c);
							num++;
							found = true;
						}
					}
					for (Field f : getFields(clazz)) {
						String signature = f.getName();
						if (canUse(f) && signature.matches(methodname)) {
							logger.trace("Adding included field " + f + " " + signature);
							calls.add(f);
							num++;
							found = true;
						}
					}
					if (!found) {
						logger.warn("Could not find any methods matching " + methodname
						        + " in class " + classname);
						logger.info("Candidates are: ");
						for (Constructor<?> c : clazz.getConstructors()) {
							logger.info("<init>"
							        + org.objectweb.asm.Type.getConstructorDescriptor(c));
						}
						for (Method m : clazz.getMethods()) {
							logger.info(m.getName()
							        + org.objectweb.asm.Type.getMethodDescriptor(m));
						}
					}
				}
			} catch (ClassNotFoundException e) {
				logger.warn("Cannot include class " + classname + ": Class not found");
			}
		}
		logger.info("Added " + num + " other calls from include file");

	}

	private static Map<String, List<String>> getExcludesFromFile() {
		String property = Properties.TEST_EXCLUDES;
		Map<String, List<String>> objs = new HashMap<String, List<String>>();
		if (property == null) {
			logger.debug("No exclude file specified");
			return objs;
		}
		File file = new File(property);
		if (!file.exists()) {
			file = new File(Properties.OUTPUT_DIR + "/" + property);
			if (!file.exists() || !file.isFile()) {
				logger.debug("No exclude file specified");
				return objs;
			}
		}

		List<String> lines = Utils.readFile(file);
		for (String line : lines) {
			line = line.trim();
			// Skip comments
			if (line.startsWith("#"))
				continue;

			String[] parameters = line.split(",");
			if (parameters.length != 2)
				continue;
			if (!objs.containsKey(parameters[0]))
				objs.put(parameters[0], new ArrayList<String>());

			objs.get(parameters[0]).add(parameters[1]);
		}
		logger.info("Found " + objs.size() + " classes with excludes");

		return objs;
	}

	private Collection<String> getCluster() {

		File clusterFile = new File(Properties.OUTPUT_DIR + "/" + Properties.TARGET_CLASS
		        + ".cluster");
		if (clusterFile.exists()) {
			logger.info("Loading files from precalculated cluster");
			return Utils.readFile(clusterFile);
		} else {
			logger.info("Creating cluster on the fly");
			return getHierarchy().getAllClasses();
		}
	}

	/**
	 * Load all classes in the current project
	 */
	private void analyzeTarget() {
		logger.info("Getting list of classes");

		Collection<String> all_classes = getCluster();
		Set<Class<?>> dependencies = new HashSet<Class<?>>();

		if (Properties.STUBS) {
			try {
				calls.add(Sandbox.class.getDeclaredField("accessedFiles"));
				calls.add(Sandbox.class.getDeclaredField("lastAccessedFile"));
				logger.info("Added file handling");
			} catch (SecurityException e) {
				logger.info("Failed to add file handling: ", e);
			} catch (NoSuchFieldException e) {
				logger.info("Failed to add file handling: ", e);
			}
		}

		// Analyze each class
		for (String classname : all_classes) {
			// In prefix?
			// if (classname.startsWith(Properties.PROJECT_PREFIX)) {
			try {
				logger.debug("Current class: " + classname);
				// Class<?> toadd = Class.forName(classname);
				Class<?> toadd = classLoader.loadClass(classname);
				analyzedClasses.add(toadd);
				if (!canUse(toadd)) {
					logger.debug("Not using class " + classname);
					continue;
				}

				if (toadd.isEnum()) {
					logger.debug("Skipping enum " + classname);
					continue;
				}

				// Keep all accessible constructors
				for (Constructor<?> constructor : getConstructors(toadd)) {
					logger.trace("Considering constructor " + constructor);
					if (test_excludes.containsKey(classname)) {
						boolean valid = true;
						String full_name = "<init>"
						        + org.objectweb.asm.Type.getConstructorDescriptor(constructor);
						for (String regex : test_excludes.get(classname)) {
							if (full_name.matches(regex)) {
								logger.info("Found excluded constructor: " + constructor
								        + " matches " + regex);
								valid = false;
								break;
							}
						}
						if (!valid)
							continue;
					}
					if (canUse(constructor)) {
						for (Class<?> clazz : constructor.getParameterTypes()) {
							if (!all_classes.contains(clazz.getName())) {
								if (clazz.isArray())
									dependencies.add(clazz.getComponentType());
								else
									dependencies.add(clazz);
							}
						}
						logger.debug("Adding constructor " + constructor);
						constructor.setAccessible(true);
						calls.add(constructor);
					} else {
						logger.trace("Constructor " + constructor + " is not public");
					}
				}

				// Keep all accessible methods
				for (Method method : getMethods(toadd)) {
					// if(method.getDeclaringClass().equals(Object.class))
					// continue;
					if (test_excludes.containsKey(classname)) {
						boolean valid = true;
						String full_name = method.getName()
						        + org.objectweb.asm.Type.getMethodDescriptor(method);
						for (String regex : test_excludes.get(classname)) {
							if (full_name.matches(regex)) {
								valid = false;
								logger.info("Found excluded method: " + classname + "."
								        + full_name + " matches " + regex);
								break;
							}
						}
						if (!valid)
							continue;
					}
					if (canUse(method)) {
						for (Class<?> clazz : method.getParameterTypes()) {
							if (!all_classes.contains(clazz.getName())) {
								if (clazz.isArray())
									dependencies.add(clazz.getComponentType());
								else
									dependencies.add(clazz);
							}
						}
						method.setAccessible(true);
						calls.add(method);
						logger.debug("Adding method " + method);
					}
				}

				// Keep all accessible fields
				for (Field field : getFields(toadd)) {
					// if(!Modifier.isPrivate(field.getModifiers()) &&
					// !Modifier.isProtected(field.getModifiers()) &&
					// !Modifier.isProtected(field.getDeclaringClass().getModifiers())
					// &&
					// !Modifier.isPrivate(field.getDeclaringClass().getModifiers()))
					// {
					if (test_excludes.containsKey(classname)) {
						boolean valid = true;
						for (String regex : test_excludes.get(classname)) {
							if (field.getName().matches(regex)) {
								valid = false;
								logger.info("Found excluded field: " + classname + "."
								        + field.getName() + " matches " + regex);
								break;
							}
						}
						if (!valid)
							continue;
					}

					if (canUse(field) && !Modifier.isFinal(field.getModifiers())) {
						field.setAccessible(true);
						calls.add(field);
						logger.trace("Adding field " + field);
					} else {
						logger.trace("Cannot use field " + field);
					}
				}
			} catch (ClassNotFoundException e) {
				logger.debug("Ignoring class " + classname);
			} catch (ExceptionInInitializerError e) {
				logger.debug("Problem - ignoring class " + classname + ": " + e);
			} catch (Throwable t) {
				logger.info("Error when trying to read class " + classname + ": " + t);
			}

			// }
		}
		logger.info("Found " + calls.size() + " other calls");
		// logger.info("Found "+dependencies.size()+" unsatisfied dependencies:");
		logger.info("Unsatisfied dependencies:");
		Set<Class<?>> neededDependencies = new HashSet<Class<?>>();
		for (Class<?> clazz : dependencies) {
			while (clazz.isArray()) {
				clazz = clazz.getComponentType();
			}
			if (clazz.isPrimitive())
				continue;
			if (hasGenerator(clazz))
				continue;
			logger.info("  " + clazz.getName());
			neededDependencies.add(clazz);
			// addCalls(clazz);
		}
		if (!neededDependencies.isEmpty())
			loadDependencies(neededDependencies, 0);
	}

	private void loadClass(Class<?> clazz, Set<Class<?>> dependencies) {

	}

	/**
	 * If there is a class for which we have no generator, start a search of the
	 * classpath to identify something that can serve as generator
	 * 
	 * @param dependencies
	 */
	private void loadDependencies(Set<Class<?>> all_classes, int depth) {
		logger.info("Getting list of classes");
		if (depth > 3)
			return;

		Set<Class<?>> dependencies = new HashSet<Class<?>>();

		// Analyze each class
		for (Class<?> toadd : all_classes) {
			analyzedClasses.add(toadd);

			String classname = toadd.getName();
			logger.trace("Current class: " + classname);
			if (!canUse(toadd)) {
				logger.debug("Not using class " + classname);
				continue;
			}

			if (toadd.isEnum()) {
				logger.debug("Skipping enum " + classname);
				continue;
			}

			// Keep all accessible constructors
			for (Constructor<?> constructor : getConstructors(toadd)) {
				logger.trace("Considering constructor " + constructor);
				if (test_excludes.containsKey(classname)) {
					boolean valid = true;
					String full_name = "<init>"
					        + org.objectweb.asm.Type.getConstructorDescriptor(constructor);
					for (String regex : test_excludes.get(classname)) {
						if (full_name.matches(regex)) {
							logger.info("Found excluded constructor: " + constructor
							        + " matches " + regex);
							valid = false;
							break;
						}
					}
					if (!valid)
						continue;
				}
				if (canUse(constructor)) {
					for (Class<?> clazz : constructor.getParameterTypes()) {
						if (!analyzedClasses.contains(clazz)) {
							if (clazz.isArray())
								dependencies.add(clazz.getComponentType());
							else
								dependencies.add(clazz);
						}
					}
					logger.debug("Adding constructor " + constructor);
					constructor.setAccessible(true);
					calls.add(constructor);
				} else {
					logger.trace("Constructor " + constructor + " is not public");
				}
			}

			// Keep all accessible methods
			for (Method method : getMethods(toadd)) {
				// if(method.getDeclaringClass().equals(Object.class))
				// continue;
				if (test_excludes.containsKey(classname)) {
					boolean valid = true;
					String full_name = method.getName()
					        + org.objectweb.asm.Type.getMethodDescriptor(method);
					for (String regex : test_excludes.get(classname)) {
						if (full_name.matches(regex)) {
							valid = false;
							logger.info("Found excluded method: " + classname + "."
							        + full_name + " matches " + regex);
							break;
						}
					}
					if (!valid)
						continue;
				}
				if (canUse(method)) {
					for (Class<?> clazz : method.getParameterTypes()) {
						if (!analyzedClasses.contains(clazz)) {
							if (clazz.isArray())
								dependencies.add(clazz.getComponentType());
							else
								dependencies.add(clazz);
						}
					}
					method.setAccessible(true);
					calls.add(method);
					logger.debug("Adding method " + method);
				}
			}

			// Keep all accessible fields
			for (Field field : getFields(toadd)) {
				// if(!Modifier.isPrivate(field.getModifiers()) &&
				// !Modifier.isProtected(field.getModifiers()) &&
				// !Modifier.isProtected(field.getDeclaringClass().getModifiers())
				// &&
				// !Modifier.isPrivate(field.getDeclaringClass().getModifiers()))
				// {
				if (test_excludes.containsKey(classname)) {
					boolean valid = true;
					for (String regex : test_excludes.get(classname)) {
						if (field.getName().matches(regex)) {
							valid = false;
							logger.info("Found excluded field: " + classname + "."
							        + field.getName() + " matches " + regex);
							break;
						}
					}
					if (!valid)
						continue;
				}

				if (canUse(field) && !Modifier.isFinal(field.getModifiers())) {
					field.setAccessible(true);
					calls.add(field);
					logger.trace("Adding field " + field);
				} else {
					logger.trace("Cannot use field " + field);
				}
			}

		}
		logger.info("Found " + calls.size() + " other calls");
		// logger.info("Found "+dependencies.size()+" unsatisfied dependencies:");
		logger.info("Unsatisfied dependencies:");
		Set<Class<?>> neededDependencies = new HashSet<Class<?>>();
		for (Class<?> clazz : dependencies) {
			if (clazz.isArray()) {
				clazz = clazz.getComponentType();
			}
			if (clazz.isPrimitive())
				continue;
			if (hasGenerator(clazz))
				continue;
			logger.info("  " + clazz.getName());
			neededDependencies.add(clazz);
			// addCalls(clazz);
		}
		if (!neededDependencies.isEmpty())
			loadDependencies(neededDependencies, depth + 1);

	}

	/**
	 * Call each of the duplicated static constructors
	 */
	@Override
	public void resetStaticClasses() {
		ExecutionTracer.disable();
		for (Method m : static_initializers) {
			// TODO: Which classes need to be reset? All?
			if (!m.getDeclaringClass().equals(Properties.getTargetClass()))
				continue;

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
			;
		}
		ExecutionTracer.enable();
	}

	/**
	 * Determine the set of classes that have static constructors
	 */
	private void getStaticClasses() {
		Iterator<String> it = StaticInitializationClassAdapter.static_classes.iterator();
		while (it.hasNext()) {
			String classname = it.next();
			try {
				// Class<?> clazz = Class.forName(classname);
				Class<?> clazz = classLoader.loadClass(classname);
				Method m = clazz.getMethod("__STATIC_RESET", (Class<?>[]) null);
				m.setAccessible(true);
				static_initializers.add(m);
				logger.info("Adding static class: " + classname);
			} catch (ClassNotFoundException e) {
				logger.info("Static: Could not find class: " + classname);
			} catch (SecurityException e) {
				logger.info("Static: Security exception: " + classname);
				// TODO Auto-generated catch block
				// e.printStackTrace();
			} catch (NoSuchMethodException e) {
				logger.info("Static: Could not find method clinit in : " + classname);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Unload all classes; perform cleanup
	 */
	@Override
	public void resetCluster() {

		calls.clear();
		test_constructors.clear();
		test_methods.clear();
		test_fields.clear();
		generators.clear();

		BranchPool.clear();
		CFGPool.clear();
		BytecodeInstructionPool.clear();

		// Get new classloader
		classLoader = new InstrumentingClassLoader();
	}

	/**
	 * Find a class that matches the given name
	 * 
	 * @param name
	 * @return
	 * @throws ClassNotFoundException
	 */
	@Override
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
			classLoader.loadClass("java.lang." + name);
		} catch (ClassNotFoundException e) {
			// Ignore it as we throw our own
		}

		throw new ClassNotFoundException(name);

	}

	/**
	 * @return the analyzedClasses
	 */
	@Override
	public Set<Class<?>> getAnalyzedClasses() {
		return analyzedClasses;
	}

	/**
	 * Integrate a new class into the test cluster
	 * 
	 * @param name
	 * @throws ClassNotFoundException
	 */
	@Override
	public Class<?> importClass(String name) throws ClassNotFoundException {

		Class<?> clazz = classLoader.loadClass(name);

		analyzedClasses.add(clazz);

		logger.trace("Importing class: " + name);

		// Keep all accessible constructors
		for (Constructor<?> constructor : getConstructors(clazz)) {
			logger.trace("Considering constructor " + constructor);
			if (test_excludes.containsKey(name)) {
				boolean valid = true;
				String full_name = "<init>"
				        + org.objectweb.asm.Type.getConstructorDescriptor(constructor);
				for (String regex : test_excludes.get(name)) {
					if (full_name.matches(regex)) {
						logger.info("Found excluded constructor: " + constructor
						        + " matches " + regex);
						valid = false;
						break;
					}
				}
				if (!valid)
					continue;
			}
			if (canUse(constructor)) {
				logger.debug("Adding constructor " + constructor);
				constructor.setAccessible(true);
				calls.add(constructor);
			} else {
				logger.trace("Constructor " + constructor + " is not public");
			}
		}

		// Keep all accessible methods
		for (Method method : getMethods(clazz)) {
			// if(method.getDeclaringClass().equals(Object.class))
			// continue;
			if (test_excludes.containsKey(name)) {
				boolean valid = true;
				String full_name = method.getName()
				        + org.objectweb.asm.Type.getMethodDescriptor(method);
				for (String regex : test_excludes.get(name)) {
					if (full_name.matches(regex)) {
						valid = false;
						logger.info("Found excluded method: " + name + "." + full_name
						        + " matches " + regex);
						break;
					}
				}
				if (!valid)
					continue;
			}
			if (canUse(method)) {
				method.setAccessible(true);
				calls.add(method);
				logger.debug("Adding method " + method);
			}
		}

		// Keep all accessible fields
		for (Field field : getFields(clazz)) {
			if (test_excludes.containsKey(name)) {
				boolean valid = true;
				for (String regex : test_excludes.get(name)) {
					if (field.getName().matches(regex)) {
						valid = false;
						logger.info("Found excluded field: " + name + "."
						        + field.getName() + " matches " + regex);
						break;
					}
				}
				if (!valid)
					continue;
			}

			if (canUse(field) && !Modifier.isFinal(field.getModifiers())) {
				field.setAccessible(true);
				calls.add(field);
				logger.trace("Adding field " + field);
			} else {
				logger.trace("Cannot use field " + field);
			}
		}

		return clazz;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.testcase.TestCluster#checkDependencies(java.lang
	 * .reflect.AccessibleObject)
	 */
	@Override
	public void checkDependencies(AccessibleObject o) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unisb.cs.st.evosuite.testcase.TestCluster#getTestCalls()
	 */
	@Override
	public List<AccessibleObject> getTestCalls() {
		List<AccessibleObject> testCalls = new ArrayList<AccessibleObject>();
		testCalls.addAll(test_constructors);
		testCalls.addAll(test_methods);
		return testCalls;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.testcase.TestCluster#getKnownMatchingClasses(
	 * java.lang.String)
	 */
	@Override
	public Collection<Class<?>> getKnownMatchingClasses(String name) {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		for (Class<?> c : analyzedClasses) {
			if (c.getName().endsWith(name))
				classes.add(c);
		}
		return classes;
	}

}
