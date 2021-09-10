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
package org.evosuite.testcarver.capture;

import org.evosuite.classpath.ResourceList;
import org.objectweb.asm.Type;


public final class CaptureUtil {

    private CaptureUtil() {
    }

    public static Class<?> loadClass(final String internalClassName) {
        final String className = ResourceList.getClassNameFromResourcePath(internalClassName);

        try {
            return Class.forName(className);
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> getClassFromDesc(final String desc) {
        final Type type = Type.getType(desc);
        if (type.equals(Type.BOOLEAN_TYPE)) {
            return boolean.class;
        } else if (type.equals(Type.BYTE_TYPE)) {
            return byte.class;
        } else if (type.equals(Type.CHAR_TYPE)) {
            return char.class;
        } else if (type.equals(Type.DOUBLE_TYPE)) {
            return double.class;
        } else if (type.equals(Type.FLOAT_TYPE)) {
            return float.class;
        } else if (type.equals(Type.INT_TYPE)) {
            return int.class;
        } else if (type.equals(Type.LONG_TYPE)) {
            return long.class;
        } else if (type.equals(Type.SHORT_TYPE)) {
            return short.class;
        }

        try {
            return Class.forName(ResourceList.getClassNameFromResourcePath(type.getInternalName()));
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


}
