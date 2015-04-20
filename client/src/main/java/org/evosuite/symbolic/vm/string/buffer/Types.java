package org.evosuite.symbolic.vm.string.buffer;

import org.objectweb.asm.Type;

public interface Types {

	public static final String JAVA_LANG_STRING_BUFFER = StringBuffer.class
			.getName().replace(".", "/");

	public static final Type STRING_TYPE = Type.getType(String.class);

	public static final String STR_TO_VOID_DESCRIPTOR = Type
			.getMethodDescriptor(Type.VOID_TYPE, STRING_TYPE);

	public static final String JAVA_LANG_STRING = String.class.getName()
			.replace(".", "/");

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
			Type.VOID_TYPE, Type.INT_TYPE);;

}
