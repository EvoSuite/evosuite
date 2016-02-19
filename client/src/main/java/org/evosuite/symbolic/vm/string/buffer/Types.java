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
package org.evosuite.symbolic.vm.string.buffer;

import org.objectweb.asm.Type;

public interface Types {

	public static final String JAVA_LANG_STRING_BUFFER = StringBuffer.class
			.getName().replace('.', '/');

	public static final Type STRING_TYPE = Type.getType(String.class);

	public static final String STR_TO_VOID_DESCRIPTOR = Type
			.getMethodDescriptor(Type.VOID_TYPE, STRING_TYPE);

	public static final String JAVA_LANG_STRING = String.class.getName()
			.replace('.', '/');

	public static final String TO_STR_DESCRIPTOR = Type
			.getMethodDescriptor(STRING_TYPE);

	public static final Type STRING_BUFFER_TYPE = Type
			.getType(StringBuffer.class);

	public static final String Z_TO_STRING_BUFFER = Type.getMethodDescriptor(
			STRING_BUFFER_TYPE, Type.BOOLEAN_TYPE);

	public static final String C_TO_STRING_BUFFER = Type.getMethodDescriptor(
			STRING_BUFFER_TYPE, Type.CHAR_TYPE);

	public static final String I_TO_STRING_BUFFER = Type.getMethodDescriptor(
			STRING_BUFFER_TYPE, Type.INT_TYPE);

	public static final String L_TO_STRING_BUFFER = Type.getMethodDescriptor(
			STRING_BUFFER_TYPE, Type.LONG_TYPE);

	public static final String F_TO_STRING_BUFFER = Type.getMethodDescriptor(
			STRING_BUFFER_TYPE, Type.FLOAT_TYPE);

	public static final String D_TO_STRING_BUFFER = Type.getMethodDescriptor(
			STRING_BUFFER_TYPE, Type.DOUBLE_TYPE);

	public static final String STR_TO_STRING_BUFFER = Type.getMethodDescriptor(
			STRING_BUFFER_TYPE, STRING_TYPE);

	public static final String INT_TO_VOID_DESCRIPTOR = Type.getMethodDescriptor(
			Type.VOID_TYPE, Type.INT_TYPE);

}
