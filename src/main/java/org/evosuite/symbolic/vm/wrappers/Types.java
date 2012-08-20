package org.evosuite.symbolic.vm.wrappers;

import static org.objectweb.asm.Type.INT_TYPE;
import static org.objectweb.asm.Type.LONG_TYPE;
import static org.objectweb.asm.Type.FLOAT_TYPE;
import static org.objectweb.asm.Type.DOUBLE_TYPE;

import org.objectweb.asm.Type;

public interface Types {

	public static Type INTEGER = Type.getType(Integer.class);
	public static Type LONG = Type.getType(Long.class);
	public static Type FLOAT = Type.getType(Float.class);
	public static Type DOUBLE = Type.getType(Double.class);

	public static String INT_TO_INTEGER = Type.getMethodDescriptor(INTEGER,
			INT_TYPE);

	public static String TO_INT = Type.getMethodDescriptor(INT_TYPE);

	public static String JAVA_LANG_LONG = Long.class.getName()
			.replace(".", "/");

	public static String L_TO_LONG = Type.getMethodDescriptor(LONG, LONG_TYPE);

	public static String TO_LONG = Type.getMethodDescriptor(LONG_TYPE);

	public static String JAVA_LANG_FLOAT = Float.class.getName().replace(".",
			"/");

	public static String F_TO_FLOAT = Type.getMethodDescriptor(FLOAT,
			FLOAT_TYPE);

	public static String TO_FLOAT = Type.getMethodDescriptor(FLOAT_TYPE);

	public static String JAVA_LANG_DOUBLE = Double.class.getName().replace(".",
			"/");

	public static String D_TO_DOUBLE = Type.getMethodDescriptor(DOUBLE,
			DOUBLE_TYPE);

	public static String TO_DOUBLE = Type.getMethodDescriptor(DOUBLE_TYPE);

	static final String JAVA_LANG_INTEGER = Integer.class.getName().replace(
			".", "/");

}
