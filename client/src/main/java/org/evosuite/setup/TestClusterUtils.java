/*
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
package org.evosuite.setup;

import org.apache.commons.lang3.StringUtils;
import org.evosuite.PackageInfo;
import org.evosuite.TestGenerationContext;
import org.evosuite.runtime.Reflection;
import org.evosuite.runtime.mock.MockList;
import org.evosuite.runtime.util.ReflectionUtils;
import org.junit.Test;
import org.junit.runners.Suite;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Set of pure static methods
 */
public class TestClusterUtils {

	protected static final Logger logger = LoggerFactory.getLogger(TestClusterUtils.class);

	/*
		Only final constants and caches should instantiated in this class
	 */

	private static final List<String> classExceptions = Collections.unmodifiableList(Arrays.asList(new String[] {
	        "com.apple.", "apple.", "sun.", "com.sun.", "com.oracle.", "sun.awt."
	}));
	private final static Map<Class<?>, Set<Field>> accessibleFieldCache = new LinkedHashMap<>();
	private final static Map<Class<?>, Set<Method>> methodCache = new LinkedHashMap<>();


	/**
	 * Determine if this class contains JUnit tests
	 * @deprecated use {@code org.evosuite.junit.CoverageAnalysis.isTest(Class<?> cls)}
	 *
	 * @param className
	 * @return
	 */
	@Deprecated
	public static boolean isTest(String className) {
		// TODO-JRO Identifying tests should be done differently:
		// If the class either contains methods
		// annotated with @Test (> JUnit 4.0)
		// or contains Test or Suite in it's inheritance structure
		try {
			Class<?> clazz = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(className);
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

	public static boolean isAnonymousClass(String className) {
		int pos = className.lastIndexOf('$');
		if(pos < 0)
			return false;
		if(pos == className.length() - 1)
			return false; // Classnames can end in $ - see #179
		char firstLetter = className.charAt(pos + 1);
		if(firstLetter >= '0' && firstLetter <= '9')
			return true;

		return false;
	}

	public static void makeAccessible(Field field) {
		if (!Modifier.isPublic(field.getModifiers())
		        || !Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
			field.setAccessible(true);
		}
	}

	public static void makeAccessible(Method method) {
		if (!Modifier.isPublic(method.getModifiers())
		        || !Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
			method.setAccessible(true);
		}
	}

	public static void makeAccessible(Constructor<?> constructor) {
		if (!Modifier.isPublic(constructor.getModifiers())
		        || !Modifier.isPublic(constructor.getDeclaringClass().getModifiers())) {
			constructor.setAccessible(true);
		}
	}

	public static boolean isEvoSuiteClass(Class<?> c) {
        return c.getName().startsWith(PackageInfo.getEvoSuitePackage());
                //|| c.getName().equals("java.lang.String");    // This is now handled in addDependencyClass
    }

	/**
	 * Calculate package distance between two classnames
	 *
	 * @param className1
	 * @param className2
	 * @return
	 */
	public static int getPackageDistance(String className1, String className2) {

		String[] package1 = StringUtils.split(className1, '.');
		String[] package2 = StringUtils.split(className2, '.');

		int distance = 0;
		int same = 1;
		int num = 0;
		while (num < package1.length && num < package2.length
		        && package1[num].equals(package2[num])) {
			same++;
			num++;
		}

		if (package1.length > same)
			distance += package1.length - same;

		if (package2.length > same)
			distance += package2.length - same;

		return distance;
	}

	/**
	 * Check if we can use the given class directly in a JUnit test
	 *
	 * @param className
	 *            a {@link String} object.
	 * @return a boolean.
	 */
	public static boolean checkIfCanUse(String className) {

		if (MockList.shouldBeMocked(className)) {
			return false;
		}

		for (String s : classExceptions) {
			if (className.startsWith(s)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Get the set of constructors defined in this class and its superclasses
	 *
	 * @param clazz
	 * @return
	 */
	public static Set<Constructor<?>> getConstructors(Class<?> clazz) {
		Map<String, Constructor<?>> helper = new TreeMap<>();

		for (Constructor<?> c : Reflection.getDeclaredConstructors(clazz)) {
			helper.put(org.objectweb.asm.Type.getConstructorDescriptor(c), c);
		}
		LinkedHashSet<Constructor<?>> constructors = new LinkedHashSet<>(helper.values().stream().sorted((o1, o2) -> o1.getName().compareTo(o2.getName())).collect(Collectors.toList()));
		return constructors;
	}

	/**
	 * Get the set of fields defined in this class and its superclasses
	 *
	 * @param clazz
	 * @return
	 */
	public static Set<Field> getFields(Class<?> clazz) {
		// TODO: Helper not necessary here!
		Map<String, Field> helper = new TreeMap<>();

        if (clazz.getSuperclass() != null) {
			for (Field f : getFields(clazz.getSuperclass())) {
				helper.put(f.toGenericString(), f);
			}

		}
		for (Class<?> in : Reflection.getInterfaces(clazz)) {
			for (Field f : getFields(in)) {
				helper.put(f.toGenericString(), f);
			}
		}

		for (Field f : Reflection.getDeclaredFields(clazz)) {
			helper.put(f.toGenericString(), f);
		}
        Set<Field> fields = new LinkedHashSet<>(helper.values());

		return fields;
	}

	public static boolean hasStaticGenerator(Class<?> clazz) {
		for(Method m : ReflectionUtils.getMethods(clazz)) {
			if(Modifier.isStatic(m.getModifiers())) {
				if(clazz.isAssignableFrom(m.getReturnType())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get the set of fields defined in this class and its superclasses
	 *
	 * @param clazz
	 * @return
	 */
	public static Set<Field> getAccessibleFields(Class<?> clazz) {
		if(accessibleFieldCache.containsKey(clazz)) {
			return accessibleFieldCache.get(clazz);
		}

		Set<Field> fields = new LinkedHashSet<>();
		for (Field f : Reflection.getFields(clazz)) {
			if (TestUsageChecker.canUse(f) && !Modifier.isFinal(f.getModifiers())) {
				fields.add(f);
			}
		}
		fields = new LinkedHashSet<>(fields.stream().sorted((o1, o2) -> o1.getName().compareTo(o2.getName())).collect(Collectors.toList()));

		accessibleFieldCache.put(clazz, fields);
		return fields;
	}

	/**
	 * Get the set of methods defined in this class and its superclasses
	 *
	 * @param clazz
	 * @return
	 */
	public static Set<Method> getMethods(Class<?> clazz) {

		// As this is expensive, doing some caching here
		// Note that with the change of a class loader the cached values could
		// be thrown away
		if(methodCache.containsKey(clazz)) {
			return methodCache.get(clazz);
		}
		Map<String, Method> helper = new TreeMap<>();

		if (clazz.getSuperclass() != null) {
			for (Method m : getMethods(clazz.getSuperclass())) {
				helper.put(m.getName() + org.objectweb.asm.Type.getMethodDescriptor(m), m);
			}
		}
		for (Class<?> in : Reflection.getInterfaces(clazz)) {
			for (Method m : getMethods(in)) {
				helper.put(m.getName() + org.objectweb.asm.Type.getMethodDescriptor(m), m);
			}
		}

		for (Method m : Reflection.getDeclaredMethods(clazz)) {
			helper.put(m.getName() + org.objectweb.asm.Type.getMethodDescriptor(m), m);
		}

		LinkedHashSet<Method> methods = new LinkedHashSet<>(helper.values().stream().sorted((o1, o2) -> o1.getName().compareTo(o2.getName())).collect(Collectors.toList()));
		methodCache.put(clazz, methods);
		return methods;
	}

	public static Method getMethod(Class<?> clazz, String methodName, String desc) {
		for (Method method : Reflection.getMethods(clazz)) {
			if (method.getName().equals(methodName)
					&& Type.getMethodDescriptor(method).equals(desc))
				return method;
		}
		return null;
	}

	public static Class<?> getClass(String className) {
		try {
			Class<?> clazz = Class.forName(className,
			                               true,
			                               TestGenerationContext.getInstance().getClassLoaderForSUT());
			return clazz;
		} catch (ClassNotFoundException e) {
			return null;
		} catch (NoClassDefFoundError e) {
			// an ExceptionInInitializationError might have happened during class initialization.
			return null;
		}
	}
}
