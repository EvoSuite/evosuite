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

package de.unisb.cs.st.evosuite.setup;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.objectweb.asm.Type;

import de.unisb.cs.st.ds.util.MakefileGenerator.Target;
import de.unisb.cs.st.ds.util.io.Io;
import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.javalanche.coverage.distance.Hierarchy;
import de.unisb.cs.st.javalanche.mutation.javaagent.classFileTransfomer.mutationDecision.Excludes;

/**
 * @author Gordon Fraser
 *
 */
public class TestTaskGenerator {

	private static Logger logger = Logger.getLogger(TestTaskGenerator.class);

	static Hierarchy hierarchy = Hierarchy.readFromDefaultLocation();
	
	static Excludes excludes = Excludes.getInstance(); 
	
	static String prefix;
	
	static Map<String, List<String> > method_excludes = getExcludesFromFile();
	
	private static final boolean CREATE_OBJECTFILES = Properties.getProperty("generate_objects") != null;
	
	/**
	 * Get the set of public/default constructors
	 * @param clazz
	 *    The class to be analyzed
	 * @return
	 *    The set of accessible constructors
	 */
	public static Set<Constructor<?> > getConstructors(Class<?> clazz) {
		Set<Constructor<?>> constructors = new HashSet<Constructor<?>>();
		if(clazz.getSuperclass() != null ) {
			constructors.addAll(getConstructors(clazz.getSuperclass()));
		}
		for(Class<?> in : clazz.getInterfaces()) {
			constructors.addAll(getConstructors(in));			
		}

		for(Constructor<?> c : clazz.getDeclaredConstructors()) {
			constructors.add(c);			
		}
		return constructors;
	}

	/**
	 * Get the set of public/default methods
	 * @param clazz
	 *    The class to be analyzed
	 * @return
	 *    The set of accessible methods
	 */
	public static Set<Method> getMethods(Class<?> clazz) {
		Set<Method> methods = new HashSet<Method>();
		if(clazz.getSuperclass() != null ) {
			methods.addAll(getMethods(clazz.getSuperclass()));
		}
		for(Class<?> in : clazz.getInterfaces()) {
			methods.addAll(getMethods(in));			
		}

		//for(Method m : clazz.getMethods()) {
		//	methods.add(m);			
		//}
		for(Method m : clazz.getDeclaredMethods()) {
			methods.add(m);			
		}
		return methods;
	}	

	/**
	 * Get the set of public/default fields
	 * @param clazz
	 *    The class to be analyzed
	 * @return
	 *    The set of accessible fields
	 */
	public static Set<Field> getFields(Class<?> clazz) {
		Set<Field> fields = new HashSet<Field>();
		if(clazz.getSuperclass() != null ) {
			fields.addAll(getFields(clazz.getSuperclass()));
		}
		for(Class<?> in : clazz.getInterfaces()) {
			fields.addAll(getFields(in));			
		}
		for(Field m : clazz.getFields()) {
			fields.add(m);			
		}
		//for(Field m : clazz.getDeclaredFields()) {
		//	fields.add(m);			
		//}
		return fields;
	}	
	
	/**
	 * Read classes to be excluded
	 * @return
	 *   Map from classname to methods that should not be used
	 */
	public static Map<String, List<String> > getExcludesFromFile() {
		String property = System.getProperty("test_excludes");
		Map<String, List<String> > objs = new HashMap<String, List<String> >();
		if(property == null)
			return objs;
		File file = new File(property);
		if(!file.exists()) {
			logger.warn("Exclude file "+property+" does not exist, skipping");
			return objs;
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
	 * Get all usable superclasses of the given class
	 * @param classname
	 *     Name of given class
	 * @return
	 *     All superclasses that are within prefix and not excluded
	 */
	private static List<String> getSuperClasses(String classname) {
		Set<String> superclasses = hierarchy.getAllSupers(classname);
		List<String> ret = new ArrayList<String>();
		ret.add(classname);
		for(String sup : superclasses) {
			if(sup.startsWith(prefix))
				if(!excludes.shouldExclude(sup))
					ret.add(sup);
		}
		
		return ret;
	}
	
	/**
	 * Get all usable subclasses of the given class
	 * @param classname
	 *     Name of given class
	 * @return
	 *     All subclasses that are within prefix and not excluded
	 */
	private static List<String> getSubClasses(String classname) {
		Set<String> subclasses = hierarchy.getAllSubclasses(classname);

		Set<String> owned_classes = hierarchy.getAllClasses();
		for(String sub : owned_classes) {
			if(sub.startsWith(classname+"$"))
				if(!excludes.shouldExclude(sub))
					subclasses.add(sub);
		}

		List<String> ret = new ArrayList<String>();
		ret.add(classname);
		for(String sub : subclasses) {
			if(sub.startsWith(prefix))
				if(!excludes.shouldExclude(sub))
					ret.add(sub);
		}
		
		
		return ret;
	}

	private static boolean isPurelyAbstract(String classname) {
		
		List<String> subclasses = getSubClasses(classname);
		for(String subclass : subclasses) {
			Class<?> clazz;
			try {
				clazz = Class.forName(subclass);
				if(!Modifier.isAbstract(clazz.getModifiers()))
					return false;
			} catch (ClassNotFoundException e) {
			}					
		}
		
		return true;
	}
	
	/**
	 * Get all usable subclasses of the given class
	 * @param classname
	 *     Name of given class
	 * @return
	 *     All subclasses that are within prefix and not excluded
	 */
	private static List<String> getOwnedClasses(String classname) {
		Set<String> subclasses = hierarchy.getAllClasses();
		List<String> ret = new ArrayList<String>();
		for(String sub : subclasses) {
			if(sub.startsWith(classname+"$"))
				if(!excludes.shouldExclude(sub))
					ret.add(sub);
		}
		
		return ret;
	}
	/**
	 * Get list of all classes in the given prefix
	 * 
	 * @param prefix
	 *    Package prefix
	 * @return
	 *    List of classnames in prefix that are not excluded
	 *    The list is ascending sorted by number of subclasses
	 */
	protected static List<String> getClasses(String prefix) {
		logger.info("Getting list of classes for prefix "+prefix);
		Set<String> all_classes = hierarchy.getAllClasses();
		logger.info("Number of classes: "+all_classes.size());

		TreeMap<Integer, Set<String> > classes = new TreeMap<Integer, Set<String>>();
		//hierarchy.calculateInferiors();
		for(String classname : all_classes) {
			if(classname.startsWith(prefix)) {
				if(!excludes.shouldExclude(classname)) {
					int num_subclasses = hierarchy.getAllSubclasses(classname).size();
					if(!classes.containsKey(num_subclasses))
						classes.put(num_subclasses, new HashSet<String>());
					classes.get(num_subclasses).add(classname);
				}
			}
		}
		
		List<String> sorted_classes = new ArrayList<String>();
		for(Entry<Integer, Set<String>> entry : classes.entrySet()) {
			logger.debug(entry.getKey()+" subclasses: ");
			for(String name : entry.getValue()) {
				logger.debug("   "+name);
			}
		}
		for(Set<String> classset : classes.values()) {
			sorted_classes.addAll(classset);
		}
		logger.info("Number of sorted classes: "+sorted_classes.size());

		return sorted_classes;
	}

	/**
	 * Check if class is accessible
	 * @param c
	 *    Class to check
	 * @return
	 *    Returns true if class is either public or has default access rights
	 */
	public static boolean canUse(Class<?> c) {
//	    return !Modifier.isPrivate(c.getModifiers()) && !Modifier.isProtected(c.getModifiers()) && !c.isAnonymousClass() &&!Exception.class.isAssignableFrom(c);
//		if(Modifier.isAbstract(c.getModifiers()))
//		return false;
	
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
    	logger.info(c+" looks like an anonymous class, ignoring it");
    	return false;
	 }

    
    /*
    if(Modifier.isPublic(c.getModifiers()))
    	return true;

    if(Modifier.isProtected(c.getModifiers()))
    	return true;
*/
    
    return true;
	}

	/**
	 * Check if method is accessible
	 * @param m
	 *    Method to check
	 * @return
	 *    Returns true is method is accessible and not a special case
	 */
	public static boolean canUse(Method m) {
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

		if (Modifier.isProtected(m.getModifiers()) || Modifier.isPrivate(m.getModifiers())) {
			logger.debug("Excluding protected or private method "+m.toString());
			return false;
		}

		if(Modifier.isProtected(m.getDeclaringClass().getModifiers()) || Modifier.isPrivate(m.getDeclaringClass().getModifiers())) {
			logger.debug("Excluding public method from nonpublic superclass "+m.toString());
			return false;
		}

		//TODO we could enable some methods from Object, like getClass
		if (m.getDeclaringClass().equals(java.lang.Object.class)) {
			logger.debug("Excluding method declared in Object "+m.toString());
			return false;
		}

		if (m.getDeclaringClass().equals(java.lang.Thread.class)) {
			logger.debug("Excluding method declared in Thread "+m.toString());
			return false;
		}

		if(m.getName().equals("main") && Modifier.isStatic(m.getModifiers())) {
			logger.debug("Will not use: " + m.toString());
			logger.debug("  reason: looks like a main method");
			return false;	    	
		}

		String reason = doNotUseSpecialCase(m);
		if (reason != null) {
			logger.debug("Will not use: " + m.toString());
			logger.debug("  reason: " + reason);
			return false;
		}

		return true;
	}

	/**
	 * Check for method special cases (from Randoop)
	 * @param m
	 * @return
	 */
	private static String doNotUseSpecialCase(Method m) {

		if(m == null)
			return "Method is null";
		if(m.getDeclaringClass() == null)
			return "Declaring class is null";
		if(m.getDeclaringClass().getCanonicalName() == null)
			return "Canonical name is null";

		// Special case 1: 
		// We're skipping compareTo method in enums - you can call it only with the same type as receiver 
		// but the signature does not tell you that 
		if (m.getDeclaringClass().getCanonicalName().equals("java.lang.Enum")
				&& m.getName().equals("compareTo")
				&& m.getParameterTypes().length == 1
				&& m.getParameterTypes()[0].equals(Enum.class))
			return "We're skipping compareTo method in enums";

		// Special case 2: 
		//hashCode is bad in general but String.hashCode is fair game
		if (m.getName().equals("hashCode") && ! m.getDeclaringClass().equals(String.class))
			return "hashCode";

		// Special case 3: (just clumps together a bunch of hashCodes, so skip it)
		if (m.getName().equals("deepHashCode") && m.getDeclaringClass().equals(Arrays.class))
			return "deepHashCode";

		// Special case 4: (differs too much between JDK installations) 
		if (m.getName().equals("getAvailableLocales"))
			return "getAvailableLocales";

		return null;
	}

	/**
	 * Check if constructor is usable
	 * @param c
	 * @return
	 *    True if constructor is accessible (public/package)
	 */
	public static boolean canUse(Constructor<?> c) {

		//synthetic constructors are OK

		//	    if (Modifier.isAbstract(c.getDeclaringClass().getModifiers())) {
		//	    	logger.info("Not using constructor because declaring class is abstract");
		//	    	return false;
		//	    }
		if (Modifier.isProtected(c.getModifiers()) || Modifier.isPrivate(c.getModifiers())) {
			logger.debug("Non public constructor in class "+c.getDeclaringClass().getName());
			return false;
		}
		return true;
	}

	protected static boolean isExcluded(String classname, String methodname) {
		if(method_excludes.containsKey(classname) && method_excludes.get(classname).contains(methodname))
			logger.info("Is excluded: "+classname+","+methodname);
		return method_excludes.containsKey(classname) && method_excludes.get(classname).contains(methodname);
	}

	/**
	 * Get all constructors for a class and add all accessible constructors to test candidates
	 * @param candidates
	 * @param classname
	 */
	protected static void addConstructors(Set<String> candidates, String classname) {
		try {
			Class<?> clazz = Class.forName(classname);
			if(!canUse(clazz)) {
				logger.debug("Not using suggested dependency");
				return;
			}
			logger.debug("Adding constructors for class "+classname);				
			for(Constructor<?> constructor : getConstructors(clazz)) {
				if(canUse(constructor) && !isExcluded(classname, "<init>"+Type.getConstructorDescriptor(constructor))) {
					logger.debug("Adding constructor "+classname+"."+constructor.getName()+Type.getConstructorDescriptor(constructor));
					candidates.add(classname+","+Pattern.quote("<init>"+Type.getConstructorDescriptor(constructor)));
				} else {
					if(!canUse(constructor))
						logger.debug("canUse says no");
					if(isExcluded(classname, "<init>"+Type.getConstructorDescriptor(constructor)))
						logger.debug("Is excluded");							
				}
			}				
		} catch (ClassNotFoundException e) {
			logger.error("Class not found: "+classname+", ignoring for tests");
		} catch(NoClassDefFoundError e) {
			logger.warn("NoClassDefFoundError "+classname);				
		}
	}

	/**
	 * Get all constructors for a class and add all accessible methods to test candidates
	 * @param candidates
	 * @param classname
	 */
	protected static void addMethods(Set<String> candidates, String classname) {
		try {
			Class<?> clazz = Class.forName(classname);
			if(!canUse(clazz)) {
				logger.debug("Not using suggested dependency");
				return;
			}
			for(Method method : getMethods(clazz)) {
				if(canUse(method) && !isExcluded(classname, method.getName()+Type.getMethodDescriptor(method))) {
					logger.debug("Adding method "+classname+"."+method.getName()+Type.getMethodDescriptor(method));
					candidates.add(classname+","+Pattern.quote(method.getName()+Type.getMethodDescriptor(method)));
				} else {
					logger.debug("NOT adding method "+classname+"."+method.getName()+Type.getMethodDescriptor(method));
				}
			}
		} catch (ClassNotFoundException e) {
			logger.error("Class not found: "+classname+", ignoring for tests");
		} catch(NoClassDefFoundError e) {
			logger.warn("NoClassDefFoundError "+classname);				
		}		
	}

	/**
	 * Get all constructors for a class and add all accessible fields to test candidates
	 * @param candidates
	 * @param classname
	 */
	protected static void addFields(Set<String> candidates, String classname) {
		try {
			Class<?> clazz = Class.forName(classname);
			if(!canUse(clazz)) {
				logger.debug("Not using suggested dependency");
				return;
			}
			for(Field field : getFields(clazz)) {
				logger.debug("Adding field "+classname+"."+field.getName());
				candidates.add(classname+","+Pattern.quote(field.getName()));
			}

		} catch (ClassNotFoundException e) {
			logger.error("Class not found: "+classname+", ignoring for tests");
		} catch(NoClassDefFoundError e) {
			logger.warn("NoClassDefFoundError "+classname);				
		}		
	}
	
	/**
	 * Get all constructors for a class and add all accessible methods to test candidates
	 * @param candidates
	 * @param classname
	 */
	protected static void addObjectMethods(Set<String> candidates, String classname) {
		try {
			Class<?> clazz = Class.forName(classname);
			for(Method method : getMethods(clazz)) {
				if(canUse(method) && !isExcluded(classname, method.getName()+Type.getMethodDescriptor(method)) && !method.getName().equals("clone") && !method.getName().equals("compareTo") && !method.getName().equals("equals")) {
					//if(method.getReturnType().equals(Object.class) || Arrays.asList(method.getParameterTypes()).contains(Object.class)) {
						logger.debug("Adding method to signature file "+classname+"."+method.getName()+Type.getMethodDescriptor(method));
						//String name = classname+"."+method.getName()+Type.getMethodDescriptor(method);
						candidates.add(classname+"."+method.getName()+Type.getMethodDescriptor(method)+","+Type.getMethodDescriptor(method).replace("Ljava/lang/Object;", "Ljava/lang/Integer;"));
						//candidates.add(method);
					//} else {
					//	logger.debug("NOT adding method t signature file "+classname+"."+method.getName()+Type.getMethodDescriptor(method));
					//}
				}
			}
		} catch (ClassNotFoundException e) {
			logger.error("Class not found: "+classname+", ignoring for tests");
		} catch(NoClassDefFoundError e) {
			logger.warn("NoClassDefFoundError "+classname);				
		}		
	}


	/**
	 * Write set of possible inspector methods to file
	 * @param classname
	 * @param filename
	 */
	protected static void writeInspectors(String classname, String filename) {
		StringBuffer sb = new StringBuffer();
		File file = new File(Properties.OUTPUT_DIR, filename);
		
		// TODO: Don't really need super classes here!
		List<String> classes = getSuperClasses(classname);
		Set<String> methods = new HashSet<String>();
		
		for(String cl : classes) {
			try {
				Class<?> clazz = Class.forName(cl);
				for(Method method : clazz.getMethods()) {
					if(!Modifier.isProtected(method.getModifiers()) && !Modifier.isPrivate(method.getModifiers()) && method.getReturnType().isPrimitive() && !method.getReturnType().equals(void.class) && method.getParameterTypes().length == 0 && !method.getName().equals("hashCode")) {
						methods.add(method.getName()+Type.getMethodDescriptor(method));
					}
				}
			} catch (ClassNotFoundException e) {
			} catch (NoClassDefFoundError e) {
			}
		}
		for(String method : methods) {
			sb.append(method);
			sb.append("\n");
		}
		Io.writeFile(sb.toString(), file);
	}
	
	/**
	 * Write test case generation task file
	 * @param candidates
	 * @param filename
	 */
	protected static void writeTask(Set<String> candidates, String filename) {
		StringBuffer sb = new StringBuffer();
		File file = new File(Properties.OUTPUT_DIR, filename);
		for (String dep : candidates) {
			sb.append(dep);
			sb.append("\n");
		}
		Io.writeFile(sb.toString(), file);	
	}
	
	protected static void writeObjectMethods(Set<String> methods, String filename) {
		StringBuffer sb = new StringBuffer();
		File file = new File(Properties.OUTPUT_DIR, filename);
		for (String method: methods) {
			sb.append(method);
			/*
			sb.append(method.getName()+Type.getMethodDescriptor(method));
			sb.append(",");
			sb.append(method.getName()+Type.getMethodDescriptor(method));
			*/
			/*
			sb.append(method.getReturnType().getCanonicalName());
			for(Class<?> parameter : method.getParameterTypes()) {
				sb.append(",");
				sb.append(parameter.getCanonicalName());
			}
			*/
			sb.append("\n");
		}
		Io.writeFile(sb.toString(), file);	
	}
	
	/**
	 * Central function of the task creator.
	 * Creates test task files, mutation task files, and inspector files
	 * 
	 * @param prefix
	 *    Project prefix
	 */
	protected static void suggestTasks(String prefix) {
		List<String> classes = getClasses(prefix);
		//Set<String> classes = new HashSet<String>();
		//classes.add("org.joda.time.field.DecoratedDurationField");
		List<Target> make_targets = new ArrayList<Target>();
		List<String> calls = new ArrayList<String>();
		//Set<Long> ids = new HashSet<Long>();
		
		int num = 0;
		//int num_mutants = 0;
		for(String classname : classes) {
			Class<?> clazz = null;
			try {
				clazz = Class.forName(classname);
			} catch (ClassNotFoundException e) {
				logger.warn("TG: Class not found: "+classname+", ignoring for tests");
				continue;
			} catch(NoClassDefFoundError e) {
				logger.warn("NoClassDefFoundError "+classname);
				continue;
			} catch(ExceptionInInitializerError e) {
				logger.warn("ExceptionInInitializerError "+classname);
				continue;
			} catch(IllegalAccessError e) {
				logger.warn("IllegalAccessError when trying to access "+classname);
				continue;
			}
			if(!canUse(clazz)) {
				logger.info("Ignoring private or abstract class "+classname);
				List<String> mutant_classes = new ArrayList<String>();
				mutant_classes.add(classname);
				//num_mutants += mutationIds.size();
				continue;				
			}
			if(clazz.isInterface()) {
				logger.info("Ignoring interface "+classname);
				Set<String> object_methods = new HashSet<String>();
				addObjectMethods(object_methods, classname);
				String classfilename = classname.replace("$","_");
				writeObjectMethods(object_methods, classfilename+".obj");
				writeInspectors(classname, classname.replace("$","_")+".inspectors");
				continue;				
			}
			if(clazz.getDeclaredMethods().length == 0 && clazz.getDeclaredConstructors().length == 0) {
				logger.info("Ignoring class without methods: "+classname);
				continue;
			}
			if(clazz.isMemberClass() && clazz.getConstructors().length == 0) {
				logger.info("Ignoring member class without public constructors "+classname);
				List<String> mutant_classes = new ArrayList<String>();
				mutant_classes.add(classname);
				writeInspectors(classname, classname.replace("$","_")+".inspectors");
				//num_mutants += mutationIds.size();
				continue;								
			}
			if(clazz.isMemberClass()) {
				logger.info("Testing member class "+classname);
				writeInspectors(classname, classname.replace("$","_")+".inspectors");
				continue;
			}
			if(clazz.isLocalClass()) {
				logger.info("Testing local class "+classname);
				writeInspectors(classname, classname.replace("$","_")+".inspectors");
				continue;
			}
			if(clazz.isAnonymousClass()) {
				logger.info("Testing anonymous class "+classname);
				continue;
			}
			if(clazz.getCanonicalName() != null) {
				logger.debug("Canonical name: "+clazz.getCanonicalName());
			}
			if(classname.matches(".*\\$\\d+$")) {
				logger.info("Bugger that, it must be an anonymous class");
				continue;
			}
			logger.info("Analyzing dependencies of class "+classname);
			if(clazz.getEnclosingClass() != null) {
				logger.info("  defined in "+clazz.getEnclosingClass().getName());
				writeInspectors(classname, classname.replace("$","_")+".inspectors");
				continue;
			}
			if(clazz.getDeclaringClass() != null) {
				logger.info("  defined in "+clazz.getDeclaringClass().getName());
				writeInspectors(classname, classname.replace("$","_")+".inspectors");
				continue;
			}
			List<String> dependencies  = getSubClasses(clazz.getName());
			List<String> owned_classes = getOwnedClasses(clazz.getName());
			Set<String> object_methods = new HashSet<String>();
			addObjectMethods(object_methods, classname);

			if(Modifier.isAbstract(clazz.getModifiers())) {
				if(hierarchy.getAllSubclasses(classname).isEmpty() || isPurelyAbstract(classname)) {
					logger.info("Ignoring abstract class without concrete subclasses "+classname);
					String classfilename = classname.replace("$","_");
					writeObjectMethods(object_methods, classfilename+".obj");
					continue;
				}
			}

			Set<String> suggestion = new TreeSet<String>();
			//suggestion.add(classname+",.*");
			addConstructors(suggestion, classname);
			addMethods(suggestion, classname);
			addFields(suggestion, classname);
			
			for(String dependency : dependencies) {
				if(dependency.equals(classname))
					continue;
				//logger.info("  Dependency: "+dependency);
				//suggestion.add(dependency+",<init>.*");
				//addObjectMethods(object_methods, dependency);
				addConstructors(suggestion, dependency);
				addMethods(suggestion, dependency);
				addFields(suggestion, dependency);
			}
			for(String iclass : owned_classes) {
				if(iclass.equals(classname))
					continue;
				addObjectMethods(object_methods, iclass);
			}
			
//			List<Long> mutationIds = getMutations(getSuperClasses(classname));
			List<String> mutant_classes = new ArrayList<String>();
			mutant_classes.add(classname);
//			List<Long> mutationIds = getMutations(getSuperClasses(classname));
			if(suggestion.isEmpty()) {
				logger.info("No usable methods found, skipping "+classname);
				continue;				
			}
			//ids.addAll(mutationIds);
			String classfilename = classname.replace("$","_");
//			writeTask(suggestion, classfilename+"_"+num+".task");
			writeTask(suggestion, classfilename+".task");
			if(CREATE_OBJECTFILES)
				writeObjectMethods(object_methods, classfilename+".obj");
//			writeInspectors(classname, classfilename+"_"+num+".inspectors");
			writeInspectors(classname, classfilename+".inspectors");

//			make_targets.add(new Target(classfilename, "ant -f javalanche.xml generateSuite -Dtest.classes="+MutationProperties.OUTPUT_DIR+"/"+classfilename+"_"+num+".task -Dtarget.class="+classfilename+" | tee LOG_"+classfilename));
//			calls.add("ant -f javalanche.xml generateSuite -Dtest.classes="+MutationProperties.OUTPUT_DIR+"/"+classfilename+"_"+num+".task  -Dtarget.class="+classfilename+ "| tee LOG_"+classfilename);
			make_targets.add(new Target(classfilename, "ant -f evosuite.xml generateSuite -Dtarget.class="+classfilename+" | tee log/"+classfilename));
			calls.add("ant -f evosuite.xml generateSuite -Dtarget.class="+classfilename+ "| tee log/"+classfilename);
			num++;
		}

		/*
		String makefile = MakefileGenerator.generateMakefile(make_targets);
		File mfile = new File("Makefile");
		Io.writeFile(makefile, mfile);
		StringBuffer buffer = new StringBuffer();
		for(String call : calls) {
			buffer.append(call);
			buffer.append("\n");
		}
		File cfile = new File("runall.sh");
		Io.writeFile(buffer.toString(), cfile);
		System.out.println("Created "+num+" tasks out of "+classes.size()+" classes");
		*/
		//logger.info("Covering a total of "+num_mutants+" mutations");		
		//logger.info("Covering a total of "+ids.size()+" mutations");
	}
	
	/**
	 * Entry point - generate task files
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		prefix = Properties.PROJECT_PREFIX;
		System.out.println("Creating test files for "+prefix);
		//MutationProperties.checkProperty(MutationProperties.PROJECT_PREFIX_KEY);
		//HandleUnsafeMutations.handleUnsafeMutations(HibernateUtil.getSessionFactory());
		//Set<ClassEntry> entries = XmlIo.get("/Users/fraser/Documents/Source/javalanche-0.3.2/examples/string_test/evosuite/inheritance.xml");
		hierarchy.calculateSubclasses();
		suggestTasks(prefix);
	}
}
