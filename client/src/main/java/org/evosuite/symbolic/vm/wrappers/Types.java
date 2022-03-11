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
package org.evosuite.symbolic.vm.wrappers;

import org.objectweb.asm.Type;

import static org.objectweb.asm.Type.*;

public interface Types {

    // primitive types
    Type INTEGER = Type.getType(Integer.class);
    Type LONG = Type.getType(Long.class);
    Type FLOAT = Type.getType(Float.class);
    Type DOUBLE = Type.getType(Double.class);
    Type SHORT = Type.getType(Short.class);
    Type CHARACTER = Type.getType(Character.class);
    Type BYTE = Type.getType(Byte.class);
    Type BOOLEAN = Type.getType(Boolean.class);

    // wrapper types
    String JAVA_LANG_LONG = Long.class.getName()
            .replace('.', '/');
    String JAVA_LANG_FLOAT = Float.class.getName().replace(".",
            "/");
    String JAVA_LANG_DOUBLE = Double.class.getName().replace(".",
            "/");
    String JAVA_LANG_SHORT = Short.class.getName().replace(".",
            "/");
    String JAVA_LANG_BYTE = Byte.class.getName()
            .replace('.', '/');
    String JAVA_LANG_CHARACTER = Character.class.getName()
            .replace('.', '/');
    String JAVA_LANG_BOOLEAN = Boolean.class.getName().replace(
            ".", "/");
    String JAVA_LANG_INTEGER = Integer.class.getName().replace(
            ".", "/");
    String JAVA_LANG_STRING = String.class.getName().replace(".",
            "/");

    Type STRING_TYPE = Type.getType(String.class);
    String STR_TO_INT_DESCRIPTOR = Type.getMethodDescriptor(
            INT_TYPE, STRING_TYPE);

    // valueOf Descriptos
    String I_TO_INTEGER = Type.getMethodDescriptor(INTEGER,
            INT_TYPE);
    String J_TO_LONG = Type.getMethodDescriptor(LONG, LONG_TYPE);
    String F_TO_FLOAT = Type.getMethodDescriptor(FLOAT,
            FLOAT_TYPE);
    String D_TO_DOUBLE = Type.getMethodDescriptor(DOUBLE,
            DOUBLE_TYPE);
    String S_TO_SHORT = Type.getMethodDescriptor(SHORT,
            SHORT_TYPE);
    String B_TO_BYTE = Type.getMethodDescriptor(BYTE, BYTE_TYPE);
    String C_TO_CHARACTER = Type.getMethodDescriptor(CHARACTER,
            CHAR_TYPE);
    String Z_TO_BOOLEAN = Type.getMethodDescriptor(BOOLEAN,
            BOOLEAN_TYPE);

    // intValue/shortValue,etc. descriptors
    String TO_INT = Type.getMethodDescriptor(INT_TYPE);

    String TO_LONG = Type.getMethodDescriptor(LONG_TYPE);

    String TO_FLOAT = Type.getMethodDescriptor(FLOAT_TYPE);

    String TO_DOUBLE = Type.getMethodDescriptor(DOUBLE_TYPE);

    String TO_SHORT = Type.getMethodDescriptor(SHORT_TYPE);

    String TO_BYTE = Type.getMethodDescriptor(BYTE_TYPE);

    String TO_CHAR = Type.getMethodDescriptor(CHAR_TYPE);

    String TO_BOOLEAN = Type.getMethodDescriptor(BOOLEAN_TYPE);

    String I_TO_VOID = Type.getMethodDescriptor(VOID_TYPE,
            INT_TYPE);

    String INIT = "<init>";

    String B_TO_VOID = Type.getMethodDescriptor(VOID_TYPE,
            BYTE_TYPE);
    String C_TO_VOID = Type.getMethodDescriptor(VOID_TYPE,
            CHAR_TYPE);
    String S_TO_VOID = Type.getMethodDescriptor(VOID_TYPE,
            SHORT_TYPE);
    String Z_TO_VOID = Type.getMethodDescriptor(VOID_TYPE,
            BOOLEAN_TYPE);
    String J_TO_VOID = Type.getMethodDescriptor(VOID_TYPE,
            LONG_TYPE);
    String F_TO_VOID = Type.getMethodDescriptor(VOID_TYPE,
            FLOAT_TYPE);
    String D_TO_VOID = Type.getMethodDescriptor(VOID_TYPE,
            DOUBLE_TYPE);

    String C_TO_I = Type.getMethodDescriptor(INT_TYPE, CHAR_TYPE);

    String C_TO_Z = Type.getMethodDescriptor(BOOLEAN_TYPE,
            CHAR_TYPE);
}
