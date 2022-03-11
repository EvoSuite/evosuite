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

import org.evosuite.PackageInfo;
import org.evosuite.TestGenerationContext;
import org.evosuite.runtime.Reflection;
import org.evosuite.runtime.mock.MockList;
import org.evosuite.runtime.util.ReflectionUtils;
import org.junit.Test;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;
import static org.objectweb.asm.Type.getMethodDescriptor;

/**
 * Set of pure static methods
 */
public class TestClusterUtils {

    protected static final Logger logger = LoggerFactory.getLogger(TestClusterUtils.class);

	/*
		Only final constants and caches should instantiated in this class
	 */

    private static final List<String> classExceptions = Collections.unmodifiableList(
            Arrays.asList("com.apple.", "apple.", "sun.", "com.sun.", "com.oracle.", "sun.awt."));
    private final static Map<Class<?>, Set<Field>> accessibleFieldCache = new LinkedHashMap<>();
    private final static Map<Class<?>, Set<Method>> methodCache = new LinkedHashMap<>();


    /**
     * Determine if this class contains JUnit tests
     *
     * @param className
     * @return
     * @deprecated use {@code org.evosuite.junit.CoverageAnalysis.isTest(Class<?> cls)}
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
            logger.info("Could not load class: {}", className);
        }
        return false;
    }

    public static boolean isAnonymousClass(String className) {
        int pos = className.lastIndexOf('$');
        if (pos < 0)
            return false;
        if (pos == className.length() - 1)
            return false; // Classnames can end in $ - see #179
        char firstLetter = className.charAt(pos + 1);
        return firstLetter >= '0' && firstLetter <= '9';
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
     * Calculates the package distance between two classes, which must be given by their
     * fully-qualified names. For example, the package distance between {@code "java.util.List"} and
     * {@code "java.util.Set"} is 0, and the distance between {@code "java.util.List"} and
     * {@code "java.lang.reflect.Class"} is 3.
     *
     * @param className1 fully-qualified name of the first class
     * @param className2 fully-qualified name of the second class
     * @return the package distance between the two classes
     */
    public static int getPackageDistance(final String className1, final String className2) {
        final String[] packages1 = className1.split("\\.");
        final String[] packages2 = className2.split("\\.");

        // We ignore the last array entries because they're just class names and thus irrelevant.
        final int numPackages1 = packages1.length - 1;
        final int numPackages2 = packages2.length - 1;

        // Find the length of the longest common prefix.
        int length = 0;
        final int n = Math.min(numPackages1, numPackages2);
        while (length < n && packages1[length].equals(packages2[length])) {
            length++;
        }

        final int distance = numPackages1 + numPackages2 - 2 * length;
        return distance;
    }

    /**
     * Check if we can use the given class directly in a JUnit test
     *
     * @param className a {@link String} object.
     * @return a boolean.
     */
    public static boolean checkIfCanUse(final String className) {
        if (MockList.shouldBeMocked(className)) {
            return false;
        }

        return classExceptions.stream().noneMatch(className::startsWith);
    }

    /**
     * Get the set of constructors defined in this class and its superclasses
     *
     * @param clazz
     * @return
     */
    public static Set<Constructor<?>> getConstructors(Class<?> clazz) {
        final Map<String, Constructor<?>> helper = new TreeMap<>();

        for (final Constructor<?> c : Reflection.getDeclaredConstructors(clazz)) {
            helper.put(org.objectweb.asm.Type.getConstructorDescriptor(c), c);
        }

        final Set<Constructor<?>> constructors =
                helper.values().stream()
                        .sorted(comparing(Constructor::getName))
                        .collect(toCollection(LinkedHashSet::new));

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
        final Stream<Method> methods = Arrays.stream(ReflectionUtils.getMethods(clazz));
        final Predicate<Method> isStaticGenerator = m ->
                Modifier.isStatic(m.getModifiers()) && clazz.isAssignableFrom(m.getReturnType());
        return methods.anyMatch(isStaticGenerator);
    }

    /**
     * Get the set of fields defined in this class and its superclasses
     *
     * @param clazz
     * @return
     */
    public static Set<Field> getAccessibleFields(final Class<?> clazz) {
        return accessibleFieldCache.computeIfAbsent(clazz, TestClusterUtils::computeAccessibleFields);
    }

    private static Set<Field> computeAccessibleFields(final Class<?> clazz) {
        final Set<Field> accessibleFields = Arrays.stream(Reflection.getFields(clazz))
                .filter(f -> TestUsageChecker.canUse(f) && !Modifier.isFinal(f.getModifiers()))
                .sorted(comparing(Field::getName))
                .collect(toCollection(LinkedHashSet::new));
        return accessibleFields;
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
        if (methodCache.containsKey(clazz)) {
            return methodCache.get(clazz);
        }

        final Map<String, Method> helper = new TreeMap<>();

        if (clazz.getSuperclass() != null) {
            for (final Method m : getMethods(clazz.getSuperclass())) {
                helper.put(m.getName() + getMethodDescriptor(m), m);
            }
        }
        for (final Class<?> in : Reflection.getInterfaces(clazz)) {
            for (final Method m : getMethods(in)) {
                helper.put(m.getName() + getMethodDescriptor(m), m);
            }
        }

        for (final Method m : Reflection.getDeclaredMethods(clazz)) {
            helper.put(m.getName() + getMethodDescriptor(m), m);
        }

        final LinkedHashSet<Method> methods = helper.values().stream()
                .sorted(comparing(Method::getName))
                .collect(toCollection(LinkedHashSet::new));
        methodCache.put(clazz, methods);
        return methods;
    }

    public static Method getMethod(Class<?> clazz, String methodName, String desc) {
        for (Method method : Reflection.getMethods(clazz)) {
            if (method.getName().equals(methodName)
                    && getMethodDescriptor(method).equals(desc))
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
