/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
import org.evosuite.runtime.mock.MockList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.*;

public class ConcreteClassAnalyzer {

    private static Logger logger = LoggerFactory.getLogger(ConcreteClassAnalyzer.class);

    private static ConcreteClassAnalyzer instance;

    private ConcreteClassAnalyzer() {

    }

    public static ConcreteClassAnalyzer getInstance() {
        if(instance == null)
            instance = new ConcreteClassAnalyzer();

        return instance;
    }


    private Map<Class<?>, Set<Class<?>>> cache = new LinkedHashMap<>();

    public void clear() {
        cache.clear();
    }

    public Set<Class<?>> getConcreteClasses(Class<?> clazz,
                                            InheritanceTree inheritanceTree) {
        if(cache.containsKey(clazz))
            return cache.get(clazz);

        Set<Class<?>> classes = getConcreteClassesImpl(clazz, inheritanceTree);
        cache.put(clazz, classes);
        return classes;
    }

    private Set<Class<?>> getConcreteClassesImpl(Class<?> clazz,
                                                   InheritanceTree inheritanceTree) {

        // Some special cases
        if (clazz.equals(java.util.Map.class))
            return getConcreteClassesMap();
        else if (clazz.equals(java.util.List.class))
            return getConcreteClassesList();
        else if (clazz.equals(Set.class))
            return getConcreteClassesSet();
        else if (clazz.equals(java.util.Collection.class))
            return getConcreteClassesList();
        else if (clazz.equals(java.util.Iterator.class))
            // We don't want to explicitly create iterators
            // This would only pull in java.util.Scanner, the only
            // concrete subclass
            return new LinkedHashSet<Class<?>>();
        else if (clazz.equals(java.util.ListIterator.class))
            // We don't want to explicitly create iterators
            return new LinkedHashSet<Class<?>>();
        else if (clazz.equals(java.io.Serializable.class))
            return new LinkedHashSet<Class<?>>();
        else if (clazz.equals(Comparable.class))
            return getConcreteClassesComparable();
        else if (clazz.equals(java.util.Comparator.class))
            return new LinkedHashSet<Class<?>>();

        Set<Class<?>> actualClasses = new LinkedHashSet<Class<?>>();
        if (Modifier.isAbstract(clazz.getModifiers())
                || Modifier.isInterface(clazz.getModifiers()) || clazz.equals(Enum.class)) {
            // We have to use getName here and not getCanonicalName
            // because getCanonicalname uses . rather than $ for inner classes
            // but the InheritanceTree uses $
            String className = clazz.getName();
            if(MockList.isAMockClass(className))
                className = clazz.getSuperclass().getName();
            Set<String> subClasses = inheritanceTree.getSubclasses(className);
            logger.debug("Subclasses of " + clazz.getName() + ": " + subClasses);
            Map<String, Integer> classDistance = new HashMap<String, Integer>();
            int maxDistance = -1;
            String name = clazz.getName();
            if (clazz.equals(Enum.class)) {
                name = Properties.TARGET_CLASS;
            }
            for (String subClass : subClasses) {
                int distance = TestClusterUtils.getPackageDistance(subClass, name);
                classDistance.put(subClass, distance);
                maxDistance = Math.max(distance, maxDistance);
            }
            int distance = 0;
            while (actualClasses.isEmpty() && distance <= maxDistance) {
                logger.debug(" Current distance: " + distance);
                for (String subClass : subClasses) {
                    if (TestClusterUtils.isAnonymousClass(subClass)) {
                        continue;
                    }

                    if (classDistance.get(subClass) == distance) {
                        try {
                            TestGenerationContext.getInstance().goingToExecuteSUTCode();
                            Class<?> subClazz = Class.forName(subClass,
                                                              false,
                                                              TestGenerationContext.getInstance().getClassLoaderForSUT());
                            if (!TestUsageChecker.canUse(subClazz))
                                continue;
                            if (subClazz.isInterface())
                                continue;
                            if (Modifier.isAbstract(subClazz.getModifiers())) {
                                if(!TestClusterUtils.hasStaticGenerator(subClazz))
                                    continue;
                            }
                            Class<?> mock = MockList.getMockClass(subClazz.getCanonicalName());
                            if (mock != null) {
                                /*
                                 * If we are mocking this class, then such class should not be used
                                 * in the generated JUnit test cases, but rather its mock.
                                 */
                                logger.debug("Adding mock " + mock + " instead of "
                                        + clazz);
                                subClazz = mock;
                            } else {

                                if (!TestClusterUtils.checkIfCanUse(subClazz.getCanonicalName())) {
                                    continue;
                                }
                            }

                            actualClasses.add(subClazz);

                        } catch (ClassNotFoundException | IncompatibleClassChangeError | NoClassDefFoundError e) {
                            logger.error("Problem for " + Properties.TARGET_CLASS
                                    + ". Class not found: " + subClass, e);
                            logger.error("Removing class from inheritance tree");
                            inheritanceTree.removeClass(subClass);
                        } finally {
                            TestGenerationContext.getInstance().doneWithExecutingSUTCode();
                        }
                    }
                }
                distance++;
            }
            if(TestClusterUtils.hasStaticGenerator(clazz)) {
                actualClasses.add(clazz);
            }
            if (actualClasses.isEmpty()) {
                logger.info("Don't know how to instantiate abstract class {}", clazz.getName());
            }
        } else {
            actualClasses.add(clazz);
        }

        logger.debug("Subclasses of " + clazz.getName() + ": " + actualClasses);
        return actualClasses;
    }

    private Set<Class<?>> getConcreteClassesMap() {
        Set<Class<?>> mapClasses = new LinkedHashSet<>();
        Class<?> mapClazz;
        try {
            mapClazz = Class.forName("java.util.HashMap",
                                     false,
                                     TestGenerationContext.getInstance().getClassLoaderForSUT());
            mapClasses.add(mapClazz);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage());
        }
        return mapClasses;
    }

    private Set<Class<?>> getConcreteClassesList() {
        Set<Class<?>> mapClasses = new LinkedHashSet<Class<?>>();
        Class<?> mapClazz;
        try {
            mapClazz = Class.forName("java.util.LinkedList",
                                     false,
                                     TestGenerationContext.getInstance().getClassLoaderForSUT());
            mapClasses.add(mapClazz);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage());
        }
        return mapClasses;
    }

    private Set<Class<?>> getConcreteClassesSet() {
        Set<Class<?>> mapClasses = new LinkedHashSet<Class<?>>();
        Class<?> setClazz;
        try {
            setClazz = Class.forName("java.util.LinkedHashSet",
                                     false,
                                     TestGenerationContext.getInstance().getClassLoaderForSUT());
            mapClasses.add(setClazz);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage());
        }
        return mapClasses;
    }

    private Set<Class<?>> getConcreteClassesComparable() {
        Set<Class<?>> comparableClasses = new LinkedHashSet<Class<?>>();
        Class<?> comparableClazz;
        try {
            comparableClazz = Class.forName("java.lang.Integer",
                                            false,
                                            TestGenerationContext.getInstance().getClassLoaderForSUT());
            comparableClasses.add(comparableClazz);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage());
        }
        return comparableClasses;
    }
}
