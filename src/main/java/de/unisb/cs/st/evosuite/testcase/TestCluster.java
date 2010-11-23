/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */


package de.unisb.cs.st.evosuite.testcase;

import java.io.File;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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

import org.apache.log4j.Logger;

import de.unisb.cs.st.ds.util.io.Io;
import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.cfg.CFGMethodAdapter;
import de.unisb.cs.st.evosuite.ga.ConstructionFailedException;
import de.unisb.cs.st.evosuite.ga.Randomness;
import de.unisb.cs.st.evosuite.javaagent.StaticInitializationClassAdapter;
import de.unisb.cs.st.javalanche.coverage.distance.Hierarchy;
import de.unisb.cs.st.javalanche.coverage.distance.MethodDescription;
import de.unisb.cs.st.javalanche.mutation.javaagent.classFileTransfomer.mutationDecision.Excludes;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;

/**
 * The test cluster contains the information about all classes 
 * and their members in the target package
 * 
 * @author Gordon Fraser
 *
 */
public class TestCluster {

	/** Logger */
	private static Logger logger = Logger.getLogger(TestCluster.class); 
	
	/** Random number generator */
	private Randomness randomness = Randomness.getInstance();

	/** Classes excluded by Javalanche */
	private Excludes excludes = Excludes.getInstance(); 

	/** Instance variable */
	private static TestCluster instance = null;
	
	/** The usable methods of the class under test */
	public List<Method> test_methods = new ArrayList<Method>();
	
	/** The usable constructor of the class under test */
	private List<Constructor<?>> test_constructors = new ArrayList<Constructor<?>>();;

	/** Cache results about generators */
	private HashMap<Type, List<AccessibleObject> > generators = new HashMap<Type, List<AccessibleObject>>(); 
	
	private HashMap<Type, List<AccessibleObject> > calls_with = new HashMap<Type, List<AccessibleObject>>();

	private HashMap<Type, List<AccessibleObject> > calls_for  = new HashMap<Type, List<AccessibleObject>>();

	/** The entire set of calls available */
	Set<AccessibleObject> calls = new HashSet<AccessibleObject>();
	
	private List<Method> static_initializers = new ArrayList<Method>();
	
	public static final List<String> EXCLUDE = Arrays.asList("<clinit>", "__STATIC_RESET");

	
//	public int num_defined_methods = 2;
	public int num_defined_methods = 0;
	
	/**
	 * Private constructor
	 */
	private TestCluster() {
		populate();
		addIncludes();
		analyzeTarget();
		countTargetFunctions();
		/*
		for(Method m : TestHelper.class.getDeclaredMethods()) {
			calls.add(m);
			test_methods.add(m);
		}
		*/
		
		getStaticClasses();
		ExecutionTracer.enable();
	}
	
	/**
	 * Instance accessor
	 * @return
	 */
	public static TestCluster getInstance() {
		if(instance == null) 
			instance = new TestCluster();
		
		return instance;
	}
	
	/**
	 * Get a list of all generator objects for the type
	 * @param type
	 * @return
	 * @throws ConstructionFailedException 
	 */
	public List<AccessibleObject> getGenerators(Type type) throws ConstructionFailedException {
		cacheGeneratorType(type);
		if(!generators.containsKey(type))
			throw new ConstructionFailedException();
		
		return generators.get(type);
	}

	/**
	 * Determine if there are generators 
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	public boolean hasGenerator(Type type) throws ConstructionFailedException {
		cacheGeneratorType(type);
		if(!generators.containsKey(type))
			throw new ConstructionFailedException();
		return !generators.get(type).isEmpty();
	}

	/**
	 * Randomly select one generator
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	public AccessibleObject getRandomGenerator(Type type) throws ConstructionFailedException {
		cacheGeneratorType(type);
		if(!generators.containsKey(type))
			return null;
		
		return randomness.choice(generators.get(type));
	}
	
	/**
	 * Randomly select one generator
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	public AccessibleObject getRandomGenerator(Type type, Set<AccessibleObject> excluded) throws ConstructionFailedException {
		cacheGeneratorType(type);
		if(!generators.containsKey(type))
			return null;
		
		List<AccessibleObject> choice = new ArrayList<AccessibleObject>(generators.get(type));
		logger.debug("Removing "+excluded.size()+" from "+choice.size()+" generators");
		choice.removeAll(excluded);
		if(choice.isEmpty())
			return null;
		
		int num = 0;
        int param = 1000;
        for(int i = 0; i < Properties.GENERATOR_TOURNAMENT; i++) {
        	int new_num = randomness.nextInt(choice.size());
        	AccessibleObject o = choice.get(new_num);
        	if(o instanceof Constructor<?>) {
        		Constructor<?> c = (Constructor<?>)o;
        		if(c.getParameterTypes().length < param) {
        			param = c.getParameterTypes().length;
        			num = new_num;
        		}
        		else if(o instanceof Method) {
        			Method m = (Method)o;
        			int p = m.getParameterTypes().length;
        			if(!Modifier.isStatic(m.getModifiers()))
        				p++;
        			if(p < param) {
        				param = p;
        				num = new_num;
        			}
        		}
        		else if(o instanceof Field) {
//        			param = 2;
 //       			num = new_num;
        			Field f = (Field)o;
        			int p = 0;
        			if(!Modifier.isStatic(f.getModifiers()))
        				p++;
        			if(p < param) {
        				param = p;
        				num = new_num;
        			}
        		}
        	}
        }
        return choice.get(num);
//		return randomness.choice(choice);
	}
	
	private void cacheSuperGeneratorType(Type type, List<AccessibleObject> g) {
		//if(generators.containsKey(type))
		//	return;
		
		logger.debug("Checking superconstructors for class "+type);
		if(!(type instanceof Class<?>))
			return;
		Class<?> clazz = (Class<?>)type;
		if(clazz.isAnonymousClass() || clazz.isLocalClass() || clazz.getCanonicalName().startsWith("java.")) {
			logger.debug("Skipping superconstructors for class "+type);
			return;
		} else if(logger.isDebugEnabled()){
			logger.debug(clazz.getCanonicalName());
		}
		
//		List<AccessibleObject> g = new ArrayList<AccessibleObject>();
		
		for(AccessibleObject o : calls) {
			if(o instanceof Constructor<?>) {
				Constructor<?> c = (Constructor<?>) o;
				if(GenericClass.isSubclass(c.getDeclaringClass(), type) && c.getDeclaringClass().getName().startsWith(Properties.getProperty("PROJECT_PREFIX"))) {
					g.add(o);
				}
			} else if(o instanceof Method) {
				Method m = (Method) o;
				if(GenericClass.isSubclass(m.getGenericReturnType(), type) && m.getReturnType().getName().startsWith(Properties.getProperty("PROJECT_PREFIX"))) {
					g.add(o);
				} 
				//else if(m.getReturnType().isAssignableFrom(type) && m.getName().equals("getInstance"))
				//	g.add(o);
			} else if(o instanceof Field) {
				Field f = (Field) o;
				if(GenericClass.isSubclass(f.getGenericType(), type) && f.getType().getName().startsWith(Properties.getProperty("PROJECT_PREFIX"))) {
					g.add(f);
				}
			}
		}
		if(logger.isDebugEnabled()) {
			logger.debug("Found "+g.size()+" generators for superclasses of "+type);
			for(AccessibleObject o : g) {
				logger.debug(o);
			}
		}
		//generators.put(type, g);
	
	}
	
	/**
	 * Fill cache with information about generators
	 * @param type
	 */
	private void cacheGeneratorType(Type type) {
		if(generators.containsKey(type))
			return;
		
		// TODO: At this point check the files that redefine signatures?
		// -> This covers changed return types
		// -> But what about changed parameters?
		
		List<AccessibleObject> g = new ArrayList<AccessibleObject>();
		
		for(AccessibleObject o : calls) {
			if(o instanceof Constructor<?>) {
				Constructor<?> c = (Constructor<?>) o;
				if(GenericClass.isAssignable(type, c.getDeclaringClass())) {
					g.add(o);
				}
			} else if(o instanceof Method) {
				Method m = (Method) o;
				if(GenericClass.isAssignable(type, m.getGenericReturnType())) {
					g.add(o);
				} 
				//else if(m.getReturnType().isAssignableFrom(type) && m.getName().equals("getInstance"))
				//	g.add(o);
			} else if(o instanceof Field) {
				Field f = (Field) o;
				if(GenericClass.isAssignable(type, f.getGenericType())) {
					g.add(f);
				}
			}
		}
		if(g.isEmpty()) {
			cacheSuperGeneratorType(type, g);
		}
	//	} else
			generators.put(type, g);
	}
	
	/**
	 * Return all calls that have a parameter with given type
	 * @param type
	 * @return
	 */
	public List<AccessibleObject> getTestCallsWith(Type type) {
		List<AccessibleObject> calls = new ArrayList<AccessibleObject>();
		calls.addAll(getTestConstructorsWith(type));
		calls.addAll(getTestMethodsWith(type));
		return calls;
	}

	/**
	 * Return all calls that have a parameter with given type
	 * @param type
	 * @return
	 */
	public List<AccessibleObject> getCallsWith(Type type) {
		if(calls_with.containsKey(type))
			return calls_with.get(type);
		
		List<AccessibleObject> relevant_calls = new ArrayList<AccessibleObject>();
		for(AccessibleObject call : calls) {
			List<Type> parameters = new ArrayList<Type>();
			
			if(call instanceof Method) {
				parameters.addAll(Arrays.asList(((Method)call).getGenericParameterTypes()));
			} else if(call instanceof Constructor<?>) {
				parameters.addAll(Arrays.asList(((Constructor<?>)call).getGenericParameterTypes()));
			}
			
			if(parameters.contains(type))
				relevant_calls.add(call);
		}
		
		calls_with.put(type, relevant_calls);
		return relevant_calls;
	}
	
	/**
	 * Return all calls that have a parameter with given type
	 * @param type
	 * @return
	 */
	public List<AccessibleObject> getCallsFor(Type type) {
		if(calls_for.containsKey(type))
			return calls_for.get(type);
		
		List<AccessibleObject> relevant_calls = new ArrayList<AccessibleObject>();
		for(AccessibleObject call : calls) {
			if(call instanceof Method) {
				if(((Method)call).getDeclaringClass().isAssignableFrom((Class<?>)type))
					relevant_calls.add(call);
			}
		}
		calls_for.put(type, relevant_calls);
		return relevant_calls;
	}
	
	/**
	 * Get random method or constructor of unit under test
	 * @return
	 */
	public AccessibleObject getRandomTestCall() {
		int num_methods = test_methods.size();
		int num_constructors = test_constructors.size();
		
		int num = randomness.nextInt(num_methods + num_constructors);
		if(num >= num_methods) {
			return test_constructors.get(num - num_methods);
		}
		else {
			return test_methods.get(num);
		}
	}

	/**
	 * Get entirely random call
	 */
	public AccessibleObject getRandomCall() {
		return randomness.choice(calls);
	}

	
	/**
	 * Legacy code: get calls related to mutation
	 * @param m
	 * @return
	 */
	public List<AccessibleObject> getRelatedTestCalls(Mutation m) {
		List<AccessibleObject> result = new ArrayList<AccessibleObject>();
		
		MethodDistanceGraph distance_graph = MethodDistanceGraph.getMethodDistanceGraph();
		//DistanceGraph distance_graph = DistanceGraph.getDefault();
		String m_methodname = m.getMethodName();
		if(m_methodname == null)
			m_methodname = "<init>";
		String m_classname  = m.getClassName();
		logger.trace("Getting method descriptor "+m_classname+" "+m_methodname);
		MethodDescription mutant_md = distance_graph.getMethodDesc(m_classname, m_methodname);
		
		for(Method method : test_methods) {
			MethodDescription md = null; // = distance_graph.getMethodDesc(((Member) call).getDeclaringClass().getName(), ((Member) call).getName());
			if(method.getDeclaringClass().getName().startsWith(Properties.getProperty("PROJECT_PREFIX")))
				md = distance_graph.getMethodDesc(method.getDeclaringClass().getName(), method.getName() + org.objectweb.asm.Type.getMethodDescriptor(method));
			if(m_methodname.equals(method.getName()+org.objectweb.asm.Type.getMethodDescriptor(method)) && m_classname.equals(method.getDeclaringClass())) {
				logger.debug("Candidate "+method.getDeclaringClass().getName()+"."+m_methodname+" has distance "+distance_graph.getDistance(md, mutant_md));
			}

			double distance = distance_graph.getDistance(md, mutant_md);
			if(distance < 1000) {
				logger.debug("Related: "+mutant_md+" "+md+" = "+distance);
				result.add(method);
			}
		}
		
		for(Constructor<?> c : test_constructors) {
			MethodDescription md = null; // = distance_graph.getMethodDesc(((Member) call).getDeclaringClass().getName(), ((Member) call).getName());
			if(c.getDeclaringClass().getName().startsWith(Properties.getProperty("PROJECT_PREFIX")))
				md = distance_graph.getMethodDesc(c.getDeclaringClass().getName(), "<init>"+org.objectweb.asm.Type.getConstructorDescriptor(c));
			double distance = distance_graph.getDistance(md, mutant_md);
			if(distance < 1000) {
//			if(distance < 1) {
				logger.debug("Related: "+mutant_md+" "+md+" = "+distance);
				result.add(c);
			}
		}
		
		if(!result.isEmpty())
			return result;
		
		// For each element in constructors and methods: Calculate distance
		for(AccessibleObject call : calls) {
			MethodDescription md = null; // = distance_graph.getMethodDesc(((Member) call).getDeclaringClass().getName(), ((Member) call).getName());
			if(call instanceof Constructor<?>) {
				Constructor<?> c = (Constructor<?>) call;
				if(c.getDeclaringClass().getName().startsWith(Properties.getProperty("PROJECT_PREFIX")))
					md = distance_graph.getMethodDesc(c.getDeclaringClass().getName(), "<init>"+org.objectweb.asm.Type.getConstructorDescriptor(c));
			}
			else if(call instanceof Method) {
				Method me = (Method)call;
				if(me.getDeclaringClass().getName().startsWith(Properties.getProperty("PROJECT_PREFIX")))
					md = distance_graph.getMethodDesc(me.getDeclaringClass().getName(), me.getName() + org.objectweb.asm.Type.getMethodDescriptor(me));
				if(m_methodname.equals(me.getName()+org.objectweb.asm.Type.getMethodDescriptor(me)) && m_classname.equals(me.getDeclaringClass())) {
					logger.debug("Candidate "+me.getDeclaringClass().getName()+"."+m_methodname+" has distance "+distance_graph.getDistance(md, mutant_md));
				}
			}
			double distance = distance_graph.getDistance(md, mutant_md);
			if(distance < 1000) {
//			if(distance < 1) {
				logger.debug("Related: "+mutant_md+" "+md+" = "+distance);
				result.add(call);
			}
		}

		return result;
	}
	
	//----------------------------------------------------------------------------------
	
	
	/**
	 * Create list of all methods using a certain type as parameter
	 * 
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	private List<Method> getTestMethodsWith(Type type)  {
		List<Method> suitable_methods = new ArrayList<Method>();
		
		for(Method m : test_methods) {
			if(Arrays.asList(m.getGenericParameterTypes()).contains(type))
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
	private List<Constructor<?>> getTestConstructorsWith(Type type)  {
		List<Constructor<?>> suitable_constructors = new ArrayList<Constructor<?>>();
		
		for(Constructor<?> c : test_constructors) {
			if(Arrays.asList(c.getGenericParameterTypes()).contains(type))
				suitable_constructors.add(c);
		}
		return suitable_constructors;
	}

	
	/**
	 * Get the set of constructors defined in this class and its superclasses
	 * @param clazz
	 * @return
	 */
	public static Set<Constructor<?>> getConstructors(Class<?> clazz) {
		Map<String,Constructor<?>> helper = new HashMap<String,Constructor<?>>();
		
		Set<Constructor<?>> constructors = new HashSet<Constructor<?>>();
		if(clazz.getSuperclass() != null ) {
//			constructors.addAll(getConstructors(clazz.getSuperclass()));
			for(Constructor<?> c : getConstructors(clazz.getSuperclass())) {
				helper.put(org.objectweb.asm.Type.getConstructorDescriptor(c), c);
			}
		}
		for(Class<?> in : clazz.getInterfaces()) {
			for(Constructor<?> c : getConstructors(in)) {
				helper.put(org.objectweb.asm.Type.getConstructorDescriptor(c), c);
			}
//			constructors.addAll(getConstructors(in));			
		}

		
		//for(Constructor c : clazz.getConstructors()) {
		//	constructors.add(c);			
		//}
		for(Constructor<?> c : clazz.getDeclaredConstructors()) {
			//constructors.add(c);
			helper.put(org.objectweb.asm.Type.getConstructorDescriptor(c), c);
		}
		for(Constructor<?> c : helper.values()) {
			constructors.add(c);
		}
		return constructors;
	}

	/**
	 * Get the set of methods defined in this class and its superclasses
	 * @param clazz
	 * @return
	 */
	public static Set<Method> getMethods(Class<?> clazz) {
		
		Map<String,Method> helper = new HashMap<String,Method>();
		Set<Method> methods = new HashSet<Method>();

		if(clazz.getSuperclass() != null ) {
//			constructors.addAll(getConstructors(clazz.getSuperclass()));
			for(Method m : getMethods(clazz.getSuperclass())) {
				helper.put(m.getName()+org.objectweb.asm.Type.getMethodDescriptor(m), m);
			}
		}
		for(Class<?> in : clazz.getInterfaces()) {
			for(Method m : getMethods(in)) {
				helper.put(m.getName()+org.objectweb.asm.Type.getMethodDescriptor(m), m);
			}
//			constructors.addAll(getConstructors(in));			
		}

		
		//for(Constructor c : clazz.getConstructors()) {
		//	constructors.add(c);			
		//}
		for(Method m : clazz.getDeclaredMethods()) {
			//constructors.add(c);
			helper.put(m.getName()+org.objectweb.asm.Type.getMethodDescriptor(m), m);
		}
		for(Method m : helper.values()) {
			methods.add(m);
		}
		return methods;
	}	
	
	/**
	 * Get the set of fields defined in this class and its superclasses
	 * @param clazz
	 * @return
	 */
	private static Set<Field> getFields(Class<?> clazz) {
		// TODO: Helper not necessary here!
		Map<String,Field> helper = new HashMap<String,Field>();

		Set<Field> fields = new HashSet<Field>();
		if(clazz.getSuperclass() != null ) {
			//fields.addAll(getFields(clazz.getSuperclass()));
			for(Field f : getFields(clazz.getSuperclass())) {
				helper.put(f.toGenericString(), f);
			}

		}
		for(Class<?> in : clazz.getInterfaces()) {
//			fields.addAll(getFields(in));			
			for(Field f : getFields(in)) {
				helper.put(f.toGenericString(), f);
			}
		}

		for(Field f : clazz.getDeclaredFields()) {
//			fields.add(m);			
			helper.put(f.toGenericString(), f);
		}
		//for(Field m : clazz.getDeclaredFields()) {
		//	fields.add(m);			
		//}
		for(Field f: helper.values()) {
			fields.add(f);
		}

		return fields;
	}
	
	/**
	 * Load test methods from test task file
	 * @return
	 *   Map from classname to list of methodnames
	 */
	private Map<String, List<String> > getTestObjectsFromFile() {
		//String property = System.getProperty("test.classes");
		String property = Properties.TARGET_CLASS;
		//String filename = property;
		//if(property == null || property.equals("${test.classes}")) {
	//		property = Properties.TARGET_CLASS;
			String filename = Properties.OUTPUT_DIR+"/"+property+".task";
		//}
		logger.info("Reading test methods from "+filename);
		File file = new File(filename);
		List<String> lines = Io.getLinesFromFile(file);
		Map<String, List<String> > objs = new HashMap<String, List<String> >();
		for(String line : lines) {
			line = line.trim();
			// Skip comments
			if(line.startsWith("#"))
				continue;
			
			String[] parameters = line.split(",");
			if(parameters.length != 2)
				continue;
			if(!objs.containsKey(parameters[0]))
				objs.put(parameters[0], new ArrayList<String>());
			
			objs.get(parameters[0]).add(parameters[1]);
		}
		return objs;
	}
	
	private static boolean canUse(Class<?> c) {
//		if(Modifier.isAbstract(c.getModifiers()))
//			return false;
		
		if(Throwable.class.isAssignableFrom(c))
			return false;
	    if(Modifier.isPrivate(c.getModifiers())) // && !(Modifier.isProtected(c.getModifiers())))
	    	return false;

	    /*
	    if(Modifier.isAbstract(c.getModifiers()))
	    	return false;

	    if(c.isLocalClass() || c.isAnonymousClass())
			return false;
	*/    
	    
	    if(c.getName().matches(".*\\$\\d+$")) {
	    	logger.debug(c+" looks like an anonymous class, ignoring it");
	    	return false;
		 }

	    
	    if(Modifier.isPublic(c.getModifiers()))
	    	return true;
	    /*

	    if(Modifier.isProtected(c.getModifiers()))
	    	return true;
*/
	    
	    return false;
	}

	private static boolean canUse(Field f) {
		//if(Modifier.isPrivate(f.getDeclaringClass().getModifiers())) //Modifier.isProtected(f.getDeclaringClass().getModifiers()) || 
		//	return false;

	    //TODO we could enable some methods from Object, like getClass
	    if (f.getDeclaringClass().equals(java.lang.Object.class))
	    	return false;//handled here to avoid printing reasons

	    if (f.getDeclaringClass().equals(java.lang.Thread.class))
	    	return false;//handled here to avoid printing reasons

		if(Modifier.isPublic(f.getModifiers()))
	    	return true;

	    /*
		if(Modifier.isProtected(f.getModifiers()))
	    	return true;
	    	*/
/*
	    if(!(Modifier.isPrivate(f.getModifiers()))) // && !(Modifier.isProtected(f.getModifiers())))
	    	return true;	    
*/
	    return false;
	}

	private static boolean canUse(Method m) {
		
		if(EXCLUDE.contains(m.getName())) {
			logger.debug("Excluding method");
			return false;
		}
		
		if (m.isBridge()){
			logger.debug("Will not use: " + m.toString());
	        logger.debug("  reason: it's a bridge method");
	        return false;
		}

	    if (m.isSynthetic()){
	    	logger.debug("Will not use: " + m.toString());
	        logger.debug("  reason: it's a synthetic method");
	        return false;
	    }

	   // if(!Modifier.isPublic(m.getModifiers()))
	   // 	return false;
	    
	    //if (Modifier.isPrivate(m.getModifiers())) // || Modifier.isProtected(m.getModifiers()))
	    //	return false;

	    //TODO?
//		if(Modifier.isProtected(m.getDeclaringClass().getModifiers()) || Modifier.isPrivate(m.getDeclaringClass().getModifiers()))
		//if(Modifier.isPrivate(m.getDeclaringClass().getModifiers()))
		//	return false;

	    //TODO we could enable some methods from Object, like getClass
		
	    if (m.getDeclaringClass().equals(java.lang.Object.class)) {
	    	return false;
//	    	if(!m.getName().equals("toString") &&
//	    	   !m.getName().equals("getClass"))
//	    		return false;//handled here to avoid printing reasons
	    }
	    

	    if (m.getDeclaringClass().equals(java.lang.Thread.class))
	    	return false;//handled here to avoid printing reasons

	    String reason = doNotUseSpecialCase(m);
	    if (reason != null) {
	    	logger.debug("Will not use: " + m.toString());
	        logger.debug("  reason: " + reason);
	        return false;
	    }
	    
	    if(m.getName().equals("__STATIC_RESET")) {
	    	logger.info("Ignoring static reset class");
	    	return false;
	    }

	    // If default or 
	    if (Modifier.isPublic(m.getModifiers())) // || Modifier.isProtected(m.getModifiers()))
	    	return true;

	    return false;
	  }

	  private static String doNotUseSpecialCase(Method m) {

		// Special case 1: 
	    // We're skipping compareTo method in enums - you can call it only with the same type as receiver 
	    // but the signature does not tell you that 
	    if (m.getDeclaringClass().getCanonicalName() != null && m.getDeclaringClass().getCanonicalName().equals("java.lang.Enum")
	        && m.getName().equals("compareTo")
	        && m.getParameterTypes().length == 1
	        && m.getParameterTypes()[0].equals(Enum.class))
	      return "We're skipping compareTo method in enums";

	    // Special case 2: 
	    //hashCode is bad in general but String.hashCode is fair game
//	    if (m.getName().equals("hashCode") && ! m.getDeclaringClass().equals(String.class))
//	      return "hashCode";
	    if (m.getName().equals("hashCode") && m.getDeclaringClass().equals(Object.class))
		      return "hashCode";

	    // Special case 3: (just clumps together a bunch of hashCodes, so skip it)
	    if (m.getName().equals("deepHashCode") && m.getDeclaringClass().equals(Arrays.class))
	      return "deepHashCode";

	    // Special case 4: (differs too much between JDK installations) 
	    if (m.getName().equals("getAvailableLocales"))
	      return "getAvailableLocales";
	    return null;
	  }

	  private static boolean canUse(Constructor<?> c) {

	    //synthetic constructors are OK
	    if (Modifier.isAbstract(c.getDeclaringClass().getModifiers()))
	    	return false;
	    
	    //TODO we could enable some methods from Object, like getClass
	    if (c.getDeclaringClass().equals(java.lang.Object.class))
	    	return false;//handled here to avoid printing reasons

	    if (c.getDeclaringClass().equals(java.lang.Thread.class))
	    	return false;//handled here to avoid printing reasons

	    
	    if (Modifier.isPublic(c.getModifiers()))
	      return true;    
	    //if (!Modifier.isPrivate(c.getModifiers())) // && !Modifier.isProtected(c.getModifiers()))
		//      return true;
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
		  for(String regex : regexs) {
			  if(name.matches(regex))
				  return true;
		  }
		  return false;
	  }

	  private void countTargetFunctions() {
		  num_defined_methods = CFGMethodAdapter.methods.size();
		  logger.info("Target class has "+num_defined_methods+" functions");
		  logger.info("Target class has "+CFGMethodAdapter.branch_counter+" branches");
		  logger.info("Target class has "+CFGMethodAdapter.branchless_methods.size()+" methods without branches");
		  logger.info("That means for coverage information: "+(CFGMethodAdapter.branchless_methods.size() + 2 *CFGMethodAdapter.branch_counter));
	  }
	  
	  /**
	   * Read information from task file
	   */
	  public void populate() {
		  // Parse test task
		  Map<String, List<String> > allowed = getTestObjectsFromFile();
		  Set<String> target_functions = new HashSet<String>();
		  
		  String target_class = Properties.TARGET_CLASS;

		  // Analyze each entry of test task
		  for(String classname : allowed.keySet()) {
			  try {
				  Class<?> clazz = Class.forName(classname);

				  logger.debug("Analysing class "+classname);
				  List<String> restriction = allowed.get(classname);
				  
				  // Add all constructors
				  for(Constructor<?> constructor : getConstructors(clazz)) {

					  if(constructor.getDeclaringClass().getName().startsWith(target_class) && !constructor.isSynthetic() && !Modifier.isAbstract(constructor.getModifiers())) {
						  target_functions.add(constructor.getDeclaringClass().getName()+"."+constructor.getName()+org.objectweb.asm.Type.getConstructorDescriptor(constructor));
//						  num_defined_methods++;
						  logger.debug("Keeping track of "+constructor.getDeclaringClass().getName()+"."+constructor.getName()+org.objectweb.asm.Type.getConstructorDescriptor(constructor));
						  logger.debug(constructor.getDeclaringClass().getName()+" starts with "+classname);
					  }
					  
//					  if(count && !constructor.getDeclaringClass().equals(Object.class)) {
					  /*
					  if(constructor.getDeclaringClass().getName().startsWith(classname)) {
						  logger.debug("Keeping track of "+constructor.getDeclaringClass().getName()+"."+constructor.getName()+org.objectweb.asm.Type.getConstructorDescriptor(constructor));
						  logger.debug(constructor.getDeclaringClass().getName()+" starts with "+classname);
						  num_defined_methods++;
					  }
					  */
					  
					  if(canUse(constructor) && matches("<init>"+org.objectweb.asm.Type.getConstructorDescriptor(constructor),restriction)) {
						  logger.debug("Adding constructor "+classname+"."+constructor.getName()+org.objectweb.asm.Type.getConstructorDescriptor(constructor));
						  test_constructors.add(constructor);
						  calls.add(constructor);

					  } else {
						  if(!canUse(constructor)) {
							  logger.debug("Constructor cannot be used: "+constructor);
						  } else {
							  logger.debug("Constructor does not match: "+constructor);
						  }
					  }
				  }

				  // Add all methods
				  for(Method method : getMethods(clazz)) {
					  if(method.getDeclaringClass().getName().startsWith(target_class) && !method.isSynthetic() && !Modifier.isAbstract(method.getModifiers())) {
						  target_functions.add(method.getDeclaringClass().getName()+"."+method.getName()+org.objectweb.asm.Type.getMethodDescriptor(method));
						  //num_defined_methods++;
						  logger.debug("Keeping track of "+method.getDeclaringClass().getName()+"."+method.getName()+org.objectweb.asm.Type.getMethodDescriptor(method));
						  logger.debug(method.getDeclaringClass().getName()+" starts with "+target_class);
					  }

					  /*
					  if(method.getDeclaringClass().getName().startsWith(classname)) {
//					  if(count && !method.getDeclaringClass().equals(Object.class)) {
						  logger.debug("Keeping track of "+method.getDeclaringClass().getName()+"."+method.getName()+org.objectweb.asm.Type.getMethodDescriptor(method));
						  logger.debug(method.getDeclaringClass().getName()+" starts with "+classname);
						  num_defined_methods++;
					  }
					  */
					  if(canUse(method) && matches(method.getName()+org.objectweb.asm.Type.getMethodDescriptor(method),restriction)) {
						  logger.debug("Adding method "+classname+"."+method.getName()+org.objectweb.asm.Type.getMethodDescriptor(method));
						  test_methods.add(method);
						  calls.add(method);
					  } else {
						  if(!canUse(method)) {
							  logger.debug("Method cannot be used: "+method);
						  } else {
							  logger.debug("Method does not match: "+method);
						  }  
					  }
				  }

				  // Add all fields
				  for(Field field : getFields(clazz)) {
					  if(canUse(field) && matches(field.getName(), restriction)) {
						  logger.debug("Adding field "+classname+"."+field.getName());
						  calls.add(field);
						  //addGenerator(field, field.getType());
					  }
				  }

			  } catch (ClassNotFoundException e) {
				  logger.error("Class not found: "+classname+", ignoring for tests");
				  continue;
			  }
		  }
		  logger.info("Found " + test_constructors.size() + " constructors");
		  logger.info("Found " + test_methods.size() + " methods");
		  
		  //num_defined_methods = target_functions.size();
		  //logger.info("Target class has "+num_defined_methods+" functions");
		  
	  }
		
	  private static Map<String, List<String> > getIncludesFromFile() {
		  String property = Properties.getProperty("test_includes");
		  Map<String, List<String> > objs = new HashMap<String, List<String> >();
		  if(property == null) {
			  logger.debug("No include file specified");
			  return objs;
		  }
			  
		  File file = new File(property);
		  if(!file.exists()) {
			  file = new File(Properties.OUTPUT_DIR+"/"+property);
			  if(!file.exists() || !file.isFile()) {
				  logger.debug("No include file specified");
				  return objs;
			  }
		  }
		  List<String> lines = Io.getLinesFromFile(file);
		  for(String line : lines) {
			  line = line.trim();
			  // Skip comments
			  if(line.startsWith("#"))
				  continue;

			  String[] parameters = line.split(",");
			  if(parameters.length != 2)
				  continue;
			  if(!objs.containsKey(parameters[0]))
				  objs.put(parameters[0], new ArrayList<String>());

			  objs.get(parameters[0]).add(parameters[1]);
		  }
		  return objs;
	  }
	  
	  private void addStandardIncludes() {
		  try {
			calls.add(Integer.class.getConstructor(int.class));
			calls.add(Double.class.getConstructor(double.class));
			calls.add(Float.class.getConstructor(float.class));  
			calls.add(Long.class.getConstructor(long.class));  
			calls.add(Short.class.getConstructor(short.class));  
			calls.add(Character.class.getConstructor(char.class));  
			calls.add(Boolean.class.getConstructor(boolean.class));  
		  } catch (SecurityException e) {
		  } catch (NoSuchMethodException e) {
		  }
	  }
	  
	  /**
	   * Add all classes that are explicitly requested by the user
	   */
	  private void addIncludes() {
		  addStandardIncludes();
		  
		  Map<String, List<String> > include_map = getIncludesFromFile();
		  int num = 0;
		  for(String classname : include_map.keySet()) {
			  try {
				  Class<?> clazz = Class.forName(classname);
				  boolean found = false;
				  for(String methodname : include_map.get(classname)) {
					  for(Method m : getMethods(clazz)) {
						  String signature = m.getName()+org.objectweb.asm.Type.getMethodDescriptor(m);
						  if(canUse(m) && signature.matches(methodname)) {
							  logger.trace("Adding included method "+m);
							  calls.add(m);
							  num++;
							  found = true;
						  }
					  }
					  for(Constructor<?> c : getConstructors(clazz)) {
						  String signature = "<init>"+org.objectweb.asm.Type.getConstructorDescriptor(c);
						  if(canUse(c) && signature.matches(methodname)) {
							  logger.trace("Adding included constructor "+c+" "+signature);
							  calls.add(c);
							  num++;
							  found = true;
						  }
					  }
					  if(!found) {
						  logger.warn("Could not find any methods matching "+methodname+" in class "+classname);
						  logger.info("Candidates are: ");
						  for(Constructor<?> c : clazz.getConstructors()) {
							  logger.info("<init>"+org.objectweb.asm.Type.getConstructorDescriptor(c));
						  }
						  for(Method m : clazz.getMethods()) {
							  logger.info(m.getName()+org.objectweb.asm.Type.getMethodDescriptor(m));
						  }
					  }
				  }
			  } catch(ClassNotFoundException e) {
				  logger.warn("Cannot include class "+classname+ ": Class not found");
			  }			
		  }
		  logger.info("Added " + num + " other calls from include file");

	  }
	  
	  private static Map<String, List<String> > getExcludesFromFile() {
		  String property = Properties.getProperty("test_excludes");
		  Map<String, List<String> > objs = new HashMap<String, List<String> >();
		  if(property == null) {
			  logger.debug("No exclude file specified");
			  return objs;			  
		  }
		  File file = new File(property);
		  if(!file.exists()) {
			  file = new File(Properties.OUTPUT_DIR+"/"+property);
			  if(!file.exists() || !file.isFile()) {
				  logger.debug("No exclude file specified");
				  return objs;
			  }
		  }

		  List<String> lines = Io.getLinesFromFile(file);
		  for(String line : lines) {
			  line = line.trim();
			  // Skip comments
			  if(line.startsWith("#"))
				  continue;

			  String[] parameters = line.split(",");
			  if(parameters.length != 2)
				  continue;
			  if(!objs.containsKey(parameters[0]))
				  objs.put(parameters[0], new ArrayList<String>());

			  objs.get(parameters[0]).add(parameters[1]);
		  }
		  return objs;
	  }
	  
	  /**
	   * Load all classes in the current project
	   */
	  private void analyzeTarget() {
		  logger.info("Getting list of classes");
		  Hierarchy hierarchy = Hierarchy.readFromDefaultLocation();
		  String prefix = Properties.getProperty("PROJECT_PREFIX");

		  Set<String> all_classes = hierarchy.getAllClasses();
		  Set<Class<?>> dependencies = new HashSet<Class<?>>();
		  Map<String, List<String> > test_excludes = getExcludesFromFile();

		  // Analyze each class
		  for(String classname : all_classes) {
			  // In prefix?
			  if(classname.startsWith(prefix)) {
				  //Not excluded?
				  if(!excludes.shouldExclude(classname)) {
					  try {
						  logger.trace("Current class: "+classname);
						  Class<?> toadd = Class.forName(classname);
						  if(!canUse(toadd)) {
							  logger.debug("Not using class "+classname);
							  continue;
						  }

						  // Keep all accessible constructors
						  for(Constructor<?> constructor : getConstructors(toadd)) {
							  logger.trace("Considering constructor "+constructor);
							  if(test_excludes.containsKey(classname)) {
								  boolean valid = true;
								  String full_name = "<init>" + org.objectweb.asm.Type.getConstructorDescriptor(constructor);
								  for(String regex : test_excludes.get(classname)) {
									  if(full_name.matches(regex)) {
										  logger.info("Found excluded constructor: "+constructor+" matches "+regex);
										  valid = false;
										  break;
									  }
								  }
								  if(!valid)
									  continue;
							  }
							  if(canUse(constructor)) {
								  for(Class<?> clazz : constructor.getParameterTypes()) {
									  if(!all_classes.contains(clazz.getName())) {
										  dependencies.add(clazz);
									  }
								  }
								  logger.trace("Adding constructor "+constructor);
								  constructor.setAccessible(true);
								  calls.add(constructor);
							  } else {
								  logger.trace("Constructor "+constructor+" is not public");
							  }
						  }

						  // Keep all accessible methods
						  for(Method method : getMethods(toadd)) {
							  //if(method.getDeclaringClass().equals(Object.class))
							//	  continue;
							  if(test_excludes.containsKey(classname)) {
								  boolean valid = true;
								  String full_name = method.getName() + org.objectweb.asm.Type.getMethodDescriptor(method);
								  for(String regex : test_excludes.get(classname)) {
									  if(full_name.matches(regex)) {
										  valid = false;
										  logger.info("Found excluded method: "+classname+"."+full_name+" matches "+regex);
										  break;
									  }
								  }
								  if(!valid)
									  continue;
							  }
							  if(canUse(method)) {
								  for(Class<?> clazz : method.getParameterTypes()) {
									  if(!all_classes.contains(clazz.getName())) {
										  dependencies.add(clazz);
									  }
								  }
								  method.setAccessible(true);
								  calls.add(method);
								  logger.trace("Adding method "+method);
							  }
						  }

						  // Keep all accessible fields
						  for(Field field : getFields(toadd)) {
//							  if(!Modifier.isPrivate(field.getModifiers()) && !Modifier.isProtected(field.getModifiers()) && !Modifier.isProtected(field.getDeclaringClass().getModifiers()) && !Modifier.isPrivate(field.getDeclaringClass().getModifiers())) {
							  if(test_excludes.containsKey(classname)) {
								  boolean valid = true;
								  for(String regex : test_excludes.get(classname)) {
									  if(field.getName().matches(regex)) {
										  valid = false;
										  logger.info("Found excluded field: "+classname+"."+field.getName()+" matches "+regex);
										  break;
									  }
								  }
								  if(!valid)
									  continue;
							  }

							  if(canUse(field)) {
								  field.setAccessible(true);
								  calls.add(field);
								  logger.trace("Adding field "+field);
							  } else {
								  logger.trace("Cannot use field "+field);
							  }
						  }					
					  } catch(ClassNotFoundException e) {
						  logger.debug("Ignoring class "+classname);
					  } catch(ExceptionInInitializerError e) {
						  logger.debug("Problem - ignoring class "+classname);						
					  }
				  }
			  }
		  }
		  logger.info("Found " + calls.size() + " other calls");
		  //			logger.info("Found "+dependencies.size()+" unsatisfied dependencies:");
		  logger.info("Unsatisfied dependencies:");
		  for(Class<?> clazz : dependencies) {
			  if(clazz.isPrimitive())
				  continue;
			  if(clazz.isArray())
				  continue;
			  try {
				  if(hasGenerator(clazz))
					  continue;
			  } catch (ConstructionFailedException e) {
			  }
			  logger.info("  "+clazz.getName());
			  //addCalls(clazz);
		  }
	  }
	  
	  
	  public void resetStaticClasses() {
		  ExecutionTracer.disable();
		  for(Method m : static_initializers) {
			  try {	  
				  m.invoke(null, (Object[])null);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			};
		  }
		  ExecutionTracer.enable();
	  }
	  
	  private void getStaticClasses() {
		  for(String classname : StaticInitializationClassAdapter.static_classes) {
			  try {
				  Class<?> clazz = Class.forName(classname);
				  Method m = clazz.getMethod("__STATIC_RESET", (Class<?>[])null);
				  m.setAccessible(true);
				  static_initializers.add(m);
				  logger.info("Adding static class: "+classname);
			  } catch(ClassNotFoundException e) {
				  logger.info("Static: Could not find class: "+classname);
			  } catch (SecurityException e) {
				  logger.info("Static: Security exception: "+classname);
				// TODO Auto-generated catch block
				//e.printStackTrace();
			} catch (NoSuchMethodException e) {
				  logger.info("Static: Could not find method clinit in : "+classname);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }
	  }
}

