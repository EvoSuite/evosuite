package edu.uta.cse.dsc.vm2.wrappers;

import static org.objectweb.asm.Type.INT_TYPE;

import org.objectweb.asm.Type;

public interface Types {

	public static Type INTEGER = Type.getType(Integer.class);

	public static String INT_TO_INTEGER = Type.getMethodDescriptor(INTEGER,
			INT_TYPE);

	public static String TO_INT = Type.getMethodDescriptor(INT_TYPE);

}
