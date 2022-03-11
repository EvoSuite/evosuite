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
package org.evosuite.runtime;

import org.evosuite.runtime.instrumentation.InstrumentedClass;
import org.evosuite.runtime.instrumentation.RemoveFinalClassAdapter;
import org.evosuite.runtime.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static java.util.Comparator.comparing;

/**
 * The content of arrays in reflection methods may differ between classloaders, therefore
 * we sort the output alphabetically
 *
 * @author gordon
 */
public class Reflection {

    private static <T> T[] sortArrayInPlace(T[] original) {
        List<T> methods = Arrays.asList(original);
        methods.sort(comparing(Object::toString));

        methods.toArray(original);
        return original;
    }

    public static Annotation[] getAnnotations(Class<?> clazz) throws SecurityException {
        return sortArrayInPlace(ReflectionUtils.getAnnotations(clazz));
    }

    public static Class<?>[] getClasses(Class<?> clazz) throws SecurityException {
        return sortArrayInPlace(ReflectionUtils.getClasses(clazz));
    }

    // TODO: Should return mocked methods?
    public static Method[] getMethods(Class<?> clazz) throws SecurityException {
        return sortArrayInPlace(ReflectionUtils.getMethods(clazz));
    }

    public static Field[] getFields(Class<?> clazz) throws SecurityException {
        return sortArrayInPlace(ReflectionUtils.getFields(clazz));
    }

    public static Constructor<?>[] getConstructors(Class<?> clazz) throws SecurityException {
        return sortArrayInPlace(ReflectionUtils.getConstructors(clazz));
    }

    public static Class<?>[] getInterfaces(Class<?> clazz) throws SecurityException {
        return sortArrayInPlace(Arrays.stream(ReflectionUtils.getInterfaces(clazz)).filter(c -> !c.equals(InstrumentedClass.class)).toArray(Class[]::new));
    }

    public static Annotation[] getDeclaredAnnotations(Class<?> clazz) throws SecurityException {
        return sortArrayInPlace(ReflectionUtils.getDeclaredAnnotations(clazz));
    }

    public static Annotation[] getDeclaredAnnotations(Field field) throws SecurityException {
        return sortArrayInPlace(ReflectionUtils.getDeclaredAnnotations(field));
    }

    public static Class<?>[] getDeclaredClasses(Class<?> clazz) throws SecurityException {
        return sortArrayInPlace(ReflectionUtils.getDeclaredClasses(clazz));
    }

    public static Method[] getDeclaredMethods(Class<?> clazz) throws SecurityException {
        return sortArrayInPlace(ReflectionUtils.getDeclaredMethods(clazz));
    }

    public static Field[] getDeclaredFields(Class<?> clazz) throws SecurityException {
        return sortArrayInPlace(ReflectionUtils.getDeclaredFields(clazz));
    }

    public static Constructor<?>[] getDeclaredConstructors(Class<?> clazz) throws SecurityException {
        return sortArrayInPlace(ReflectionUtils.getDeclaredConstructors(clazz));
    }

    public static int getModifiers(Class<?> clazz) {
        int modifier = clazz.getModifiers();
        if (RemoveFinalClassAdapter.finalClasses.contains(clazz.getCanonicalName())) {
            modifier = modifier | Modifier.FINAL;
        }
        return modifier;
    }

    public static void setField(Field field, Object sourceObject, Object value) throws IllegalAccessException {
        if (field.getType().equals(int.class))
            field.setInt(sourceObject, getIntValue(value));
        else if (field.getType().equals(boolean.class))
            field.setBoolean(sourceObject, (Boolean) value);
        else if (field.getType().equals(byte.class))
            field.setByte(sourceObject, (byte) getIntValue(value));
        else if (field.getType().equals(char.class))
            field.setChar(sourceObject, getCharValue(value));
        else if (field.getType().equals(double.class))
            field.setDouble(sourceObject, getDoubleValue(value));
        else if (field.getType().equals(float.class))
            field.setFloat(sourceObject, getFloatValue(value));
        else if (field.getType().equals(long.class))
            field.setLong(sourceObject, getLongValue(value));
        else if (field.getType().equals(short.class))
            field.setShort(sourceObject, (short) getIntValue(value));
        else {
            field.set(sourceObject, value);
        }
    }

    private static int getIntValue(Object object) {
        if (object instanceof Number) {
            return ((Number) object).intValue();
        } else if (object instanceof Character) {
            return (Character) object;
        } else
            return 0;
    }

    private static long getLongValue(Object object) {
        if (object instanceof Number) {
            return ((Number) object).longValue();
        } else if (object instanceof Character) {
            return (Character) object;
        } else
            return 0L;
    }

    private static float getFloatValue(Object object) {
        if (object instanceof Number) {
            return ((Number) object).floatValue();
        } else if (object instanceof Character) {
            return (Character) object;
        } else
            return 0F;
    }

    private static double getDoubleValue(Object object) {
        if (object instanceof Number) {
            return ((Number) object).doubleValue();
        } else if (object instanceof Character) {
            return (Character) object;
        } else
            return 0.0;
    }

    private static char getCharValue(Object object) {
        if (object instanceof Character) {
            return (Character) object;
        } else if (object instanceof Number) {
            return (char) ((Number) object).intValue();
        } else
            return '0';
    }

}
