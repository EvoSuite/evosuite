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
package org.evosuite.symbolic.vm.string;

import org.objectweb.asm.Type;

import java.io.Reader;
import java.io.StringReader;
import java.util.StringTokenizer;

import static org.objectweb.asm.Type.*;

public interface Types {

    Type CHARSEQ_TYPE = Type.getType(CharSequence.class);

    Type OBJECT_TYPE = Type.getType(Object.class);

    Type STRING_TYPE = Type.getType(String.class);

    Type STRING_BUILDER_TYPE = Type
            .getType(StringBuilder.class);

    String TO_INT_DESCRIPTOR = getMethodDescriptor(INT_TYPE);

    String TO_STR_DESCRIPTOR = getMethodDescriptor(STRING_TYPE);

    String STR_TO_INT_DESCRIPTOR = getMethodDescriptor(
            INT_TYPE, STRING_TYPE);

    String INT_TO_INT_DESCRIPTOR = getMethodDescriptor(
            INT_TYPE, INT_TYPE);

    String INT_TO_CHAR_DESCRIPTOR = getMethodDescriptor(
            CHAR_TYPE, INT_TYPE);

    String STR_TO_STR_DESCRIPTOR = getMethodDescriptor(
            STRING_TYPE, STRING_TYPE);

    String CHAR_CHAR_TO_STR_DESCRIPTOR = getMethodDescriptor(
            STRING_TYPE, CHAR_TYPE, CHAR_TYPE);

    String INT_INT_TO_STR_DESCRIPTOR = getMethodDescriptor(
            STRING_TYPE, INT_TYPE, INT_TYPE);

    String INT_TO_STR_DESCRIPTOR = getMethodDescriptor(
            STRING_TYPE, INT_TYPE);

    String LONG_TO_STR_DESCRIPTOR = getMethodDescriptor(
            STRING_TYPE, LONG_TYPE);

    String CHAR_TO_STR_DESCRIPTOR = getMethodDescriptor(
            STRING_TYPE, CHAR_TYPE);

    String BOOLEAN_TO_STR_DESCRIPTOR = getMethodDescriptor(
            STRING_TYPE, BOOLEAN_TYPE);

    String INT_INT_TO_INT_DESCRIPTOR = getMethodDescriptor(
            INT_TYPE, INT_TYPE, INT_TYPE);

    String STR_STR_TO_STR_DESCRIPTOR = getMethodDescriptor(
            STRING_TYPE, STRING_TYPE, STRING_TYPE);

    String STR_INT_TO_INT_DESCRIPTOR = getMethodDescriptor(
            INT_TYPE, STRING_TYPE, INT_TYPE);

    String OBJECT_TO_BOOL_DESCRIPTOR = getMethodDescriptor(
            BOOLEAN_TYPE, OBJECT_TYPE);

    String OBJECT_TO_STR_DESCRIPTOR = getMethodDescriptor(
            STRING_TYPE, OBJECT_TYPE);

    String STR_TO_BOOL_DESCRIPTOR = getMethodDescriptor(
            BOOLEAN_TYPE, STRING_TYPE);

    String STR_INT_TO_BOOL_DESCRIPTOR = getMethodDescriptor(
            BOOLEAN_TYPE, STRING_TYPE, INT_TYPE);

    String BOOL_INT_STR_INT_INT_TO_BOOL_DESCRIPTOR = getMethodDescriptor(
            BOOLEAN_TYPE, Type.BOOLEAN_TYPE, INT_TYPE, STRING_TYPE, INT_TYPE,
            INT_TYPE);

    String CHARSEQ_TO_BOOL_DESCRIPTOR = getMethodDescriptor(
            BOOLEAN_TYPE, CHARSEQ_TYPE);

    String CHARSEQ_CHARSEQ_TO_STR_DESCRIPTOR = getMethodDescriptor(
            STRING_TYPE, CHARSEQ_TYPE, CHARSEQ_TYPE);

    String STR_TO_VOID_DESCRIPTOR = getMethodDescriptor(
            VOID_TYPE, STRING_TYPE);

    String STR_TO_STRBUILDER_DESCRIPTOR = getMethodDescriptor(
            STRING_BUILDER_TYPE, STRING_TYPE);

    String CHAR_TO_STRBUILDER_DESCRIPTOR = getMethodDescriptor(
            STRING_BUILDER_TYPE, CHAR_TYPE);

    String CHARSEQ_TO_VOID_DESCRIPTOR = getMethodDescriptor(
            VOID_TYPE, CHARSEQ_TYPE);

    String INT_TO_STRBUILDER_DESCRIPTOR = getMethodDescriptor(
            STRING_BUILDER_TYPE, INT_TYPE);

    String LONG_TO_STRBUILDER_DESCRIPTOR = getMethodDescriptor(
            STRING_BUILDER_TYPE, LONG_TYPE);

    String BOOLEAN_TO_STRBUILDER_DESCRIPTOR = getMethodDescriptor(
            STRING_BUILDER_TYPE, BOOLEAN_TYPE);

    String FLOAT_TO_STRBUILDER_DESCRIPTOR = getMethodDescriptor(
            STRING_BUILDER_TYPE, FLOAT_TYPE);

    String DOUBLE_TO_STRBUILDER_DESCRIPTOR = getMethodDescriptor(
            STRING_BUILDER_TYPE, DOUBLE_TYPE);

    String OBJECT_TO_STRBUILDER_DESCRIPTOR = getMethodDescriptor(
            STRING_BUILDER_TYPE, OBJECT_TYPE);

    String JAVA_LANG_STRING = String.class.getName().replace(".",
            "/");

    String JAVA_LANG_STRING_BUILDER = StringBuilder.class
            .getName().replace('.', '/');

    String JAVA_UTIL_STRING_TOKENIZER = StringTokenizer.class
            .getName().replace('.', '/');

    String STR_STR_TO_VOID_DESCRIPTOR = getMethodDescriptor(
            VOID_TYPE, STRING_TYPE, STRING_TYPE);

    String TO_BOOLEAN_DESCRIPTOR = getMethodDescriptor(BOOLEAN_TYPE);

    String INT_STR_INT_INT_TO_BOOL_DESCRIPTOR = getMethodDescriptor(
            BOOLEAN_TYPE, INT_TYPE, STRING_TYPE, INT_TYPE, INT_TYPE);

    String JAVA_IO_STRING_READER = StringReader.class.getName()
            .replace('.', '/');

    String JAVA_IO_READER = Reader.class.getName().replace(
            ".", "/");

}
