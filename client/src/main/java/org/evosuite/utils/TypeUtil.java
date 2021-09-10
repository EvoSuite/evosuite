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

/**
 * Utils for general types checking
 *
 * @author Ignacio Lebrero
 */
public class TypeUtil {

    public static final String UNEXPECTED_VALUE = "Unexpected value: ";
    public static final String IS_NOT_A_PRIMITIVE_VALUE_CLASS = " is not a primitive value class!";

    /**
     * ASM Type related helpers
     */

    public static boolean isBv32(Type t) {
        return t.equals(Type.CHAR_TYPE) || t.equals(Type.BOOLEAN_TYPE) || t.equals(Type.SHORT_TYPE)
                || t.equals(Type.BYTE_TYPE) || t.equals(Type.INT_TYPE);
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

    public static boolean isStringValue(Type t) {
        Type stringType = Type.getType(String.class);
        return t.equals(stringType);
    }

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
        return int.class.equals(clazz)
                || char.class.equals(clazz)
                || byte.class.equals(clazz)
                || short.class.equals(clazz)
                || boolean.class.equals(clazz);
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
        if (t.equals(Type.BOOLEAN_TYPE))
            return boolean[].class;
        if (t.equals(Type.CHAR_TYPE))
            return char[].class;
        if (t.equals(Type.SHORT_TYPE))
            return short[].class;
        if (t.equals(Type.BYTE_TYPE))
            return byte[].class;
        if (t.equals(Type.INT_TYPE))
            return int[].class;
        if (t.equals(Type.LONG_TYPE))
            return long[].class;
        if (t.equals(Type.FLOAT_TYPE))
            return float[].class;
        if (t.equals(Type.DOUBLE_TYPE))
            return double[].class;

        throw new EvosuiteError(t + IS_NOT_A_PRIMITIVE_VALUE_CLASS);
    }

    public static Object unboxIntegerPrimitiveValue(Object o) {
        if (Integer.class.getName().equals(o.getClass().getName())) {
            return ((Integer) o).longValue();
        } else if (Short.class.getName().equals(o.getClass().getName())) {
            return ((Short) o).longValue();
        } else if (Byte.class.getName().equals(o.getClass().getName())) {
            return ((Byte) o).longValue();
        } else if (Boolean.class.getName().equals(o.getClass().getName())) {
            return ((Boolean) o) ? 1L : 0L;
        } else if (Long.class.getName().equals(o.getClass().getName())) {
            return ((Long) o).longValue();
        } else if (Character.class.getName().equals(o.getClass().getName())) {
            return (long) ((Character) o).charValue();
        }

        throw new IllegalStateException(UNEXPECTED_VALUE + " The object " + o.getClass().getName() + " is not a primitive type wrapper.");
    }

    public static Object unboxRealPrimitiveValue(Object o) {
        if (Float.class.getName().equals(o.getClass().getName())) {
            return ((Float) o).doubleValue();
        } else if (Double.class.getName().equals(o.getClass().getName())) {
            return ((Double) o).doubleValue();
        }

        throw new IllegalStateException(UNEXPECTED_VALUE + " The object " + o.getClass().getName() + " is not a primitive type wrapper.");
    }

    public static Object unboxPrimitiveValue(Object o) {
        if (Integer.class.getName().equals(o.getClass().getName())) {
            return ((Integer) o).longValue();
        } else if (Short.class.getName().equals(o.getClass().getName())) {
            return ((Short) o).longValue();
        } else if (Byte.class.getName().equals(o.getClass().getName())) {
            return ((Byte) o).longValue();
        } else if (Boolean.class.getName().equals(o.getClass().getName())) {
            return ((Boolean) o) ? 1L : 0L;
        } else if (Character.class.getName().equals(o.getClass().getName())) {
            return (long) (((Character) o).charValue());
        } else if (Long.class.getName().equals(o.getClass().getName())) {
            return ((Long) o).longValue();
        } else if (Float.class.getName().equals(o.getClass().getName())) {
            return ((Float) o).doubleValue();
        } else if (Double.class.getName().equals(o.getClass().getName())) {
            return ((Double) o).doubleValue();
        }

        throw new IllegalStateException(UNEXPECTED_VALUE + " The object " + o.getClass().getName() + " is not a primitive type wrapper.");
    }

    public static Object convertIntegerTo(Long value, String componentTypeName) {
        if (int.class.getName().equals(componentTypeName)) {
            return value.intValue();
        } else if (short.class.getName().equals(componentTypeName)) {
            return value.shortValue();
        } else if (byte.class.getName().equals(componentTypeName)) {
            return value.byteValue();
        } else if (char.class.getName().equals(componentTypeName)) {
            return (char) value.longValue();
        } else if (boolean.class.getName().equals(componentTypeName)) {
            return value > 1;
        } else if (long.class.getName().equals(componentTypeName)) {
            return value.longValue();
        }

        throw new IllegalStateException(UNEXPECTED_VALUE + componentTypeName);
    }

    public static Object convertRealTo(Double value, String componentTypeName) {
        if (float.class.getName().equals(componentTypeName)) {
            return value.floatValue();
        } else if (double.class.getName().equals(componentTypeName)) {
            return value.doubleValue();
        }

        throw new IllegalStateException(UNEXPECTED_VALUE + componentTypeName);
    }
}