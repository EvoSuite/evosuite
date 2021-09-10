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
package org.evosuite;

import junit.framework.Assert;
import org.evosuite.instrumentation.testability.TestabilityTransformationClassLoader;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TestUtil {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TestUtil.class);

    public static void assertCorrectStart(Class<?> clazz) {
        String projectPrefix = clazz.getPackage().getName();
        Assert.assertEquals("Must start test with '-DDPROJECT_PREFIX=" + projectPrefix + "'.",
                Properties.PROJECT_PREFIX, projectPrefix);
        String targetClass = clazz.getName();
        Assert.assertEquals("Must start test with '-DTARGET_CLASS=" + targetClass + "'.", Properties.TARGET_CLASS,
                targetClass);
    }

    public static void assertCorrectStart(String clazz) {
        // TODO Replace with
        // Properties.OUTPUT_DIR = "examples/facts/evosuite-files/";
        // ClassTransformer.getInstance().instrumentClass(clazz);
        // TODO When doing so remember to also remove the -javaagent param from
        // the launch config
        String projectPrefix = clazz.substring(0, clazz.lastIndexOf("."));
        Assert.assertEquals("Must start test with '-DDPROJECT_PREFIX=" + projectPrefix + "'.",
                Properties.PROJECT_PREFIX, projectPrefix);
        String targetClass = clazz;
        Assert.assertEquals("Must start test with '-DTARGET_CLASS=" + targetClass + "'.", Properties.TARGET_CLASS,
                targetClass);
    }

    public static String getPrefix(String fullyQualifiedClass) {
        return fullyQualifiedClass.substring(0, fullyQualifiedClass.lastIndexOf("."));
    }

    public static Object invokeMethod(Class<?> targetClass, Object target, String methodName, Class<?>[] argClasses,
                                      Object[] args) {
        try {
            Method method = targetClass.getDeclaredMethod(methodName, argClasses);
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (Exception exc) {
            logger.error("Encountered exception when calling method:", exc);
            throw new RuntimeException(exc);
        }
    }

    public static Object invokeMethod(Object target, String methodName, Object... args) {
        return invokeMethod(target.getClass(), target, methodName, getArgClasses(args), args);
    }

    public static Object loadInstrumented(String className, Object... constructorArgs) {
        try {
            Properties.TARGET_CLASS = className;
            Properties.PROJECT_PREFIX = getPrefix(className);
            Properties.TARGET_CLASS_PREFIX = Properties.PROJECT_PREFIX;
            TestabilityTransformationClassLoader classLoader = new TestabilityTransformationClassLoader();
            Class<?> factsComparatorClass = classLoader.loadClass(className);
            Class<?>[] argClasses = getArgClasses(constructorArgs);
            Constructor<?> factsComparatorConstructor = factsComparatorClass.getConstructor(argClasses);
            Object target = factsComparatorConstructor.newInstance(constructorArgs);
            return target;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }

    public static void setField(Object target, String fieldName, Object value) {
        try {
            Class<?> clazz = target.getClass();
            Field field = clazz.getField(fieldName);
            field.set(target, value);
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }

    private static Class<?>[] getArgClasses(Object... args) {
        Class<?>[] argClasses = new Class[args.length];
        for (int idx = 0; idx < args.length; idx++) {
            argClasses[idx] = args[idx].getClass();
        }
        return argClasses;
    }

    private TestUtil() {
        // private constructor
    }
}
