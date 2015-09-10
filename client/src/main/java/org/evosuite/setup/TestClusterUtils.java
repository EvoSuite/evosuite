/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.setup;

import org.evosuite.TestGenerationContext;
import org.junit.Test;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Set of pure static methods
 */
public class TestClusterUtils {

	protected static final Logger logger = LoggerFactory.getLogger(TestClusterUtils.class);


	/**
	 * Determine if this class contains JUnit tests
	 * @deprecated use {@code org.evosuite.junit.CoverageAnalysis.isTest(Class<?> cls)}
	 *
	 * @param className
	 * @return
	 */
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
        return c.getName().startsWith("org.evosuite");
                //|| c.getName().equals("java.lang.String");    // This is now handled in addDependencyClass
    }

}
