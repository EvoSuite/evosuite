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


import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.runtime.mock.MockList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static org.evosuite.setup.TestClusterUtils.getPackageDistance;

/**
 * Analyzes
 */
public class ConcreteClassAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(ConcreteClassAnalyzer.class);

    private static ConcreteClassAnalyzer instance;

    private ConcreteClassAnalyzer() {
        // singleton pattern
    }

    public static ConcreteClassAnalyzer getInstance() {
        if (instance == null) {
            instance = new ConcreteClassAnalyzer();
        }

        return instance;
    }

    /**
     * Maps abstract classes and interfaces to concrete subclasses that extend or implement the
     * abstract class or interface, respectively.
     */
    private final Map<Class<?>, Set<Class<?>>> cache = new LinkedHashMap<>();

    /**
     * Clears all mappings of abstract classes/interfaces to concrete classes recorded so far.
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Given a class {@code clazz} and an inheritance tree, returns all subclasses of {@code clazz}
     * that are present in the inheritance tree.
     * <p>
     * <b>Warning:</b> May return incorrect and inconsistent results when different inheritance
     * trees are passed for the same class across multiple calls.
     *
     * @param clazz           for which to return its subclasses, not {@code null}
     * @param inheritanceTree modeling subclass-relationships for {@code clazz}, not {@code null}
     * @return subclasses of {@code clazz} found in the {@code inheritanceTree}
     */
    public Set<Class<?>> getConcreteClasses(final Class<?> clazz,
                                            final InheritanceTree inheritanceTree) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(inheritanceTree);

        // Checks the cache if a mapping for the given class is already recorded. If so, it returns
        // the cached mapping. Otherwise, the mapping is computed, put in the cache and returned.
        return cache.computeIfAbsent(clazz, c -> computeConcreteClasses(c, inheritanceTree));
    }

    /**
     * Helper method for {@code getConcreteClasses} that computes a new mapping from {@code clazz}
     * to concrete implementing subclasses based on the given {@code inheritanceTree} (modulo a
     * few special cases that are currently handled differently).
     *
     * @param clazz           for which to compute a mapping
     * @param inheritanceTree modeling sub-class relationships for {@code clazz}
     * @return subclasses of {@code clazz}
     */
    private Set<Class<?>> computeConcreteClasses(final Class<?> clazz,
                                                 final InheritanceTree inheritanceTree) {
        Set<Class<?>> concreteClasses = handleSpecialClass(clazz);
        if (concreteClasses != null) {
            return concreteClasses;
        }

        concreteClasses = handleAbstractClassOrInterface(clazz, inheritanceTree);
        if (concreteClasses != null) {
            return concreteClasses;
        }

        return handleRegularClass(clazz);
    }

    private Set<Class<?>> handleRegularClass(final Class<?> clazz) {
        final Set<Class<?>> actualClasses = new LinkedHashSet<>();
        actualClasses.add(clazz);
        logger.debug("Subclasses of " + clazz.getName() + ": " + actualClasses);
        return actualClasses;
    }

    private Set<Class<?>> handleSpecialClass(final Class<?> clazz) {
        if (clazz.equals(java.util.Map.class)) {
            return getConcreteClassesMap();
        } else if (clazz.equals(java.util.List.class)) {
            return getConcreteClassesList();
        } else if (clazz.equals(Set.class)) {
            return getConcreteClassesSet();
        } else if (clazz.equals(java.util.Collection.class)) {
            return getConcreteClassesList();
        } else if (clazz.equals(java.util.Iterator.class)) {
            // We don't want to explicitly create iterators. This would only pull in
            // java.util.Scanner, the only concrete subclass
            return new LinkedHashSet<>();
        } else if (clazz.equals(java.util.ListIterator.class)) {
            // We don't want to explicitly create iterators.
            return new LinkedHashSet<>();
        } else if (clazz.equals(java.io.Serializable.class)) {
            return new LinkedHashSet<>();
        } else if (clazz.equals(Comparable.class)) {
            return getConcreteClassesComparable();
        } else if (clazz.equals(java.util.Comparator.class)) {
            return new LinkedHashSet<>();
        } else if (clazz.equals(java.io.Reader.class)) {
            return getConcreteClassesReader();
        } else if (clazz.equals(java.io.Writer.class)) {
            return getConcreteClassesWriter();
        } else { // Given class is not a special case, can't handle it here.
            return null;
        }
    }

    /**
     * Tries to return a set of concrete subclasses for the given {@code clazz} using the
     * inheritance {@code tree}. All classes in the returned set have the lowest possible
     * package distance to {@code clazz}. Returns {@code null} if {@code clazz} can't be handled.
     *
     * @param clazz for which to find subclasses
     * @param tree  inheritance tree modelling subclass relationships
     * @return the set of subclasses for {@code clazz}, all having the lowest package distance,
     * or {@code null} (see above)
     */
    private Set<Class<?>> handleAbstractClassOrInterface(final Class<?> clazz,
                                                         final InheritanceTree tree) {
        final boolean isAbstract = Modifier.isAbstract(clazz.getModifiers());
        final boolean isInterface = Modifier.isAbstract(clazz.getModifiers());
        final boolean isEnum = clazz.equals(Enum.class);
        final boolean canHandle = isAbstract || isInterface || isEnum;

        if (!canHandle) {
            return null;
        }

        // We have to use getName() here and not getCanonicalName() because getCanonicalName() uses
        // "." rather than "$" for inner classes but the InheritanceTree uses "$".
        final String className = clazz.getName(), superclass;
        if (MockList.isAMockClass(className)) {
            superclass = clazz.getSuperclass().getName();
        } else if (clazz.equals(Enum.class)) {
            superclass = Properties.TARGET_CLASS;
        } else {
            superclass = className;
        }

        final Set<String> subclasses = tree.getSubclasses(superclass);
        logger.debug("Subclasses of " + className + ": " + subclasses);

        // Maps package distances to the classes having that distance. The map is sorted from lowest
        // to highest package distance.
        final SortedMap<Integer, Set<String>> classesByDistance =
                subclasses.stream().collect(groupingBy(
                        subclass -> getPackageDistance(subclass, superclass), // group by distance
                        TreeMap::new, // organize groups in a tree map using distances as keys
                        toSet())); // every group is a set of class names

        // Stores all concrete subclasses with the lowest possible distance to the superclass.
        final Set<Class<?>> actualClasses = new LinkedHashSet<>();

        // Try to find concrete subclasses, all with the lowest possible package distance.
        for (final Map.Entry<Integer, Set<String>> entry : classesByDistance.entrySet()) {
            logger.debug(" Current distance: {}", entry.getKey());

            for (final String subclass : entry.getValue()) {
                final Class<?> subClass = tryReflection(subclass, tree);

                if (subClass != null) {
                    actualClasses.add(subClass);
                }
            }

            if (!actualClasses.isEmpty()) {
                break; // Don't include classes with higher package distance.
            }
        }

        if (TestClusterUtils.hasStaticGenerator(clazz)) {
            actualClasses.add(clazz);
        }

        if (actualClasses.isEmpty()) {
            logger.info("Don't know how to instantiate abstract class {}", className);
        }

        logger.debug("Subclasses of {}: {}", className, actualClasses);

        return actualClasses;
    }

    private static Class<?> tryReflection(final String className, final InheritanceTree tree) {
        if (TestClusterUtils.isAnonymousClass(className)) {
            return null; // can't handle anonymous subclasses
        }

        testGenerationContext().goingToExecuteSUTCode();
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className, false, loaderForSUT());
        } catch (ClassNotFoundException
                | IncompatibleClassChangeError
                | NoClassDefFoundError e) {
            logger.error("Problem for {}. Class not found: {}", Properties.TARGET_CLASS, className);
            logger.error("Removing class from inheritance tree");
            tree.removeClass(className);
        } finally {
            testGenerationContext().doneWithExecutingSUTCode();
        }

        if (clazz == null) {
            return null;
        }

        final boolean uninstantiable = !TestUsageChecker.canUse(clazz)
                || clazz.isInterface()
                || (Modifier.isAbstract(clazz.getModifiers())
                && !TestClusterUtils.hasStaticGenerator(clazz));
        if (uninstantiable) {
            return null;
        }

        final Class<?> mock = MockList.getMockClass(clazz.getCanonicalName());
        if (mock != null) {
            // If we are mocking this class, then such class should not be used
            // in the generated JUnit test cases, but rather its mock.
            logger.debug("Adding mock " + mock + " instead of " + clazz);
            clazz = mock;
        } else if (!TestClusterUtils.checkIfCanUse(clazz.getCanonicalName())) {
            return null;
        }

        return clazz;
    }

    private static TestGenerationContext testGenerationContext() {
        return TestGenerationContext.getInstance();
    }

    /**
     * Tries to load concrete (sub)classes for {@code java.util.Map} using the SUT class loader.
     * The returned set contains the classes it was able to successfully load and is otherwise
     * empty.
     *
     * @return the set of concrete {@code Map} classes
     */
    private Set<Class<?>> getConcreteClassesMap() {
        return getClassesForNames("java.util.HashMap");
    }

    /**
     * Tries to load concrete (sub)classes for {@code java.util.List} using the SUT class loader.
     * The returned set contains the classes it was able to successfully load and is otherwise
     * empty.
     *
     * @return the set of concrete {@code List} classes
     */
    private Set<Class<?>> getConcreteClassesList() {
        return getClassesForNames("java.util.LinkedList");
    }

    /**
     * Tries to load concrete (sub)classes for {@code java.util.Set} using the SUT class loader.
     * The returned set contains the classes it was able to successfully load and is otherwise
     * empty.
     *
     * @return the set of concrete {@code Set} classes
     */
    private Set<Class<?>> getConcreteClassesSet() {
        return getClassesForNames("java.util.LinkedHashSet");
    }

    /**
     * Tries to load concrete (sub)classes for {@code java.io.Reader} using the SUT class loader.
     * The returned set contains the classes it was able to successfully load and is otherwise
     * empty.
     *
     * @return the set of concrete {@code Reader} classes
     */
    private Set<Class<?>> getConcreteClassesReader() {
        return getClassesForNames("java.io.StringReader");
    }

    /**
     * Tries to load concrete (sub)classes for {@code java.io.Writer} using the SUT class loader.
     * The returned set contains the classes it was able to successfully load and is otherwise
     * empty.
     *
     * @return the set of concrete {@code Writer} classes
     */
    private Set<Class<?>> getConcreteClassesWriter() {
        return getClassesForNames("java.io.StringWriter");
    }

    /**
     * Tries to load concrete (sub)classes for {@code java.lang.Comparable} using the SUT class
     * loader. The returned set contains the classes it was able to successfully load and is otherwise
     * empty.
     *
     * @return the set of concrete {@code Comparable} classes
     */
    private Set<Class<?>> getConcreteClassesComparable() {
        return getClassesForNames("java.lang.Integer");
    }

    /**
     * Returns the {@code Class} objects for the classes or interfaces with the given
     * fully-qualified names.
     *
     * @param fullyQualifiedClassNames
     * @return
     */
    private Set<Class<?>> getClassesForNames(final String... fullyQualifiedClassNames) {
        final Set<Class<?>> classes = new LinkedHashSet<>();

        for (final String name : fullyQualifiedClassNames) {
            try {
                final Class<?> clazz = Class.forName(name, false, loaderForSUT());
                classes.add(clazz);
            } catch (ClassNotFoundException e) {
                logger.error(e.getMessage());
            }
        }

        return classes;
    }

    private static InstrumentingClassLoader loaderForSUT() {
        return testGenerationContext().getClassLoaderForSUT();
    }
}
