package de.unisb.cs.st.evosuite.symbolic.nativepeer;

import gov.nasa.jpf.jvm.MJIEnv;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.symbolic.expr.IntegerVariable;
import de.unisb.cs.st.evosuite.symbolic.expr.RealVariable;

public class JPF_de_unisb_cs_st_evosuite_symbolic_nativepeer_ConcolicMarker {
	private static int counter = 0;

	private static Logger logger = Logger.getLogger(JPF_de_unisb_cs_st_evosuite_symbolic_nativepeer_ConcolicMarker.class);

	public static boolean mark__Z__Z(MJIEnv env, int rcls, boolean v0) {
		env.setReturnAttribute(new IntegerVariable("mark" + counter++ + "__SYM",
		        Integer.MIN_VALUE, Integer.MAX_VALUE));
		return v0;
	}

	public static char mark__C__C(MJIEnv env, int rcls, char v0) {
		env.setReturnAttribute(new IntegerVariable("mark" + counter++ + "__SYM",
		        Character.MIN_VALUE, Character.MAX_VALUE));
		return v0;
	}

	public static short mark__S__S(MJIEnv env, int rcls, short v0) {
		env.setReturnAttribute(new IntegerVariable("mark" + counter++ + "__SYM",
		        Short.MIN_VALUE, Short.MAX_VALUE));
		return v0;
	}

	public static byte mark__B__B(MJIEnv env, int rcls, byte v0) {
		env.setReturnAttribute(new IntegerVariable("mark" + counter++ + "__SYM",
		        Byte.MIN_VALUE, Byte.MAX_VALUE));
		return v0;
	}

	public static int mark__I__I(MJIEnv env, int rcls, int v0) {
		env.setReturnAttribute(new IntegerVariable("mark" + counter++ + "__SYM",
		        Integer.MIN_VALUE, Integer.MAX_VALUE));
		logger.info("Marked integer!");
		return v0;
	}

	public static long mark__J__J(MJIEnv env, int rcls, long v0) {
		env.setReturnAttribute(new IntegerVariable("mark" + counter++ + "__SYM",
		        Long.MIN_VALUE, Long.MAX_VALUE));
		return v0;
	}

	public static float mark__F__F(MJIEnv env, int rcls, float v0) {
		env.setReturnAttribute(new RealVariable("mark" + counter++ + "__SYM",
		        -Float.MAX_VALUE, Float.MAX_VALUE));
		return v0;
	}

	public static double mark__D__D(MJIEnv env, int rcls, double v0) {
		env.setReturnAttribute(new RealVariable("mark" + counter++ + "__SYM",
		        -Double.MAX_VALUE, Double.MAX_VALUE));
		return v0;
	}

	public static boolean mark__ZLjava_lang_String_2__Z(MJIEnv env, int rcls, boolean v0,
	        int rString1) {
		env.setReturnAttribute(new IntegerVariable(env.getStringObject(rString1)
		        + "__SYM", Integer.MIN_VALUE, Integer.MAX_VALUE));
		return v0;
	}

	public static char mark__CLjava_lang_String_2__C(MJIEnv env, int rcls, char v0,
	        int rString1) {
		env.setReturnAttribute(new IntegerVariable(env.getStringObject(rString1)
		        + "__SYM", Character.MIN_VALUE, Character.MAX_VALUE));
		return v0;
	}

	public static short mark__SLjava_lang_String_2__S(MJIEnv env, int rcls, short v0,
	        int rString1) {
		env.setReturnAttribute(new IntegerVariable(env.getStringObject(rString1)
		        + "__SYM", Short.MIN_VALUE, Short.MAX_VALUE));
		return v0;
	}

	public static byte mark__BLjava_lang_String_2__B(MJIEnv env, int rcls, byte v0,
	        int rString1) {
		env.setReturnAttribute(new IntegerVariable(env.getStringObject(rString1)
		        + "__SYM", Byte.MIN_VALUE, Byte.MAX_VALUE));
		return v0;
	}

	public static int mark__ILjava_lang_String_2__I(MJIEnv env, int rcls, int v0,
	        int rString1) {
		env.setReturnAttribute(new IntegerVariable(env.getStringObject(rString1)
		        + "__SYM", Integer.MIN_VALUE, Integer.MAX_VALUE));
		return v0;
	}

	public static long mark__JLjava_lang_String_2__J(MJIEnv env, int rcls, long v0,
	        int rString1) {
		env.setReturnAttribute(new IntegerVariable(env.getStringObject(rString1)
		        + "__SYM", Long.MIN_VALUE, Long.MAX_VALUE));
		return v0;
	}

	public static float mark__FLjava_lang_String_2__F(MJIEnv env, int rcls, float v0,
	        int rString1) {
		env.setReturnAttribute(new RealVariable(env.getStringObject(rString1) + "__SYM",
		        -Float.MAX_VALUE, Float.MAX_VALUE));
		return v0;
	}

	public static double mark__DLjava_lang_String_2__D(MJIEnv env, int rcls, double v0,
	        int rString1) {
		env.setReturnAttribute(new RealVariable(env.getStringObject(rString1) + "__SYM",
		        -Double.MAX_VALUE, Double.MAX_VALUE));
		return v0;
	}
}