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
package org.evosuite.symbolic.vm.wrappers;

import static org.objectweb.asm.Type.INT_TYPE;
import static org.objectweb.asm.Type.LONG_TYPE;
import static org.objectweb.asm.Type.FLOAT_TYPE;
import static org.objectweb.asm.Type.DOUBLE_TYPE;
import static org.objectweb.asm.Type.SHORT_TYPE;
import static org.objectweb.asm.Type.BYTE_TYPE;
import static org.objectweb.asm.Type.CHAR_TYPE;
import static org.objectweb.asm.Type.BOOLEAN_TYPE;
import static org.objectweb.asm.Type.VOID_TYPE;

import org.objectweb.asm.Type;

public interface Types {

	// primitive types
	public static Type INTEGER = Type.getType(Integer.class);
	public static Type LONG = Type.getType(Long.class);
	public static Type FLOAT = Type.getType(Float.class);
	public static Type DOUBLE = Type.getType(Double.class);
	public static Type SHORT = Type.getType(Short.class);
	public static Type CHARACTER = Type.getType(Character.class);
	public static Type BYTE = Type.getType(Byte.class);
	public static Type BOOLEAN = Type.getType(Boolean.class);

	// wrapper types
	public static String JAVA_LANG_LONG = Long.class.getName()
			.replace('.', '/');
	public static String JAVA_LANG_FLOAT = Float.class.getName().replace(".",
			"/");
	public static String JAVA_LANG_DOUBLE = Double.class.getName().replace(".",
			"/");
	public static String JAVA_LANG_SHORT = Short.class.getName().replace(".",
			"/");
	public static String JAVA_LANG_BYTE = Byte.class.getName()
			.replace('.', '/');
	public static String JAVA_LANG_CHARACTER = Character.class.getName()
			.replace('.', '/');
	public static String JAVA_LANG_BOOLEAN = Boolean.class.getName().replace(
			".", "/");
	static final String JAVA_LANG_INTEGER = Integer.class.getName().replace(
			".", "/");
	public static String JAVA_LANG_STRING = String.class.getName().replace(".",
			"/");

	public static Type STRING_TYPE = Type.getType(String.class);
	public static String STR_TO_INT_DESCRIPTOR = Type.getMethodDescriptor(
			INT_TYPE, STRING_TYPE);

	// valueOf Descriptos
	public static String I_TO_INTEGER = Type.getMethodDescriptor(INTEGER,
			INT_TYPE);
	public static String J_TO_LONG = Type.getMethodDescriptor(LONG, LONG_TYPE);
	public static String F_TO_FLOAT = Type.getMethodDescriptor(FLOAT,
			FLOAT_TYPE);
	public static String D_TO_DOUBLE = Type.getMethodDescriptor(DOUBLE,
			DOUBLE_TYPE);
	public static String S_TO_SHORT = Type.getMethodDescriptor(SHORT,
			SHORT_TYPE);
	public static String B_TO_BYTE = Type.getMethodDescriptor(BYTE, BYTE_TYPE);
	public static String C_TO_CHARACTER = Type.getMethodDescriptor(CHARACTER,
			CHAR_TYPE);
	public static String Z_TO_BOOLEAN = Type.getMethodDescriptor(BOOLEAN,
			BOOLEAN_TYPE);

	// intValue/shortValue,etc. descriptors
	public static String TO_INT = Type.getMethodDescriptor(INT_TYPE);

	public static String TO_LONG = Type.getMethodDescriptor(LONG_TYPE);

	public static String TO_FLOAT = Type.getMethodDescriptor(FLOAT_TYPE);

	public static String TO_DOUBLE = Type.getMethodDescriptor(DOUBLE_TYPE);

	public static String TO_SHORT = Type.getMethodDescriptor(SHORT_TYPE);

	public static String TO_BYTE = Type.getMethodDescriptor(BYTE_TYPE);

	public static String TO_CHAR = Type.getMethodDescriptor(CHAR_TYPE);

	public static String TO_BOOLEAN = Type.getMethodDescriptor(BOOLEAN_TYPE);

	public static String I_TO_VOID = Type.getMethodDescriptor(VOID_TYPE,
			INT_TYPE);

	public static String INIT = "<init>";

	public static String B_TO_VOID = Type.getMethodDescriptor(VOID_TYPE,
			BYTE_TYPE);
	public static String C_TO_VOID = Type.getMethodDescriptor(VOID_TYPE,
			CHAR_TYPE);
	public static String S_TO_VOID = Type.getMethodDescriptor(VOID_TYPE,
			SHORT_TYPE);
	public static String Z_TO_VOID = Type.getMethodDescriptor(VOID_TYPE,
			BOOLEAN_TYPE);
	public static String J_TO_VOID = Type.getMethodDescriptor(VOID_TYPE,
			LONG_TYPE);
	public static String F_TO_VOID = Type.getMethodDescriptor(VOID_TYPE,
			FLOAT_TYPE);
	public static String D_TO_VOID = Type.getMethodDescriptor(VOID_TYPE,
			DOUBLE_TYPE);

	public static String C_TO_I = Type.getMethodDescriptor(INT_TYPE, CHAR_TYPE);

	public static String C_TO_Z = Type.getMethodDescriptor(BOOLEAN_TYPE,
			CHAR_TYPE);
}
