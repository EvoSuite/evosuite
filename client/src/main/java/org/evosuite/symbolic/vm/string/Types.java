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
package org.evosuite.symbolic.vm.string;

import static org.objectweb.asm.Type.BOOLEAN_TYPE;
import static org.objectweb.asm.Type.CHAR_TYPE;
import static org.objectweb.asm.Type.DOUBLE_TYPE;
import static org.objectweb.asm.Type.FLOAT_TYPE;
import static org.objectweb.asm.Type.INT_TYPE;
import static org.objectweb.asm.Type.LONG_TYPE;
import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.Type.getMethodDescriptor;

import java.io.Reader;
import java.io.StringReader;
import java.util.StringTokenizer;

import org.objectweb.asm.Type;

public interface Types {

	public static final Type CHARSEQ_TYPE = Type.getType(CharSequence.class);

	public static final Type OBJECT_TYPE = Type.getType(Object.class);

	public static final Type STRING_TYPE = Type.getType(String.class);

	public static final Type STRING_BUILDER_TYPE = Type
			.getType(StringBuilder.class);

	public static final String TO_INT_DESCRIPTOR = getMethodDescriptor(INT_TYPE);

	public static final String TO_STR_DESCRIPTOR = getMethodDescriptor(STRING_TYPE);

	public static final String STR_TO_INT_DESCRIPTOR = getMethodDescriptor(
			INT_TYPE, STRING_TYPE);

	public static final String INT_TO_INT_DESCRIPTOR = getMethodDescriptor(
			INT_TYPE, INT_TYPE);

	public static final String INT_TO_CHAR_DESCRIPTOR = getMethodDescriptor(
			CHAR_TYPE, INT_TYPE);

	public static final String STR_TO_STR_DESCRIPTOR = getMethodDescriptor(
			STRING_TYPE, STRING_TYPE);

	public static final String CHAR_CHAR_TO_STR_DESCRIPTOR = getMethodDescriptor(
			STRING_TYPE, CHAR_TYPE, CHAR_TYPE);

	public static final String INT_INT_TO_STR_DESCRIPTOR = getMethodDescriptor(
			STRING_TYPE, INT_TYPE, INT_TYPE);

	public static final String INT_TO_STR_DESCRIPTOR = getMethodDescriptor(
			STRING_TYPE, INT_TYPE);

	public static final String LONG_TO_STR_DESCRIPTOR = getMethodDescriptor(
			STRING_TYPE, LONG_TYPE);

	public static final String CHAR_TO_STR_DESCRIPTOR = getMethodDescriptor(
			STRING_TYPE, CHAR_TYPE);

	public static final String BOOLEAN_TO_STR_DESCRIPTOR = getMethodDescriptor(
			STRING_TYPE, BOOLEAN_TYPE);
	
	public static final String INT_INT_TO_INT_DESCRIPTOR = getMethodDescriptor(
			INT_TYPE, INT_TYPE, INT_TYPE);

	public static final String STR_STR_TO_STR_DESCRIPTOR = getMethodDescriptor(
			STRING_TYPE, STRING_TYPE, STRING_TYPE);

	public static final String STR_INT_TO_INT_DESCRIPTOR = getMethodDescriptor(
			INT_TYPE, STRING_TYPE, INT_TYPE);

	public static final String OBJECT_TO_BOOL_DESCRIPTOR = getMethodDescriptor(
			BOOLEAN_TYPE, OBJECT_TYPE);

	public static final String OBJECT_TO_STR_DESCRIPTOR = getMethodDescriptor(
			STRING_TYPE, OBJECT_TYPE);

	public static final String STR_TO_BOOL_DESCRIPTOR = getMethodDescriptor(
			BOOLEAN_TYPE, STRING_TYPE);

	public static final String STR_INT_TO_BOOL_DESCRIPTOR = getMethodDescriptor(
			BOOLEAN_TYPE, STRING_TYPE, INT_TYPE);

	public static final String BOOL_INT_STR_INT_INT_TO_BOOL_DESCRIPTOR = getMethodDescriptor(
			BOOLEAN_TYPE, Type.BOOLEAN_TYPE, INT_TYPE, STRING_TYPE, INT_TYPE,
			INT_TYPE);

	public static final String CHARSEQ_TO_BOOL_DESCRIPTOR = getMethodDescriptor(
			BOOLEAN_TYPE, CHARSEQ_TYPE);

	public static final String CHARSEQ_CHARSEQ_TO_STR_DESCRIPTOR = getMethodDescriptor(
			STRING_TYPE, CHARSEQ_TYPE, CHARSEQ_TYPE);

	public static final String STR_TO_VOID_DESCRIPTOR = getMethodDescriptor(
			VOID_TYPE, STRING_TYPE);

	public static final String STR_TO_STRBUILDER_DESCRIPTOR = getMethodDescriptor(
			STRING_BUILDER_TYPE, STRING_TYPE);

	public static final String CHAR_TO_STRBUILDER_DESCRIPTOR = getMethodDescriptor(
			STRING_BUILDER_TYPE, CHAR_TYPE);

	public static final String CHARSEQ_TO_VOID_DESCRIPTOR = getMethodDescriptor(
			VOID_TYPE, CHARSEQ_TYPE);

	public static final String INT_TO_STRBUILDER_DESCRIPTOR = getMethodDescriptor(
			STRING_BUILDER_TYPE, INT_TYPE);

	public static final String LONG_TO_STRBUILDER_DESCRIPTOR = getMethodDescriptor(
			STRING_BUILDER_TYPE, LONG_TYPE);

	public static final String BOOLEAN_TO_STRBUILDER_DESCRIPTOR = getMethodDescriptor(
			STRING_BUILDER_TYPE, BOOLEAN_TYPE);

	public static final String FLOAT_TO_STRBUILDER_DESCRIPTOR = getMethodDescriptor(
			STRING_BUILDER_TYPE, FLOAT_TYPE);

	public static final String DOUBLE_TO_STRBUILDER_DESCRIPTOR = getMethodDescriptor(
			STRING_BUILDER_TYPE, DOUBLE_TYPE);

	public static final String OBJECT_TO_STRBUILDER_DESCRIPTOR = getMethodDescriptor(
			STRING_BUILDER_TYPE, OBJECT_TYPE);

	static final String JAVA_LANG_STRING = String.class.getName().replace(".",
			"/");

	static final String JAVA_LANG_STRING_BUILDER = StringBuilder.class
			.getName().replace('.', '/');

	static final String JAVA_UTIL_STRING_TOKENIZER = StringTokenizer.class
			.getName().replace('.', '/');

	public static final String STR_STR_TO_VOID_DESCRIPTOR = getMethodDescriptor(
			VOID_TYPE, STRING_TYPE, STRING_TYPE);

	public static final String TO_BOOLEAN_DESCRIPTOR = getMethodDescriptor(BOOLEAN_TYPE);

	public static final String INT_STR_INT_INT_TO_BOOL_DESCRIPTOR = getMethodDescriptor(
			BOOLEAN_TYPE, INT_TYPE, STRING_TYPE, INT_TYPE, INT_TYPE);

	static final String JAVA_IO_STRING_READER = StringReader.class.getName()
			.replace('.', '/');

	public static final String JAVA_IO_READER = Reader.class.getName().replace(
			".", "/");

}
