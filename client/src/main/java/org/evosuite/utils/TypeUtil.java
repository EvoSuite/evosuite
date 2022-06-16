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
package org.evosuite.utils;

import org.evosuite.testcase.execution.EvosuiteError;
import org.objectweb.asm.Type;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utils for general types checking
 *
 * @author Ignacio Lebrero
 */
public class TypeUtil {

    public static final String UNEXPECTED_VALUE = "Unexpected value: ";
    public static final String IS_NOT_A_PRIMITIVE_VALUE_CLASS = " is not a primitive value class!";

    //region 32 bits types
    /** 32 bits types */
    public static final Set<Type> bv32Types = Stream.of(
          Type.BOOLEAN_TYPE,
          Type.SHORT_TYPE,
          Type.CHAR_TYPE,
          Type.INT_TYPE,
          Type.BYTE_TYPE
    ).collect(Collectors.toSet());

    public static final Set<Class> primitiveBv32Classes = Stream.of(
          boolean.class,
          short.class,
          char.class,
          int.class,
          byte.class
    ).collect(Collectors.toSet());

    public static final Set<Integer> primitiveBv32TypesSorts = Stream.of(
        Type.BOOLEAN,
        Type.SHORT,
        Type.CHAR,
        Type.INT,
        Type.BYTE
    ).collect(Collectors.toSet());

    public static final Set<Integer> referenceTypesSorts = Stream.of(
        Type.OBJECT,
        Type.ARRAY
    ).collect(Collectors.toSet());

    //endregion

    //region ASM Type related helpers

    /**
     * ASM Type related helpers
     */
    public static boolean isBv32(Type t) {
        return bv32Types.contains(t);
    }

    public static boolean isBv64(Type t) {
        return t.equals(Type.LONG_TYPE);
    }

    public static boolean isFp32(Type t) {
        return t.equals(Type.FLOAT_TYPE);
    }

    public static boolean isFp64(Type t) {
        return t.equals(Type.DOUBLE_TYPE);
    }

    public static boolean isValue(Type t) {
        return isBv32(t) || isBv64(t) || isFp32(t) || isFp64(t);
    }

    public static boolean isRealValue(Type t) {
        return isFp32(t) || isFp64(t);
    }

    public static boolean isIntegerValue(Type t) {
        return isBv32(t) || isBv64(t);
    }

    public static boolean isReferenceValue(Type arrayType) {
        return referenceTypesSorts.contains(arrayType.getSort());
    }

    public static boolean isStringValue(Type t) {
        Type stringType = Type.getType(String.class);
        return t.equals(stringType);
    }

    //endregion

    //region Class related helpers

    /**
     * Class related helpers
     */
    public static boolean isPrimitiveFp64(Class clazz) {
        return double.class.equals(clazz);
    }

    public static boolean isPrimitiveFp32(Class clazz) {
        return float.class.equals(clazz);
    }

    public static boolean isPrimitiveBv32(Class clazz) {
        return primitiveBv32Classes.contains(clazz);
    }

    public static boolean isPrimitiveBv64(Class clazz) {
        return long.class.equals(clazz);
    }

    public static boolean isPrimitiveInteger(Class clazz) {
        return isPrimitiveBv32(clazz) || isPrimitiveBv64(clazz);
    }

    public static boolean isPrimitiveReal(Class clazz) {
        return isPrimitiveFp32(clazz) || isPrimitiveFp64(clazz);
    }

    public static boolean isStringValue(Class clazz) {
        return String.class.equals(clazz);
    }

    public static boolean isPrimitiveValue(Class clazz) {
        return isPrimitiveBv32(clazz) || isPrimitiveBv64(clazz) || isPrimitiveFp32(clazz) || isPrimitiveFp64(clazz);
    }

    public static Class<?> getPrimitiveArrayClassFromElementType(Type t) {
       if (t.equals(Type.BOOLEAN_TYPE)) return boolean[].class;
		if (t.equals(Type.CHAR_TYPE)) return char[].class;
		if (t.equals(Type.SHORT_TYPE)) return short[].class;
		if (t.equals(Type.BYTE_TYPE)) return byte[].class;
		if (t.equals(Type.INT_TYPE)) return int[].class;
		if (t.equals(Type.LONG_TYPE)) return long[].class;
		if (t.equals(Type.FLOAT_TYPE)) return float[].class;
		if (t.equals(Type.DOUBLE_TYPE)) return double[].class;

        throw new EvosuiteError(t + IS_NOT_A_PRIMITIVE_VALUE_CLASS);
    }

    public static Object unboxIntegerPrimitiveValue(Object o) {
        if (Integer.class.getName().equals(o.getClass().getName())) return ((Integer) o).longValue();
        if (Short.class.getName().equals(o.getClass().getName())) return ((Short) o).longValue();
        if (Byte.class.getName().equals(o.getClass().getName())) return ((Byte) o).longValue();
        if (Boolean.class.getName().equals(o.getClass().getName())) return ((Boolean) o) ? 1L : 0L;
        if (Long.class.getName().equals(o.getClass().getName())) return ((Long) o).longValue();
        if (Character.class.getName().equals(o.getClass().getName())) return (long) ((Character) o).charValue();

        throw new IllegalStateException(UNEXPECTED_VALUE + " The object " + o.getClass().getName() + " is not a primitive type wrapper.");
    }

    public static Object unboxRealPrimitiveValue(Object o) {
         if (Float.class.getName().equals(o.getClass().getName())) return  ((Float) o).doubleValue();
         if (Double.class.getName().equals(o.getClass().getName())) return ((Double) o).doubleValue();

        throw new IllegalStateException(UNEXPECTED_VALUE + " The object " + o.getClass().getName() + " is not a primitive type wrapper.");
    }

    public static Object unboxPrimitiveValue(Object o) {
        if (Integer.class.getName().equals(o.getClass().getName())) return ((Integer) o).longValue();
        if (Short.class.getName().equals(o.getClass().getName())) return ((Short) o).longValue();
        if (Byte.class.getName().equals(o.getClass().getName())) return ((Byte) o).longValue();
        if (Boolean.class.getName().equals(o.getClass().getName())) return ((Boolean) o) ? 1L : 0L;
        if (Character.class.getName().equals(o.getClass().getName())) return (long) (((Character) o).charValue());
        if (Long.class.getName().equals(o.getClass().getName())) return ((Long) o).longValue();
        if (Float.class.getName().equals(o.getClass().getName())) return  ((Float) o).doubleValue();
        if (Double.class.getName().equals(o.getClass().getName())) return ((Double) o).doubleValue();

        throw new IllegalStateException(UNEXPECTED_VALUE + " The object " + o.getClass().getName() + " is not a primitive type wrapper.");
    }

    public static Object convertIntegerTo(Long value, String componentTypeName) {
        if (int.class.getName().equals(componentTypeName)) return value.intValue();
        if (short.class.getName().equals(componentTypeName)) return value.shortValue();
        if (byte.class.getName().equals(componentTypeName)) return value.byteValue();
        if (char.class.getName().equals(componentTypeName)) return (char) value.longValue();
        if (boolean.class.getName().equals(componentTypeName)) return value > 1;
        if (long.class.getName().equals(componentTypeName)) return value.longValue();

        throw new IllegalStateException(UNEXPECTED_VALUE + componentTypeName);
    }

    public static Object convertRealTo(Double value, String componentTypeName) {
        if (float.class.getName().equals(componentTypeName)) return value.floatValue();
        if (double.class.getName().equals(componentTypeName)) return value.doubleValue();

        throw new IllegalStateException(UNEXPECTED_VALUE + componentTypeName);
    }

    //endregion
}