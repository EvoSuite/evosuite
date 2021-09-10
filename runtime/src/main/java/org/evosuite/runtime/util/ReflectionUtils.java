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
package org.evosuite.runtime.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtils {

    protected static final Logger logger = LoggerFactory.getLogger(ReflectionUtils.class);

    public static Class<?>[] getDeclaredClasses(Class<?> clazz) {
        try {
            return clazz.getDeclaredClasses();
        } catch (NoClassDefFoundError e) {
            // TODO: What shall we do?
            logger.info("Error while analyzing all classes of class " + clazz + ": " + e);
            return new Class<?>[0];
        }
    }

    public static Class<?>[] getClasses(Class<?> clazz) {
        try {
            return clazz.getClasses();
        } catch (NoClassDefFoundError e) {
            // TODO: What shall we do?
            logger.info("Error while analyzing all classes of class " + clazz + ": " + e);
            return new Class<?>[0];
        }
    }

    public static Constructor<?>[] getDeclaredConstructors(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructors();
        } catch (NoClassDefFoundError e) {
            // TODO: What shall we do?
            logger.info("Error while analyzing declared constructors of class " + clazz + ": " + e);
            return new Constructor<?>[0];
        }
    }

    public static Constructor<?>[] getConstructors(Class<?> clazz) {
        try {
            return clazz.getConstructors();
        } catch (NoClassDefFoundError e) {
            // TODO: What shall we do?
            logger.info("Error while analyzing constructors of class " + clazz + ": " + e);
            return new Constructor<?>[0];
        }
    }

    public static Class<?>[] getInterfaces(Class<?> clazz) {
        try {
            return clazz.getInterfaces();
        } catch (NoClassDefFoundError e) {
            // TODO: What shall we do?
            logger.info("Error while analyzing interfaces of class " + clazz + ": " + e);
            return new Class<?>[0];
        }
    }

    public static Method[] getDeclaredMethods(Class<?> clazz) {
        try {
            return clazz.getDeclaredMethods();
        } catch (NoClassDefFoundError e) {
            // TODO: What shall we do?
            logger.info(
                    "Error while trying to load declared methods of class " + clazz.getName() + ": " + e);
            return new Method[0];
        }
    }

    public static Method[] getMethods(Class<?> clazz) {
        try {
            return clazz.getMethods();
        } catch (NoClassDefFoundError e) {
            // TODO: What shall we do?
            logger.info("Error while trying to load methods of class " + clazz.getName() + ": " + e);
            return new Method[0];
        }
    }

    public static Field[] getDeclaredFields(Class<?> clazz) {
        try {
            return clazz.getDeclaredFields();
        } catch (NoClassDefFoundError e) {
            // TODO: What shall we do?
            logger.info(
                    "Error while trying to load declared fields of class " + clazz.getName() + ": " + e);
            return new Field[0];
        }
    }

    public static Field getDeclaredField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoClassDefFoundError | NoSuchFieldException e) {
            logger.info("Error while trying to load declared field '" + fieldName + "' of class "
                    + clazz.getName() + ": " + e);
            return null;
        }
    }

    public static Field[] getFields(Class<?> clazz) {
        try {
            return clazz.getFields();
        } catch (NoClassDefFoundError e) {
            // TODO: What shall we do?
            logger.info("Error while trying to load fields of class " + clazz.getName() + ": " + e);
            return new Field[0];
        }
    }

    public static Annotation[] getDeclaredAnnotations(Class<?> clazz) {
        return clazz.getDeclaredAnnotations();
    }

    public static Annotation[] getAnnotations(Class<?> clazz) {
        return clazz.getAnnotations();
    }

    public static Annotation[] getDeclaredAnnotations(Field field) {
        return field.getDeclaredAnnotations();
    }

    public static Annotation[] getDeclaredAnnotations(Method method) {
        return method.getDeclaredAnnotations();
    }
}
