package org.evosuite.symbolic.vm.bigint;

import java.math.BigInteger;

import org.objectweb.asm.Type;

public interface Types {

	public final static String JAVA_MATH_BIG_INTEGER = BigInteger.class
			.getName().replace(".", "/");

	public static final String INIT = "<init>";

	public static final String JAVA_LANG_STRING = String.class.getName()
			.replace(".", "/");

	public static final Type BIG_INTEGER = Type.getType(BigInteger.class);

	public static final Type BIG_INTEGER_ARRAY = Type
			.getType(BigInteger[].class);

	public static final String BIG_INTEGER_TO_BIG_INTEGER_ARRAY = Type
			.getMethodDescriptor(BIG_INTEGER_ARRAY, BIG_INTEGER);

	public static final String TO_INT = Type.getMethodDescriptor(Type.INT_TYPE);

	public String STRING_TO_VOID = Type.getMethodDescriptor(Type.VOID_TYPE,
			Type.getType(String.class));

	public String BIG_INTEGER_TO_BIG_INTEGER = Type.getMethodDescriptor(
			BIG_INTEGER, BIG_INTEGER);

}
